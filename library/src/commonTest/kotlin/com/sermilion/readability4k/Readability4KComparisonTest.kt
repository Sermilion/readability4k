package com.sermilion.readability4k

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlin.test.Test

class Readability4KComparisonTest {

  @Test
  fun `should extract article from simple HTML`() {
    val html = """
      <!DOCTYPE html>
      <html>
        <head>
          <title>Test Article Title</title>
          <meta name="author" content="John Doe">
          <meta name="description" content="This is a test article">
        </head>
        <body>
          <div class="header">Header content</div>
          <div class="content">
            <h1>Main Article Heading</h1>
            <p>This is the first paragraph of the article. It contains meaningful content.</p>
            <p>This is the second paragraph with more content. It provides additional information.</p>
            <p>Third paragraph continues the article with even more details.</p>
          </div>
          <div class="sidebar">Sidebar content</div>
          <div class="footer">Footer content</div>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    article.title shouldNotBe null
    article.title shouldContain "Test Article"

    article.byline shouldBe "John Doe"
    article.excerpt shouldBe "This is a test article"

    article.articleContent shouldNotBe null
    article.articleContent?.html()?.shouldNotBeEmpty()
    article.articleContent?.text() shouldContain "first paragraph"
    article.articleContent?.text() shouldContain "second paragraph"
    article.articleContent?.text() shouldContain "Third paragraph"
  }

  @Test
  fun `should handle article with metadata`() {
    val html = """
      <!DOCTYPE html>
      <html>
        <head>
          <title>Amazing Article - Site Name</title>
          <meta property="og:title" content="Amazing Article">
          <meta property="og:description" content="An amazing article about testing">
          <meta name="author" content="Jane Smith">
        </head>
        <body>
          <article>
            <h1>Amazing Article</h1>
            <p>First paragraph with substantial content to make it count.</p>
            <p>Second paragraph also with good amount of text content.</p>
            <p>Third paragraph to ensure we have enough content.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article2", html)
    val article = readability.parse()

    article.title shouldContain "Amazing Article"
    article.excerpt shouldBe "An amazing article about testing"
    article.byline shouldBe "Jane Smith"
    article.articleContent shouldNotBe null
  }

  @Test
  fun `should extract complex article with multiple elements`() {
    val html = """
      <!DOCTYPE html>
      <html>
        <head>
          <title>Complex Article</title>
        </head>
        <body>
          <nav>Navigation</nav>
          <main>
            <article>
              <h1>Complex Article Title</h1>
              <p>Introduction paragraph with enough text to be meaningful.</p>
              <h2>Section 1</h2>
              <p>Content for section 1 with detailed information.</p>
              <p>More content for section 1 to ensure proper extraction.</p>
              <h2>Section 2</h2>
              <p>Content for section 2 with additional details.</p>
              <blockquote>A quote within the article</blockquote>
              <p>Final paragraph wrapping up the article content.</p>
            </article>
          </main>
          <aside>Sidebar</aside>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/complex", html)
    val article = readability.parse()

    article.articleContent shouldNotBe null
    val content = article.articleContent?.text() ?: ""

    content shouldContain "Introduction paragraph"
    content shouldContain "Section 1"
    content shouldContain "Section 2"
    content shouldContain "quote within the article"
    content shouldContain "Final paragraph"
  }

  @Test
  fun `should remove boilerplate and keep main content`() {
    val html = """
      <!DOCTYPE html>
      <html>
        <body>
          <div class="advertisement">Ad content here</div>
          <div class="main-content">
            <h1>Main Article</h1>
            <p>This is the main article content with substantial text.</p>
            <p>More main content that should be preserved in output.</p>
            <p>Additional paragraph to ensure we have enough content.</p>
          </div>
          <div class="comments">
            <p>Comment 1</p>
            <p>Comment 2</p>
          </div>
        </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    val content = article.articleContent?.text() ?: ""
    content shouldContain "main article content"
    content shouldContain "More main content"
  }
}
