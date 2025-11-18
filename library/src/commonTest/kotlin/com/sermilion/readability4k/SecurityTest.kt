package com.sermilion.readability4k

import com.sermilion.readability4k.model.ReadabilityOptions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Security tests for HTML injection and XSS attack vectors.
 *
 * These tests verify that the Readability4K library handles potentially malicious HTML safely.
 * Note: The library extracts article content and does not inherently sanitize HTML for display.
 * Applications using this library MUST properly escape/sanitize the output before rendering
 * in WebViews or other HTML contexts.
 */
class SecurityTest :
  FunSpec({

    context("script tag handling") {
      test("should extract content without executing script tags") {
        val html = """
        <html>
          <head><title>Test Article</title></head>
          <body>
            <article>
              <h1>Article Title</h1>
              <script>alert('XSS')</script>
              <p>This is the main content of the article.</p>
              <script src="https://evil.com/malicious.js"></script>
              <p>More article content here.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.shouldNotBeNull()
        article.title shouldBe "Test Article"

        // The preprocessor removes script tags during document preparation
        val content = article.content.orEmpty()
        content shouldNotContain "<script>"
        content shouldNotContain "alert"
        content shouldNotContain "malicious.js"

        // Verify article content was extracted
        article.textContent shouldContain "main content"
        article.textContent shouldContain "More article content"
      }

      test("documents handling of inline event handlers") {
        val html = """
        <html>
          <body>
            <article>
              <h1>Test</h1>
              <p onclick="alert('XSS')">Click me</p>
              <img src="x" onerror="alert('XSS')">
              <a href="#" onmouseover="alert('XSS')">Link</a>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.shouldNotBeNull()

        // IMPORTANT: Readability4K is a content extraction library, NOT an HTML sanitizer.
        // Event handlers may be preserved in the output HTML.
        // Applications MUST use a dedicated HTML sanitizer (e.g., OWASP Java HTML Sanitizer)
        // before rendering content in WebViews or other HTML contexts.

        // Verify text content is still extracted
        article.textContent shouldContain "Click me"
        article.textContent shouldContain "Link"
      }
    }

    context("iframe and embed handling") {
      test("should handle iframe elements according to Readability algorithm") {
        val html = """
        <html>
          <body>
            <article>
              <h1>Article with iframe</h1>
              <p>Some content before iframe.</p>
              <iframe src="https://external.com/embed"></iframe>
              <p>Some content after iframe.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.shouldNotBeNull()
        // Note: Readability may keep video embeds (YouTube, Vimeo) but removes others
        // This behavior matches Mozilla's Readability algorithm
        val content = article.content.orEmpty()

        // Text content should be preserved
        article.textContent shouldContain "content before iframe"
        article.textContent shouldContain "content after iframe"
      }
    }

    context("data URL attacks") {
      test("documents handling of data: URLs in image sources") {
        val html = """
        <html>
          <body>
            <article>
              <h1>Article</h1>
              <img src="data:text/html,<script>alert('XSS')</script>">
              <p>Article content</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.shouldNotBeNull()

        // Data URLs may be preserved. Applications should validate and sanitize
        // all URLs before use, especially when rendering in WebViews.
        article.textContent shouldContain "Article content"
      }
    }

    context("HTML entity encoding") {
      test("should preserve HTML entities in content") {
        val html = """
        <html>
          <body>
            <article>
              <h1>Special Characters &amp; Entities</h1>
              <p>Less than &lt; and greater than &gt;</p>
              <p>Quote: &quot; and apostrophe: &apos;</p>
              <p>Non-breaking space:&nbsp;here</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.shouldNotBeNull()
        val text = article.textContent.orEmpty()

        // Entities should be decoded in text content
        text shouldContain "Special Characters & Entities"
        text shouldContain "Less than < and greater than >"
      }
    }

    context("large document DoS prevention") {
      test("should throw exception when document exceeds maxElemsToParse") {
        val html = buildString {
          append("<html><body><article>")
          // Create 1100 elements
          repeat(1100) {
            append("<div>Content $it</div>")
          }
          append("</article></body></html>")
        }

        val options = ReadabilityOptions(maxElemsToParse = 1000)
        val readability = Readability4K("https://example.com", html, options)

        val exception = runCatching { readability.parse() }.exceptionOrNull()

        exception.shouldNotBeNull()
        exception.shouldBeInstanceOf<MaxElementsExceededException>()

        exception.elementCount shouldBe 1105 // Ksoup creates additional elements during parsing
        exception.maxElements shouldBe 1000
      }
    }

    context("malformed HTML handling") {
      test("should gracefully handle unclosed tags") {
        val html = """
        <html>
          <body>
            <article>
              <h1>Title
              <p>Paragraph 1
              <p>Paragraph 2</p>
              <div>Content
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = runCatching { readability.parse() }.getOrNull()

        // Ksoup should handle malformed HTML gracefully
        article.shouldNotBeNull()
        article.textContent shouldContain "Title"
        article.textContent shouldContain "Paragraph 1"
      }

      test("should handle deeply nested elements") {
        val html = buildString {
          append("<html><body><article>")
          append("<h1>Title</h1>")

          // Create deeply nested structure (50 levels)
          repeat(50) { append("<div>") }
          append("<p>Deeply nested content</p>")
          repeat(50) { append("</div>") }

          append("</article></body></html>")
        }

        val readability = Readability4K("https://example.com", html)
        val article = runCatching { readability.parse() }.getOrNull()

        article.shouldNotBeNull()
        article.textContent shouldContain "Deeply nested content"
      }
    }

    context("URL handling") {
      test("should convert relative URLs to absolute") {
        val html = """
        <html>
          <body>
            <article>
              <h1>Article</h1>
              <img src="/images/photo.jpg">
              <a href="/page">Link</a>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        val content = article.content.orEmpty()

        // URLs should be made absolute during postprocessing
        content shouldContain "https://example.com/images/photo.jpg"
        content shouldContain "https://example.com/page"
      }
    }
  })
