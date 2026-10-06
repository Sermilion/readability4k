package com.sermilion.readability4k

import com.sermilion.readability4k.model.Article
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class AuthorBioDisclaimerRemovalTest :
  FunSpec(
    {

      val body =
        "The commission said chat assistants can assemble advertising profiles from ordinary conversations. " +
          "Publishers were told to treat those profiles as personal data and to keep the ruling at the center " +
          "of the report for readers who follow consumer-privacy cases."

      fun parseArticle(innerHtml: String): Article {
        val html = """
          <html>
            <body>
              <article>
                $innerHtml
              </article>
            </body>
          </html>
        """.trimIndent()
        return Readability4K("https://example.com", html).parse()
      }

      fun Article.shouldKeep(vararg snippets: String) {
        articleContent shouldNotBe null
        val text = articleContent!!.text()
        val html = content.orEmpty()
        snippets.forEach { snippet ->
          text shouldContain snippet
          html shouldContain snippet
        }
      }

      fun Article.shouldDrop(vararg snippets: String) {
        val text = articleContent!!.text()
        val html = content.orEmpty()
        snippets.forEach { snippet ->
          text shouldNotContain snippet
          html shouldNotContain snippet
        }
      }

      test("named author headings drop the bio section and keep later same-rank headings") {
        val cases = listOf(
          Triple(
            "About the author",
            "Jane covers consumer gadgets and writes a weekly column on firmware.",
            true,
          ),
          Triple(
            "About author",
            "Sam reviews laptops and tracks battery-life claims for readers.",
            false,
          ),
          Triple(
            "Author bio",
            "Riley documents chipset changes and thermal limits in notebooks.",
            false,
          ),
        )

        cases.forEach { (heading, bio, includeLaterSection) ->
          val later = "Related coverage appears in the next briefing for subscribers."
          val inner = buildString {
            append("<p>$body</p>")
            append("<h2>$heading</h2>")
            append("<p>$bio</p>")
            if (includeLaterSection) {
              append("<h2>Further reading</h2>")
              append("<p>$later</p>")
            }
          }
          val article = parseArticle(inner)
          article.shouldKeep(body)
          article.shouldDrop(bio)
          if (includeLaterSection) {
            article.shouldKeep(later, "Further reading")
            article.content.orEmpty() shouldNotContain heading
          }
        }
      }

      test("compound author-bio class removes nested heading and image") {
        val bio =
          "I write about IT security and artificial intelligence for everyday users of consumer devices."
        val nestedHeading = "Staff writer"
        val article = parseArticle(
          """
            <p>$body</p>
            <div class="author-bio">
              <h3>$nestedHeading</h3>
              <img src="https://example.com/portrait.jpg" alt="Staff writer">
              <p>$bio</p>
            </div>
          """.trimIndent(),
        )

        article.shouldKeep(body)
        article.shouldDrop(bio, nestedHeading)
        article.articleContent!!.select("img").size shouldBe 0
      }

      test("author-box id without an author class is removed") {
        val bio =
          "I write about IT security and artificial intelligence. Cybersecurity is one of my regular topics, " +
            "and my focus is on practical AI for everyday users of consumer devices."
        val article = parseArticle(
          """
            <p>$body</p>
            <div id="author-box">
              <p>$bio</p>
            </div>
          """.trimIndent(),
        )

        article.shouldKeep(body)
        article.shouldDrop(bio)
      }

      test("trailing Disclaimer heading shorter than the body is removed") {
        val disclaimer =
          "This report is for general information only, including legal, medical, tax, insurance, " +
            "investment, housing, travel, and employment topics, and is not advice."
        val article = parseArticle(
          """
            <p>$body</p>
            <h2>Disclaimer</h2>
            <p>$disclaimer</p>
          """.trimIndent(),
        )

        article.shouldKeep(body)
        article.shouldDrop(disclaimer)
      }

      test("disclaimer class token shorter than the body is removed") {
        val disclaimer =
          "This report is for general information only, including legal, medical, tax, insurance, " +
            "investment, housing, travel, and employment topics, and is not advice."
        val article = parseArticle(
          """
            <p>$body</p>
            <div class="legal disclaimer">
              <p>$disclaimer</p>
            </div>
          """.trimIndent(),
        )

        article.shouldKeep(body)
        article.shouldDrop(disclaimer)
      }

      test("a body sentence that mentions about the author is kept") {
        val mentioned =
          "The magazine published a column about the author earlier this year, after the profile feature launched."
        val article = parseArticle(
          """
            <p>$body</p>
            <p>$mentioned</p>
          """.trimIndent(),
        )

        article.shouldKeep(body, mentioned)
      }

      test("an author class wrapper that holds most of the article is kept") {
        val lead = "Intro." + "\n ".repeat(400) + "end"
        val wrapperBody =
          "Investigators described how the chat logs were grouped into advertising segments for each account. " +
            "The filing walks through retention windows, deletion requests, and the limits of consent banners " +
            "that appeared beside the product's compose box."
        val html = buildString {
          append("<html><body><article><p>")
          append(lead)
          append("</p><div class=\"author\"><p>")
          append(wrapperBody)
          append("</p></div></article></body></html>")
        }
        val article = Readability4K("https://example.com", html).parse()

        article.shouldKeep("Intro.", wrapperBody)
      }

      test("a short lead does not strip a longer About the author section") {
        val lead = "Brief preface." + "\n ".repeat(300) + "end"
        val longSection =
          "The longer narrative explains how the chat logs were grouped into advertising segments over months " +
            "of ordinary use, including search, summarization, and drafting. It also records how retention " +
            "windows, deletion requests, and consent banners failed to describe that secondary profiling to " +
            "people who only wanted a writing assistant for weekend errands and schoolwork."
        val html = buildString {
          append("<html><body><article><p>")
          append(lead)
          append("</p><h2>About the author</h2><p>")
          append(longSection)
          append("</p></article></body></html>")
        }
        val article = Readability4K("https://example.com", html).parse()

        article.shouldKeep("Brief preface.", longSection)
      }

      test("rel=author person box drops the bio and keeps the body") {
        val bio =
          "I write about IT security and artificial intelligence. Cybersecurity is one of my regular topics, " +
            "among others for Golem.de. At Notebookcheck, my focus is on practical AI for everyday users."
        val article = parseArticle(
          """
            <p>$body</p>
            <div itemscope itemtype="http://schema.org/Person" rel="author" class="journalist_bottom">
              <div class="j_author">Staff writer</div>
              <div class="j_abstract">$bio</div>
            </div>
          """.trimIndent(),
        )

        article.shouldKeep(body)
        article.shouldDrop(bio, "Staff writer")
      }

      test("a disclaimer heading removes a following text node and keeps the next section") {
        val disclaimer =
          "Offers in this note are not personal advice and do not create a client relationship."
        val leadIn = "Some readers keep this lead-in sentence beside the report."
        val later = "Related coverage stays in the paper for subscribers."
        val article = parseArticle(
          """
            <p>$body</p>
            $leadIn
            <h2>Disclaimer</h2>
            $disclaimer
            <h2>Further reading</h2>
            <p>$later</p>
          """.trimIndent(),
        )

        article.shouldKeep(body, leadIn, later, "Further reading")
        article.shouldDrop(disclaimer)
      }
    },
  )
