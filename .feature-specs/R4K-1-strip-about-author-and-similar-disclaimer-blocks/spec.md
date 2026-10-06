# R4K-1 - strip-about-author-and-similar-disclaimer-blocks

## Mode

single_spec

## Intended Outcome

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

## Overview

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

## Acceptance Criteria

1. R4K-1 Strip About-author and similar disclaimer blocks Requirements: - Update readability4k so extracted article HTML/text no longer includes trailing “About the author” / author bio / similar disclaimer sections that currently survive into Readian News reading mode. - Solution MUST be site-agnostic (any publisher). Do not hardcode tweaktown.com, notebookcheck.net, or publisher-specific selectors. - Fixtures for verification only: - https://www.tweaktown.com/rss/click/news/113920/?c=news-mf (resolve redirects) - https://www.notebookcheck.net/ChatGPT-ads-German-regulators-warn-about-profiles-built-from-chats.1416940.0.html — leftover bio text includes: “I write about IT security and artificial intelligence. Cybersecurity is one of my regular topics, among others for Golem.de. At Notebookcheck, my focus is on practical AI for everyday users…” (sidebar author box pattern). - Prefer general heuristics: headings like About the author / About author / author bio; common author-box class/id patterns; trailing bio/disclaimer blocks — without removing legitimate article body. - Read AGENTS.md and docs/architecture.md before parser changes. Add fixture-backed tests. Run library tests. - Open a PR on Sermilion/readability4k. Do NOT publish/release or bump readian-android. Acceptance: - Those fixtures’ extracted content no longer contain the author-about / bio blocks. - Heuristic works beyond those two sites. - Tests cover the patterns. - PR opened. Non-goals: Readian app dependency bump; Maven release; site-specific scrapers.

## Constraints

- Supplied requirements are authoritative and need no tracker lookup. Locally allocated issue keys do not require a tracker connection. Only an explicit unresolved tracker reference without requirements needs lookup through its connected tracker before planning. Use the returned requirements, not the URL title. If that lookup fails, block with the returned reason before implementation; never infer or substitute requirements.

## Non-Goals

- None

## Validation Strategy

Run the repository's required checks and verify every supplied acceptance criterion.
