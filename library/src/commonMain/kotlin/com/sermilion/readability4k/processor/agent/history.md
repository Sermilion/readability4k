## [2026-10-06] Read component name attributes as name signals; carousel is negative
Areas: processor package, util/RegExUtil, commonTest
- `SEMANTIC_HOOK_ATTRIBUTES` adds `data-component` and `data-component-name`.
- `NEGATIVE_DEFAULT_PATTERN` adds `carousel`.
- Regression corpus: 206 real pages fetched from the Readian feed plus 20 from the adb run, compared against 0.2.0. Only two pages differ: lifehacker.ru gets back an inline word, and IGN has a spacing change. The 109 jvmTest pages are unchanged against main.
Feature flag: N/A

## [2026-10-06] Keep inline elements inside sentences
Areas: processor package, commonTest
- `isInRunningText` guards unlikely-candidate stripping and `cleanMatchedNodes`. It holds for phrasing content (`PHRASING_ELEMS` plus `PHRASING_CONTAINER_ELEMS`, every descendant phrasing) whose parent has non-blank text.
- `cleanMatchedNodes` now matches `getMatchString`, so the share cleaner sees semantic class tokens, the id, and test hooks.
- On the real Investing.com HTML the article keeps "Former Anthropic researcher" and "his former employer and OpenAI". None of the 109 jvmTest HTML pages changes extraction against main.
Feature flag: N/A

## [2026-10-06] Name signals ignore utility classes and include test hooks
Areas: processor package, util/RegExUtil, commonTest
- One match string feeds every name check in the grabber: byline, unlikely-candidate stripping, cousin candidates, the media-bonus container check, and class weight. It holds the class without utility tokens, the id, and test hook values (`data-test`, `data-testid`, `data-test-id`, `data-qa`, `data-cy`).
- `RegExUtil.UTILITY_CLASS_DEFAULT_PATTERN` (constructor parameter `utilityClassPattern`) names atomic CSS tokens that collide with the scoring vocabulary: variant and arbitrary-value forms, `text-*`, overflow and scroll utilities, and content-alignment utilities. `isUtilityClass` matches a whole token.
- Class weight scores hook names together with the class name, so the per-element range stays -50..+50. Strip and weight flags, the retry sequence, and the 500-character threshold are unchanged.
- Regression: NameSignalTest uses the real NEWS-144 article text in Investing.com-style markup and fails on main. A default parse of the 109 jvmTest HTML pages changes on four pages compared with main: a BBC byline is lifted out of the body, and three IGN pages keep a one-line affiliate notice that main dropped.
Feature flag: N/A
Acceptance criteria: 1/1 implemented

## [2026-10-06] Injectable scored author-bio extension
Areas: processor package, Readability4K, commonTest, jvmTest author-bio corpus, README, docs/architecture.md
- Default `Readability4K(uri, html)` parse leaves author bios and disclaimers in the article. Callers opt in by passing `AuthorBioDisclaimerExtension` on the single `contentExtension` parameter.
- `ArticleContentExtension`, `ArticleExtensionSnapshot`, and `ArticleEvidence` are the generic hook, reusable. The grabber keeps a read-only snapshot local to the attempt.
- Each extract attempt captures before `prepareNodes`, copies `contentScore` before link adjustment, and applies after `prepArticle`, before the readability page wrap.
- Finder, scorer, and remover are separate. `decisions()` explains the tree, then `apply` deletes. `AuthorBioScoring` holds threshold 4, a 45 percent size veto, and disclaimer keeps at 10 commas and 600 characters.
- Class and id rules ignore `weightClasses`. Exact paragraph labels count. Unwrap stores the parent class, id, rel, and itemtype on the surviving child. Link density, a descendant rel=author link, and `contentScore` are recorded and do not score.
- The 100-page corpus stores body sentences in `must_keep`. JVM tests hard-fail a missing body sentence, a calibration-host miss, and the TweakTown and Notebookcheck seeds. Other validation-host misses print only. `commonMain` has no host names.
- Long constructors take `contentExtension` immediately before `articleGrabber`. The two-argument constructors are unchanged. `@JvmOverloads` stays off. Java callers of the long constructors need a recompile.
- Non-English labels are unimplemented. One signal does not remove a block. Validation hosts other than the two seeds are not weight-tuning targets.
Feature flag: N/A
Acceptance criteria: 1/1 implemented
