package com.sermilion.readability4k

import com.sermilion.readability4k.model.ReadabilityOptions
import com.sermilion.readability4k.transformer.RedditUrlTransformer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest

class UrlTransformerTest :
  FunSpec({

    test("should transform new Reddit URL to old Reddit") {
      runTest {
        val html = "<html><body><p>Test content</p></body></html>"
        val newRedditUrl = "https://www.reddit.com/r/test/comments/123/title/"

        val readability = Readability4K(newRedditUrl, html)

        readability.parse().uri shouldBe "https://old.reddit.com/r/test/comments/123/title/"
      }
    }

    test("should not transform old Reddit URL") {
      runTest {
        val html = "<html><body><p>Test content</p></body></html>"
        val oldRedditUrl = "https://old.reddit.com/r/test/comments/123/title/"

        val readability = Readability4K(oldRedditUrl, html)

        readability.parse().uri shouldBe "https://old.reddit.com/r/test/comments/123/title/"
      }
    }

    test("should handle reddit.com without www") {
      runTest {
        val html = "<html><body><p>Test content</p></body></html>"
        val url = "https://reddit.com/r/test/comments/123/title/"

        val readability = Readability4K(url, html)

        readability.parse().uri shouldBe "https://old.reddit.com/r/test/comments/123/title/"
      }
    }

    test("should allow disabling URL transformation") {
      runTest {
        val html = "<html><body><p>Test content</p></body></html>"
        val newRedditUrl = "https://www.reddit.com/r/test/comments/123/title/"

        val options = ReadabilityOptions(urlTransformers = emptyList())
        val readability = Readability4K(newRedditUrl, html, options)

        readability.parse().uri shouldBe "https://www.reddit.com/r/test/comments/123/title/"
      }
    }

    test("should allow custom URL transformers") {
      runTest {
        val html = "<html><body><p>Test content</p></body></html>"
        val url = "http://example.com/test"

        val customTransformer = object : com.sermilion.readability4k.transformer.UrlTransformer {
          override val priority: Int = 50
          override fun transform(url: String): String = url.replace("http://", "https://")
        }

        val options = ReadabilityOptions(urlTransformers = listOf(customTransformer))
        val readability = Readability4K(url, html, options)

        readability.parse().uri shouldBe "https://example.com/test"
      }
    }

    test("should apply transformers in priority order") {
      runTest {
        val html = "<html><body><p>Test content</p></body></html>"
        val url = "http://reddit.com/r/test/comments/123/title/"

        val httpTransformer = object : com.sermilion.readability4k.transformer.UrlTransformer {
          override val priority: Int = 10
          override fun transform(url: String): String = url.replace("http://", "https://")
        }

        val options = ReadabilityOptions(
          urlTransformers = listOf(RedditUrlTransformer(), httpTransformer),
        )
        val readability = Readability4K(url, html, options)

        readability.parse().uri shouldBe "https://old.reddit.com/r/test/comments/123/title/"
      }
    }

    test("RedditUrlTransformer should have correct priority") {
      val transformer = RedditUrlTransformer()
      transformer.priority shouldBe 100
    }
  })
