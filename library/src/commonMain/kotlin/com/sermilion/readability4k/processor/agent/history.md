## [2026-10-06] Drop data-test footer disclosures from reading mode
Areas: processor package, commonTest
- prepareNodes removes every non-body element whose data-test equals footer, case-insensitive on the whole value, on every grab attempt. It reuses the existing removal walk. Class and id unlikely stripping stays behind stripUnlikelyCandidates. matchString and byline checks are unchanged.
- Class weight still returns zero when weightClasses is false. It still applies one negative penalty on the full class string and one on the full id. The positive bonus ignores whitespace-delimited text- tokens whose remainder is not a positive class, on both class and id. Shared positive and negative patterns are unchanged.
- A class of footer plus a text- utility now keeps the negative penalty, so conditional cleaning can remove that node. That side effect is intended.
- The regression is a synthetic commonTest: a neutral article sibling against a data-test footer classed text-sm text-gray-500, for a long body and a short body. No Investing.com fixture. Author-bio scoring and its tests are unchanged.
- There is no feature flag. The default parse changes for every caller. ReadabilityOptions and ArticleGrabberOptions callers stay source compatible. If every attempt stays under the 500-character threshold, parse still returns the longest non-empty attempt.
- Known limit: partial values such as footer-legal stay in the document. The live Investing.com page was not fetched.
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
