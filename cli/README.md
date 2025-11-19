# Readability4K CLI

A command-line tool for testing and using Readability4K to extract article content from web pages.

## Installation

Build the CLI tool:

```bash
./gradlew :cli:build
```

## Usage

### Basic Usage

```bash
./gradlew :cli:run --args="https://example.com/article"
```

### With Output Format

```bash
# HTML output (default)
./gradlew :cli:run --args="https://example.com/article html"

# Plain text output
./gradlew :cli:run --args="https://example.com/article text"

# JSON output
./gradlew :cli:run --args="https://example.com/article json"

# Metadata only
./gradlew :cli:run --args="https://example.com/article metadata"

# Everything (metadata + text + HTML)
./gradlew :cli:run --args="https://example.com/article all"
```

### With Custom Character Threshold

```bash
# Set minimum character threshold to 300
./gradlew :cli:run --args="https://example.com/article text 300"
```

## Output Formats

### `html` (default)
Outputs only the extracted HTML content.

```bash
./gradlew :cli:run --args="https://example.com/article html"
```

### `text`
Outputs only the plain text content without HTML markup.

```bash
./gradlew :cli:run --args="https://example.com/article text"
```

### `json`
Outputs all article data in JSON format.

```bash
./gradlew :cli:run --args="https://example.com/article json"
```

### `metadata`
Outputs only the article metadata (title, author, site name, etc.).

```bash
./gradlew :cli:run --args="https://example.com/article metadata"
```

### `all`
Outputs everything: metadata, text content, and HTML content.

```bash
./gradlew :cli:run --args="https://example.com/article all"
```

## Examples

### Extract article and save to file

```bash
# Save HTML content
./gradlew :cli:run --args="https://example.com/article html" > output.html

# Save text content
./gradlew :cli:run --args="https://example.com/article text" > output.txt

# Save JSON data
./gradlew :cli:run --args="https://example.com/article json" > output.json
```

### Quick metadata check

```bash
./gradlew :cli:run --args="https://example.com/article metadata"
```

Output:
```
================================================================================
ARTICLE METADATA
================================================================================
URL:            https://example.com/article
Title:          Article Title
Author:         John Doe
Site Name:      Example Site
Language:       en
Published:      2024-01-01
Length:         1234 characters
Excerpt:        Article excerpt...
================================================================================
```

## Using the Installable Distribution

For easier usage, you can install the CLI tool:

```bash
# Build and install
./gradlew :cli:installDist

# Run the installed version
./cli/build/install/cli/bin/cli https://example.com/article

# Or add to your PATH
export PATH="$PATH:$(pwd)/cli/build/install/cli/bin"
cli https://example.com/article
```

## Testing Real Articles

Try these examples:

```bash
# Wikipedia article
./gradlew :cli:run --args="https://en.wikipedia.org/wiki/Kotlin_(programming_language) metadata"

# Medium article
./gradlew :cli:run --args="https://medium.com/@username/article-slug text"

# News article
./gradlew :cli:run --args="https://www.bbc.com/news/article-id all"
```

## Troubleshooting

### Connection Issues

If you encounter connection issues, make sure:
- You have internet connectivity
- The URL is accessible from your network
- The website doesn't block automated requests

### No Content Extracted

If no content is extracted:
- Try lowering the character threshold: `./gradlew :cli:run --args="https://example.com/article text 100"`
- Check if the page has actual article content
- Some websites may require JavaScript rendering (not supported)

## Development

To modify the CLI tool, edit:
- `cli/src/main/kotlin/com/sermilion/readability4k/cli/Main.kt`

Then rebuild:
```bash
./gradlew :cli:build
```
