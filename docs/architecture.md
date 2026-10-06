# Readability4K Architecture

This document summarizes the architecture and implementation conventions used in Readability4K.

## High-Level Structure

Readability4K is a Kotlin Multiplatform repository with:

- `library/`: cross-platform readability engine and public API
- `cli/`: JVM CLI wrapper for running extraction from terminal
- `iosApp/`: iOS consumer surface
- `build-logic/`: shared Gradle convention plugins

The library is the source of truth for behavior. CLI and sample consumers should not duplicate parsing logic.

## Library Module Layout

Main package: `com.sermilion.readability4k`

### Public API (stable surface)

- `Readability4K`: primary entry point
- `Article`: extraction result model
- `model/ReadabilityOptions`: parser configuration
- `IsProbablyReaderable`: lightweight pre-check helper
- `processor/ArticleContentExtension`: optional single-slot extraction hook
- `processor/AuthorBioDisclaimerExtension`: opt-in scored author-bio / disclaimer cleanup

### Internal Pipeline Components

- `processor/ReadabilityPreprocessor`
- `processor/ReadabilityMetadataParser`
- `processor/ReadabilityArticleGrabber`
- `processor/ReadabilityPostprocessor`
- Supporting processors: `NoscriptHandler`, `CommentParser`, `CandidateFilter`, `RedditCommentParser`

### Supporting Packages

- `style/`: social/embed style rendering helpers
- `transformer/`: URL transformation hooks (for example Reddit)
- `util/`: DOM and regex helpers, logging interface, visibility checks

## Parsing Flow

Standard flow for `Readability4K.parse()` and `parseAsync()`:

1. Parse incoming HTML into DOM.
2. Collect metadata (title, byline, excerpt, site/language data when available).
3. Preprocess nodes (cleanup, normalization, readability heuristics prep).
4. Score candidates and pick top content container(s). An optional
   `contentExtension` captures a snapshot before grabber normalization and
   applies cleanup after article prep, before the readability page wrap.
   `ReadabilityOptions` does not hold this slot; the default is null.
5. Postprocess output content (cleanup + serialization).
6. Construct `Article` result with HTML/text/metadata fields.

`parseAsync()` should be preferred by coroutine-based consumers to avoid blocking callers.

## Extension and Change Guidelines

When adding parser behavior:

1. Prefer extending existing processors over creating parallel logic paths.
2. Keep option handling centralized in `ReadabilityOptions` and respect defaults.
3. Preserve output shape and semantics unless a breaking change is explicitly intended.
4. Add tests in `commonTest` for behavior changes, and `jvmTest` for fixture/regression scenarios.

## Testing Strategy

- `commonTest`: algorithm and option behavior across platforms
- `jvmTest`: HTML fixture regression tests and mock-based tests
- Root `./gradlew check` should remain green after any library or CLI change

## CLI Relationship

`cli/src/main/kotlin/com/sermilion/readability4k/cli/Main.kt` is a thin wrapper:

- Fetches/reads HTML input
- Invokes library API
- Formats output (`html`, `text`, `json`, `metadata`, `all`)

Any extraction logic change should happen in `library`, not `cli`.
