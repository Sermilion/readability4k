# R4K-1 Subtask 1 - Implement the requested change

Parent spec: [.feature-specs/R4K-1-replace-author-bio-and-disclaimer-cleanup-with-an-injectable-scored-extension/spec.md](./spec.md)
Issue key: R4K-1

## Scope

R4K-1 Replace author-bio and disclaimer cleanup with an injectable scored extension

Work in /home/sermilion/IdeaProjects/readability4k.

We want to fully replace the R4K-1 plan, then stop for review. This request authorizes planning only. Do not implement, commit, push, open a PR, publish a release, or update Readian.

### Problem

Readian reading mode retains author bios and ancillary disclaimer blocks in extracted article HTML and text. Examples include Notebookcheck sidebar bios and TweakTown “About the author” sections.

Normal Readability cleanup misses them because long bios evade short-byline detection, some author/sidebar classes survive unlikely-candidate removal, and dense containers survive conditional cleaning.

The current implementation adds AuthorBioDisclaimer.strip at the end of prepArticle. It removes blocks through class/id matches, English heading matches, semantic author attributes, and preceding-text length comparisons. It does not use content scoring. It also introduces author-specific exceptions in cleanHeaders and attribute copying during div unwrapping.

We want to replace that design.

### Mandatory architecture

- Focused, separate classes must own author-bio/disclaimer detection, scoring, and removal.
- The behavior must be explicitly injectable through a generic optional extension contract.
- An empty default must run ordinary Readability without this cleanup.
- The main extraction algorithm must not reference, instantiate, or require the concrete author cleanup.
- Remove author-specific hooks from the grabber, including heading exemptions and matching class/id copying.
- Choose the smallest generic lifecycle that works. Investigate capturing source evidence before normalization and applying cleanup after article selection. Avoid building a broad plugin framework.
- Reuse generic Readability evidence through a read-only boundary. Do not expose mutable grabber state or require subclassing.
- Preserve KMP support and existing public API compatibility where practical. Consider constructor compatibility and synchronous/asynchronous parsing.
- Keep evidence and cleanup state local to each parse and extraction attempt.

### Scoring requirements

Replace unconditional deletion with an explainable score that weighs ancillary evidence against body-preservation evidence.

Potential signals include class/id weights, exact section labels, rel=author, schema Person metadata, link density, position, structural separation, and relative paragraph support.

Important constraints:

- Long, dense bios must remain removable even when normal paragraph scoring rewards their prose.
- A class name, heading, negative weight, high link density, or Person attribute alone must not trigger deletion.
- Preserve ambiguous cases, main-body wrappers named author-bio, profile articles, legitimate author-related sections, substantive disclaimers, and link-heavy body sections.
- Calibrate bios and disclaimers separately.
- Define candidate boundaries, feature calculations, decision formula, initial weights, thresholds, and calibration strategy. Identify provisional numerical choices.
- Evaluate a stable snapshot before deleting nodes and resolve overlapping candidates deterministically.
- Never remove the article root or consume the next equal-or-higher heading section.

Existing contentScore cannot simply be reused unchanged. Candidate selection overwrites it with a link-adjusted value, it mixes several signals, and some elements have no stored score. Avoid double-counting class weight or link adjustment. Account for DOM replacements, unwrapping, retries, and missing scores.

### Verification

Read AGENTS.md and docs/architecture.md first.

Relevant files:

- library/src/commonMain/kotlin/com/sermilion/readability4k/processor/AuthorBioDisclaimer.kt
- library/src/commonMain/kotlin/com/sermilion/readability4k/processor/ReadabilityArticleGrabber.kt
- library/src/commonTest/kotlin/com/sermilion/readability4k/AuthorBioDisclaimerRemovalTest.kt
- library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusRegressionTest.kt
- library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusIndex.kt
- library/src/jvmTest/resources/author-bio-corpus/

The offline corpus contains about 100 real pages across 31 hosts. Reuse it, but strengthen body-retention annotations beyond one phrase. Some current retention phrases appear to include widgets or subscription text and need review.

Seed fixtures, for verification only:

- https://www.tweaktown.com/rss/click/news/113920/?c=news-mf
- https://www.notebookcheck.net/ChatGPT-ads-German-regulators-warn-about-profiles-built-from-chats.1416940.0.html

The planning investigation found that TweakTown’s “About the author” label is a paragraph, so discovery must handle labels as well as heading tags.

Plan meaningful tests for explicit injection, empty injection, long bios, short articles, misleading classes, substantive disclaimers, person-profile articles, heading boundaries, nested candidates, lost semantic attributes during normalization, retries with class weighting disabled, state isolation, and sync/async equivalence.

