package com.sermilion.readability4k

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import com.sermilion.readability4k.model.ReadabilityOptions

class EdgeCaseTest :
  FunSpec(
    {

      context("empty or minimal content") {
        test("should handle empty HTML gracefully") {
          val html = ""

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
          article.uri shouldBe "https://example.com"
        }

        test("should handle HTML with only whitespace") {
          val html = "   \n\n   "

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle HTML without body tag") {
          val html = """
        <html>
          <head><title>Test</title></head>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle very short content") {
          val html = """
        <html>
          <body>
            <p>Short.</p>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle content below word threshold") {
          val html = """
        <html>
          <body>
            <article>
              <p>Too short content.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val options = ReadabilityOptions(wordThreshold = 100)
          val readability = Readability4K("https://example.com", html, options)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle content with only images and no text") {
          val html = """
        <html>
          <body>
            <article>
              <img src="image1.jpg" alt="Image 1">
              <img src="image2.jpg" alt="Image 2">
              <img src="image3.jpg" alt="Image 3">
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }
      }

      context("configuration limits") {
        test("should throw when exceeding max elements") {
          val html = buildString {
            append("<html><body>")
            repeat(1100) {
              append("<div>Content $it</div>")
            }
            append("</body></html>")
          }

          val options = ReadabilityOptions(maxElemsToParse = 1000)
          val readability = Readability4K("https://example.com", html, options)

          shouldThrow<MaxElementsExceededException> {
            readability.parse()
          }
        }
      }

      context("malformed HTML") {
        test("should handle malformed HTML") {
          val html = """
        <html>
          <body>
            <p>Unclosed paragraph
            <div>Unclosed div
            <article>
              <p>Some content here with enough words.</p>
              <p>More content to pass threshold.</p>
          </body>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
          article.content shouldNotBe null
        }

        test("should handle deeply nested structure") {
          val html = buildString {
            append("<html><body>")
            repeat(20) { i ->
              append("<div class='level-$i'>")
            }
            append("<article><p>Deep content with sufficient text for extraction.</p>")
            append("<p>Additional paragraph to meet requirements.</p></article>")
            repeat(20) {
              append("</div>")
            }
            append("</body></html>")
          }

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
          article.textContent shouldNotBe null
        }

        test("should handle comments in HTML") {
          val html = """
        <html>
          <body>
            <!-- This is a comment -->
            <article>
              <!-- Another comment -->
              <p>Content with comments and sufficient text length.</p>
              <p>More content here to pass threshold.</p>
            </article>
            <!-- Final comment -->
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle CDATA sections") {
          val html = """
        <html>
          <body>
            <article>
              <p>Content before CDATA with enough text.</p>
              <![CDATA[This is CDATA content]]>
              <p>Content after CDATA with more text.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }
      }

      context("special URL handling") {
        test("should handle special characters in URL") {
          val html = """
        <html>
          <body>
            <article>
              <p>Content with enough text for extraction to work properly.</p>
              <p>Additional content paragraph.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com/article?id=123&lang=en", html)
          val article = readability.parse()

          article shouldNotBe null
          article.uri shouldBe "https://example.com/article?id=123&lang=en"
        }

        test("should handle JavaScript URLs in links") {
          val html = """
        <html>
          <body>
            <article>
              <p>Content with <a href="javascript:void(0)">JS link</a>.</p>
              <p>More content with enough text for extraction.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }
      }

      context("text direction and encoding") {
        test("should handle content with mixed RTL and LTR text") {
          val html = """
        <html>
          <body dir="rtl">
            <article>
              <p>مرحبا Hello this is mixed content with sufficient length.</p>
              <p>Additional paragraph to meet word threshold.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
          article.textContent shouldNotBe null
        }
      }

      context("complex structures") {
        test("should handle content with many links") {
          val html = """
        <html>
          <body>
            <article>
              <p>
                <a href="/link1">Link 1</a>
                <a href="/link2">Link 2</a>
                <a href="/link3">Link 3</a>
                Some actual content here with enough words to pass.
              </p>
              <p>More content without links to balance out.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle content with iframes") {
          val html = """
        <html>
          <body>
            <article>
              <p>Content before iframe with sufficient text.</p>
              <iframe src="https://example.com/embed" width="560" height="315"></iframe>
              <p>Content after iframe with more text.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle multiple article tags") {
          val html = """
        <html>
          <body>
            <article>
              <p>First article with substantial content for testing.</p>
            </article>
            <article>
              <p>Second article with even more content here.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
          article.content shouldNotBe null
        }

        test("should handle content with forms") {
          val html = """
        <html>
          <body>
            <article>
              <p>Article content before form with sufficient text.</p>
              <form action="/submit">
                <input type="text" name="email">
                <button>Submit</button>
              </form>
              <p>Article content after form with more text.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }

        test("should handle noscript tags") {
          val html = """
        <html>
          <body>
            <article>
              <p>Content with noscript and enough text for extraction.</p>
              <noscript>
                <img src="fallback.jpg" alt="Fallback">
              </noscript>
              <p>More content after noscript tag.</p>
            </article>
          </body>
        </html>
          """.trimIndent()

          val readability = Readability4K("https://example.com", html)
          val article = readability.parse()

          article shouldNotBe null
        }
      }

      test("parseAsync should work with coroutines") {
        val html = """
        <html>
          <head><title>Async Test</title></head>
          <body>
            <article>
              <h1>Testing Async Parsing</h1>
              <p>This tests the suspend variant of parse.</p>
            </article>
          </body>
        </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parseAsync() // Suspend function

        article shouldNotBe null
        article.title shouldBe "Async Test"
        article.textContent shouldContain "Testing Async Parsing"
      }
    },
  )
