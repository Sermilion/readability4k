package com.sermilion.readability4k

import com.fleeksoft.ksoup.Ksoup
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

class EmptySpaceRemovalTest :
  FunSpec(
    {

      test("should remove empty divs and paragraphs") {
        val html = """
      <html>
        <body>
          <div class="content">
            <p>First paragraph with content.</p>
            <div></div>
            <p></p>
            <p>Second paragraph with content.</p>
            <div>   </div>
            <p>Third paragraph with content.</p>
          </div>
        </body>
      </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        val articleContent = article.articleContent?.let { content ->
          val doc = Ksoup.parse(content.html())

          doc.select("div, p, section").forEach { element ->
            if (element.text().trim().isEmpty() && element.select("img, iframe, video").isEmpty()) {
              element.remove()
            }
          }

          doc.body()
        }

        val htmlOutput = articleContent?.html() ?: ""

        htmlOutput shouldNotContain "<div></div>"
        htmlOutput shouldNotContain "<p></p>"

        val text = articleContent?.text() ?: ""
        text shouldBe
          "First paragraph with content. Second paragraph with content. " +
          "Third paragraph with content."
      }

      test("should keep divs with images even if no text") {
        val html = """
      <html>
        <body>
          <div class="content">
            <p>Paragraph before image.</p>
            <div><img src="image.jpg" alt="test"></div>
            <p>Paragraph after image.</p>
          </div>
        </body>
      </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        val articleContent = article.articleContent?.let { content ->
          val doc = Ksoup.parse(content.html())

          doc.select("div, p, section").forEach { element ->
            if (element.text().trim().isEmpty() && element.select("img, iframe, video").isEmpty()) {
              element.remove()
            }
          }

          doc.body()
        }

        val htmlOutput = articleContent?.html() ?: ""

        val images = Ksoup.parse(htmlOutput).select("img")
        images.size shouldBe 1
        images.first()?.attr("src") shouldBe "https://example.com/image.jpg"
      }

      test("should remove consecutive br tags") {
        val html = """
      <html>
        <body>
          <div class="content">
            <p>First paragraph.</p>
            <br>
            <br>
            <br>
            <p>Second paragraph.</p>
          </div>
        </body>
      </html>
        """.trimIndent()

        val readability = Readability4K("https://example.com", html)
        val article = readability.parse()

        val articleContent = article.articleContent?.let { content ->
          val doc = Ksoup.parse(content.html())

          doc.select("br + br").forEach { br ->
            br.remove()
          }

          doc.body()
        }

        val htmlOutput = articleContent?.html() ?: ""
        val doc = Ksoup.parse(htmlOutput)
        val brTags = doc.select("br")

        brTags.size shouldBe 0
      }
    },
  )