Split calibration and validation by publisher. Measure wrongly removed body content separately from missed ancillary blocks. Prioritize body preservation.

No runtime host checks, publisher-specific selectors, Readian dependency bumps, releases, or broad changes to top-candidate selection.

### State

The following command succeeded:

skill-bill goal purge R4K-1 --confirm-issue-key R4K-1 --repo-root /home/sermilion/IdeaProjects/readability4k

Purge retained the old spec bundle. The first planning attempt refused to overwrite it, so it was archived at:

/tmp/readability4k-R4K-1-superseded.8hyFXK/R4K-1-strip-about-author-and-similar-disclaimer-blocks/

The archived spec is historical reference only. The existing implementation and corpus remain in the repository. Purging workflow state did not remove code. There is no readable R4K-1 spec in the working tree. Create a fresh R4K-1 spec bundle and actionable implementation plan. Do not restore the archived spec as the plan.

The second planning attempt failed before authoring the replacement plan because its planning worker selected gpt-6.1-sol. Codex returned: The 'gpt-6.1-sol' model is not supported when using Codex with a ChatGPT account. Failed phase invocation: phr-0e645c77-1aed-49d0-aa34-cd34546aa5e0. Use an available agent and model. Do not select gpt-6.1-sol.

Finish with links to the replacement artifacts and any unresolved design decisions. Do not start implementation.

Supplied requirements are authoritative and need no tracker lookup. Locally allocated issue keys do not require a tracker connection. Only an explicit unresolved tracker reference without requirements needs lookup through its connected tracker before planning. Use the returned requirements, not the URL title. If that lookup fails, block with the returned reason before implementation; never infer or substitute requirements.

## Acceptance Criteria

1. R4K-1 Replace author-bio and disclaimer cleanup with an injectable scored extension Work in /home/sermilion/IdeaProjects/readability4k. We want to fully replace the R4K-1 plan, then stop for review. This request authorizes planning only. Do not implement, commit, push, open a PR, publish a release, or update Readian. ### Problem Readian reading mode retains author bios and ancillary disclaimer blocks in extracted article HTML and text. Examples include Notebookcheck sidebar bios and TweakTown “About the author” sections. Normal Readability cleanup misses them because long bios evade short-byline detection, some author/sidebar classes survive unlikely-candidate removal, and dense containers survive conditional cleaning. The current implementation adds AuthorBioDisclaimer.strip at the end of prepArticle. It removes blocks through class/id matches, English heading matches, semantic author attributes, and preceding-text length comparisons. It does not use content scoring. It also introduces author-specific exceptions in cleanHeaders and attribute copying during div unwrapping. We want to replace that design. ### Mandatory architecture - Focused, separate classes must own author-bio/disclaimer detection, scoring, and removal. - The behavior must be explicitly injectable through a generic optional extension contract. - An empty default must run ordinary Readability without this cleanup. - The main extraction algorithm must not reference, instantiate, or require the concrete author cleanup. - Remove author-specific hooks from the grabber, including heading exemptions and matching class/id copying. - Choose the smallest generic lifecycle that works. Investigate capturing source evidence before normalization and applying cleanup after article selection. Avoid building a broad plugin framework. - Reuse generic Readability evidence through a read-only boundary. Do not expose mutable grabber state or require subclassing. - Preserve KMP support and existing public API compatibility where practical. Consider constructor compatibility and synchronous/asynchronous parsing. - Keep evidence and cleanup state local to each parse and extraction attempt. ### Scoring requirements Replace unconditional deletion with an explainable score that weighs ancillary evidence against body-preservation evidence. Potential signals include class/id weights, exact section labels, rel=author, schema Person metadata, link density, position, structural separation, and relative paragraph support. Important constraints: - Long, dense bios must remain removable even when normal paragraph scoring rewards their prose. - A class name, heading, negative weight, high link density, or Person attribute alone must not trigger deletion. - Preserve ambiguous cases, main-body wrappers named author-bio, profile articles, legitimate author-related sections, substantive disclaimers, and link-heavy body sections. - Calibrate bios and disclaimers separately. - Define candidate boundaries, feature calculations, decision formula, initial weights, thresholds, and calibration strategy. Identify provisional numerical choices. - Evaluate a stable snapshot before deleting nodes and resolve overlapping candidates deterministically. - Never remove the article root or consume the next equal-or-higher heading section. Existing contentScore cannot simply be reused unchanged. Candidate selection overwrites it with a link-adjusted value, it mixes several signals, and some elements have no stored score. Avoid double-counting class weight or link adjustment. Account for DOM replacements, unwrapping, retries, and missing scores. ### Verification Read AGENTS.md and docs/architecture.md first. Relevant files: - library/src/commonMain/kotlin/com/sermilion/readability4k/processor/AuthorBioDisclaimer.kt - library/src/commonMain/kotlin/com/sermilion/readability4k/processor/ReadabilityArticleGrabber.kt - library/src/commonTest/kotlin/com/sermilion/readability4k/AuthorBioDisclaimerRemovalTest.kt - library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusRegressionTest.kt - library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusIndex.kt - library/src/jvmTest/resources/author-bio-corpus/ The offline corpus contains about 100 real pages across 31 hosts. Reuse it, but strengthen body-retention annotations beyond one phrase. Some current retention phrases appear to include widgets or subscription text and need review. Seed fixtures, for verification only: - https://www.tweaktown.com/rss/click/news/113920/?c=news-mf - https://www.notebookcheck.net/ChatGPT-ads-German-regulators-warn-about-profiles-built-from-chats.1416940.0.html The planning investigation found that TweakTown’s “About the author” label is a paragraph, so discovery must handle labels as well as heading tags. Plan meaningful tests for explicit injection, empty injection, long bios, short articles, misleading classes, substantive disclaimers, person-profile articles, heading boundaries, nested candidates, lost semantic attributes during normalization, retries with class weighting disabled, state isolation, and sync/async equivalence. Split calibration and validation by publisher. Measure wrongly removed body content separately from missed ancillary blocks. Prioritize body preservation. No runtime host checks, publisher-specific selectors, Readian dependency bumps, releases, or broad changes to top-candidate selection. ### State The following command succeeded: skill-bill goal purge R4K-1 --confirm-issue-key R4K-1 --repo-root /home/sermilion/IdeaProjects/readability4k Purge retained the old spec bundle. The first planning attempt refused to overwrite it, so it was archived at: /tmp/readability4k-R4K-1-superseded.8hyFXK/R4K-1-strip-about-author-and-similar-disclaimer-blocks/ The archived spec is historical reference only. The existing implementation and corpus remain in the repository. Purging workflow state did not remove code. There is no readable R4K-1 spec in the working tree. Create a fresh R4K-1 spec bundle and actionable implementation plan. Do not restore the archived spec as the plan. The second planning attempt failed before authoring the replacement plan because its planning worker selected gpt-6.1-sol. Codex returned: The 'gpt-6.1-sol' model is not supported when using Codex with a ChatGPT account. Failed phase invocation: phr-0e645c77-1aed-49d0-aa34-cd34546aa5e0. Use an available agent and model. Do not select gpt-6.1-sol. Finish with links to the replacement artifacts and any unresolved design decisions. Do not start implementation.

