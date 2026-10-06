package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.fleeksoft.ksoup.nodes.TextNode

internal class AuthorBioScorer {
  fun evaluate(
    articleContent: Element,
    candidates: List<AuthorBioCandidate>,
    snapshot: AuthorBioSnapshot,
    evidence: ArticleEvidence,
  ): List<EvaluatedCandidate> {
    val order = documentOrder(articleContent)
    val articleTextLength = articleContent.text().length
    val scored = candidates.map { candidate ->
      scoreOne(articleContent, candidate, snapshot, evidence, order, articleTextLength)
    }
    return select(articleContent, scored)
  }

  private fun scoreOne(
    articleContent: Element,
    candidate: AuthorBioCandidate,
    snapshot: AuthorBioSnapshot,
    evidence: ArticleEvidence,
    order: Map<Node, Int>,
    articleTextLength: Int,
  ): EvaluatedCandidate {
    val inTree = candidate.nodes.filter { node ->
      node.parent() != null && AuthorBioDom.isInside(articleContent, node) &&
        node !== articleContent
    }
    val nodes = inTree.ifEmpty { candidate.nodes }
    val start = inTree.firstOrNull() ?: candidate.start
    val depth = AuthorBioDom.nodeDepth(start)
    val orderIndex = inTree.firstOrNull()?.let { order[it] } ?: order[candidate.start] ?: Int.MAX_VALUE
    val elementForEvidence = primaryElement(candidate, nodes)
    val linkDensity = elementForEvidence?.let { evidence.linkDensity(it) } ?: 0.0
    val hasDescendantRel = hasDescendantRelAuthor(nodes)
    val contentScore = elementForEvidence?.let { evidence.contentScoreBeforeLinkAdjustment(it) }
    val attrs = elementForEvidence?.let { AuthorBioDom.effectiveAttrs(it, snapshot) }
      ?: CapturedAttributes("", "", "", "", "")
    val precedingLonger = precedingIsLonger(articleContent, nodes)
    val spanText = AuthorBioDom.combinedText(nodes)
    val commaCount = spanText.count { it == ',' }

    val hardVeto = hardStopVeto(
      articleContent = articleContent,
      candidate = candidate,
      inTree = inTree,
      spanTextLength = spanText.length,
      articleTextLength = articleTextLength,
    )
    val decisions = trackDecisions(
      candidate = candidate,
      attrs = attrs,
      context = DecisionContext(
        linkDensity = linkDensity,
        hasDescendantRelAuthor = hasDescendantRel,
        contentScoreBeforeLinkAdjustment = contentScore,
        precedingLonger = precedingLonger,
        hardVeto = hardVeto,
        commaCount = commaCount,
        spanTextLength = spanText.length,
      ),
    )
    val shouldRemove = decisions.any { it.removed }
    return EvaluatedCandidate(
      candidate = candidate.copy(nodes = nodes, start = start),
      order = orderIndex,
      depth = depth,
      decisions = decisions,
      shouldRemove = shouldRemove,
    )
  }

  private fun select(articleContent: Element, scored: List<EvaluatedCandidate>): List<EvaluatedCandidate> {
    val sorted = scored.sortedWith(compareBy<EvaluatedCandidate> { it.order }.thenBy { it.depth })
    val chosen = ArrayList<EvaluatedCandidate>()
    return sorted.map { evaluated ->
      if (!evaluated.shouldRemove) {
        return@map evaluated
      }
      if (isNested(evaluated, chosen, articleContent)) {
        evaluated.copy(
          shouldRemove = false,
          decisions = evaluated.decisions.map { decision ->
            decision.copy(veto = AuthorBioScoring.VETO_NESTED, removed = false)
          },
        )
      } else {
        chosen.add(evaluated)
        evaluated
      }
    }
  }

  private fun isNested(
    evaluated: EvaluatedCandidate,
    chosen: List<EvaluatedCandidate>,
    articleContent: Element,
  ): Boolean {
    val start = evaluated.candidate.start
    chosen.forEach { selected ->
      selected.candidate.nodes.forEach { node ->
        if (node !is Element) {
          return@forEach
        }
        if (AuthorBioDom.isInside(node, start) && node !== articleContent) {
          return true
        }
      }
    }
    return false
  }

