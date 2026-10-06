# R4K-1 Subtask 1 - Implement the requested change

Parent spec: [.feature-specs/R4K-1-strip-about-author-and-similar-disclaimer-blocks/spec.md](./spec.md)
Issue key: R4K-1

## Scope

R4K-1 Strip About-author and similar disclaimer blocks

Requirements:
- Update readability4k so extracted article HTML/text no longer includes trailing “About the author” / author bio / similar disclaimer sections that currently survive into Readian News reading mode.
- Solution MUST be site-agnostic (any publisher). Do not hardcode tweaktown.com, notebookcheck.net, or publisher-specific selectors.
- Fixtures for verification only:
  - https://www.tweaktown.com/rss/click/news/113920/?c=news-mf (resolve redirects)
  - https://www.notebookcheck.net/ChatGPT-ads-German-regulators-warn-about-profiles-built-from-chats.1416940.0.html — leftover bio text includes: “I write about IT security and artificial intelligence. Cybersecurity is one of my regular topics, among others for Golem.de. At Notebookcheck, my focus is on practical AI for everyday users…” (sidebar author box pattern).
- Prefer general heuristics: headings like About the author / About author / author bio; common author-box class/id patterns; trailing bio/disclaimer blocks — without removing legitimate article body.
- Read AGENTS.md and docs/architecture.md before parser changes. Add fixture-backed tests. Run library tests.
- Open a PR on Sermilion/readability4k. Do NOT publish/release or bump readian-android.

Acceptance:
- Those fixtures’ extracted content no longer contain the author-about / bio blocks.
- Heuristic works beyond those two sites.
- Tests cover the patterns.
- PR opened.

Non-goals: Readian app dependency bump; Maven release; site-specific scrapers.

Supplied requirements are authoritative and need no tracker lookup. Locally allocated issue keys do not require a tracker connection. Only an explicit unresolved tracker reference without requirements needs lookup through its connected tracker before planning. Use the returned requirements, not the URL title. If that lookup fails, block with the returned reason before implementation; never infer or substitute requirements.

## Acceptance Criteria

1. R4K-1 Strip About-author and similar disclaimer blocks Requirements: - Update readability4k so extracted article HTML/text no longer includes trailing “About the author” / author bio / similar disclaimer sections that currently survive into Readian News reading mode. - Solution MUST be site-agnostic (any publisher). Do not hardcode tweaktown.com, notebookcheck.net, or publisher-specific selectors. - Fixtures for verification only: - https://www.tweaktown.com/rss/click/news/113920/?c=news-mf (resolve redirects) - https://www.notebookcheck.net/ChatGPT-ads-German-regulators-warn-about-profiles-built-from-chats.1416940.0.html — leftover bio text includes: “I write about IT security and artificial intelligence. Cybersecurity is one of my regular topics, among others for Golem.de. At Notebookcheck, my focus is on practical AI for everyday users…” (sidebar author box pattern). - Prefer general heuristics: headings like About the author / About author / author bio; common author-box class/id patterns; trailing bio/disclaimer blocks — without removing legitimate article body. - Read AGENTS.md and docs/architecture.md before parser changes. Add fixture-backed tests. Run library tests. - Open a PR on Sermilion/readability4k. Do NOT publish/release or bump readian-android. Acceptance: - Those fixtures’ extracted content no longer contain the author-about / bio blocks. - Heuristic works beyond those two sites. - Tests cover the patterns. - PR opened. Non-goals: Readian app dependency bump; Maven release; site-specific scrapers.

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

.feature-specs/R4K-1-strip-about-author-and-similar-disclaimer-blocks/spec_subtask_1_implement-the-requested-change.md

## Implementation Details

Strip trailing author-bio and disclaimer blocks inside `ReadabilityArticleGrabber.prepArticle`, after the selected article element exists and while class and id attributes are still present. `parse()` calls `grabArticle` and only then `postProcessContent`. `removeMetadataTextElements` drops short span/div metadata under 100 characters and will not see these bios. `postProcessContent` strips classes when `keepClasses` is false, so the class and id check has to run in the grabber. `grabArticle` measures text length only after `prepArticle`, and every retry calls `prepArticle`, including attempts that set `stripUnlikelyCandidates`, `weightClasses`, or `cleanConditionally` to false. The strip stays on for all of those attempts. There is no new option and no public flag.