## Non-Goals

- None

## Dependency Notes

Depends on: none
The full goal owns planning and execution of the supplied requirements.

## Validation Strategy

Run the repository's required checks and verify every supplied acceptance criterion.

## Next Path

Complete the goal and prepare its pull request.

## Spec Path

.feature-specs/R4K-1-replace-author-bio-and-disclaimer-cleanup-with-an-injectable-scored-extension/spec_subtask_1_implement-the-requested-change.md

## Implementation Details

Settled from the preplan digest. Implement this design. Do not reopen the digest, and do not restore the archived spec under `/tmp/readability4k-R4K-1-superseded.8hyFXK/`.

Default `Readability4K(uri, html).parse()` and `parseAsync()` stay ordinary Readability: author bios and disclaimer blocks remain. Callers who want removal pass `AuthorBioDisclaimerExtension`. The grabber and the facade never name that class. `ReadabilityOptions`, `ArticleGrabberOptions`, and `CandidateFilter` stay unchanged. One optional extension slot, not a list and not a plugin framework.

Provisional numbers are frozen as the starting calibration. Formula shape stays: two tracks, threshold 4, 45 percent fraction veto, disclaimer vetoes at 10 commas and more than 600 characters, link density and descendant `a[rel=author]` recorded and left out of both totals. Weight changes, if any, happen only after the host split below is frozen, and only by editing the weight constants.

### Settled decisions

