# Author-bio real-article corpus (R4K-1)

## Purpose

Offline, deterministic regression fixtures for scored author-bio / disclaimer
cleanup via `AuthorBioDisclaimerExtension`. Default parse does not apply this
cleanup.

Unit tests in `commonTest` cover synthetic patterns. This corpus exercises **real
publisher markup** (~100 pages) so CI does not depend on live network fetches.

## How the corpus was built (2026-10-06)

1. Discovered article URLs from public RSS/Atom feeds (Ars Technica, The Verge,
   TechCrunch, Wired, Engadget, BBC Tech, Guardian Tech, CNET, Android Authority,
   9to5Mac, MacRumors, Polygon, Kotaku, Eurogamer, Rock Paper Shotgun, IGN,
   Nature, Smithsonian, NPR, Al Jazeera, plus HN-linked blogs) and from publisher
   homepages for **Notebookcheck** and **TweakTown**.
2. Fetched each HTML document with a desktop browser User-Agent.
3. Slimmed pages for git size: removed `script` / `style` / `svg` / `iframe` /
   `noscript` / stylesheet links and HTML comments. Document structure and body
   text were kept so Readability4K still sees real DOM shapes.
4. Selected **100** pages with domain balancing (30 hosts), always including the
   R4K-1 seed pages (TweakTown news/113920 and Notebookcheck ChatGPT-ads article).
5. For each page, recorded:
   - `must_keep`: a list of distinctive body sentences that must survive extraction
     (two phrases when the slimmed HTML has two distinct body sentences)
   - `expect_absent` (when the scored formula should drop the block): distinctive
     author-box / bio phrases that must not appear in extracted text
   - `notes`: annotation reasons, including pages the formula intentionally keeps

Rebuilding requires network access; CI only reads these checked-in files.

## Layout

- `manifest.json` — index of articles and expectations
- `html/*.html` — slimmed real-page fixtures
- `CORPUS.md` — this document

## Running

```bash
./gradlew :library:jvmTest --tests 'com.sermilion.readability4k.AuthorBioCorpusRegressionTest'
```
