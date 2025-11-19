package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode
import com.sermilion.readability4k.util.Logger
import com.sermilion.readability4k.util.RegExUtil

/**
 * Contains common utils for Preprocessor and Postprocessor
 */
abstract class ProcessorBase(protected val logger: Logger = Logger.NONE) {

  companion object {
    protected const val TRUNCATE_LOG_OUTPUT = false
  }

  protected open fun removeNodes(element: Element, tagName: String, filterFunction: ((Element) -> Boolean)? = null) {
    element.getElementsByTag(tagName).reversed().forEach { childElement ->
      if (childElement.parentNode() != null) {
        if (filterFunction == null || filterFunction(childElement)) {
          printAndRemove(childElement, "removeNode('$tagName')")
        }
      }
    }
  }

  protected open fun printAndRemove(node: Node, reason: String) {
    if (node.parent() != null) {
      logNodeInfo(node, reason)
      node.remove()
    }
  }

  protected open fun logNodeInfo(node: Node, reason: String) {
    val nodeToString =
      if (TRUNCATE_LOG_OUTPUT) {
        node.outerHtml().substring(0, minOf(node.outerHtml().length, 80)).replace("\n", "")
      } else {
        "\n------\n" + node.outerHtml() + "\n------\n"
      }

    logger.debug("$reason [$nodeToString]")
  }

  protected open fun replaceNodes(parentElement: Element, tagName: String, newTagName: String) {
    parentElement.getElementsByTag(tagName).forEach { element ->
      element.tagName(newTagName)
    }
  }

  /**
   * Finds the next element, starting from the given node, and ignoring
   * whitespace in between. If the given node is an element, the same node is
   * returned.
   */
  protected open fun nextElement(node: Node?, regEx: RegExUtil): Element? {
    var next: Node? = node

    while (next != null &&
      (next is Element == false) &&
      (next is TextNode && regEx.isWhitespace(next.text()))
    ) {
      next = next.nextSibling()
    }

    return next as? Element
  }

  /**
   * Get the inner text of a node - cross browser compatibly.
   * This also strips out any excess whitespace to be found.
   */
  protected open fun getInnerText(e: Element, regEx: RegExUtil? = null, normalizeSpaces: Boolean = true): String {
    val textContent = e.text().trim()

    if (normalizeSpaces && regEx != null) {
      return regEx.normalize(textContent)
    }

    return textContent
  }

  protected open fun isProbablyVisible(node: Element): Boolean {
    val style = node.attr("style")
    if (style.contains("display:none", ignoreCase = true) ||
      style.contains("display: none", ignoreCase = true)
    ) {
      return false
    }
    if (style.contains("visibility:hidden", ignoreCase = true) ||
      style.contains("visibility: hidden", ignoreCase = true)
    ) {
      return false
    }
    if (node.hasAttr("hidden")) {
      return false
    }
    if (node.hasAttr("aria-hidden") && node.attr("aria-hidden") == "true") {
      val className = node.className()
      if (!className.contains("fallback-image")) {
        return false
      }
    }
    return true
  }

  protected open fun isSingleImage(node: Element): Boolean {
    var current: Element? = node
    while (current != null) {
      if (current.tagName() == "img") {
        return true
      }

      val children = current.children()
      if (children.size != 1 || current.textNodes().any { it.text().trim().isNotEmpty() }) {
        return false
      }

      current = children.firstOrNull()
    }
    return false
  }
}
