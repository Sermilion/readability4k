package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node

internal class AuthorBioCandidateFinder {
  fun capture(root: Element): AuthorBioSnapshot {
    val attributes = HashMap<Element, CapturedAttributes>()
    val labelSpans = ArrayList<LabelSpanCapture>()
    val elements = root.getAllElements()
    elements.forEach { element ->
      attributes[element] = AuthorBioDom.captureAttrs(element)
      val rank = AuthorBioDom.headingRank(element)
      val isParagraph = element.tagName() == "p"
      val text = element.text()
      if ((rank != null || isParagraph) &&
        (AuthorBioSignals.isBioLabel(text) || AuthorBioSignals.isDisclaimerLabel(text))
      ) {
        val nodes = if (rank != null) {
          AuthorBioDom.nodesUntilStop(element) { sibling ->
            val siblingRank = AuthorBioDom.headingRank(sibling)
            siblingRank != null && siblingRank in 1..rank
          }
        } else {
          AuthorBioDom.nodesUntilStop(element) { sibling ->
            AuthorBioDom.headingRank(sibling) != null
          }
        }
        labelSpans.add(LabelSpanCapture(element, nodes))
      }
    }
    elements.forEach { element ->
      if (element.tagName() != "div" || !AuthorBioDom.hasSinglePInsideElement(element)) {
        return@forEach
      }
      val child = element.child(0)
      val attrs = attributes[element] ?: return@forEach
      val childAttrs = attributes[child] ?: AuthorBioDom.captureAttrs(child)
      attributes[child] = CapturedAttributes(
        className = attrs.className.ifBlank { childAttrs.className },
        id = attrs.id.ifBlank { childAttrs.id },
        rel = attrs.rel.ifBlank { childAttrs.rel },
        itemtype = attrs.itemtype.ifBlank { childAttrs.itemtype },
        tagName = childAttrs.tagName,
      )
    }
    return AuthorBioSnapshot(attributes, labelSpans)
  }

  fun find(articleContent: Element, snapshot: AuthorBioSnapshot): List<AuthorBioCandidate> {
    val candidates = ArrayList<AuthorBioCandidate>()
    val signaledBlocks = HashSet<Element>()
    addSignaledBlocks(articleContent, snapshot, candidates, signaledBlocks)
    addUnwrappedParagraphs(articleContent, snapshot, candidates, signaledBlocks)
    addBareLabelSpans(articleContent, snapshot, candidates, signaledBlocks)
    return candidates
  }

  private fun addSignaledBlocks(
    articleContent: Element,
    snapshot: AuthorBioSnapshot,
    candidates: MutableList<AuthorBioCandidate>,
    signaledBlocks: MutableSet<Element>,
  ) {
    articleContent.getAllElements().forEach { element ->
      if (!isBlockCandidate(element, snapshot)) {
        return@forEach
      }
      val attrs = AuthorBioDom.effectiveAttrs(element, snapshot)
      if (!AuthorBioSignals.hasDiscoverySignal(attrs)) {
        return@forEach
      }
      signaledBlocks.add(element)
      candidates.add(blockCandidate(element, snapshot, signaledBlocks))
    }
  }

  private fun addUnwrappedParagraphs(
    articleContent: Element,
    snapshot: AuthorBioSnapshot,
    candidates: MutableList<AuthorBioCandidate>,
    signaledBlocks: MutableSet<Element>,
  ) {
    articleContent.getAllElements().forEach { element ->
      if (!isUnwrappedAuthorParagraph(element, snapshot)) {
        return@forEach
      }
      signaledBlocks.add(element)
      candidates.add(blockCandidate(element, snapshot, signaledBlocks))
    }
  }

  private fun isUnwrappedAuthorParagraph(element: Element, snapshot: AuthorBioSnapshot): Boolean {
    val stored = snapshot.attributesByElement[element]
    if (element.tagName().lowercase() != "p" || stored == null) {
      return false
    }
    val text = element.text()
    val inheritedSignal = stored.tagName.lowercase() !in AuthorBioSignals.blockTags &&
      stored != AuthorBioDom.captureAttrs(element) &&
      AuthorBioSignals.hasDiscoverySignal(stored) &&
      AuthorBioSignals.hasDiscoverySignal(AuthorBioDom.effectiveAttrs(element, snapshot))
    val prose = !AuthorBioSignals.isBioLabel(text) && !AuthorBioSignals.isDisclaimerLabel(text)
    return inheritedSignal && prose
  }