Leave `Readability4K`, `ReadabilityOptions`, `Article`, `ArticleGrabberOptions`, `RegExUtil`'s constructor parameter list, the CLI, and publishing untouched. Leave `UNLIKELY_CANDIDATES_DEFAULT_PATTERN`, `OK_MAYBE_ITS_A_CANDIDATE_DEFAULT_PATTERN`, `NEGATIVE_DEFAULT_PATTERN`, `checkByline`, and `isValidByline` unchanged. A long author box already matches `author` in `BYLINE_DEFAULT_PATTERN` and stays in the tree because `isValidByline` requires trimmed text under 100 characters. `sidebar` is cancelled when the same class or id also contains `author`, because `and` in the ok-maybe pattern matches inside `author`. `cleanConditionally` keeps a div once it has 10 or more commas. Those are why this cleanup is a separate pass.

Assumption for the file path: the class lives at `library/src/commonMain/kotlin/com/sermilion/readability4k/ReadabilityArticleGrabber.kt`, same package as `Readability4K.kt`. Implement confirms that path from the `open class ReadabilityArticleGrabber` declaration and edits that file if it sits elsewhere. Preplan already read `AGENTS.md` and `docs/architecture.md`. Implement follows this plan and does not rediscover the pipeline.

Settled from the digest, because the two publisher pages' class and id strings are unknown: use the compound tokens and English heading phrases below, and do not fetch those URLs in this subtask. Host names `tweaktown.com` and `notebookcheck.net` must not appear in library code, patterns, selectors, or test URLs. Non-English headings stay unimplemented. If a later phase supplies captured HTML for those two pages, store it only as jvmTest resources and assert that the known bio sentence is absent from `article.content` and `article.articleContent.text()`. Extend the compound list with one more generic token only when that captured markup uses an author-box name that is not already listed. Do not copy publisher-specific selectors into the library. Existing `NotebookcheckTest` and `NotebookcheckRealTest` cover a different article, do not expect that bio sentence, and must keep passing unchanged. No tweaktown fixture exists today.

### Task 1 — Call a new protected method at the end of `prepArticle`

Serves acceptance criterion 1: site-agnostic removal of author-about, bio, and disclaimer blocks from extracted HTML and text, without a publisher selector and without a new public option.

Paths and symbols:
- `ReadabilityArticleGrabber.prepArticle(articleContent: Element, options: ArticleGrabberOptions, metadata: ArticleMetadata)`
- One new `protected open` method on `ReadabilityArticleGrabber`. Name it `stripAuthorBioAndDisclaimerBlocks(articleContent: Element)`.
- Call it once, at the end of `prepArticle`, after the existing share cleanup.
- Do not add the logic inline in `prepArticle`. Detekt already suppresses `TooManyFunctions` on the grabber; one new method avoids pushing `prepArticle` further over complexity limits.

Tests: none for the call site itself. The behavior tests in Task 3 observe the result through `parse()`.

Constraints: matchers are local `Regex(..., RegexOption.IGNORE_CASE)` values inside the new method. Reuse `cleanMatchedNodes` only as the pattern for how class and id are combined (`className + " " + id`) and for removing a matched descendant. `cleanMatchedNodes` starts at the first child, so the article root is not class-matched; this method must walk descendants itself and must not remove `articleContent`. Also reuse `ProcessorBase.removeNodes` where a tag-plus-filter removal fits. Do not change `RegExUtil`.

### Task 2 — Remove the three site-agnostic shapes and keep the article body

Serves acceptance criterion 1: headings such as About the author / About author / author bio, common author-box class and id patterns, and trailing disclaimer blocks disappear, while legitimate article body stays. The heuristic is generic, so it applies beyond the two verification URLs.

Inside `stripAuthorBioAndDisclaimerBlocks`, remove these shapes.

1. Compound class or id, case-insensitive, on a descendant that is not the article root. Match against `className + " " + id` for: `author-bio`, `authorbio`, `author-box`, `authorbox`, `author-info`, `author-profile`, `author-description`, `author-details`, `about-author`, `about-the-author`, `aboutauthor`, `writer-bio`, `bio-box`. Remove the whole element, including a heading and image nested inside it. A compound match may be removed even when the element is short. These names identify the box rather than the article, so the majority guard does not apply.

2. A descendant whose class or id contains a bare `author` token, or a `disclaimer` token. Remove it only when the text before that element is longer than the element's own text. Compare trimmed text length of the preceding article content with the element's trimmed text length. Keep the element when it holds most of the article, so a content wrapper whose class or id is `author` stays. Match `author` and `disclaimer` as tokens, not as substrings of unrelated words.

