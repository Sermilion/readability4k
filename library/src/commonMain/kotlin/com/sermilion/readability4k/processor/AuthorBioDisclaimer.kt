package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode

internal object AuthorBioDisclaimer {
  private val compoundClassOrId = Regex(
    "author-bio|authorbio|author-box|authorbox|author-info|author-profile|" +
      "author-description|author-details|about-author|about-the-author|" +
      "aboutauthor|writer-bio|bio-box|journalist",
    RegexOption.IGNORE_CASE,
  )
  private val authorOrDisclaimerToken = Regex("""\b(?:author|disclaimer)\b""", RegexOption.IGNORE_CASE)
  private val relAuthorToken = Regex("""(?:^|\s)author(?:\s|$)""", RegexOption.IGNORE_CASE)
  private val trailingColon = Regex("""\s*:\s*$""")
  private val headingNames = setOf(
    "about the author",
    "about author",
    "author bio",
    "disclaimer",
  )
  private val authorBoxTags = setOf("div", "aside", "section", "footer", "article")
  private const val TEXT_CUT_MARKER = "\uE000"

  fun isAuthorBioOrDisclaimerHeading(element: Element): Boolean {
    val normalized = element.text().replace(trailingColon, "").trim().lowercase()
    return normalized in headingNames
  }

  /**
   * Single-paragraph divs are replaced by their child before [strip] runs.
   * Copy a matching class or id onto that child so the later pass can still see it.
   */
  fun transferMatchingClassAndId(from: Element, to: Element) {
    val matchString = from.className() + " " + from.id()
    val semanticAuthorBox =
      from.tagName().lowercase() in authorBoxTags &&
        (
          relAuthorToken.containsMatchIn(from.attr("rel")) ||
            from.attr("itemtype").contains("Person", ignoreCase = true)
          )
    val matches = compoundClassOrId.containsMatchIn(matchString) ||
      authorOrDisclaimerToken.containsMatchIn(matchString) ||
      semanticAuthorBox
    if (!matches) {
      return
    }
    if (from.id().isNotEmpty() && to.id().isEmpty()) {
      to.attr("id", from.id())
    }
    from.classNames().toList().forEach { className ->
      to.addClass(className)
    }
  }

  fun strip(
    articleContent: Element,
    nextNode: (Element, Boolean) -> Element?,
    removeElement: (Element) -> Element?,
    removeTagged: (String, (Element) -> Boolean) -> Unit,
    removeNode: (Node) -> Unit,
  ) {
    removeNamedHeadingBlocks(articleContent, removeTagged, removeNode)
    removeClassAndIdBlocks(articleContent, nextNode, removeElement)
  }

  private fun removeNamedHeadingBlocks(
    articleContent: Element,
    removeTagged: (String, (Element) -> Boolean) -> Unit,
    removeNode: (Node) -> Unit,
  ) {
    for (level in 1..6) {
      removeTagged("h$level") { heading ->
        headingBlockShouldBeRemoved(articleContent, heading, level, removeNode)
      }
    }
  }

  private fun headingBlockShouldBeRemoved(
    articleContent: Element,
    heading: Element,
    level: Int,
    removeNode: (Node) -> Unit,
  ): Boolean {
    if (!isAuthorBioOrDisclaimerHeading(heading)) {
      return false
    }
    val trailing = nodesUntilNextHeading(heading, level)
    val scratch = Element("div")
    scratch.appendChild(heading.clone())
    trailing.forEach { node -> scratch.appendChild(node.clone()) }
    val blockLength = scratch.text().length
    if (precedingTextLength(articleContent, heading) <= blockLength) {
      return false
    }
    trailing.asReversed().forEach { node -> removeNode(node) }
    return true
  }

  private fun removeClassAndIdBlocks(
    articleContent: Element,
    nextNode: (Element, Boolean) -> Element?,
    removeElement: (Element) -> Element?,
  ) {
    val endOfSearch = nextNode(articleContent, true)
    var node = nextNode(articleContent, false)
    while (node != null && node != endOfSearch) {
      val current = node
      node = if (shouldRemoveClassOrId(articleContent, current)) {
        removeElement(current)
      } else {
        nextNode(current, false)
      }
    }
  }

  private fun shouldRemoveClassOrId(articleContent: Element, node: Element): Boolean {
    if (isHeadingElement(node, 6) && isAuthorBioOrDisclaimerHeading(node)) {
      return false
    }
    val matchString = node.className() + " " + node.id()
    if (compoundClassOrId.containsMatchIn(matchString)) {
      return true
    }
    val semanticAuthorBox =
      node.tagName().lowercase() in authorBoxTags &&
        (
          relAuthorToken.containsMatchIn(node.attr("rel")) ||
            node.attr("itemtype").contains("Person", ignoreCase = true)
          )
    if (!authorOrDisclaimerToken.containsMatchIn(matchString) && !semanticAuthorBox) {
      return false
    }
    return precedingTextLength(articleContent, node) > node.text().length
  }

  private fun nodesUntilNextHeading(heading: Element, level: Int): List<Node> {
    val trailing = ArrayList<Node>()
    var sibling = heading.nextSibling()
    while (sibling != null) {
      val current = sibling
      if (current is Element && isHeadingElement(current, level)) {
        break
      }
      trailing.add(current)
      sibling = current.nextSibling()
    }
    return trailing
  }

  private fun isHeadingElement(element: Element, maxRank: Int): Boolean {
    val tag = element.tagName().lowercase()
    if (tag.length != 2 || tag[0] != 'h') {
      return false
    }
    val rank = tag[1].digitToIntOrNull() ?: return false
    return rank in 1..maxRank
  }

  private fun precedingTextLength(root: Element, target: Node): Int {
    if (target.parent() == null) {
      return 0
    }
    val marker = TextNode(TEXT_CUT_MARKER)
    target.before(marker)
    return try {
      val text = root.text()
      val cut = text.indexOf(TEXT_CUT_MARKER)
      if (cut < 0) 0 else text.substring(0, cut).trim().length
    } finally {
      marker.remove()
    }
  }
}
