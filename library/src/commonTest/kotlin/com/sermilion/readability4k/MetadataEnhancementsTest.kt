package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class MetadataEnhancementsTest : FunSpec({

  context("JSON-LD metadata parsing") {
    test("should extract metadata from JSON-LD script tag") {
      val html = """
        <html>
          <head>
            <script type="application/ld+json">
            {
              "@context": "https://schema.org",
              "@type": "NewsArticle",
              "headline": "Article Headline from JSON-LD",
              "name": "Article Name",
              "author": {
                "name": "John Doe"
              },
              "description": "This is the article description",
              "publisher": {
                "name": "Example Publisher"
              },
              "datePublished": "2024-01-15"
            }
            </script>
          </head>
          <body>
            <article>
              <p>Article content here with enough text to be extracted properly. This needs to be substantial content.</p>
              <p>More content to ensure extraction works correctly with enough words and sentences.</p>
              <p>Additional paragraphs to make sure the article passes the word threshold for extraction.</p>
              <p>Even more content to ensure we have a robust article that will definitely be extracted.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.byline.shouldNotBeNull()
      article.byline shouldBe "John Doe"
      article.excerpt.shouldNotBeNull()
      article.siteName.shouldNotBeNull()
      article.publishedTime.shouldNotBeNull()
    }

    test("should handle BlogPosting type") {
      val html = """
        <html>
          <head>
            <script type="application/ld+json">
            {
              "@type": "BlogPosting",
              "headline": "Blog Post Title",
              "author": "Jane Smith"
            }
            </script>
          </head>
          <body>
            <article>
              <p>Blog post content here with substantial text to ensure proper extraction by the algorithm.</p>
              <p>More paragraphs with meaningful content that helps the article pass the extraction threshold.</p>
              <p>Additional content to make sure this blog post is recognized as a valid article.</p>
              <p>Even more content to ensure we meet all the requirements for successful extraction.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.byline.shouldNotBeNull()
    }
  }

  context("Open Graph metadata") {
    test("should extract siteName from og:site_name") {
      val html = """
        <html>
          <head>
            <meta property="og:site_name" content="Example News Site" />
            <meta property="og:title" content="Article Title" />
          </head>
          <body>
            <article>
              <p>Article content here with enough text for proper extraction and recognition by the algorithm.</p>
              <p>More content to ensure the article is extracted correctly with all metadata preserved.</p>
              <p>Additional paragraphs that add substance to the article and ensure it passes thresholds.</p>
              <p>Even more content to make absolutely sure this article will be successfully extracted.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.siteName.shouldNotBeNull()
    }

    test("should extract publishedTime from article:published_time") {
      val html = """
        <html>
          <head>
            <meta property="article:published_time" content="2024-01-15T10:30:00Z" />
          </head>
          <body>
            <article>
              <h1>Article Title</h1>
              <p>Article content here with enough text to be extracted properly by the readability algorithm.</p>
              <p>More content to ensure proper extraction with sufficient word count and meaningful text.</p>
              <p>Additional paragraphs that make this a substantial article worth extracting and processing.</p>
              <p>Even more content to guarantee the article passes all extraction thresholds successfully.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.publishedTime.shouldNotBeNull()
    }
  }

  context("HTML entity unescaping") {
    test("should unescape HTML entities in title") {
      val html = """
        <html>
          <head>
            <title>Article &amp; Title &quot;Quoted&quot;</title>
          </head>
          <body>
            <article>
              <p>Article content here.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.title shouldBe "Article & Title \"Quoted\""
    }

    test("should unescape HTML entities in byline") {
      val html = """
        <html>
          <head>
            <meta name="author" content="John &amp; Jane Doe" />
          </head>
          <body>
            <article>
              <h1>Article Title</h1>
              <p>Article content here.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.byline shouldBe "John & Jane Doe"
    }
  }

  context("Language detection") {
    test("should extract language from html lang attribute") {
      val html = """
        <html lang="en-US">
          <body>
            <article>
              <h1>Article Title</h1>
              <p>Article content here.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.lang shouldBe "en-US"
    }

    test("should handle missing language attribute") {
      val html = """
        <html>
          <body>
            <article>
              <h1>Article Title</h1>
              <p>Article content here.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article shouldNotBe null
      article.lang shouldBe null
    }
  }

  context("Title deduplication") {
    test("should remove H1 that duplicates title") {
      val html = """
        <html>
          <head>
            <title>Complete Article Title</title>
          </head>
          <body>
            <article>
              <h1>Complete Article Title</h1>
              <p>Article content starts here with actual information.</p>
              <p>More content to ensure proper extraction.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article.shouldNotBeNull()
      article.title shouldBe "Complete Article Title"
      article.textContent?.shouldNotBeNull()
    }

    test("should remove H2 that duplicates title") {
      val html = """
        <html>
          <head>
            <title>Article Title</title>
          </head>
          <body>
            <article>
              <h2>Article Title</h2>
              <p>Article content starts here.</p>
              <p>More content for extraction.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article.shouldNotBeNull()
      article.title shouldBe "Article Title"
    }

    test("should keep headers that do not duplicate title") {
      val html = """
        <html>
          <head>
            <title>Main Article Title</title>
          </head>
          <body>
            <article>
              <h2>Section Heading</h2>
              <p>Article content here.</p>
              <h2>Another Section</h2>
              <p>More content here.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val readability = Readability4K("https://example.com", html)
      val article = readability.parse()

      article.shouldNotBeNull()
      article.content shouldContain "Section Heading"
      article.content shouldContain "Another Section"
    }
  }
})
