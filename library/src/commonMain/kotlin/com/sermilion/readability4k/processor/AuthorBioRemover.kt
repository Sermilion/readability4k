package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node

internal class AuthorBioRemover {
  fun remove(articleContent: Element, selected: List<EvaluatedCandidate>) {
    selected.forEach { evaluated ->
      if (!evaluated.shouldRemove) {
        return@forEach
      }
      evaluated.candidate.nodes.forEach { node ->
        removeNode(articleContent, node)
      }
    }
  }

  private fun removeNode(articleContent: Element, node: Node) {
    if (node.parent() == null) {
      return
    }
    if (node === articleContent) {
      return
    }
    if (!AuthorBioDom.isInside(articleContent, node)) {
      return
    }
    node.remove()
  }
}
