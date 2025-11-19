package com.sermilion.readability4k

import com.fleeksoft.ksoup.Ksoup
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IsProbablyReaderableTest : FunSpec({

  test("should return true for article with sufficient content") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title That Is Long Enough</h1>
            <p>This is a paragraph with enough content to be considered readable by the algorithm. It contains multiple sentences
            and provides substantial information for the reader to consume and understand the topic being discussed.</p>
            <p>Another paragraph with more content to ensure the article passes the readability threshold with flying colors.</p>
            <p>Even more content here to make sure we have a proper article structure that meets all requirements.</p>
            <p>Additional content to really ensure we pass the minimum score threshold.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe true
  }

  test("should return false for article with insufficient content") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Title</h1>
            <p>Short.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe false
  }

  test("should return false for navigation-heavy pages") {
    val html = """
      <html>
        <body>
          <div class="menu">
            <a href="#">Home</a>
            <a href="#">About</a>
            <a href="#">Contact</a>
          </div>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe false
  }

  test("should consider pre tags as content with sufficient text") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Code Example Article</h1>
            <p>This article contains code examples with substantial explanatory text.</p>
            <pre>
              function exampleFunction() {
                console.log("This is code with enough content to be considered readable by the algorithm");
                console.log("We need to add more content here to reach the threshold for readability");
                console.log("Additional logging statements and code to make this substantial");
                return true;
              }
            </pre>
            <p>More explanatory text after the code block to ensure readability.</p>
            <p>Additional content that makes this a substantial article.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe true
  }

  test("should respect custom minScore threshold") {
    val html = """
      <html>
        <body>
          <article>
            <p>Some content that might be borderline for readability checks with enough text to meet minimum length.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)

    isProbablyReaderable(doc, ReadableCheckOptions(minScore = 1, minContentLength = 50)) shouldBe true
    isProbablyReaderable(doc, ReadableCheckOptions(minScore = 100, minContentLength = 50)) shouldBe false
  }

  test("should respect custom minContentLength threshold") {
    val html = """
      <html>
        <body>
          <article>
            <p>This is a medium length paragraph with enough text to test the threshold.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)

    isProbablyReaderable(doc, ReadableCheckOptions(minContentLength = 10, minScore = 1)) shouldBe true
    isProbablyReaderable(doc, ReadableCheckOptions(minContentLength = 200, minScore = 1)) shouldBe false
  }

  test("should ignore hidden content") {
    val html = """
      <html>
        <body>
          <article style="display:none">
            <p>This is hidden content that should not count toward readability score. It has plenty
            of text but it's not visible to the user.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe false
  }

  test("should ignore content with aria-hidden") {
    val html = """
      <html>
        <body>
          <article aria-hidden="true">
            <p>This content is marked as aria-hidden and should not count. Even though it has
            enough text, it's meant to be hidden from accessibility tools.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe false
  }

  test("should filter out unlikely candidates") {
    val html = """
      <html>
        <body>
          <div class="banner">
            <p>This is banner text with plenty of content but it should be filtered out because
            it's in a banner class which is typically used for ads or navigation.</p>
          </div>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe false
  }

  test("should consider divs with br tags") {
    val html = """
      <html>
        <body>
          <div>
            This is a paragraph-like div with br tags<br>
            It contains multiple lines of content<br>
            And should be considered for readability scoring<br>
            Because it has enough content to be meaningful<br>
          </div>
        </body>
      </html>
    """.trimIndent()

    val doc = Ksoup.parse(html)
    isProbablyReaderable(doc) shouldBe true
  }
})
