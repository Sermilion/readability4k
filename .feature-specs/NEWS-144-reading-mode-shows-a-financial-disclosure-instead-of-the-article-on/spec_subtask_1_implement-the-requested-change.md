# NEWS-144 Subtask 1 - Implement the requested change

Parent spec: [.feature-specs/NEWS-144-reading-mode-shows-a-financial-disclosure-instead-of-the-article-on/spec.md](./spec.md)
Issue key: NEWS-144

## Scope

https://linear.app/readian/issue/NEWS-144/reading-mode-shows-a-financial-disclosure-instead-of-the-article-on Reading Mode extracted the wrong block on an Investing.com story

Reading Mode extracted the wrong block on an Investing.com story. It showed a financial disclosure instead of the article body.

Repro

Open this article in Readian: https://www.investing.com/news/economy-news/former-anthropic-researcher-coxon-to-testify-at-new-york-city-ai-hearing-bloomberg-news-reports-4930955

Tap Reading Mode.

Expected

Clear text of the main article (Reuters: former Anthropic researcher Jacob Coxon to testify at a New York City AI hearing, per Bloomberg).

Actual

A financial disclosure (or similar boilerplate) that has nothing to do with the article.

Shared from https://readian.news on 2026-10-04.

Findings: Reading Mode picked the Fusion Media footer. readability4k 0.2.0 scores the Fusion Media footer above the short Reuters body because the footer is only marked data-test="footer" and a Tailwind text- class scores as content.

The Readian app uses readability4k. Implement the fix in this readability4k repository.

Supplied requirements are authoritative and need no tracker lookup. Locally allocated issue keys do not require a tracker connection. Only an explicit unresolved tracker reference without requirements needs lookup through its connected tracker before planning. Use the returned requirements, not the URL title. If that lookup fails, block with the returned reason before implementation; never infer or substitute requirements.

## Acceptance Criteria

1. https://linear.app/readian/issue/NEWS-144/reading-mode-shows-a-financial-disclosure-instead-of-the-article-on Reading Mode extracted the wrong block on an Investing.com story Reading Mode extracted the wrong block on an Investing.com story. It showed a financial disclosure instead of the article body. Repro Open this article in Readian: https://www.investing.com/news/economy-news/former-anthropic-researcher-coxon-to-testify-at-new-york-city-ai-hearing-bloomberg-news-reports-4930955 Tap Reading Mode. Expected Clear text of the main article (Reuters: former Anthropic researcher Jacob Coxon to testify at a New York City AI hearing, per Bloomberg). Actual A financial disclosure (or similar boilerplate) that has nothing to do with the article. Shared from https://readian.news on 2026-10-04. Findings: Reading Mode picked the Fusion Media footer. readability4k 0.2.0 scores the Fusion Media footer above the short Reuters body because the footer is only marked data-test="footer" and a Tailwind text- class scores as content. The Readian app uses readability4k. Implement the fix in this readability4k repository.

## Non-Goals

- None

## Dependency Notes

Depends on: none
The full goal owns planning and execution of the supplied requirements.

## Validation Strategy

Run the repository's required checks and verify every supplied acceptance criterion.

## Implementation Details

### Settled Assumptions

The live Investing.com DOM is not in the repo. Findings name `data-test="footer"` and a Tailwind class that begins with `text-`. Exact utility names, compound values such as `footer-legal`, and the live Reuters body length are unknown. This plan uses the findings and does not fetch the page.

Match `data-test` with case-insensitive equality on the whole value `footer`. Leave values such as `footer-legal` and `us_tc_ros_dt_mid_center` in the document. Implement confirms the match only if the repro attribute is a different string.

Apply the positive-class exception per whitespace-delimited token, on the class string and on the id. A token that starts with `text-` gets no positive bonus when the remainder fails `isPositive`. `text-sm` and `text-gray-500` add nothing. `text-content` still gains the bonus because `content` matches. `article-text` still gains the bonus because the token does not start with `text-`. The negative pattern still runs on the full class string and the full id, so a class or id of `footer` still subtracts 25.

Keep `POSITIVE_DEFAULT_PATTERN` unchanged. Mozilla's current positive list was not compared. Keeping the pattern limits the edit to `prepareNodes` and `getClassWeight`.

The disclosure and the article body are separate blocks. Delete the element whose own `data-test` equals `footer`. Leave its ancestors in place. Skip the `body` tag if it carries that attribute, so the document root stays. Remove every other tag, including `div` and `a`.

`grabArticle` returns the first attempt whose extracted text length meets `ReadabilityOptions.DEFAULT_CHAR_THRESHOLD` of 500. The digest does not say what is returned when every attempt stays under 500. Assume `parse()` still yields `articleContent` text for the short body after the footer is gone, so the article sentence stays observable. Implement confirms that fallback in `grabArticle`. If the fallback is null, keep the disclosure longer than 500 characters and point the fixed-path assertion at the content `parse()` actually returns, with the article sentence present and the disclosure absent.

### Ordered Tasks

1. Remove `data-test="footer"` inside `prepareNodes`.

   Serves acceptance criterion 1.

   Edit `prepareNodes` in `library/src/commonMain/kotlin/com/sermilion/readability4k/processor/ReadabilityArticleGrabber.kt`.

   On every attempt, including attempts with `stripUnlikelyCandidates` false, remove an element whose `data-test` value equals `footer`, ignoring case. Reuse the removal call `prepareNodes` already uses for unlikely candidates. Class and id unlikely removal stays behind `stripUnlikelyCandidates`. `matchString` stays `node.className() + " " + node.id()`. `checkByline` keeps using that string and the pattern `byline|author|dateline|writtenby|p-author`.

   test_obligations: none on this task alone. Task 3 locks the behavior. The bug is attempt 2 returning a disclosure marked only with `data-test="footer"` after class and id stripping is turned off.

