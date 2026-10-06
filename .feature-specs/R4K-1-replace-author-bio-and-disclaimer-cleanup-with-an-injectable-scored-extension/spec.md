# R4K-1 - replace-author-bio-and-disclaimer-cleanup-with-an-injectable-scored-extension

## Mode

single_spec

## Intended Outcome

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

## Overview

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

## Acceptance Criteria

1. R4K-1 Replace author-bio and disclaimer cleanup with an injectable scored extension Work in /home/sermilion/IdeaProjects/readability4k. We want to fully replace the R4K-1 plan, then stop for review. This request authorizes planning only. Do not implement, commit, push, open a PR, publish a release, or update Readian. ### Problem Readian reading mode retains author bios and ancillary disclaimer blocks in extracted article HTML and text. Examples include Notebookcheck sidebar bios and TweakTown “About the author” sections. Normal Readability cleanup misses them because long bios evade short-byline detection, some author/sidebar classes survive unlikely-candidate removal, and dense containers survive conditional cleaning. The current implementation adds AuthorBioDisclaimer.strip at the end of prepArticle. It removes blocks through class/id matches, English heading matches, semantic author attributes, and preceding-text length comparisons. It does not use content scoring. It also introduces author-specific exceptions in cleanHeaders and attribute copying during div unwrapping. We want to replace that design. ### Mandatory architecture - Focused, separate classes must own author-bio/disclaimer detection, scoring, and removal. - The behavior must be explicitly injectable through a generic optional extension contract. - An empty default must run ordinary Readability without this cleanup. - The main extraction algorithm must not reference, instantiate, or require the concrete author cleanup. - Remove author-specific hooks from the grabber, including heading exemptions and matching class/id copying. - Choose the smallest generic lifecycle that works. Investigate capturing source evidence before normalization and applying cleanup after article selection. Avoid building a broad plugin framework. - Reuse generic Readability evidence through a read-only boundary. Do not expose mutable grabber state or require subclassing. - Preserve KMP support and existing public API compatibility where practical. Consider constructor compatibility and synchronous/asynchronous parsing. - Keep evidence and cleanup state local to each parse and extraction attempt. ### Scoring requirements Replace unconditional deletion with an explainable score that weighs ancillary evidence against body-preservation evidence. Potential signals include class/id weights, exact section labels, rel=author, schema Person metadata, link density, position, structural separation, and relative paragraph support. Important constraints: - Long, dense bios must remain removable even when normal paragraph scoring rewards their prose. - A class name, heading, negative weight, high link density, or Person attribute alone must not trigger deletion. - Preserve ambiguous cases, main-body wrappers named author-bio, profile articles, legitimate author-related sections, substantive disclaimers, and link-heavy body sections. - Calibrate bios and disclaimers separately. - Define candidate boundaries, feature calculations, decision formula, initial weights, thresholds, and calibration strategy. Identify provisional numerical choices. - Evaluate a stable snapshot before deleting nodes and resolve overlapping candidates deterministically. - Never remove the article root or consume the next equal-or-higher heading section. Existing contentScore cannot simply be reused unchanged. Candidate selection overwrites it with a link-adjusted value, it mixes several signals, and some elements have no stored score. Avoid double-counting class weight or link adjustment. Account for DOM replacements, unwrapping, retries, and missing scores. ### Verification Read AGENTS.md and docs/architecture.md first. Relevant files: - library/src/commonMain/kotlin/com/sermilion/readability4k/processor/AuthorBioDisclaimer.kt - library/src/commonMain/kotlin/com/sermilion/readability4k/processor/ReadabilityArticleGrabber.kt - library/src/commonTest/kotlin/com/sermilion/readability4k/AuthorBioDisclaimerRemovalTest.kt - library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusRegressionTest.kt - library/src/jvmTest/kotlin/com/sermilion/readability4k/AuthorBioCorpusIndex.kt - library/src/jvmTest/resources/author-bio-corpus/ The offline corpus contains about 100 real pages across 31 hosts. Reuse it, but strengthen body-retention annotations beyond one phrase. Some current retention phrases appear to include widgets or subscription text and need review. Seed fixtures, for verification only: - https://www.tweaktown.com/rss/click/news/113920/?c=news-mf - https://www.notebookcheck.net/ChatGPT-ads-German-regulators-warn-about-profiles-built-from-chats.1416940.0.html The planning investigation found that TweakTown’s “About the author” label is a paragraph, so discovery must handle labels as well as heading tags. Plan meaningful tests for explicit injection, empty injection, long bios, short articles, misleading classes, substantive disclaimers, person-profile articles, heading boundaries, nested candidates, lost semantic attributes during normalization, retries with class weighting disabled, state isolation, and sync/async equivalence. Split calibration and validation by publisher. Measure wrongly removed body content separately from missed ancillary blocks. Prioritize body preservation. No runtime host checks, publisher-specific selectors, Readian dependency bumps, releases, or broad changes to top-candidate selection. ### State The following command succeeded: skill-bill goal purge R4K-1 --confirm-issue-key R4K-1 --repo-root /home/sermilion/IdeaProjects/readability4k Purge retained the old spec bundle. The first planning attempt refused to overwrite it, so it was archived at: /tmp/readability4k-R4K-1-superseded.8hyFXK/R4K-1-strip-about-author-and-similar-disclaimer-blocks/ The archived spec is historical reference only. The existing implementation and corpus remain in the repository. Purging workflow state did not remove code. There is no readable R4K-1 spec in the working tree. Create a fresh R4K-1 spec bundle and actionable implementation plan. Do not restore the archived spec as the plan. The second planning attempt failed before authoring the replacement plan because its planning worker selected gpt-6.1-sol. Codex returned: The 'gpt-6.1-sol' model is not supported when using Codex with a ChatGPT account. Failed phase invocation: phr-0e645c77-1aed-49d0-aa34-cd34546aa5e0. Use an available agent and model. Do not select gpt-6.1-sol. Finish with links to the replacement artifacts and any unresolved design decisions. Do not start implementation.

## Constraints

- Supplied requirements are authoritative and need no tracker lookup. Locally allocated issue keys do not require a tracker connection. Only an explicit unresolved tracker reference without requirements needs lookup through its connected tracker before planning. Use the returned requirements, not the URL title. If that lookup fails, block with the returned reason before implementation; never infer or substitute requirements.

## Non-Goals

- None

## Validation Strategy

Run the repository's required checks and verify every supplied acceptance criterion.
