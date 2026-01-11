package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class NextJsImagesTest : FunSpec({

  test("should extract real image URL from Next.js optimized src") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="/_next/image?url=https%3A%2F%2Fexample.com%2Fimage.jpg&w=640&q=75" alt="Test image" />
            <p>Article content here with enough text to be extracted properly.</p>
            <p>More content to ensure proper scoring.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/image.jpg"
    article.content shouldNotContain "_next/image"
  }

  test("should extract real image URLs from Next.js optimized srcset") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img srcset="/_next/image?url=https%3A%2F%2Fexample.com%2Fimage.jpg&w=640&q=75 640w, /_next/image?url=https%3A%2F%2Fexample.com%2Fimage.jpg&w=750&q=75 750w"
                 src="/_next/image?url=https%3A%2F%2Fexample.com%2Fimage.jpg&w=640&q=75"
                 alt="Test image" />
            <p>Article content here with enough text to be extracted properly.</p>
            <p>More content to ensure proper scoring.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/image.jpg"
    article.content shouldNotContain "_next/image"
  }

  test("should handle both Next.js src and srcset") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="/_next/image?url=https%3A%2F%2Fexample.com%2Fimage.jpg&w=3840&q=75"
                 srcset="/_next/image?url=https%3A%2F%2Fexample.com%2Fimage.jpg&w=640&q=75 640w, /_next/image?url=https%3A%2F%2Fexample.com%2Fimage.jpg&w=1920&q=75 1920w"
                 alt="Test image" />
            <p>Article content here with enough text to be extracted properly.</p>
            <p>More content to ensure proper scoring.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/image.jpg"
    article.content shouldNotContain "_next/image"
  }

  test("should not modify images with regular URLs") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="https://example.com/regular-image.jpg" alt="Regular image" />
            <p>Article content here with enough text to be extracted properly.</p>
            <p>More content to ensure proper scoring.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/regular-image.jpg"
  }

  test("should handle Interesting Engineering article with Next.js images") {
    val html = """
      <html>
        <body>
          <article>
            <h1>200,000 satellites: China seeks approval</h1>
            <img alt="China has submitted plans to deploy up to 200,000 satellites"
                 width="1920"
                 height="1080"
                 src="/_next/image?url=https%3A%2F%2Fcms.interestingengineering.com%2Fwp-content%2Fuploads%2F2026%2F01%2Fimage-1920x1080-2026-01-11T174651.359.png&w=3840&q=75"
                 srcset="/_next/image?url=https%3A%2F%2Fcms.interestingengineering.com%2Fwp-content%2Fuploads%2F2026%2F01%2Fimage-1920x1080-2026-01-11T174651.359.png&w=640&q=75 640w, /_next/image?url=https%3A%2F%2Fcms.interestingengineering.com%2Fwp-content%2Fuploads%2F2026%2F01%2Fimage-1920x1080-2026-01-11T174651.359.png&w=1920&q=75 1920w" />
            <p>China has submitted plans to deploy up to 200,000 satellites into orbit.</p>
            <p>This is in response to collision risks posed by SpaceX's Starlink network.</p>
            <p>The massive satellite constellation would be one of the largest ever proposed.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://interestingengineering.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://cms.interestingengineering.com/wp-content/uploads/2026/01/image-1920x1080-2026-01-11T174651.359.png"
    article.content shouldNotContain "_next/image"
  }

  test("should handle mixed Next.js and regular images") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="/_next/image?url=https%3A%2F%2Fexample.com%2Fnextjs-image.jpg&w=640&q=75" alt="Next.js image" />
            <p>Article content here with enough text.</p>
            <img src="https://example.com/regular-image.jpg" alt="Regular image" />
            <p>More content to ensure proper scoring.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/nextjs-image.jpg"
    article.content shouldContain "https://example.com/regular-image.jpg"
    article.content shouldNotContain "_next/image"
  }

  test("should handle empty or missing src attributes") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img alt="No src" />
            <p>Article content here with enough text to be extracted properly.</p>
            <p>More content to ensure proper scoring.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
  }

  test("should decode URL-encoded characters in image URLs") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <img src="/_next/image?url=https%3A%2F%2Fexample.com%2Fpath%2Fto%2Fimage%20with%20spaces.jpg&w=640&q=75" alt="Test" />
            <p>Article content here with enough text to be extracted properly.</p>
            <p>More content to ensure proper scoring.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "https://example.com/path/to/image with spaces.jpg"
  }
})