2. Skip non-positive `text-` tokens in `getClassWeight`.

   Serves acceptance criterion 1.

   Edit `getClassWeight` in `library/src/commonMain/kotlin/com/sermilion/readability4k/processor/ReadabilityArticleGrabber.kt`. Leave `RegExUtil.isPositive`, `RegExUtil.isNegative`, `POSITIVE_DEFAULT_PATTERN`, and `NEGATIVE_DEFAULT_PATTERN` unchanged.

   Return 0 when `weightClasses` is false. Otherwise keep one +25 and one -25 per class string and per id. When deciding the positive bonus, ignore a whitespace-delimited token that starts with `text-` if the remainder of that token does not match `isPositive`. Apply that same positive-token rule to the id. Score the negative pattern against the full class string and the full id.

   `cleanConditionally` and `cleanHeaders` already call `getClassWeight` and treat that weight as the whole score, removing the node when the weight is below 0. A class of `footer text-sm` currently nets to 0. After this change the negative penalty remains and conditional cleaning can remove that node. That side effect is intended.

   test_obligations: none. A direct `getClassWeight` test would restate the token rule. Once task 1 drops the marked footer, that unit test would not catch a separate user-visible failure. Existing corpus tests guard `article-text` and author-bio pages. If a corpus body sentence disappears, tighten this exception so a real `text` class still scores, and leave `AuthorBioScoring` unchanged.

3. Add the commonTest regression.

   Serves acceptance criterion 1.

   Add one test file under `library/src/commonTest/kotlin/com/sermilion/readability4k/`, in the style of `Readability4KComparisonTest`. Use `kotlin.test.Test`, a backtick test name, inline HTML, `Readability4K(url, html).parse()`, Kotest `shouldContain` on `article.articleContent?.text()`, and `shouldNotContain` for the disclosure.

   Use a neutral article container whose class and id match neither the positive nor the negative pattern. Use paragraphs of at least 25 characters, because `scoreElements` skips shorter paragraphs. Put a sibling `div` next to the article container with `data-test="footer"` and class `text-sm text-gray-500` only. Give the body one unique article sentence and the footer one unique disclosure sentence. Do not add an Investing.com HTML fixture. Leave `AuthorBioDisclaimerRemovalTest` unchanged, including `short page still drops the bio on the weightClasses retry`.

   The unfixed fixture must select the disclosure. Give the footer enough scored paragraph text that current class weight plus paragraph scoring prefers it. On current code the short-body case can still return on attempt 1, because a disclosure of 500 or more characters never reaches later attempts. The short-body test still earns its cost after a partial fix: if removal runs only while `stripUnlikelyCandidates` is true, attempt 1 drops the footer, the body stays under 500, and attempt 2 returns the disclosure while class weights stay on.

   test_obligations:
   - Long body. Body text longer than 500 characters. The article sentence is present and the disclosure sentence is absent. Realistic bug: attempt 1, with stripping and class weights still at their defaults, returns the footer because `data-test` is absent from `matchString` and `text-sm` matches `text` for +25.
   - Short body. Body text shorter than 500 characters and disclosure text longer than 500 characters. The same two assertions. Realistic bug: the body misses the 500-character threshold, `generateOptionsSequence` retries with `stripUnlikelyCandidates` false and `weightClasses` still true, and the long disclosure becomes the returned article.

### Constraints

Leave these unchanged: `generateOptionsSequence`, `ArticleGrabberOptions`, `ReadabilityOptions`, the `Readability4K.parse()` wiring that builds `ArticleGrabberOptions` from `preserveImages` and `preserveVideos`, `isProbablyReaderable`, `AuthorBioScoring`, the `getTopCandidate` overwrite of `contentScore` with `contentScore * (1 - linkDensity)`, the bio extension's pre-adjustment score and its track totals, author class and id matching on the `weightClasses` false retry, `isValidCousinCandidate`, and `isLikelyNonContentContainer`.

Heading `50614d8d6f29` allows an `AuthorBioScoring` edit only after a must_keep sentence disappears. Heading `615b8160cc15` keeps the link-adjusted `contentScore` overwrite and keeps `contentScore` and link density out of both bio track totals. Heading `faefc9f81bee` keeps author class and id matches working while `weightClasses` is false, with capture inside the winning attempt.

`ArticleGrabberOptions` stays at `stripUnlikelyCandidates = true`, `weightClasses = true`, and `cleanConditionally = true`. `ReadabilityOptions.DEFAULT_CHAR_THRESHOLD` stays 500. Each attempt reloads `pageCacheHtml` before `tryExtractArticle`.

The default `parse()` path changes for every caller. No options field turns the new removal or the token rule off. Callers of `ReadabilityOptions` and `ArticleGrabberOptions` stay source compatible. The CLI stays a wrapper. The README gains no flag. Cutting a release is outside this change.

`history.md` and tracker comments stay unread. The supplied requirements are the acceptance source.

### Validation Ownership

Implement writes `prepareNodes`, `getClassWeight`, and the commonTest file. Validate runs `./gradlew check` and judges acceptance criterion 1 from that run. Planning does not compile, run tests, or run check.

## Next Path

Complete the goal and prepare its pull request.

## Spec Path

.feature-specs/NEWS-144-reading-mode-shows-a-financial-disclosure-instead-of-the-article-on/spec_subtask_1_implement-the-requested-change.md
