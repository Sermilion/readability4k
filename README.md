# Readability4K

A Kotlin Multiplatform library for extracting the main readable content and metadata from web pages, based on Mozilla's Readability algorithm.

## Features

- **Extract article content** from any web page
- **Metadata extraction** (title, author, excerpt, etc.)
- **Multiplatform support**: Android, iOS, JVM
- **Async/Suspend support** for coroutine-based applications
- **Pure Kotlin** implementation using ksoup for HTML parsing
- **No dependency injection required** - simple constructor-based design

## Installation

### Gradle (Kotlin DSL)

Add the JitPack repository:

```kotlin
repositories {
  maven("https://jitpack.io")
}
```

Add the dependency:

```kotlin
dependencies {
  implementation("com.github.sermilion:readability4k:0.1.0")
}
```

### Supported Platforms

- **Android** - minSdk 26+
- **JVM** - Java 11+
- **iOS** - arm64, simulatorArm64, x64

## Usage

### Basic Usage

```kotlin
import com.sermilion.readability4k.Readability4K

val url = "https://example.com/article"
val html = "<html>...</html>" // Your HTML content

val readability = Readability4K(url, html)
val article = readability.parse()

println("Title: ${article.title}")
println("Author: ${article.byline}")
println("Content: ${article.content}")
println("Text only: ${article.textContent}")
```

### Async Usage (Recommended for Android/Coroutines)

```kotlin
suspend fun extractArticle(url: String, html: String) {
  val readability = Readability4K(url, html)
  val article = readability.parseAsync() // Runs on Dispatchers.IO

  println("Title: ${article.title}")
  println("Content length: ${article.length} characters")
}
```

### Custom Options

Readability4K supports extensive configuration options for 100% compatibility with Mozilla Readability:

```kotlin
import com.sermilion.readability4k.model.ReadabilityOptions

val options = ReadabilityOptions(
  // Core Options
  maxElemsToParse = 1000,           // Limit number of elements to parse (0 = unlimited)
  nbTopCandidates = 5,               // Number of top candidates to consider
  charThreshold = 500,                // Minimum characters for article detection

  // Class Handling
  additionalClassesToPreserve = listOf("custom-class"),  // Preserve specific CSS classes
  keepClasses = false,                // Keep all CSS classes (default: false)

  // Metadata Options
  disableJSONLD = false,              // Skip JSON-LD metadata parsing (default: false)

  // Video Detection
  allowedVideoRegex = Regex("youtube|vimeo|dailymotion"),  // Custom video pattern

  // Link Density
  linkDensityModifier = 0.0,          // Adjust link density threshold (default: 0.0)

  // Custom Serialization
  serializer = { element -> element.html() }  // Custom HTML serializer function
)

val readability = Readability4K(url, html, options)
val article = readability.parse()
```

#### Option Details

- **maxElemsToParse**: Prevents parsing very large documents. Set to 0 for unlimited.
- **nbTopCandidates**: Higher values may improve accuracy but increase processing time.
- **charThreshold**: Article must have this many characters to be extracted. ⚠️ **Breaking Change**: Renamed from `wordThreshold` (still counts characters, not words).
- **additionalClassesToPreserve**: Preserves specified CSS classes in addition to built-in ones ("readability-styled", "page").
- **keepClasses**: When true, preserves all CSS classes on elements.
- **disableJSONLD**: Set to true to skip JSON-LD structured data parsing for better performance.
- **allowedVideoRegex**: Custom regex to identify video embeds beyond the default YouTube/Vimeo/Dailymotion patterns.
- **linkDensityModifier**: Positive values make the algorithm more lenient with links, negative values more strict.
- **serializer**: Provide a custom function to serialize HTML elements instead of the default `element.html()`.

### Custom Logging

```kotlin
import com.sermilion.readability4k.util.Logger

val logger = object : Logger {
  override fun debug(message: String) {
    println("[DEBUG] $message")
  }

  override fun error(message: String, throwable: Throwable?) {
    System.err.println("[ERROR] $message")
    throwable?.printStackTrace()
  }
}

val readability = Readability4K(url, html, logger = logger)
```

## Article Properties

The `Article` class provides:

- `uri: String` - Original URL
- `title: String?` - Extracted article title
- `byline: String?` - Author name
- `excerpt: String?` - Article description/summary
- `content: String?` - HTML content of the article
- `contentWithUtf8Encoding: String?` - HTML content wrapped with UTF-8 encoding
- `textContent: String?` - Plain text without HTML markup
- `length: Int` - Article length in characters
- `articleContent: Element?` - Ksoup Element for advanced manipulation

## Command-Line Tool

Readability4K includes a CLI tool for testing and extracting articles from URLs.

### Quick Start

```bash
# Extract article from URL
./gradlew :cli:run --args="https://example.com/article"

# Or use the convenience script
./readability4k-cli.sh https://example.com/article
```

### Output Formats

```bash
# HTML content (default)
./readability4k-cli.sh https://example.com/article html

# Plain text
./readability4k-cli.sh https://example.com/article text

# JSON format
./readability4k-cli.sh https://example.com/article json

# Metadata only
./readability4k-cli.sh https://example.com/article metadata

# Everything
./readability4k-cli.sh https://example.com/article all
```

### Save to File

```bash
# Save HTML
./readability4k-cli.sh https://example.com/article html > output.html

# Save text
./readability4k-cli.sh https://example.com/article text > output.txt

# Save JSON
./readability4k-cli.sh https://example.com/article json > output.json
```

### Installation

For easier usage, install the CLI tool:

```bash
./gradlew :cli:installDist
export PATH="$PATH:$(pwd)/cli/build/install/cli/bin"
cli https://example.com/article
```

See [cli/README.md](cli/README.md) for complete CLI documentation.

## Architecture

Readability4K follows clean architecture principles:

- **No external DI framework required** - Uses constructor injection with sensible defaults
- **Pure Kotlin** - Fully cross-platform compatible
- **Testable** - All components can be injected for testing
- **Well-tested** - Includes comprehensive test suite

## ProGuard/R8

ProGuard rules are included automatically when you add the library dependency. If needed, they're located in the library's consumer proguard file.

## Requirements

- Kotlin 2.2.21+
- Minimum SDK versions:
  - Android: 26+
  - JVM: 11+

## Dependencies

- [ksoup](https://github.com/fleeksoft/ksoup) - Kotlin Multiplatform HTML parser
- kotlinx-coroutines-core - For async support

## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

Based on [Mozilla's Readability.js](https://github.com/mozilla/readability) algorithm.
