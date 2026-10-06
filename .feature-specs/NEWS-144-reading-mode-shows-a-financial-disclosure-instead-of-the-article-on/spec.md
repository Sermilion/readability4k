# NEWS-144 - reading-mode-shows-a-financial-disclosure-instead-of-the-article-on

## Mode

single_spec

## Intended Outcome

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

## Overview

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

## Acceptance Criteria

1. https://linear.app/readian/issue/NEWS-144/reading-mode-shows-a-financial-disclosure-instead-of-the-article-on Reading Mode extracted the wrong block on an Investing.com story Reading Mode extracted the wrong block on an Investing.com story. It showed a financial disclosure instead of the article body. Repro Open this article in Readian: https://www.investing.com/news/economy-news/former-anthropic-researcher-coxon-to-testify-at-new-york-city-ai-hearing-bloomberg-news-reports-4930955 Tap Reading Mode. Expected Clear text of the main article (Reuters: former Anthropic researcher Jacob Coxon to testify at a New York City AI hearing, per Bloomberg). Actual A financial disclosure (or similar boilerplate) that has nothing to do with the article. Shared from https://readian.news on 2026-10-04. Findings: Reading Mode picked the Fusion Media footer. readability4k 0.2.0 scores the Fusion Media footer above the short Reuters body because the footer is only marked data-test="footer" and a Tailwind text- class scores as content. The Readian app uses readability4k. Implement the fix in this readability4k repository.

## Constraints

- Supplied requirements are authoritative and need no tracker lookup. Locally allocated issue keys do not require a tracker connection. Only an explicit unresolved tracker reference without requirements needs lookup through its connected tracker before planning. Use the returned requirements, not the URL title. If that lookup fails, block with the returned reason before implementation; never infer or substitute requirements.

## Non-Goals

- None

## Validation Strategy

Run the repository's required checks and verify every supplied acceptance criterion.
