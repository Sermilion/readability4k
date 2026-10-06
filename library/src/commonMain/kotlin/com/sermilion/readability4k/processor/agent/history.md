## [2026-10-06] Strip about-author and disclaimer blocks
Areas: library processor, ReadabilityArticleGrabber and AuthorBioDisclaimer; library commonTest, AuthorBioDisclaimerRemovalTest
- prepArticle calls one protected strip after share cleanup, on every grab retry, with no new option.
- Compound class and id tokens are removed with no length guard. Bare author and disclaimer tokens, plus the English headings About the author, About author, Author bio, and Disclaimer, are removed only when the text before the block is longer than the block. Later headings of the same or higher rank stay. A body sentence that merely contains the phrase stays.
- Matchers are local and case-insensitive inside an internal processor object. The grabber walks descendants and leaves the article root in place. A matching class or id is copied onto a paragraph that replaces a single-paragraph div, so the later strip can still see it.
- AuthorBioDisclaimer is reusable inside the processor for the strip and that class and id copy. Coverage is one common test with synthetic pages at example.com.
- No public API change and no feature flag. Non-English headings are unimplemented. The two publisher pages were not captured. The pull request is still unopened.
Feature flag: N/A
Acceptance criteria: 2/4 implemented

## 2026-10-06 — Real-article author-bio corpus
- Added `jvmTest/resources/author-bio-corpus/` with 100 slimmed real HTML pages and CORPUS.md.
- Extended strip to treat block-level `rel=author` / schema.org Person boxes (and `journalist` class token) so Notebookcheck sidebar bios are removed.
- Regression: `AuthorBioCorpusRegressionTest` + generated `AuthorBioCorpusIndex`.
