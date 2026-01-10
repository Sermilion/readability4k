package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.sermilion.readability4k.model.ArticleMetadata
import com.sermilion.readability4k.util.HtmlUtil
import com.sermilion.readability4k.util.RegExUtil

open class ReadabilityMetadataParser(protected val regEx: RegExUtil = RegExUtil()) : ProcessorBase(), MetadataParser {

  private companion object {
    private const val H1_TITLE_SIMILARITY_THRESHOLD = 0.6

    private val GENERIC_TITLE_PATTERNS = listOf(
      "reddit.*the heart of the internet",
      "untitled",
      "no title",
      "404.*not found",
      "error.*page",
      "page not found",
    )
  }

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

    val isOriginalTitleGeneric = isGenericTitle(origTitle)

    val titleProcessingResult = processTitleSeparators(curTitle, origTitle, doc)
    curTitle = titleProcessingResult.first
    val titleHadHierarchicalSeparators = titleProcessingResult.second
    var titleWasModified = titleProcessingResult.third

    curTitle = curTitle.trim()
    val curTitleWordCount = wordCount(curTitle)
    if (shouldUseOriginalTitle(curTitleWordCount, titleHadHierarchicalSeparators, origTitle)) {
      curTitle = origTitle
      titleWasModified = false
    }

    val isCurrentTitleGeneric = isGenericTitle(curTitle)

    val h1Title = getSingleH1Title(doc, curTitle, titleWasModified, isOriginalTitleGeneric || isCurrentTitleGeneric)
    if (h1Title != null) {
      curTitle = h1Title
    }

    return curTitle
  }

  private fun processTitleSeparators(
    curTitle: String,
    origTitle: String,
    doc: Document,
  ): Triple<String, Boolean, Boolean> {
    var processedTitle = curTitle
    var hadHierarchicalSeparators = false
    var wasModified = false

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
      wasModified = true
    } else if (curTitle.contains(": ")) {
      val match = doc.select("h1, h2").any { it.wholeText() == curTitle }

      if (!match) {
        processedTitle = origTitle.substring(origTitle.lastIndexOf(':') + 1)

        if (wordCount(processedTitle) < 3) {
          processedTitle = origTitle.substring(origTitle.indexOf(':') + 1)
        } else if (wordCount(origTitle.take(origTitle.indexOf(':'))) > 5) {
          processedTitle = origTitle
        }
        wasModified = true
      }
    } else if (curTitle.length !in 15..150) {
      val hOnes = doc.getElementsByTag("h1")

      if (hOnes.size == 1) {
        processedTitle = getInnerText(hOnes[0], regEx)
        wasModified = true
      }
    }

    return Triple(processedTitle, hadHierarchicalSeparators, wasModified)
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

  private fun getSingleH1Title(
    doc: Document,
    currentTitle: String,
    titleHadSeparators: Boolean,
    isGenericTitle: Boolean,
  ): String? {
    if (!titleHadSeparators && !isGenericTitle) {
      return null
    }

    val h1Elements = doc.getElementsByTag("h1")
    if (h1Elements.size != 1) {
      return null
    }

    val h1Text = getInnerText(h1Elements[0], regEx).trim()
    if (h1Text.isBlank()) {
      return null
    }

    val h1WordCount = wordCount(h1Text)
    if (h1WordCount < 3) {
      return null
    }

    val similarity = HtmlUtil.textSimilarity(h1Text, currentTitle)
    if (similarity > H1_TITLE_SIMILARITY_THRESHOLD) {
      return null
    }

    return h1Text
  }

  private fun isGenericTitle(title: String): Boolean {
    if (title.isBlank()) {
      return false
    }

    val normalized = title.lowercase().trim()

    return GENERIC_TITLE_PATTERNS.any { pattern ->
      Regex(pattern, RegexOption.IGNORE_CASE).matches(normalized)
    }
  }

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
