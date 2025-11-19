package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.sermilion.readability4k.model.ArticleMetadata
import com.sermilion.readability4k.util.HtmlUtil
import com.sermilion.readability4k.util.RegExUtil

open class ReadabilityMetadataParser(protected val regEx: RegExUtil = RegExUtil()) : ProcessorBase(), MetadataParser {

  @Suppress("CyclomaticComplexMethod")
  override fun getArticleMetadata(document: Document, disableJSONLD: Boolean): ArticleMetadata {
    val values = HashMap<String, String>()
    var byline: String? = null
    var publishedTime: String? = null

    val namePattern =
      Regex("^\\s*((twitter)\\s*:\\s*)?(description|title)\\s*$", RegexOption.IGNORE_CASE)
    val propertyPattern = Regex("^\\s*og\\s*:\\s*(description|title|site_name)\\s*$", RegexOption.IGNORE_CASE)

    document.select("meta").forEach { element ->
      val elementName = element.attr("name")
      val elementProperty = element.attr("property")

      if (elementName == "author" || elementProperty == "author") {
        byline = element.attr("content")
        return@forEach
      }

      if (elementProperty == "article:published_time" || elementName == "parsely-pub-date") {
        publishedTime = element.attr("content")
        return@forEach
      }

      var name: String? = null
      if (namePattern.containsMatchIn(elementName)) {
        name = elementName
      } else if (propertyPattern.containsMatchIn(elementProperty)) {
        name = elementProperty
      }

      if (name != null) {
        val content = element.attr("content")
        if (!content.isBlank()) {
          name = name.lowercase().replace("\\s".toRegex(), "")
          values[name] = content.trim().replace("  ", " ")
        }
      }
    }

    var title: String? = null
    var excerpt: String? = null
    var siteName: String? = null

    if (!disableJSONLD) {
      val jsonLd = getJSONLD(document)
      if (jsonLd != null) {
        if (!jsonLd.title.isNullOrBlank()) {
          title = jsonLd.title
        }
        if (!jsonLd.byline.isNullOrBlank() && byline.isNullOrBlank()) {
          byline = jsonLd.byline
        }
        if (!jsonLd.excerpt.isNullOrBlank()) {
          excerpt = jsonLd.excerpt
        }
        if (!jsonLd.siteName.isNullOrBlank()) {
          siteName = jsonLd.siteName
        }
        if (!jsonLd.publishedTime.isNullOrBlank() && publishedTime.isNullOrBlank()) {
          publishedTime = jsonLd.publishedTime
        }
      }
    }

    excerpt = excerpt ?: values["description"] ?: values["og:description"] ?: values["twitter:description"]

    if (siteName.isNullOrBlank()) {
      siteName = values["og:site_name"]
    }

    if (title.isNullOrBlank()) {
      title = getArticleTitle(document)
    }
    if (title.isBlank()) {
      title = values["og:title"] ?: values["twitter:title"] ?: ""
    }

    return ArticleMetadata(
      title = HtmlUtil.unescapeHtmlEntities(title),
      byline = HtmlUtil.unescapeHtmlEntities(byline),
      excerpt = HtmlUtil.unescapeHtmlEntities(excerpt),
      charset = document.charset().name(),
      siteName = HtmlUtil.unescapeHtmlEntities(siteName),
      publishedTime = HtmlUtil.unescapeHtmlEntities(publishedTime),
    )
  }

  protected open fun getArticleTitle(doc: Document): String {
    var curTitle = ""
    var origTitle = ""

    try {
      origTitle = doc.title()
      curTitle = origTitle

      if (curTitle.isBlank()) {
        doc.select("#title").first()?.let { elementWithIdTitle ->
          origTitle = getInnerText(elementWithIdTitle, regEx)
          curTitle = origTitle
        }
      }
    } catch (@Suppress("SwallowedException") _: Exception) {
      // Title extraction is not critical, continue with empty title if it fails
    }

    val titleProcessingResult = processTitleSeparators(curTitle, origTitle, doc)
    curTitle = titleProcessingResult.first
    val titleHadHierarchicalSeparators = titleProcessingResult.second

    curTitle = curTitle.trim()
    val curTitleWordCount = wordCount(curTitle)
    if (shouldUseOriginalTitle(curTitleWordCount, titleHadHierarchicalSeparators, origTitle)) {
      curTitle = origTitle
    }

    return curTitle
  }

