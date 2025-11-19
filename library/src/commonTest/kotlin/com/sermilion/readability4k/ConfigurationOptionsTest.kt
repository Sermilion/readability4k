package com.sermilion.readability4k

import com.sermilion.readability4k.model.ReadabilityOptions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class ConfigurationOptionsTest : FunSpec({

  context("keepClasses option") {
    test("should remove classes when keepClasses is false") {
      val html = """
        <html>
          <body>
            <article>
              <div class="custom-class another-class">
                <p>Some content goes here. This is a paragraph with enough text to be considered an article.
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.
                Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
                Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
              </div>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(keepClasses = false, charThreshold = 100)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.content shouldNotBe null
      article.content shouldNotContain "custom-class"
      article.content shouldNotContain "another-class"
    }

    test("should keep classes when keepClasses is true") {
      val html = """
        <html>
          <body>
            <article>
              <div class="test-custom-class">
                <p class="test-para-class">Some content goes here. This is a paragraph with enough text to be considered an article.
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.
                Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
                Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
              </div>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(keepClasses = true, charThreshold = 100)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.content shouldNotBe null
      val hasClasses = article.content?.contains("class=") ?: false
      hasClasses shouldBe true
    }
  }

  context("disableJSONLD option") {
    test("should extract JSON-LD metadata when disableJSONLD is false") {
      val html = """
        <html>
          <head>
            <script type="application/ld+json">
            {
              "@context": "http://schema.org",
              "@type": "NewsArticle",
              "headline": "JSON-LD Title",
              "author": {"@type": "Person", "name": "JSON-LD Author"}
            }
            </script>
          </head>
          <body>
            <article>
              <p>Some content goes here. This is a paragraph with enough text to be considered an article.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.
              Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
              Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(disableJSONLD = false, charThreshold = 100)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.title shouldBe "JSON-LD Title"
      article.byline shouldBe "JSON-LD Author"
    }

    test("should not extract JSON-LD metadata when disableJSONLD is true") {
      val html = """
        <html>
          <head>
            <title>HTML Title</title>
            <meta name="author" content="Meta Author" />
            <script type="application/ld+json">
            {
              "@context": "http://schema.org",
              "@type": "NewsArticle",
              "headline": "JSON-LD Title",
              "author": {"@type": "Person", "name": "JSON-LD Author"}
            }
            </script>
          </head>
          <body>
            <article>
              <p>Some content goes here. This is a paragraph with enough text to be considered an article.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.
              Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
              Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(disableJSONLD = true, charThreshold = 100)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.title shouldBe "HTML Title"
      article.byline shouldBe "Meta Author"
    }
  }

  context("serializer option") {
    test("should use custom serializer when provided") {
      val html = """
        <html>
          <body>
            <article>
              <p>Some content goes here. This is a paragraph with enough text to be considered an article.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.
              Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
              Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val customSerializer: (com.fleeksoft.ksoup.nodes.Element) -> String = { element ->
        "CUSTOM: ${element.text()}"
      }

      val options = ReadabilityOptions(serializer = customSerializer, charThreshold = 100)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.content shouldNotBe null
      article.content shouldContain "CUSTOM:"
    }

    test("should use default serializer when not provided") {
      val html = """
        <html>
          <body>
            <article>
              <p>Some content goes here. This is a paragraph with enough text to be considered an article.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.
              Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
              Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(serializer = null, charThreshold = 100)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.content shouldNotBe null
      article.content shouldNotContain "CUSTOM:"
      article.content shouldContain "<p>"
    }
  }

  context("allowedVideoRegex option") {
    test("should preserve custom video embeds when allowedVideoRegex is provided") {
      val html = """
        <html>
          <body>
            <article>
              <p>Some content goes here. This is a paragraph with enough text to be considered an article.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.</p>
              <embed src="https://custom-video-platform.com/video/12345" />
              <p>Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
              Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(
        allowedVideoRegex = Regex("custom-video-platform"),
        charThreshold = 100,
      )
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.content shouldNotBe null
      article.content shouldContain "custom-video-platform"
    }

    test("should use default video patterns when allowedVideoRegex is not provided") {
      val html = """
        <html>
          <body>
            <article>
              <p>Some content goes here. This is a paragraph with enough text to be considered an article.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore.</p>
              <embed src="https://youtube.com/video/12345" />
              <p>Et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi.
              Ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(allowedVideoRegex = null, charThreshold = 100)
      val readability = Readability4K("https://example.com", html, options)
      val article = readability.parse()

      article.content shouldNotBe null
      article.content shouldContain "youtube"
    }
  }

  context("linkDensityModifier option") {
    test("should affect link density threshold with positive modifier") {
      val html = """
        <html>
          <body>
            <article>
              <div>
                <p>Some content with <a href="#">many</a> <a href="#">links</a> in <a href="#">it</a>.
                This is testing the link density modifier functionality with multiple links.
                Lorem ipsum dolor sit amet consectetur.</p>
              </div>
            </article>
          </body>
        </html>
      """.trimIndent()

      val optionsStrict = ReadabilityOptions(linkDensityModifier = 0.0, charThreshold = 50)
      val optionsLenient = ReadabilityOptions(linkDensityModifier = 0.5, charThreshold = 50)

      val readabilityStrict = Readability4K("https://example.com", html, optionsStrict)
      val readabilityLenient = Readability4K("https://example.com", html, optionsLenient)

      val articleStrict = readabilityStrict.parse()
      val articleLenient = readabilityLenient.parse()

      articleStrict.content shouldNotBe null
      articleLenient.content shouldNotBe null
    }
  }

  context("charThreshold option") {
    test("should work with low character threshold") {
      val html = """
        <html>
          <body>
            <article>
              <p>This is a moderately sized piece of content for testing.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val optionsLow = ReadabilityOptions(charThreshold = 10)
      val readability = Readability4K("https://example.com", html, optionsLow)
      val article = readability.parse()

      article.content shouldNotBe null
    }

    test("should extract article with sufficient content") {
      val longHtml = """
        <html>
          <body>
            <article>
              <p>This is a much longer piece of content that should definitely exceed the character threshold.
              Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore
              et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut
              aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum
              dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui
              officia deserunt mollit anim id est laborum. Sed ut perspiciatis unde omnis iste natus error sit
              voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore
              veritatis et quasi architecto beatae vitae dicta sunt explicabo.</p>
            </article>
          </body>
        </html>
      """.trimIndent()

      val options = ReadabilityOptions(charThreshold = 100)
      val readability = Readability4K("https://example.com", longHtml, options)
      val article = readability.parse()

      article.content shouldNotBe null
      article.length shouldNotBe null
    }
  }
})
