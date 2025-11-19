package com.sermilion.readability4k

import com.fleeksoft.ksoup.Ksoup
import com.sermilion.readability4k.model.ReadabilityOptions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class Readability4KIntegrationTest :
  FunSpec({

    context("article extraction") {
      test("should extract article content from simple HTML") {
        val html = """
        <html>
          <head><title>Test Article</title></head>
          <body>
            <article>
              <h1>Main Article Title</h1>
              <p>This is the first paragraph with some content.</p>
              <p>This is the second paragraph with more content.</p>
              <p>This is the third paragraph to meet word threshold.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com/article", html)
        val article = readability.parse()

        article.title shouldNotBe null
        article.content shouldNotBe null
        article.textContent shouldContain "first paragraph"
        article.textContent shouldContain "second paragraph"
      }

      test("should handle nested article structure") {
        val html = """
        <html>
          <body>
            <div class="wrapper">
              <div class="container">
                <article>
                  <header>
                    <h1>Article Title</h1>
                  </header>
                  <section>
                    <p>First section content with sufficient text.</p>
                  </section>
                  <section>
                    <p>Second section content with more text.</p>
                  </section>
                </article>
              </div>
            </div>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.content shouldNotBe null
        article.textContent shouldContain "First section"
        article.textContent shouldContain "Second section"
      }

      test("should calculate article length correctly") {
        val html = """
        <html>
          <body>
            <article>
              <p>This is a test article with some content.</p>
              <p>It has multiple paragraphs.</p>
              <p>To ensure proper length calculation.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.length shouldNotBe -1
        article.length shouldNotBe 0
      }
    }

    context("metadata extraction") {
      test("should extract metadata from document") {
        val html = """
        <html>
          <head>
            <title>Article Title</title>
            <meta name="author" content="John Doe">
            <meta name="description" content="This is the article description">
            <meta charset="utf-8">
          </head>
          <body>
            <article>
              <p>Article content goes here with enough words to pass the threshold test.</p>
              <p>More content to ensure we have sufficient text for extraction.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.title shouldBe "Article Title"
        article.byline shouldBe "John Doe"
        article.excerpt shouldBe "This is the article description"
        article.charset shouldBe "UTF-8"
      }

      test("should extract text direction from document") {
        val html = """
        <html dir="rtl">
          <head>
            <title>Article Title</title>
          </head>
          <body>
            <article>
              <p>Article content with sufficient text for extraction.</p>
              <p>More content to meet word threshold.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.dir shouldBe "rtl"
      }

      test("should handle OpenGraph metadata") {
        val html = """
        <html>
          <head>
            <title>Page Title</title>
            <meta property="og:title" content="OG Title">
            <meta property="og:description" content="OG Description">
          </head>
          <body>
            <article>
              <p>Some article content with enough words to meet the minimum threshold for extraction.</p>
              <p>Additional content paragraph to ensure sufficient length.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.excerpt shouldBe "OG Description"
      }

      test("should handle Twitter Card metadata") {
        val html = """
        <html>
          <head>
            <title>Page Title</title>
            <meta name="twitter:title" content="Twitter Title">
            <meta name="twitter:description" content="Twitter Description">
          </head>
          <body>
            <article>
              <p>Article content with sufficient words for the readability algorithm to work properly.</p>
              <p>More content to meet threshold requirements.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.excerpt shouldBe "Twitter Description"
      }

      test("should extract excerpt from first paragraph when not in metadata") {
        val html = """
        <html>
          <head><title>Test</title></head>
          <body>
            <article>
              <p>This is the first paragraph that should become the excerpt.</p>
              <p>This is the second paragraph with more content.</p>
              <p>Third paragraph to meet word count.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.excerpt shouldContain "first paragraph"
      }
    }

    context("HTML cleanup") {
      test("should remove script tags") {
        val html = """
        <html>
          <body>
            <article>
              <p>Content before script.</p>
              <script>alert('test');</script>
              <p>Content after script with enough words to pass threshold.</p>
              <p>Additional content for word count requirements.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.content shouldNotContain "<script"
        article.content shouldNotContain "alert"
      }

      test("should remove style tags and attributes") {
        val html = """
        <html>
          <head>
            <style>body { color: red; }</style>
          </head>
          <body>
            <article>
              <p style="color: blue;">Styled paragraph with content.</p>
              <p>Another paragraph to meet word threshold.</p>
              <p>Third paragraph for sufficient content.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.content shouldNotContain "<style"
      }
    }

    context("URL handling") {
      test("should convert relative URLs to absolute") {
        val html = """
        <html>
          <body>
            <article>
              <p>Content with <a href="/relative/link">relative link</a>.</p>
              <p><img src="/images/photo.jpg" alt="photo"></p>
              <p>Additional content to meet word count threshold requirements.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.content shouldContain "https://example.com/relative/link"
        article.content shouldContain "https://example.com/images/photo.jpg"
      }
    }

    context("media handling") {
      test("should preserve important images") {
        val html = """
        <html>
          <body>
            <article>
              <h1>Article with Images</h1>
              <p>First paragraph before image.</p>
              <img src="https://example.com/main-image.jpg" alt="Main image">
              <p>Paragraph after image with sufficient content.</p>
              <p>Additional paragraph for word count.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        val doc = Ksoup.parse(article.content ?: "")
        val images = doc.select("img")

        images.size shouldBe 1
        images.first()?.attr("src") shouldContain "main-image.jpg"
      }

      test("should handle content with tables") {
        val html = """
        <html>
          <body>
            <article>
              <p>Article with a data table.</p>
              <table>
                <tr>
                  <th>Header 1</th>
                  <th>Header 2</th>
                </tr>
                <tr>
                  <td>Data 1</td>
                  <td>Data 2</td>
                </tr>
              </table>
              <p>Content after table with sufficient words.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        val doc = Ksoup.parse(article.content ?: "")
        val tables = doc.select("table")

        tables.size shouldNotBe 0
      }
    }

    context("encoding and special characters") {
      test("should handle UTF-8 encoding") {
        val html = """
        <html>
          <head>
            <meta charset="utf-8">
          </head>
          <body>
            <article>
              <p>Content with special characters: é, ñ, ü, 中文.</p>
              <p>Additional content to meet threshold.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        article.contentWithUtf8Encoding shouldContain "charset=\"utf-8\""
        article.textContent shouldContain "é"
        article.textContent shouldContain "中文"
      }
    }

    context("configuration") {
      test("should handle custom options") {
        val html = """
        <html>
          <body>
            <article>
              <p>Short content.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val options = ReadabilityOptions(
          maxElemsToParse = 1000,
          charThreshold = 2,
        )

        val readability = Readability4K("https://example.com", html, options)
        val article = readability.parse()

        article.content shouldNotBe null
      }
    }
  })