- Facade parameter order. `contentExtension` is the last parameter on `ReadabilityArticleGrabber`. On both long `Readability4K` constructors it is the parameter immediately before `articleGrabber`, default `null`, and the default `ReadabilityArticleGrabber(...)` expression forwards it. It cannot be last on the facade: a Kotlin default expression can only read parameters declared before it. Short constructors `Readability4K(uri, html)` and `Readability4K(uri, document)` stay source-compatible and binary-compatible. Do not add `@JvmOverloads`. A caller who passes `articleGrabber` has already chosen that grabber's extension; the facade does not wrap it. Assumption: in-repo call sites use the short constructors or named arguments. If a positional long-constructor call fails to compile because `articleGrabber` shifted, change that call to named arguments. Confirm during implementation.
- JVM note. The long constructors' JVM signatures change. Java callers of those full constructors need a recompile. The two-argument constructors do not. No Java source in this change.
- Snapshot type. `ArticleExtensionSnapshot` is an empty public marker interface. The grabber stores the capture result only as a local of that type. The author snapshot class stays internal to the extension implementation.
- Explanation record. `AuthorBioDecision` is a public data class. `AuthorBioDisclaimerExtension` exposes a pure function that returns decisions for one tree. `apply` uses that same function, then deletes. The extension instance has no mutable fields, so two parses can share one instance. Decisions are not stored on the instance.
- Labels. Keep the four English strings already in the strip: `about the author`, `about author`, `author bio`, `disclaimer`. Non-English labels stay unimplemented. A sentence that only contains a phrase is not a candidate.
- Person JSON-LD. The Person signal is the `itemtype` attribute. Do not read JSON-LD. Preprocessing already removes `script`.
- "Alone" means one point source. Preceding text is a second source. A trailing Person-only box with a longer body ahead scores 4 and is removed. A Person box that is most of the article, or is the article root, is kept. Do not treat Person plus position as a single signal.
- Mixed tracks. Score both tracks on one candidate. Remove once if either track says remove and the hard stops allow it. The disclaimer comma and length vetoes apply only to the disclaimer track. A bio score of at least 4 still removes the node when the disclaimer track vetoes.
- Host split, 0-based. Sort the corpus hosts lexicographically. Validation hosts are `notebookcheck.net`, `tweaktown.com`, and every host whose index in that sorted list is divisible by 3. Calibration hosts are the rest. The split lives in the JVM test source only. `commonMain` stays free of host names.
- Corpus edits. No generator exists. Edit `manifest.json` and `AuthorBioCorpusIndex.kt` in the same change. `must_keep` becomes a list of body sentences on both. Drop widget, gallery, subscription, and "Popular Now" phrases, including `Popular Now: GameStop lists used PS5 Pro for $500 more than a new console`, `VIEW GALLERY - 6 IMAGES`, `Prices vary by article type from$1.95 to$39.95 Learn more`, the Verge sentence ending in `Android Authority`, and `These $4.99 per month`.
- Expect-absent annotations. Pages the formula intentionally keeps (single signal, fraction veto, disclaimer veto, article root) lose that `expect_absent` phrase. Record why on the existing `notes` field. Do not delete a body phrase to force an absent-phrase assertion green. Seed pages `tweaktown.com` / `113920` and `notebookcheck.net` / `1416940` stay absent-assertions: the provisional formula already clears them (TweakTown bare token 1 + label 2 + preceding 2; Notebookcheck `rel` 2 + Person 2 + compound `journalist` 3).
- Calibration policy. Freeze the host split before changing a weight. Change weights only. Missed ancillary blocks are acceptable when fixing them would drop a body phrase. A lost body phrase is a failed calibration on either set. Body preservation wins. Validation hosts other than the two seeds are reported and are not tuning targets.
- Index path assumption. `Readability4K` lives at `library/src/commonMain/kotlin/com/sermilion/readability4k/Readability4K.kt`. Confirm the path when editing. `CORPUS.md` is updated in the same change only if it describes `must_keep` as a single string.

### Task 1 — Generic contract

Serves AC-1 clauses: injectable optional extension, empty default, main algorithm does not reference the concrete cleanup, smallest lifecycle, read-only evidence, state local to each attempt.

Paths and symbols:

- Add `library/src/commonMain/kotlin/com/sermilion/readability4k/processor/ArticleContentExtension.kt`.
- Public types, `commonMain`, no expect/actual:

```kotlin
interface ArticleExtensionSnapshot

interface ArticleContentExtension {
  fun capture(root: Element): ArticleExtensionSnapshot
  fun apply(
    articleContent: Element,
    snapshot: ArticleExtensionSnapshot,
    evidence: ArticleEvidence,
  )
}

interface ArticleEvidence {
  fun contentScoreBeforeLinkAdjustment(element: Element): Double?
  fun linkDensity(element: Element): Double
  val weightClasses: Boolean
}
```

Constraints: one slot. `CandidateFilter` is not this slot. The extension is not a field of `ReadabilityOptions` or `ArticleGrabberOptions`. Null means `capture` and `apply` are not called.

test_obligations: none. Wiring is covered by the parse-boundary tests in task 5.

### Task 2 — Grabber lifecycle, then delete the author hooks

Serves AC-1 clauses: remove heading exemptions and class/id copying, capture before normalization, apply after article selection, read-only evidence, retries and missing scores, no concrete author type in the grabber.