  private fun hardStopVeto(
    articleContent: Element,
    candidate: AuthorBioCandidate,
    inTree: List<Node>,
    spanTextLength: Int,
    articleTextLength: Int,
  ): String? {
    val block = candidate.blockElement
    if (block === articleContent || candidate.start === articleContent) {
      return AuthorBioScoring.VETO_ARTICLE_ROOT
    }
    if (inTree.isEmpty()) {
      return AuthorBioScoring.VETO_DETACHED
    }
    if (articleTextLength > 0 &&
      spanTextLength > articleTextLength * AuthorBioScoring.FRACTION
    ) {
      return AuthorBioScoring.VETO_FRACTION
    }
    return null
  }

  private fun bioFeatures(
    candidate: AuthorBioCandidate,
    attrs: CapturedAttributes,
    precedingLonger: Boolean,
  ): List<AuthorBioFeatureAward> {
    val features = ArrayList<AuthorBioFeatureAward>()
    if (candidate.hasBioLabel) {
      features.add(AuthorBioFeatureAward(AuthorBioScoring.FEATURE_BIO_LABEL, AuthorBioScoring.BIO_LABEL))
    }
    if (AuthorBioSignals.hasCompound(attrs)) {
      features.add(AuthorBioFeatureAward(AuthorBioScoring.FEATURE_COMPOUND, AuthorBioScoring.COMPOUND))
    } else if (AuthorBioSignals.hasBareAuthor(attrs)) {
      features.add(AuthorBioFeatureAward(AuthorBioScoring.FEATURE_BARE_AUTHOR, AuthorBioScoring.BARE_AUTHOR))
    }
    if (AuthorBioSignals.hasRelAuthor(attrs)) {
      features.add(AuthorBioFeatureAward(AuthorBioScoring.FEATURE_REL_AUTHOR, AuthorBioScoring.REL_AUTHOR))
    }
    if (AuthorBioSignals.hasPerson(attrs)) {
      features.add(AuthorBioFeatureAward(AuthorBioScoring.FEATURE_PERSON, AuthorBioScoring.PERSON))
    }
    if (precedingLonger) {
      features.add(AuthorBioFeatureAward(AuthorBioScoring.FEATURE_PRECEDING, AuthorBioScoring.PRECEDING))
    }
    return features
  }

  private fun disclaimerFeatures(
    candidate: AuthorBioCandidate,
    attrs: CapturedAttributes,
    precedingLonger: Boolean,
  ): List<AuthorBioFeatureAward> {
    val features = ArrayList<AuthorBioFeatureAward>()
    if (candidate.hasDisclaimerLabel) {
      features.add(
        AuthorBioFeatureAward(AuthorBioScoring.FEATURE_DISCLAIMER_LABEL, AuthorBioScoring.DISCLAIMER_LABEL),
      )
    }
    if (AuthorBioSignals.hasDisclaimerToken(attrs)) {
      features.add(
        AuthorBioFeatureAward(AuthorBioScoring.FEATURE_DISCLAIMER_TOKEN, AuthorBioScoring.DISCLAIMER_TOKEN),
      )
    }
    if (precedingLonger && features.isNotEmpty()) {
      features.add(AuthorBioFeatureAward(AuthorBioScoring.FEATURE_PRECEDING, AuthorBioScoring.PRECEDING))
    }
    return features
  }

  private fun trackDecisions(
    candidate: AuthorBioCandidate,
    attrs: CapturedAttributes,
    context: DecisionContext,
  ): List<AuthorBioDecision> {
    val bioFeatures = bioFeatures(candidate, attrs, context.precedingLonger)
    val disclaimerFeatures = disclaimerFeatures(candidate, attrs, context.precedingLonger)
    val bioDecision = decision(
      track = AuthorBioTrack.BIO,
      features = bioFeatures,
      hardVeto = context.hardVeto,
      disclaimerVeto = null,
      context = context,
    )
    val disclaimerDecision = decision(
      track = AuthorBioTrack.DISCLAIMER,
      features = disclaimerFeatures,
      hardVeto = context.hardVeto,
      disclaimerVeto = disclaimerKeepVeto(context.hardVeto, context.commaCount, context.spanTextLength),
      context = context,
    )
    return listOfNotNull(
      bioDecision.takeIf { bioFeatures.isNotEmpty() || disclaimerFeatures.isEmpty() },
      disclaimerDecision.takeIf { disclaimerFeatures.isNotEmpty() },
    )
  }

