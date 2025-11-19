package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class LinkDensityTest : FunSpec({

  test("should handle hash URLs with reduced weight") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article Title</h1>
            <p>This is content with <a href="#section1">internal link</a> that should have reduced impact.</p>
            <p>More content here with <a href="#section2">another hash link</a> for testing.</p>
            <p>Even more content to ensure proper extraction works correctly.</p>
            <p>Additional paragraph with regular content and no links at all.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "internal link"
    article.content shouldContain "another hash link"
  }

  test("should penalize external link-heavy content") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Real Article Title</h1>
            <p>This is actual article content with valuable information.</p>
            <p>More valuable content that makes this a proper article.</p>
            <p>Additional meaningful content here.</p>
          </article>
          <div class="sidebar">
            <p>
              <a href="https://external.com/1">External Link 1</a>
              <a href="https://external.com/2">External Link 2</a>
              <a href="https://external.com/3">External Link 3</a>
              <a href="https://external.com/4">External Link 4</a>
            </p>
          </div>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "actual article content"
    article.content shouldNotContain "External Link 1"
  }

  test("should allow reasonable amount of contextual links") {
    val html = """
      <html>
        <body>
          <article>
            <h1>Article About Technology</h1>
            <p>
              Modern web development relies on many technologies including
              <a href="https://example.com/html">HTML</a>,
              <a href="https://example.com/css">CSS</a>, and
              <a href="https://example.com/js">JavaScript</a>.
            </p>
            <p>This article discusses these technologies in depth, providing examples
            and best practices for developers.</p>
            <p>Additional content that makes this a substantial article worth reading.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldContain "Modern web development"
  }
})