3. An `h1`–`h6` whose trimmed text is `About the author`, `About author`, or `Author bio`, ignoring case, extra internal whitespace, and a trailing colon. Remove that heading and the following siblings until the next heading of the same or higher rank (a smaller or equal heading level number). Apply the same sibling removal to a heading whose text is `Disclaimer`. Apply the same preceding-text guard: remove the heading block only when the text before it is longer than the block. A paragraph that merely contains the phrase "about the author" stays. A short byline under 100 characters stays on the existing `checkByline` path in `prepareNodes` and is not reimplemented here.

Removing nodes here shortens the text measured for the default 500-character threshold. The guard leaves the body that satisfied selection in place. `grabArticle` still returns the longest attempt when every attempt is under that threshold, as long as some text remains.

Tests: none in this task. Task 3 locks each branch that can fail on its own.

Constraints: no host names, no publisher selectors, no edits to the default candidate patterns, no public API change. English phrases from the spec are the whole heading set.

### Task 3 — Add one commonTest for the behaviors that can fail independently

Serves acceptance criterion 1: tests cover the patterns, and a body sentence remains while the bio or disclaimer sentence is absent. Synthetic pages are the pattern coverage. They stand in for the two live URLs, whose markup was not captured.

Add `library/src/commonTest/kotlin/com/sermilion/readability4k/AuthorBioDisclaimerRemovalTest.kt`. Follow the existing Kotest `FunSpec` shape used by `EmptySpaceRemovalTest`: inline HTML, `Readability4K("https://example.com", html).parse()`, then assert on `article.articleContent` text and `article.content`. Give the body more text than the trailing bio so scoring selects the article and the preceding-text guard fires. Short pages are valid: under `charThreshold` (default 500), `grabArticle` returns the longest attempt when some text remains. `parseAsync()` is out of scope; common tests call `parse()` directly.

test_obligations:

- Three named author headings in one test, three small documents. Realistic bug: the heading set keeps only "About the author" and still emits a section titled "About author" or "Author bio". Assert the body sentence remains and each bio sentence is absent from both `article.content` and `article.articleContent` text. Also assert a following sibling is removed and a later same-or-higher heading is kept, on the "About the author" document only.
- One compound class, `class="author-bio"`, with a nested heading and image. Realistic bug: the box survives because unlikely-candidate removal skips `sidebar` plus `author`, and negative weighting ignores `author` and `bio`. Assert the body remains and the bio sentence, nested heading text, and image are absent.
- One author-box id, `id="author-box"`, with no author class. Realistic bug: a class-only matcher leaves an id-only box in the article. Assert the body remains and the bio sentence is absent.
- One trailing `Disclaimer` heading whose block is shorter than the preceding body. Realistic bug: disclaimer prose with many commas survives `cleanConditionally` and is not in the author-heading set. Assert the body remains and the disclaimer sentence is absent.
- One descendant whose class contains a `disclaimer` token, shorter than the preceding body. Realistic bug: the heading path is implemented and the class/id token path is not, so a disclaimer box with no heading stays. Assert the body remains and the disclaimer sentence is absent.
- One body paragraph that contains the words "about the author". Realistic bug: a text search removes a real sentence in the article. Assert that sentence is present.
- One long article wrapper with `class="author"` that holds more text than anything before it. Realistic bug: a broad `author` match deletes the article container. Assert the wrapper's body sentence is present.
- One short lead followed by a longer "About the author" section. Realistic bug: the preceding-text guard is missing or inverted, so the longer section is deleted and the article is gutted. Assert the long section's sentence is present.

Do not add sibling tests that repeat a branch with another compound token, another heading level, or another URL. Do not add a test for the short-byline path: `prepareNodes` already returns after `checkByline`, and this method does not own that behavior. Do not modify `NotebookcheckTest` or `NotebookcheckRealTest` unless a run shows this strip removed legitimate body from those fixtures; in that case keep their assertions and narrow the heuristic.

### Task 4 — Format only

Serves acceptance criterion 1 indirectly: the tree has to satisfy formatting before the later validation gate.

Run `./gradlew spotlessApply` on the edited Kotlin. Do not run `./gradlew check`, `:library:jvmTest`, `:library:allTests`, `check --continue`, or a compile as proof. Validation owns those commands. Do not publish, bump a version, change readian-android, commit, push, or open the PR. The feature branch name is `feat/R4K-1-strip-about-author-and-similar-disclaimer-blocks`. Commit, history, and the PR stay with their phases. Full validation is `./gradlew check`, plus `:library:jvmTest` and `:library:allTests`, in the validate phase.

Assumption recorded for implement to confirm at the call site: `prepArticle`'s share cleanup is the local pattern (`"share".toRegex()` passed to `cleanMatchedNodes` on each child), and the new call goes after that cleanup, before `prepArticle` returns.
