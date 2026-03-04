package com.sermilion.readability4k.cli

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MainTest {

  @Test
  fun `escapeJson escapes reserved characters`() {
    val escaped = escapeJson("quote=\"x\"\npath\\to\tvalue")

    assertEquals("quote=\\\"x\\\"\\npath\\\\to\\tvalue", escaped)
  }

  @Test
  fun `extractArticle returns parsed article fields`() {
    val html = """
      <html>
        <head><title>CLI Test Title</title></head>
        <body>
          <article>
            <p>This is a paragraph with enough content for extraction.</p>
            <p>This is another paragraph to exceed thresholds.</p>
          </article>
        </body>
      </html>
    """.trimIndent()

    val article = extractArticle("https://example.com/article", html, charThreshold = 10)

    assertEquals("https://example.com/article", article.url)
    assertEquals("CLI Test Title", article.title)
    assertNotNull(article.content)
    assertTrue(article.length > 0)
  }

  @Test
  fun `printJsonOutput includes expected keys`() {
    val article = ArticleResult(
      url = "https://example.com",
      title = "Title",
      byline = "Author",
      content = "<p>content</p>",
      textContent = "content",
      length = 7,
      excerpt = "Excerpt",
      siteName = "Example",
      lang = "en",
      publishedTime = "2026-01-01",
    )

    val output = captureStdout {
      printJsonOutput(article)
    }

    assertContains(output, "\"url\": \"https://example.com\"")
    assertContains(output, "\"title\": \"Title\"")
    assertContains(output, "\"textContent\": \"content\"")
  }

  private fun captureStdout(block: () -> Unit): String {
    val originalOut = System.out
    val outputStream = ByteArrayOutputStream()
    System.setOut(PrintStream(outputStream))
    return try {
      block()
      outputStream.toString()
    } finally {
      System.setOut(originalOut)
    }
  }
}
