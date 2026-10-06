package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node

internal data class CapturedAttributes(
  val className: String,
  val id: String,
  val rel: String,
  val itemtype: String,
  val tagName: String,
) {
  val matchString: String get() = "$className $id"
}

internal data class LabelSpanCapture(
  val label: Element,
  val nodes: List<Node>,
)

internal class AuthorBioSnapshot(
  val attributesByElement: Map<Element, CapturedAttributes>,
  val labelSpans: List<LabelSpanCapture>,
) : ArticleExtensionSnapshot {
  companion object {
    val EMPTY = AuthorBioSnapshot(emptyMap(), emptyList())
  }
}

internal data class AuthorBioCandidate(
  val start: Node,
  val nodes: List<Node>,
  val blockElement: Element?,
  val hasBioLabel: Boolean,
  val hasDisclaimerLabel: Boolean,
)

internal data class EvaluatedCandidate(
  val candidate: AuthorBioCandidate,
  val order: Int,
  val depth: Int,
  val decisions: List<AuthorBioDecision>,
  val shouldRemove: Boolean,
)
