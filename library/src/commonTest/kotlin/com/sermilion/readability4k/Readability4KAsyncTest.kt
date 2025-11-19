package com.sermilion.readability4k

import com.sermilion.readability4k.model.ReadabilityOptions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Readability4KAsyncTest : FunSpec({

  context("parseAsync") {
    test("should parse article asynchronously") {
      val html = """
        <html>
          <head><title>Async Test Article</title></head>
          <body>
            <article>
              <h1>Main Article Title</h1>
              <p>This is the first paragraph with some content for async testing.</p>
              <p>This is the second paragraph with more content for the test.</p>
              <p>This is the third paragraph to meet word threshold requirements.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com/async", html)
      val article = readability.parseAsync()

      article.title shouldNotBe null
      article.content shouldNotBe null
      article.textContent shouldContain "first paragraph"
      article.uri shouldBe "https://example.com/async"
    }

    test("should produce same results as synchronous parse") {
      val html = """
        <html>
          <head>
            <title>Sync vs Async Test</title>
            <meta name="author" content="Test Author">
          </head>
          <body>
            <article>
              <p>This is test content that should be extracted identically by both methods.</p>
              <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor.</p>
              <p>Additional content to ensure we have enough text for extraction.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readabilitySync = Readability4K("https://example.com/test", html)
      val readabilityAsync = Readability4K("https://example.com/test", html)

      val syncArticle = readabilitySync.parse()
      val asyncArticle = readabilityAsync.parseAsync()

      syncArticle.title shouldBe asyncArticle.title
      syncArticle.byline shouldBe asyncArticle.byline
      syncArticle.textContent shouldBe asyncArticle.textContent
      syncArticle.length shouldBe asyncArticle.length
    }

    test("should handle custom options in async mode") {
      val html = """
        <html>
          <body>
            <article>
              <p>Short content for low threshold test.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(charThreshold = 10)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parseAsync()

      article.content shouldNotBe null
    }

    test("should handle metadata extraction in async mode") {
      val html = """
        <html>
          <head>
            <title>Async Metadata Test</title>
            <meta name="author" content="Async Author">
            <meta name="description" content="Async description content">
            <meta charset="utf-8">
          </head>
          <body>
            <article>
              <p>Article content with enough words to pass the threshold test.</p>
              <p>More content to ensure we have sufficient text for extraction.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parseAsync()

      article.title shouldBe "Async Metadata Test"
      article.byline shouldBe "Async Author"
      article.excerpt shouldBe "Async description content"
      article.charset shouldBe "UTF-8"
    }

    test("should be callable from different dispatchers") {
      val html = """
        <html>
          <body>
            <article>
              <p>Content for dispatcher test with sufficient words.</p>
              <p>Additional content to meet the threshold.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html, ReadabilityOptions(charThreshold = 50))

      val article = withContext(Dispatchers.Default) {
        readability.parseAsync()
      }

      article.content shouldNotBe null
    }

    test("should handle large documents asynchronously") {
      val paragraphs = (1..100).map { "<p>Paragraph $it with some content to make it longer.</p>" }
      val html = """
        <html>
          <body>
            <article>
              ${paragraphs.joinToString("\n")}
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parseAsync()

      article.content shouldNotBe null
      article.length shouldNotBe -1
    }
  }
})