Paths and symbols: `library/src/commonMain/kotlin/com/sermilion/readability4k/processor/ReadabilityArticleGrabber.kt`.

- Add `contentExtension: ArticleContentExtension? = null` as the last constructor parameter, after `candidateFilters`.
- Inside each `tryExtractArticle`, after `page.html(pageCacheHtml)` and before `prepareNodes`, call `capture` when the extension is non-null. Store the snapshot in a local. A retry rebuilds the DOM from `pageCacheHtml`, so a snapshot from an earlier attempt is dead. Capture inside the attempt.
- After `scoreElements` and before `getTopCandidate`, copy `ReadabilityObject.contentScore` for elements that have one. `getTopCandidate` overwrites included candidates with `contentScore * (1 - linkDensity)`. The copy is the pre-adjustment value. Elements with no `ReadabilityObject` stay absent. Skip the copy when the extension is null.
- Build a private evidence object in this file. `contentScoreBeforeLinkAdjustment` reads the copy and returns null when absent. It does not coerce null to 0 and does not add class weight or link density. `linkDensity` delegates to the existing `getLinkDensity` (link inner-text length over element inner-text length, hash `href` counted at 0.3). `weightClasses` is the flag for this attempt. Do not pass the grabber instance out.
- After `prepArticle` returns and before the `readability-page-1` wrap, call `apply`. Cleanup must finish before `grabArticle` returns, because `postProcessContent` then drops classes when `keepClasses` is false.
- Delete `AuthorBioDisclaimer.transferMatchingClassAndId` at the `div`-to-`p` unwrap (the call immediately before `replaceWith`). Delete the `cleanHeaders` exemption `AuthorBioDisclaimer.isAuthorBioOrDisclaimerHeading`. Delete `stripAuthorBioAndDisclaimerBlocks` and its call at the end of `prepArticle`. After that, this class has no reference to the author implementation.
- Leave `CandidateFilter` in place. Do not change `resetState`, `generateOptionsSequence`, top-candidate selection, or unlikely-candidate patterns. The extension is configuration, not parse state, and is not cleared in `resetState`.
- `cleanHeaders` can still drop an `h1` or `h2` whose class weight is negative before `apply`. A bare heading with weight 0 is already kept. The exemption being removed is only the negative-class case. The snapshot's pre-`prepareNodes` sibling references are how removal still finds that span. Nodes with a null parent, or nodes no longer inside `articleContent`, are skipped.
- Do not add grabber callbacks (`getNextNode`, `removeAndGetNext`, `removeNodes`, `printAndRemove`) to the extension. No test subclasses those hooks.
- Keep the existing `@Suppress("TooManyFunctions")` unless removing the hooks makes the suppress unused. Do not add a new suppress. The old ten-function cap on `AuthorBioDisclaimer` is not a constraint on the new types.

test_obligations: none on the grabber itself. Task 5 covers null extension, injected extension, the weight-classes retry, and the heading `cleanHeaders` drops.

### Task 3 — Facade forwarding

Serves AC-1 clauses: explicit injection, empty default, constructor compatibility, sync and async parsing.

Paths and symbols: `Readability4K` (assumed path in the decisions above), both long constructors.

- Insert `contentExtension: ArticleContentExtension? = null` immediately before `articleGrabber`.
- Default grabber: `ReadabilityArticleGrabber(options, regExUtil, logger, contentExtension = contentExtension)`.
- `parse()` and `parseAsync()` stay as they are. `parseAsync()` remains `withContext(Dispatchers.IO) { parse() }`. HTML-string construction keeps reparsing `sourceHtml` on each `parse()`. The `Document` constructor keeps reusing the document. The grabber instance is reused across parses; the extension is stateless, so that reuse is safe.
- Do not put the extension on `ReadabilityOptions`.

test_obligations: none beyond task 5's default parse, named injection, and `parse` / `parseAsync` pair.

### Task 4 — Finder, scorer, remover, extension; delete the old strip

Serves AC-1 clauses: separate classes for detection, scoring, and removal; explainable score; long bios still removable; no single-signal deletion; separate bio and disclaimer calibration; stable snapshot before deletion; deterministic overlap; never the article root; never the next equal-or-higher heading; paragraph labels; lost attributes; provisional weights.

Delete `library/src/commonMain/kotlin/com/sermilion/readability4k/processor/AuthorBioDisclaimer.kt`.

Add, all in `library/src/commonMain/kotlin/com/sermilion/readability4k/processor/`, public only where a caller or a test needs the type:

- `AuthorBioDisclaimerExtension` (public, the only type tests construct; implements `ArticleContentExtension`).
- `AuthorBioCandidateFinder`, `AuthorBioScorer`, `AuthorBioRemover` (separate classes; internal unless a public signature needs them).
- Internal snapshot holding, by element identity, class, id, `rel`, and `itemtype` copied before `prepareNodes`. For a `div` whose only element child is a `p`, also store the parent attributes under that child, because `replaceWith` drops the parent. Mirror the grabber's unwrap condition, including whatever it does with whitespace text nodes. Confirm that condition in `prepareNodes` while implementing.
- Also store pre-`prepareNodes` element references for each label span (the label node plus following siblings up to, but not including, the stop). `setNodeTag` keeps the same element. `replaceWith` detaches the parent and the child object survives. If a node was replaced rather than retagged, use snapshot attributes on the surviving child and skip null-parent nodes.
- Public `AuthorBioDecision`: track, each awarded feature and its points, total, veto name or null, removed flag, link density, whether a descendant `a[rel=author]` exists, and `contentScoreBeforeLinkAdjustment` (`Double?`). The last three are explanation only.

Effective attributes at apply time: use the live attribute when it is non-blank, otherwise the snapshot. The grabber does not copy attributes.

Candidate boundaries, found from the live tree plus the snapshot:

- A `div`, `aside`, `section`, `footer`, or `article` with a class, id, `rel`, or `itemtype` signal. The candidate is that subtree. Do not take following siblings outside it.
- An `h1`–`h6` whose full text is an exact label. The span is that heading plus following siblings until the next element whose heading rank is in `1..thisRank`. Do not include the stopping heading. Text nodes in the span go too.
- A `p` whose full text is an exact label. TweakTown's label is `<p class="tt-author-cards__label">About the author</p>`. The span runs until the next `h1`–`h6`, not including it.
- When a label's block ancestor also has an author class, id, `rel`, or `itemtype` signal, the candidate is that ancestor. The label's points are added once. The inner label is not a second removal.
- Exact text: trim one trailing colon (`\s*:\s*$`), lowercase, and require equality with the four strings. Case-insensitive matchers otherwise.

Class and id matchers, case-insensitive, same as the strip being deleted:

- Compound: `author-bio|authorbio|author-box|authorbox|author-info|author-profile|author-description|author-details|about-author|about-the-author|aboutauthor|writer-bio|bio-box|journalist`.
- Bare token, and only when the compound regex did not match: `\b(?:author|disclaimer)\b`. A hyphen is a boundary, so `tt-author-cards` matches `author`.
- `rel`: `(?:^|\s)author(?:\s|$)` on the candidate, and only when the tag is in the block set above.
- `itemtype` containing `Person` on the candidate.

The extension's class and id rules ignore `evidence.weightClasses`, so the `weightClasses = false` retry still removes a bio.

Bio points: exact bio label (`about the author`, `about author`, `author bio`) on the candidate or its label child, 2; compound class or id, 3; bare `author` token, 1; `rel` author, 2; `itemtype` Person, 2; preceding text longer than the candidate, 2. Remove when the sum is at least 4 and the hard stops are clear. Compound alone is 3. A label, a Person attribute, or a `rel` alone is 2. A bare class alone is 1.

Disclaimer points, separate track, same hard stops: exact label `disclaimer`, 2; class or id token `disclaimer`, 2; preceding text longer than the candidate, 2. Remove when the sum is at least 4, unless the candidate has at least 10 commas or more than 600 characters. The comma figure matches `cleanConditionally` (`getCharCount(node, ',') >= 10`). Bios do not get the comma or 600-character keep. A long bio after a longer body still clears on label 2 plus preceding 2.

Preceding length is measured on the live `articleContent` at apply time. Reimplement the private-use marker technique inside the new scorer (insert marker, read `root.text()`, remove marker). Do not call the deleted object.

`contentScoreBeforeLinkAdjustment` is copied onto the decision and is not a keep vote. Adding it would protect the long bios this score is supposed to remove. Link density and descendant `a[rel=author]` are copied onto the decision and omitted from both totals. A byline link cannot finish a weak class match.

Hard stops, applied before deletion: never remove `articleContent`; never include the stopping heading; if the candidate text length is greater than 45 percent of `articleContent` text length, keep it. Compare `text().length` on the candidate span and on `articleContent`. This holds profile articles, main-body wrappers named `author-bio`, and short pages whose bio is most of the text.

Stable decision order: collect every qualifying candidate, score them all, then delete. Sort by document order of the start node, then by ascending depth for ties. Skip a candidate that is detached, is `articleContent`, or sits inside a candidate already chosen. Delete the chosen set only after that selection. Deleting a parent covers its descendants; do not remove the inner node again.

Put the numbers in one internal constants holder (`THRESHOLD = 4`, `FRACTION = 0.45`, `DISCLAIMER_COMMA_KEEP = 10`, `DISCLAIMER_LENGTH_KEEP = 600`, and the point values). Calibration edits that holder only.

