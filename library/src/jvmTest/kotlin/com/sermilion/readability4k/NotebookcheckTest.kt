package com.sermilion.readability4k

import com.sermilion.readability4k.util.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.test.runTest

class NotebookcheckTest :
  FunSpec({

    test("should extract full review content from notebookcheck.net") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "notebookcheck-iqoo15.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url = "https://www.notebookcheck.net/" +
          "Verdict-on-the-IQOO-15-Fantastic-affordable-gaming-smartphone-with-Snapdragon-8-Elite-Gen-5.1201168.0.html"

        val logger = object : Logger {
          override fun debug(message: String) {
            if (message.contains("Candidate:") || message.contains("Looking at sibling")) {
              println(message)
            }
          }

          override fun error(message: String, throwable: Throwable?) {
            println("ERROR: $message")
          }
        }

        val readability = Readability4K(url, html, logger = logger)
        val article = readability.parseAsync()

        article shouldNotBe null
        article.articleContent shouldNotBe null

        val content = article.articleContent!!.text()
        val htmlContent = article.articleContent!!.html()

        println("====================")
        println("Title: ${article.title}")
        println("Byline: ${article.byline}")
        println("Excerpt: ${article.excerpt}")
        println("Extracted content length: ${content.length}")
        println("First 1000 chars: ${content.take(1000)}")
        println("====================")
        println("Last 1000 chars: ${content.takeLast(1000)}")
        println("====================")

        article.title shouldNotBe null
        article.title shouldContain "IQOO 15"

        content shouldContain "Snapdragon 8 Elite"
        content shouldContain "Q3 gaming chip"
        content shouldContain "PUBG Mobile"
        content shouldContain "GameBench"
        content shouldContain "ray tracing"

        content.length shouldBeGreaterThan 1200

        content shouldNotContain "Sign up"
        content shouldNotContain "Subscribe"
      }
    }
  })
