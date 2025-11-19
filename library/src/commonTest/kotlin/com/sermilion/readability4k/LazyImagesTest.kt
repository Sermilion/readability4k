package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class LazyImagesTest : FunSpec({

  test("should fix lazy-loaded images with data-src attribute") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img data-src="https://example.com/image.jpg" src="data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==" />
            <p>Article content here</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/image.jpg"
  }

  test("should fix lazy-loaded images with data-srcset attribute") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img data-srcset="https://example.com/image.jpg 1x, https://example.com/image@2x.jpg 2x"
                 src="data:image/gif;base64,placeholder" />
            <p>Article content here</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/image.jpg"
  }

  test("should handle figure elements with lazy-loaded images") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <figure data-src="https://example.com/figure-image.jpg">
              <figcaption>Image caption</figcaption>
            </figure>
            <p>Article content here with multiple paragraphs for better score.</p>
            <p>More content to ensure the article is extracted properly.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/figure-image.jpg"
  }

  test("should not modify images that already have valid src") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="https://example.com/valid-image.jpg" alt="Valid image" />
            <p>Article content here</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/valid-image.jpg"
  }
})