Veto names to record when a candidate is kept: `article_root`, `fraction`, `disclaimer_commas`, `disclaimer_length`, `below_threshold`, `nested`, `detached`.

test_obligations: none here. Observable checks are in task 5. Do not add a unit test per point value.

### Task 5 — Common tests

Serves AC-1 verification list: explicit injection, empty injection, long bios, short articles, misleading classes, substantive disclaimers, person-profile articles, heading boundaries, nested candidates, lost semantic attributes, retries with class weighting disabled, state isolation, sync/async equivalence, paragraph labels, and single-signal preservation.

Path: `library/src/commonTest/kotlin/com/sermilion/readability4k/AuthorBioDisclaimerRemovalTest.kt`. Keep it a Kotest `FunSpec`. Use `https://example.com` as the page URL. No host names.

Rewrite the file. The current cases assume the default parse strips. Point removal cases at `contentExtension = AuthorBioDisclaimerExtension()`. Rewrite any case that currently passes on one signal so it passes only when a second signal is present (compound alone is 3 and must stay).

test_obligations, one test each, asserting `articleContent.text()` and content HTML:

1. Default `Readability4K(uri, html).parse()` keeps a bio that the extension would remove. Bug: the grabber still calls the old strip when the extension is null. Explicit null is the same default; do not add a second test for it.
2. Injected extension removes a TweakTown-shaped paragraph label plus following bio when the body ahead is longer (bare token + label + preceding). The next section stays. Bug: discovery only sees `h1`–`h6`, so the paragraph label survives.
3. Long dense bio, more than 600 characters and at least 10 commas, with an exact bio label and a longer preceding body, is removed. Bug: the disclaimer length veto, or `contentScore` as a keep vote, protects the bio.
4. Bio with label and compound class whose text is more than 45 percent of the article is kept. Bug: the fraction veto is missing and the short page loses most of its text.
5. The article root (or a wrapper that is most of the text) has class `author-bio`, and a separate small multi-signal bio inside it is removed while the body stays. Bug: the root is deleted, or a class on the main wrapper deletes the article.
6. A short disclaimer with an exact label and a longer preceding body, under 10 commas and at most 600 characters, is removed, and the next `h2` stays. Bug: the disclaimer track never reaches removal.
7. A disclaimer with an exact label and a class token, at least 10 commas or more than 600 characters, is kept. Bug: the comma and length vetoes are missing, so a substantive disclaimer is deleted.
8. A person-profile block with `rel=author`, `itemtype` containing `Person`, and class `journalist`, whose text is more than 45 percent of the article, is kept. Bug: a high bio score ignores the fraction veto.
9. The same Notebookcheck markup as a small trailing block, with a longer body ahead, is removed. Bug: `rel`, Person, and compound class together stay under the threshold. This is the removal branch of test 8, not a second literal for the same size.
10. An `h2` exact bio label's span stops before the next `h2`, which remains. Bug: the span consumes the next equal-or-higher heading.
11. An outer card and an inner card both qualify; the outer node is removed once and a paragraph after the outer card remains. Bug: the inner candidate is a second removal and takes following siblings with it.
12. A `div` whose only element child is a `p`, with compound class and `itemtype` Person on the div and ordinary prose on the `p`, preceded by a longer body, is removed after unwrap. Bug: snapshot attributes are ignored once `replaceWith` drops the parent, so the live `p` scores 0.
13. An `h2` whose text is an exact bio label and whose class weight is negative is removed by `cleanHeaders` under `weightClasses = true`, and the following bio is still removed when the body ahead is longer. Bug: span discovery only sees headings that survived `prepArticle`.
14. A page whose text, including the bio, is under the default 500-character threshold, parsed with the extension, drops the bio. The sequence tries `weightClasses = false` because no attempt reaches `charThreshold`. An attempt that skips removal is longer and would win. Bug: class rules consult `weightClasses`, or capture does not run inside the winning retry. Do not add a separate direct `grabArticle` test for the same flag.
15. One extension instance parses a removable bio, then a second document whose only signal is a compound class. The second document keeps that block. Bug: mutable state on the instance leaks a removal into the next parse.
16. `parse()` and `parseAsync()` return the same text for one injected fixture. Bug: one entry point skips `apply`. Assumption: `commonTest` can run the suspend function with the coroutines runner already on the test classpath. If it cannot, put this single test in `jvmTest` and say so.
17. A body sentence that merely contains "about the author" stays. Bug: exact-label matching becomes a substring match.
18. A trailing block whose only bio points are an exact label (2) plus a descendant `a[rel=author]` and high link density, and whose preceding text is not longer, stays. Bug: descendant `rel` or link density is added into the total and crosses 4.