  private fun disclaimerKeepVeto(hardVeto: String?, commaCount: Int, spanTextLength: Int): String? = when {
    hardVeto != null -> null
    commaCount >= AuthorBioScoring.DISCLAIMER_COMMA_KEEP -> AuthorBioScoring.VETO_DISCLAIMER_COMMAS
    spanTextLength > AuthorBioScoring.DISCLAIMER_LENGTH_KEEP -> AuthorBioScoring.VETO_DISCLAIMER_LENGTH
    else -> null
  }

  private fun decision(
    track: AuthorBioTrack,
    features: List<AuthorBioFeatureAward>,
    hardVeto: String?,
    disclaimerVeto: String?,
    context: DecisionContext,
  ): AuthorBioDecision {
    val total = features.sumOf { it.points }
    val veto = when {
      hardVeto != null -> hardVeto
      track == AuthorBioTrack.DISCLAIMER && disclaimerVeto != null -> disclaimerVeto
      total < AuthorBioScoring.THRESHOLD -> AuthorBioScoring.VETO_BELOW_THRESHOLD
      else -> null
    }
    val removed = veto == null && total >= AuthorBioScoring.THRESHOLD
    return AuthorBioDecision(
      track = track,
      features = features,
      total = total,
      veto = veto,
      removed = removed,
      linkDensity = context.linkDensity,
      hasDescendantRelAuthor = context.hasDescendantRelAuthor,
      contentScoreBeforeLinkAdjustment = context.contentScoreBeforeLinkAdjustment,
    )
  }

  private fun primaryElement(candidate: AuthorBioCandidate, nodes: List<Node>): Element? {
    candidate.blockElement?.let { return it }
    nodes.filterIsInstance<Element>().firstOrNull()?.let { return it }
    return candidate.start as? Element
  }

  private fun precedingIsLonger(root: Element, nodes: List<Node>): Boolean {
    val target = nodes.firstOrNull { it.parent() != null } ?: return false
    val preceding = precedingTextLength(root, target)
    val candidateLength = AuthorBioDom.combinedText(nodes).length
    return preceding > candidateLength
  }

  private fun precedingTextLength(root: Element, target: Node): Int {
    if (target.parent() == null) {
      return 0
    }
    val marker = TextNode(MARKER)
    target.before(marker)
    return try {
      val text = root.text()
      val cut = text.indexOf(MARKER)
      if (cut < 0) 0 else text.substring(0, cut).trim().length
    } finally {
      marker.remove()
    }
  }

  private fun hasDescendantRelAuthor(nodes: List<Node>): Boolean {
    nodes.forEach { node ->
      if (node !is Element) {
        return@forEach
      }
      if (node.tagName() == "a" && AuthorBioSignals.relAuthorToken.containsMatchIn(node.attr("rel"))) {
        return true
      }
      node.getElementsByTag("a").forEach { anchor ->
        if (AuthorBioSignals.relAuthorToken.containsMatchIn(anchor.attr("rel"))) {
          return true
        }
      }
    }
    return false
  }

  private fun documentOrder(root: Element): Map<Node, Int> {
    val order = HashMap<Node, Int>()
    var index = 0
    fun walk(node: Node) {
      order[node] = index
      index++
      ArrayList(node.childNodes()).forEach { child -> walk(child) }
    }
    walk(root)
    return order
  }

  companion object {
    private const val MARKER = "\uE000"
  }
}

private data class DecisionContext(
  val linkDensity: Double,
  val hasDescendantRelAuthor: Boolean,
  val contentScoreBeforeLinkAdjustment: Double?,
  val precedingLonger: Boolean,
  val hardVeto: String?,
  val commaCount: Int,
  val spanTextLength: Int,
)
