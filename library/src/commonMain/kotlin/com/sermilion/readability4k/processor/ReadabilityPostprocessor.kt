package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode
import com.sermilion.readability4k.util.Logger

open class ReadabilityPostprocessor(protected val logger: Logger = Logger.NONE) : Postprocessor {

  companion object {
    val AbsoluteUriPattern = Regex("^[a-zA-Z][a-zA-Z0-9+\\-.]*:")

    val CLASSES_TO_PRESERVE = listOf("readability-styled", "page")
  }

  override fun postProcessContent(
    originalDocument: Document,
    articleContent: Element,
    articleUri: String,
    additionalClassesToPreserve: Collection<String>,
    keepClasses: Boolean,
  ) {
    fixRelativeUris(originalDocument, articleContent, articleUri)

    removeMetadataElements(articleContent)

    if (!keepClasses) {
      val classesToPreserve = listOf(
        CLASSES_TO_PRESERVE,
        additionalClassesToPreserve,
      ).flatten().toSet()
      cleanClasses(articleContent, classesToPreserve)
    }
  }

  protected open fun removeMetadataElements(articleContent: Element) {
    removeBreadcrumbNavigation(articleContent)
    removeMetadataTextElements(articleContent)
  }

  private fun removeBreadcrumbNavigation(articleContent: Element) {
    articleContent.select("nav").forEach { nav ->
      val classId = "${nav.className()} ${nav.id()}"
      if (isBreadcrumbElement(classId)) {
        logger.debug("Removing breadcrumb nav: $classId")
        nav.remove()
      }
    }
  }

  private fun isBreadcrumbElement(classId: String): Boolean = classId.contains("breadcrumb", ignoreCase = true) ||
    classId.contains("ui-bc", ignoreCase = true)

  private fun removeMetadataTextElements(articleContent: Element) {
    val timePattern =
      Regex(
        "\\d+\\s*(минут|час|день|week|month|year|ago|назад|янв|фев|мар|апр|май|июн|июл|авг|сен|окт|ноя|дек)",
        RegexOption.IGNORE_CASE,
      )
    val sourcePattern = Regex("Источник:", RegexOption.IGNORE_CASE)
    val categoryPattern = Regex("^(Новости|News)$", RegexOption.IGNORE_CASE)
    val singleNumberPattern = Regex("^\\d+$")

    articleContent.select("span, div").forEach { elem ->
      val text = elem.ownText().trim()
      if (shouldRemoveElement(text, timePattern, sourcePattern, categoryPattern, singleNumberPattern, elem)) {
        logger.debug("Removing metadata element: $text")
        elem.remove()
      }
    }
  }

  private fun shouldRemoveElement(
    text: String,
    timePattern: Regex,
    sourcePattern: Regex,
    categoryPattern: Regex,
    singleNumberPattern: Regex,
    elem: Element,
  ): Boolean {
    if (text.isEmpty() || text.length >= 100) return false

    if (matchesMetadataPattern(text, timePattern, sourcePattern, categoryPattern)) {
      return true
    }

    return isCommentCount(text, singleNumberPattern, elem)
  }

  private fun matchesMetadataPattern(
    text: String,
    timePattern: Regex,
    sourcePattern: Regex,
    categoryPattern: Regex,
  ): Boolean = timePattern.containsMatchIn(text) ||
    sourcePattern.containsMatchIn(text) ||
    categoryPattern.matches(text)

  private fun isCommentCount(text: String, singleNumberPattern: Regex, elem: Element): Boolean {
    if (text.length > 3 || !singleNumberPattern.matches(text)) return false

    val parent = elem.parent() ?: return false
    return parent.hasClass("flex") || parent.hasClass("items-center")
  }

