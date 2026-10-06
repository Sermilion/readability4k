package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element

enum class AuthorBioTrack {
  BIO,
  DISCLAIMER,
}

data class AuthorBioFeatureAward(
  val name: String,
  val points: Int,
)

data class AuthorBioDecision(
  val track: AuthorBioTrack,
  val features: List<AuthorBioFeatureAward>,
  val total: Int,
  val veto: String?,
  val removed: Boolean,
  val linkDensity: Double,
  val hasDescendantRelAuthor: Boolean,
  val contentScoreBeforeLinkAdjustment: Double?,
)

/**
 * Injectable scored cleanup for author-bio and ancillary disclaimer blocks.
 *
 * The instance is stateless. Callers who want this cleanup pass it as
 * `contentExtension`; the default parse does not construct or apply it.
 */
class AuthorBioDisclaimerExtension : ArticleContentExtension {
  private val finder = AuthorBioCandidateFinder()
  private val scorer = AuthorBioScorer()
  private val remover = AuthorBioRemover()

  override fun capture(root: Element): ArticleExtensionSnapshot = finder.capture(root)

  fun decisions(
    articleContent: Element,
    snapshot: ArticleExtensionSnapshot,
    evidence: ArticleEvidence,
  ): List<AuthorBioDecision> = evaluate(articleContent, snapshot, evidence).flatMap { it.decisions }

  override fun apply(articleContent: Element, snapshot: ArticleExtensionSnapshot, evidence: ArticleEvidence) {
    val evaluated = evaluate(articleContent, snapshot, evidence)
    remover.remove(articleContent, evaluated)
  }

  private fun evaluate(
    articleContent: Element,
    snapshot: ArticleExtensionSnapshot,
    evidence: ArticleEvidence,
  ): List<EvaluatedCandidate> {
    val authorSnapshot = snapshot as? AuthorBioSnapshot ?: AuthorBioSnapshot.EMPTY
    val candidates = finder.find(articleContent, authorSnapshot)
    return scorer.evaluate(articleContent, candidates, authorSnapshot, evidence)
  }
}
