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
