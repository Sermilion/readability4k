package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode

internal object AuthorBioSignals {
  private val compoundClassOrId = Regex(
    "author-bio|authorbio|author-box|authorbox|author-info|author-profile|" +
      "author-description|author-details|about-author|about-the-author|" +
      "aboutauthor|writer-bio|bio-box|journalist",
    RegexOption.IGNORE_CASE,
  )
  private val authorToken = Regex("""\bauthor\b""", RegexOption.IGNORE_CASE)
  private val disclaimerToken = Regex("""\bdisclaimer\b""", RegexOption.IGNORE_CASE)
  val relAuthorToken = Regex("""(?:^|\s)author(?:\s|$)""", RegexOption.IGNORE_CASE)
  private val trailingColon = Regex("""\s*:\s*$""")

  val blockTags = setOf("div", "aside", "section", "footer", "article")
  private val bioLabels = setOf("about the author", "about author", "author bio")
  private const val DISCLAIMER_LABEL = "disclaimer"

  private fun normalizeLabel(text: String): String = text.replace(trailingColon, "").trim().lowercase()

  fun isBioLabel(text: String): Boolean = normalizeLabel(text) in bioLabels

  fun isDisclaimerLabel(text: String): Boolean = normalizeLabel(text) == DISCLAIMER_LABEL

  fun hasCompound(attrs: CapturedAttributes): Boolean = compoundClassOrId.containsMatchIn(attrs.matchString)

  fun hasBareAuthor(attrs: CapturedAttributes): Boolean =
    !hasCompound(attrs) && authorToken.containsMatchIn(attrs.matchString)

  fun hasDisclaimerToken(attrs: CapturedAttributes): Boolean = disclaimerToken.containsMatchIn(attrs.matchString)

  fun hasRelAuthor(attrs: CapturedAttributes): Boolean = relAuthorToken.containsMatchIn(attrs.rel)

  fun hasPerson(attrs: CapturedAttributes): Boolean = attrs.itemtype.contains("Person", ignoreCase = true)

  fun hasDiscoverySignal(attrs: CapturedAttributes): Boolean = hasCompound(attrs) ||
    authorToken.containsMatchIn(attrs.matchString) ||
    hasDisclaimerToken(attrs) ||
    hasRelAuthor(attrs) ||
    hasPerson(attrs)
}

internal object AuthorBioDom {
  private val hasContent = Regex("""\S$""")

  fun headingRank(element: Element): Int? {
    val tag = element.tagName().lowercase()
    if (tag.length != 2 || tag[0] != 'h') {
      return null
    }
    val rank = tag[1].digitToIntOrNull() ?: return null
    return rank.takeIf { it in 1..6 }
  }

  fun isLabelElement(element: Element): Boolean = element.tagName().lowercase() == "p" || headingRank(element) != null

  fun hasSinglePInsideElement(element: Element): Boolean {
    if (element.children().size != 1 || element.child(0).tagName() != "p") {
      return false
    }
    element.childNodes().forEach { node ->
      if (node is TextNode && hasContent.containsMatchIn(node.text())) {
        return false
      }
    }
    return true
  }

  fun captureAttrs(element: Element): CapturedAttributes = CapturedAttributes(
    className = element.className(),
    id = element.id(),
    rel = element.attr("rel"),
    itemtype = element.attr("itemtype"),
    tagName = element.tagName().lowercase(),
  )

  fun effectiveAttrs(element: Element, snapshot: AuthorBioSnapshot): CapturedAttributes {
    val stored = snapshot.attributesByElement[element]
    fun pick(live: String, snap: String?): String = live.ifBlank { snap.orEmpty() }
    return CapturedAttributes(
      className = pick(element.className(), stored?.className),
      id = pick(element.id(), stored?.id),
      rel = pick(element.attr("rel"), stored?.rel),
      itemtype = pick(element.attr("itemtype"), stored?.itemtype),
      tagName = element.tagName().lowercase(),
    )
  }

  fun nodesUntilStop(label: Element, stop: (Element) -> Boolean): List<Node> {
    val nodes = ArrayList<Node>()
    nodes.add(label)
    var sibling = label.nextSibling()
    while (sibling != null) {
      val current = sibling
      if (current is Element && stop(current)) {
        break
      }
      nodes.add(current)
      sibling = current.nextSibling()
    }
    return nodes
  }

  fun isInside(root: Element, node: Node): Boolean {
    if (node === root) {
      return true
    }
    var parent = node.parent()
    while (parent != null) {
      if (parent === root) {
        return true
      }
      parent = parent.parent()
    }
    return false
  }

  fun combinedText(nodes: List<Node>): String {
    val scratch = Element("div")
    nodes.forEach { node -> scratch.appendChild(node.clone()) }
    return scratch.text()
  }

  fun nodeDepth(node: Node): Int {
    var depth = 0
    var parent = node.parent()
    while (parent != null) {
      depth++
      parent = parent.parent()
    }
    return depth
  }
}