  private fun addBareLabelSpans(
    articleContent: Element,
    snapshot: AuthorBioSnapshot,
    candidates: MutableList<AuthorBioCandidate>,
    signaledBlocks: Set<Element>,
  ) {
    snapshot.labelSpans.forEach { span ->
      if (signaledAncestor(span, articleContent, signaledBlocks) != null) {
        return@forEach
      }
      val remaining = span.nodes.filter { node ->
        node.parent() != null && AuthorBioDom.isInside(articleContent, node)
      }
      val start = remaining.firstOrNull() ?: span.label
      val labelText = span.label.text()
      candidates.add(
        AuthorBioCandidate(
          start = start,
          nodes = remaining.ifEmpty { span.nodes },
          blockElement = null,
          hasBioLabel = AuthorBioSignals.isBioLabel(labelText),
          hasDisclaimerLabel = AuthorBioSignals.isDisclaimerLabel(labelText),
        ),
      )
    }
  }

  private fun blockCandidate(
    element: Element,
    snapshot: AuthorBioSnapshot,
    signaledBlocks: Set<Element>,
  ): AuthorBioCandidate {
    val labels = labelsOwnedBy(element, snapshot, signaledBlocks)
    return AuthorBioCandidate(
      start = element,
      nodes = listOf(element),
      blockElement = element,
      hasBioLabel = labels.first,
      hasDisclaimerLabel = labels.second,
    )
  }

  private fun isBlockCandidate(element: Element, snapshot: AuthorBioSnapshot): Boolean {
    val liveTag = element.tagName().lowercase()
    if (liveTag in AuthorBioSignals.blockTags) {
      return true
    }
    val originalTag = snapshot.attributesByElement[element]?.tagName?.lowercase() ?: return false
    return originalTag in AuthorBioSignals.blockTags
  }

  private fun labelsOwnedBy(
    block: Element,
    snapshot: AuthorBioSnapshot,
    signaledBlocks: Set<Element>,
  ): Pair<Boolean, Boolean> {
    var bio = AuthorBioSignals.isBioLabel(block.text())
    var disclaimer = AuthorBioSignals.isDisclaimerLabel(block.text())
    snapshot.labelSpans.forEach { span ->
      if (signaledAncestor(span, block, signaledBlocks) !== block) {
        return@forEach
      }
      val text = span.label.text()
      if (AuthorBioSignals.isBioLabel(text)) {
        bio = true
      }
      if (AuthorBioSignals.isDisclaimerLabel(text)) {
        disclaimer = true
      }
    }
    if (!bio && !disclaimer) {
      block.getAllElements().forEach { child ->
        if (child === block || !AuthorBioDom.isLabelElement(child)) {
          return@forEach
        }
        val text = child.text()
        if (AuthorBioSignals.isBioLabel(text)) {
          bio = true
        }
        if (AuthorBioSignals.isDisclaimerLabel(text)) {
          disclaimer = true
        }
      }
    }
    return bio to disclaimer
  }

  private fun signaledAncestor(span: LabelSpanCapture, root: Element, signaledBlocks: Set<Element>): Element? {
    val walkFrom: Node? = when {
      span.label.parent() != null && AuthorBioDom.isInside(root, span.label) -> span.label
      else -> span.nodes.firstOrNull { node ->
        node.parent() != null && AuthorBioDom.isInside(root, node)
      }
    }
    var parent = walkFrom?.parent()
    while (parent is Element) {
      if (parent === root && parent.tagName().lowercase() in AuthorBioSignals.blockTags &&
        parent in signaledBlocks
      ) {
        return parent
      }
      if (parent !== root && parent in signaledBlocks) {
        return parent
      }
      if (parent === root) {
        break
      }
      parent = parent.parent()
    }
    return null
  }
}
