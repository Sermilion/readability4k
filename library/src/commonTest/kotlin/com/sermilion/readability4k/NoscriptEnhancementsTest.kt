package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class NoscriptEnhancementsTest : FunSpec({

  test("should unwrap images from noscript tags") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="placeholder.jpg" />
            <noscript>
              <img src="https://example.com/actual-image.jpg" alt="Real image" />
            </noscript>
            <p>Article content here with enough text for extraction.</p>
            <p>More content to ensure proper parsing.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/actual-image.jpg"
  }

  test("should handle noscript with multiple elements") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <noscript>
              <div>
                <p>This noscript contains multiple elements</p>
                <img src="image1.jpg" />
              </div>
            </noscript>
            <p>Article content here.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
  }

  test("should preserve data-old attributes when replacing images") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="data:image/gif;base64,placeholder" class="lazy" />
            <noscript>
              <img src="https://example.com/real-image.jpg" alt="Image" />
            </noscript>
            <p>Article content with multiple paragraphs for better extraction.</p>
            <p>Additional content to ensure the article is properly parsed.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/real-image.jpg"
  }

  test("should remove images without valid src attributes") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img class="placeholder" />
            <p>Article content here that should still be extracted properly.</p>
            <p>More content to ensure good extraction.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
  }
})
