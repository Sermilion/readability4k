package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest

class IxbtGamesQuoteTest :
  FunSpec({

    test("should extract full article content, not just quotation from ixbt.games with real HTML") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "ixbt-games-half-life-3.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url = "https://ixbt.games/news/2025/11/16/valve-skoro-anonsiruet-half-life-3.html"

        val logger = object : com.sermilion.readability4k.util.Logger {
          override fun debug(message: String) {
            if (message.contains("Looking at sibling") ||
              message.contains("Appending") ||
              message.contains("score")
            ) {
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
        println("Extracted content: $content")
        println("====================")

        content shouldContain "Ожидание новой части франшизы продолжается"
        content shouldContain "Пока ожидание «ноябрьского анонса Half-Life 3» не оправдались"
        content shouldContain "Gabe Follower рассказал"
        content shouldContain "Tyler McVicker утверждает"
        content shouldContain "Том Хендерсон"
        content shouldContain "Да, распространённая теория такова"
      }
    }
  })