No mock-interaction tests, no call-order assertions, and no tests that re-check the same branch with a different label string. `about author` and `author bio` share the exact-label branch with `about the author`; one label is enough.

### Task 6 — Corpus annotations, host split, regression test

Serves AC-1 clauses: reuse the offline corpus, strengthen body-retention annotations, seed fixtures, split calibration and validation by publisher, count wrongly removed body content separately from missed ancillary blocks, prioritize body preservation.

Paths:

- `library/src/jvmTest/resources/author-bio-corpus/manifest.json`
- `library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusIndex.kt`
- `library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusRegressionTest.kt`
- `library/src/jvmTest/resources/author-bio-corpus/CORPUS.md` only if it documents `must_keep` as one string

`AuthorBioCorpusArticle.mustKeep` becomes `List<String>`. JSON `must_keep` becomes an array. Keep dropping `has_author_box_signal` and `notes` from the Kotlin index; `notes` still exist in JSON for the annotation reasons in the decisions above. `COUNT` stays 100. Do not add or remove articles. CI must not fetch the network.

Each article gets body sentences from its slimmed HTML. Use at least two phrases when that HTML has two distinct body sentences; otherwise one, and note the shortfall in `notes`. Replace the known non-body phrases listed in the decisions. Assumption: this plan did not open the corpus HTML. Implement reads it while editing the annotations.

The regression test parses with `contentExtension = AuthorBioDisclaimerExtension()`, not with a bare `Readability4K(entry.finalUrl, html)`. Keep the structural asserts: `AuthorBioCorpusIndex.COUNT == ARTICLES.size`, size greater than 90, more than 20 hosts, more than 15 articles with a non-empty `expectAbsent`, extracted text length at least 120.

Failure reporting groups by `host` and prints two counts: missing body phrases, then `expectAbsent` hits. The test hard-fails on any missing body phrase, on any calibration-host `expectAbsent` hit, and on `expectAbsent` hits for `notebookcheck.net` and `tweaktown.com`. Other validation-host `expectAbsent` hits are printed and do not fail the test. Do not weaken the default-parse contract by dropping the extension on the corpus to make it pass. The default-off check is test 1 in task 5.

Do not add a separate seed test. The corpus loop plus the two hard-fail hosts cover `html/tweaktown-com-67bb559392.html` and `html/notebookcheck-net-828e157eb8.html`. A third test with the same assertions would re-cover that branch.

test_obligations:

- The updated regression test itself. Bug: body text is deleted and only an absent-phrase assertion would have passed, or a missed seed bio is invisible because validation hosts were aggregated away.
- No new test file. No commonMain test that names a publisher.

Weight edits, if corpus body phrases fail, are part of this task's end state and may change only the constants from task 4. Do not retune against validation hosts. Full `./gradlew check`, `:library:jvmTest`, and `:library:allTests` belong to the validate phase. This plan does not run them.

### Task 7 — Public docs

Serves AC-1 clause: preserve public API compatibility where practical, and name the new type once the constructor grows.

Paths: `README.md`, `docs/architecture.md`.

Name `ArticleContentExtension` and `AuthorBioDisclaimerExtension` next to `Readability4K`. Keep the two-argument example valid. Add one example that passes the extension with a named argument. Note that the long constructors gain a defaulted parameter before `articleGrabber`, that the two-argument constructors are unchanged, and that there is no `@JvmOverloads`. Do not change CLI or `iosApp` behavior. Do not bump Readian, publish, or add publisher selectors.

test_obligations: none.

### Constraints for every task

- Library module only, package `com.sermilion.readability4k`, new types under `processor`.
- KMP: pure Kotlin in `commonMain`. No expect/actual. No host names and no publisher selectors in `commonMain`.
- Do not change top-candidate selection, unlikely-candidate patterns, `ReadabilityOptions`, or `ArticleGrabberOptions`.
- Do not expose mutable grabber state and do not require subclassing.
- Evidence and cleanup locals die with the attempt. The shared extension instance stays stateless.
- No runtime host checks, no Readian dependency bump, no release, no install or uninstall commands.
- Validate owns `./gradlew check` and the focused library test tasks. Implement does not treat a local check run as the phase gate.
- Fixture output outside the author tests can change where a page contained a token the old default strip removed. That shows up in the validate-phase check run. Do not preemptively edit unrelated fixtures in this plan. If validate reports one, fix the assertion to the new default-off behavior only when the page was relying on the branch's always-on strip.

### Out of this plan

Parent spec, sibling specs, decomposition manifest, CLI behavior, `iosApp`, Readian, releases, and the archived bundle. Commit, push, and pull request stay with their phases.