  /**
   * Converts each <a> and <img> uri in the given element to an absolute URI,
   * ignoring #ref URIs.
   */
  protected open fun fixRelativeUris(originalDocument: Document, element: Element, articleUri: String) {
    try {
      val parsed = parseUrl(articleUri)
      if (parsed != null) {
        val scheme = parsed.first
        val prePath = "${parsed.first}://${parsed.second}"
        val lastSlashIndex = parsed.third.lastIndexOf("/") + 1
        val pathBase = "${parsed.first}://${parsed.second}${parsed.third.substring(
          0,
          lastSlashIndex,
        )}"
        fixRelativeUris(originalDocument, element, scheme, prePath, pathBase)
      }
    } catch (e: Exception) {
      logger.error("Could not fix relative urls for $element with base uri $articleUri", e)
    }
  }

  private fun parseUrl(url: String): Triple<String, String, String>? {
    val schemeEnd = url.indexOf("://")
    if (schemeEnd == -1) return null

    val scheme = url.substring(0, schemeEnd)
    val afterScheme = url.substring(schemeEnd + 3)
    val pathStart = afterScheme.indexOf('/')

    val host: String
    val path: String
    if (pathStart == -1) {
      host = afterScheme
      path = "/"
    } else {
      host = afterScheme.substring(0, pathStart)
      path = afterScheme.substring(pathStart)
    }

    return Triple(scheme, host, path)
  }

  protected open fun fixRelativeUris(
    originalDocument: Document,
    element: Element,
    scheme: String,
    prePath: String,
    pathBase: String,
  ) {
    fixRelativeAnchorUris(element, scheme, prePath, pathBase)

    fixRelativeImageUris(element, scheme, prePath, pathBase)
  }

  protected open fun fixRelativeAnchorUris(element: Element, scheme: String, prePath: String, pathBase: String) {
    element.getElementsByTag("a").forEach { link ->
      val href = link.attr("href")
      if (href.isNotBlank()) {
        // Replace links with javascript: URIs with text content, since
        // they won't work after scripts have been removed from the page.
        if (href.indexOf("javascript:") == 0) {
          val text = TextNode(link.wholeText())
          link.replaceWith(text)
        } else {
          link.attr("href", toAbsoluteURI(href, scheme, prePath, pathBase))
        }
      }
    }
  }

  protected open fun fixRelativeImageUris(element: Element, scheme: String, prePath: String, pathBase: String) {
    element.getElementsByTag("img").forEach { img ->
      fixRelativeImageUri(img, scheme, prePath, pathBase)
    }
  }

  protected open fun fixRelativeImageUri(img: Element, scheme: String, prePath: String, pathBase: String) {
    val src = img.attr("src")

    if (src.isNotBlank()) {
      img.attr("src", toAbsoluteURI(src, scheme, prePath, pathBase))
    }
  }

  protected open fun toAbsoluteURI(uri: String, scheme: String, prePath: String, pathBase: String): String {
    // If this is already an absolute URI, return it.
    if (isAbsoluteUri(uri) || uri.length <= 2) {
      return uri
    }

    // Scheme-rooted relative URI.
    if (uri.substring(0, 2) == "//") {
      return scheme + "://" + uri.substring(2)
    }

    // Prepath-rooted relative URI.
    if (uri[0] == '/') {
      return prePath + uri
    }

    // Dotslash relative URI.
    if (uri.indexOf("./") == 0) {
      return pathBase + uri.substring(2)
    }

    // Ignore hash URIs:
    if (uri[0] == '#') {
      return uri
    }

    // Standard relative URI add entire path. pathBase already includes a
    // trailing "/".
    return pathBase + uri
  }

  protected open fun isAbsoluteUri(uri: String): Boolean = AbsoluteUriPattern.containsMatchIn(uri)

  /**
   * Removes the class="" attribute from every element in the given
   * subtree, except those that match CLASSES_TO_PRESERVE and
   * the classesToPreserve array from the options object.
   */
  protected open fun cleanClasses(node: Element, classesToPreserve: Set<String>) {
    val classNames = node.classNames().filter { classesToPreserve.contains(it) }

    if (classNames.isNotEmpty()) {
      node.classNames(classNames.toMutableSet())
    } else {
      node.removeAttr("class")
    }

    node.children().forEach { child ->
      cleanClasses(child, classesToPreserve)
    }
  }
}
