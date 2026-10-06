package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element

/**
 * Marker for evidence captured before grabber normalization.
 *
 * Implementations may store element identity from the pre-`prepareNodes` tree.
 * The grabber holds the value only as a local of this type.
 */
interface ArticleExtensionSnapshot

/**
 * Optional, single-slot hook around article extraction.
 *
 * `capture` runs on each extraction attempt before `prepareNodes`.
 * `apply` runs after `prepArticle` and before the readability page wrap.
 * A null extension is not called.
 */
interface ArticleContentExtension {
  fun capture(root: Element): ArticleExtensionSnapshot

  fun apply(articleContent: Element, snapshot: ArticleExtensionSnapshot, evidence: ArticleEvidence)
}

/**
 * Read-only grabber evidence for one extraction attempt.
 *
 * Does not expose mutable grabber state. `contentScoreBeforeLinkAdjustment`
 * is the value copied before top-candidate link-density overwrite, or null
 * when the element has no stored score. It does not add class weight or
 * link density on top of that copy.
 */
interface ArticleEvidence {
  fun contentScoreBeforeLinkAdjustment(element: Element): Double?

  fun linkDensity(element: Element): Double

  val weightClasses: Boolean
}
