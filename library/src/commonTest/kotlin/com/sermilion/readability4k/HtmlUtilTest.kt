package com.sermilion.readability4k

import com.sermilion.readability4k.util.HtmlUtil
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

class HtmlUtilTest : FunSpec({

  context("unescapeHtmlEntities") {
    test("should return null for null input") {
      HtmlUtil.unescapeHtmlEntities(null) shouldBe null
    }

    test("should unescape common HTML entities") {
      HtmlUtil.unescapeHtmlEntities("&quot;Hello&quot;") shouldBe "\"Hello\""
      HtmlUtil.unescapeHtmlEntities("&lt;div&gt;") shouldBe "<div>"
      HtmlUtil.unescapeHtmlEntities("&amp;") shouldBe "&"
      HtmlUtil.unescapeHtmlEntities("&apos;") shouldBe "'"
    }

    test("should unescape special HTML entities") {
      HtmlUtil.unescapeHtmlEntities("&copy; 2024") shouldBe "© 2024"
      HtmlUtil.unescapeHtmlEntities("&reg;") shouldBe "®"
      HtmlUtil.unescapeHtmlEntities("&nbsp;") shouldBe " "
    }

    test("should unescape numeric HTML entities") {
      HtmlUtil.unescapeHtmlEntities("&#65;") shouldBe "A"
      HtmlUtil.unescapeHtmlEntities("&#97;") shouldBe "a"
      HtmlUtil.unescapeHtmlEntities("&#169;") shouldBe "©"
    }

    test("should unescape hexadecimal HTML entities") {
      HtmlUtil.unescapeHtmlEntities("&#x41;") shouldBe "A"
      HtmlUtil.unescapeHtmlEntities("&#x61;") shouldBe "a"
      HtmlUtil.unescapeHtmlEntities("&#xA9;") shouldBe "©"
    }

    test("should handle mixed entities in text") {
      val input = "John &amp; Jane &lt;email@example.com&gt;"
      val expected = "John & Jane <email@example.com>"
      HtmlUtil.unescapeHtmlEntities(input) shouldBe expected
    }

    test("should handle text without entities") {
      HtmlUtil.unescapeHtmlEntities("Hello World") shouldBe "Hello World"
    }
  }

  context("textSimilarity") {
    test("should return 1.0 for identical texts") {
      HtmlUtil.textSimilarity("Hello World", "Hello World") shouldBe 1.0
    }

    test("should return 1.0 for identical texts with different cases") {
      HtmlUtil.textSimilarity("Hello World", "hello world") shouldBe 1.0
    }

    test("should return 0.0 for completely different texts") {
      HtmlUtil.textSimilarity("Hello", "Goodbye") shouldBe 0.0
    }

    test("should return high similarity for similar texts") {
      val similarity = HtmlUtil.textSimilarity(
        "Mozilla Readability",
        "Mozilla Readability Library",
      )
      similarity shouldBeGreaterThan 0.5
    }

    test("should return low similarity for mostly different texts") {
      val similarity = HtmlUtil.textSimilarity(
        "Complete Article Title",
        "Something Else",
      )
      similarity shouldBeLessThan 0.3
    }

    test("should handle empty strings") {
      HtmlUtil.textSimilarity("", "") shouldBe 0.0
      HtmlUtil.textSimilarity("Hello", "") shouldBe 0.0
      HtmlUtil.textSimilarity("", "World") shouldBe 0.0
    }

    test("should calculate similarity based on unique tokens") {
      val similarity = HtmlUtil.textSimilarity(
        "The Quick Brown Fox",
        "The Quick Brown Dog",
      )
      similarity shouldBeGreaterThan 0.5
    }

    test("should ignore extra whitespace") {
      HtmlUtil.textSimilarity("Hello  World", "Hello World") shouldBe 1.0
    }
  }
})
