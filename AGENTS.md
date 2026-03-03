# Agent Guidelines for Working with This Repository

This file provides guidance to Claude Code (claude.ai/code) and other AI coding agents when working in this repository.

## Required Documentation

**IMPORTANT: Before implementing features, refactoring parsing logic, or changing publishing/release workflows, review the relevant docs first.**

### Primary Documentation

- **[Architecture](docs/architecture.md)**: **READ THIS FIRST** for implementation and refactoring work
  - Kotlin Multiplatform module boundaries
  - Parsing pipeline and key extension points
  - API surface expectations for `Readability4K`, `ReadabilityOptions`, and `Article`
  - Testing layout and conventions

### Domain-Specific Documentation

Before changing these areas, review:

- **Library internals / parser behavior**: `docs/architecture.md`
- **Publishing artifacts**: `PUBLISHING.md`
- **Release process and tagging**: `RELEASE.md`
- **CLI behavior and output formats**: `cli/README.md`

### When to Reference Documentation

1. **Before parser changes**: confirm expected pipeline behavior and option compatibility in `docs/architecture.md`
2. **Before public API changes**: verify impact on multiplatform consumers and README usage examples
3. **Before release/publishing changes**: read `PUBLISHING.md` and `RELEASE.md`
4. **Before CLI changes**: read `cli/README.md` and keep examples in sync
5. **When in doubt**: check docs first, then ask the user

## Essential Build Commands

### Build and Assembly

- Build all modules: `./gradlew build`
- Build library module: `./gradlew :library:build`
- Build CLI module: `./gradlew :cli:build`
- Clean: `./gradlew clean`

### Testing

- Run all checks/tests: `./gradlew check`
- Run library JVM tests: `./gradlew :library:jvmTest`
- Run library common tests: `./gradlew :library:allTests`

### Code Quality and Linting

- Format code: `./gradlew spotlessApply`
- Check formatting: `./gradlew spotlessCheck`
- Run static analysis: `./gradlew detekt`
- Run all quality gates: `./gradlew check`

### CLI Smoke Test

- Run CLI against a URL: `./gradlew :cli:run --args="https://example.com/article"`

### Publishing and Release

- Publish all variants locally: `./gradlew publishToMavenLocal`
- See full publishing guide: `PUBLISHING.md`
- See release/tag workflow: `RELEASE.md`

## Project Architecture

### Module Structure

This repository is a Kotlin Multiplatform project with two main modules:

- **library**: Core Readability4K engine and public API
  - `commonMain`: core algorithm, models, processors, styles, utilities
  - `commonTest`: cross-platform tests for parser behavior
  - `jvmTest`: fixture-heavy JVM integration tests
- **cli**: JVM command-line wrapper around the library
- **iosApp**: sample/consumer app surface for iOS integration
- **build-logic**: shared Gradle convention plugins

### Core Flow (Library)

Typical extraction flow:

1. Create `Readability4K(url, html, options, logger)`
2. Parse metadata and preprocess DOM
3. Score and select top content candidates
4. Postprocess and serialize extracted content
5. Return `Article` with metadata + content payloads

### Key Patterns

- **Pure Kotlin KMP**: core logic lives in `commonMain`
- **Constructor-driven configuration**: options and logger are injected explicitly
- **Behavior parity focus**: keep compatibility with Mozilla Readability semantics
- **Test-first changes**: parser behavior updates should include fixture-backed tests

## Technology Stack

- **Language**: Kotlin
- **Targets**: Android, JVM, iOS (arm64/simulator/x64)
- **HTML parser**: ksoup
- **Async support**: kotlinx.coroutines
- **Tests**: Kotlin test + Kotest (+ MockK on JVM tests)

## Development Guidelines

1. Prefer minimal, behavior-preserving changes in parser logic.
2. Preserve public API stability unless user explicitly requests a breaking change.
3. Keep README and CLI docs aligned with behavior and examples.
4. Reuse existing processors/utilities before introducing new abstractions.
5. Add or update tests for algorithmic and output-shape changes.

## Additional Documentation

- **[Architecture](docs/architecture.md)**: module boundaries, pipeline, and extension points
- **[PUBLISHING.md](PUBLISHING.md)**: local publishing and artifact usage
- **[RELEASE.md](RELEASE.md)**: tagging and release automation
- **[CLI README](cli/README.md)**: command usage and output formats
