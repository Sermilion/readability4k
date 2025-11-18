package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element

fun interface CandidateFilter {
  fun shouldIncludeCandidate(candidate: Element): Boolean
}

object BlockquoteDescendantFilter : CandidateFilter {
  override fun shouldIncludeCandidate(candidate: Element): Boolean =
    !hasAncestorTag(candidate, "blockquote")

  private fun hasAncestorTag(node: Element?, tag: String): Boolean {
    var currentNode = node
    while (currentNode?.parent() != null) {
      currentNode = currentNode.parent()
      if (currentNode?.tagName() == tag) {
        return true
      }
    }
    return false
  }
}
