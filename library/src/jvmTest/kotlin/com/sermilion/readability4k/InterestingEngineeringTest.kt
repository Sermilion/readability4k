package com.sermilion.readability4k

import com.sermilion.readability4k.util.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File

class InterestingEngineeringTest : FunSpec({

  test("should extract image from actual Interesting Engineering HTML") {
    val htmlFile = File("/Users/sermilion/Downloads/untitled.html")
    if (!htmlFile.exists()) {
      println("HTML file not found, skipping test")
      return@test
    }

    val html = htmlFile.readText()
    val url = "https://interestingengineering.com/space/china-largest-satellite-constellations-approval"

    val logger = object : Logger {
      override fun debug(message: String) {
        println("DEBUG: $message")
      }

      override fun error(message: String, throwable: Throwable?) {
        println("ERROR: $message")
        throwable?.printStackTrace()
      }
    }

    val readability = Readability4K(url, html, logger = logger)
    val article = readability.parse()

    article shouldNotBe null
    article.content shouldNotBe null

    val content = article.content.orEmpty()
    val htmlContent = article.articleContent?.html().orEmpty()

    println("====================")
    println("Title: ${article.title}")
    println("Content length: ${content.length}")
    println("HTML Content (first 2000 chars):")
    println(htmlContent.take(2000))
    println("====================")
    println("Looking for images in content:")
    val imgTags = htmlContent.split("<img").drop(1).take(5)
    imgTags.forEachIndexed { index, tag ->
      println("Image $index: <img${tag.take(200)}")
    }
    println("====================")

    content shouldContain "China"
    content shouldContain "satellites"

    htmlContent shouldContain "cms.interestingengineering.com"
    htmlContent shouldNotContain "_next/image"
  }
})