  private fun processTitleSeparators(curTitle: String, origTitle: String, doc: Document): Pair<String, Boolean> {
    var processedTitle = curTitle
    var hadHierarchicalSeparators = false

    if (curTitle.contains(" [|\\-/>»] ".toRegex())) {
      hadHierarchicalSeparators = curTitle.contains(" [/>»] ".toRegex())
      processedTitle =
        origTitle.replace("(.*)[|\\-/>»] .*".toRegex(RegexOption.IGNORE_CASE), "$1")

      if (wordCount(processedTitle) < 3) {
        processedTitle = origTitle.replace(
          "[^|\\-/>»]*[|\\-/>»](.*)".toRegex(RegexOption.IGNORE_CASE),
          "$1",
        )
      }
    } else if (curTitle.contains(": ")) {
      val match = doc.select("h1, h2").any { it.wholeText() == curTitle }

      if (!match) {
        processedTitle = origTitle.substring(origTitle.lastIndexOf(':') + 1)

        if (wordCount(processedTitle) < 3) {
          processedTitle = origTitle.substring(origTitle.indexOf(':') + 1)
        } else if (wordCount(origTitle.substring(0, origTitle.indexOf(':'))) > 5) {
          processedTitle = origTitle
        }
      }
    } else if (curTitle.length > 150 || curTitle.length < 15) {
      val hOnes = doc.getElementsByTag("h1")

      if (hOnes.size == 1) {
        processedTitle = getInnerText(hOnes[0], regEx)
      }
    }

    return Pair(processedTitle, hadHierarchicalSeparators)
  }

  private fun shouldUseOriginalTitle(
    curTitleWordCount: Int,
    titleHadHierarchicalSeparators: Boolean,
    origTitle: String,
  ): Boolean = curTitleWordCount <= 4 &&
    (
      !titleHadHierarchicalSeparators ||
        curTitleWordCount != wordCount(origTitle.replace("[|\\-/>»]+".toRegex(), "")) - 1
      )

  protected open fun wordCount(str: String): Int = str.split("\\s+".toRegex()).size

  @Suppress("NestedBlockDepth", "LoopWithTooManyJumpStatements")
  protected open fun getJSONLD(doc: Document): ArticleMetadata? {
    val scripts = doc.select("script[type=application/ld+json]")

    val jsonLdArticleTypes = Regex("Article|NewsArticle|BlogPosting|ReportageNewsArticle")

    for (script in scripts) {
      val content = script.html()
      if (content.isBlank()) continue

      try {
        if (!jsonLdArticleTypes.containsMatchIn(content)) {
          continue
        }

        val nameMatch = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"").find(content)
        val headlineMatch = Regex("\"headline\"\\s*:\\s*\"([^\"]+)\"").find(content)

        val name = nameMatch?.groupValues?.get(1)
        val headline = headlineMatch?.groupValues?.get(1)

        val title = if (!name.isNullOrBlank() && !headline.isNullOrBlank() && name != headline) {
          val titleFromHtml = doc.title()
          val nameSimilarity = HtmlUtil.textSimilarity(name, titleFromHtml)
          val headlineSimilarity = HtmlUtil.textSimilarity(headline, titleFromHtml)
          if (nameSimilarity > headlineSimilarity) name else headline
        } else {
          headline ?: name
        }

        val authorNameMatch = Regex("\"author\"\\s*:\\s*\\{[^}]*\"name\"\\s*:\\s*\"([^\"]+)\"").find(content)
        val authorStringMatch = Regex("\"author\"\\s*:\\s*\"([^\"]+)\"").find(content)
        val byline = authorNameMatch?.groupValues?.get(1) ?: authorStringMatch?.groupValues?.get(1)

        val descriptionMatch = Regex("\"description\"\\s*:\\s*\"([^\"]+)\"").find(content)
        val excerpt = descriptionMatch?.groupValues?.get(1)

        val publisherMatch = Regex("\"publisher\"\\s*:\\s*\\{[^}]*\"name\"\\s*:\\s*\"([^\"]+)\"").find(content)
        val siteName = publisherMatch?.groupValues?.get(1)

        val datePublishedMatch = Regex("\"datePublished\"\\s*:\\s*\"([^\"]+)\"").find(content)
        val publishedTime = datePublishedMatch?.groupValues?.get(1)

        return ArticleMetadata(
          title = title,
          byline = byline,
          excerpt = excerpt,
          siteName = siteName,
          publishedTime = publishedTime,
        )
      } catch (_: Exception) {
        continue
      }
    }

    return null
  }
}
