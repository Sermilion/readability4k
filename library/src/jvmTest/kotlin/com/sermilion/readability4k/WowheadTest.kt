package com.sermilion.readability4k

import com.sermilion.readability4k.util.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest

class WowheadTest :
  FunSpec({

    test("should extract full article content from wowhead.com") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "wowhead-anniversary.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url = "https://www.wowhead.com/news/" +
          "the-world-of-warcraft-21st-anniversary-celebration-begins-in-korea-and-soon-in-379284"

        val logger = object : Logger {
          override fun debug(message: String) {
            if (message.contains("Candidate:") && message.contains("score")) {
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
        println("====================")
        println("Extracted content length: ${content.length}")
        println("First 500 chars: ${content.take(500)}")
        println("====================")

        content shouldContain "Happy 21st Anniversary"
        content shouldContain "Bronze Dragon Celebration Tokens"
        content shouldContain "Loyal Watcher costume"
        content shouldContain "75% experience bonus"
      }
    }
  })
