package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.test.runTest

class IxbtGamesFallout3Test :
  FunSpec({
    test("should extract article content without metadata from ixbt.games") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "ixbt-games-fallout-3.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url = "https://ixbt.games/news/2026/01/09/sozdatel-fallout-3.html"
        val readability = Readability4K(url, html)
        val article = readability.parseAsync()

        article shouldNotBe null
        article.articleContent shouldNotBe null

        val content = article.articleContent!!.text()

        println("====================")
        println("Extracted content length: ${content.length}")
        println("First 500 chars: ${content.take(500)}")
        println("====================")

        content shouldContain "Для работы над новой частью"

        content shouldNotContain "Новости 0"
        content shouldNotContain "Источник:"
        content shouldNotContain "минут назад"
        content shouldNotContain "янв 21:00"
      }
    }
  })
