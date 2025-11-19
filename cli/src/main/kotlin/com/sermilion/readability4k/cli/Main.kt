package com.sermilion.readability4k.cli

import com.sermilion.readability4k.Readability4K
import com.sermilion.readability4k.model.ReadabilityOptions
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
  if (args.isEmpty()) {
    printUsage()
    return
  }

  val url = args[0]
  val outputFormat = args.getOrNull(1) ?: "html"
  val charThreshold = args.getOrNull(2)?.toIntOrNull() ?: 500

  runBlocking {
    try {
      val html = fetchHtml(url)
      val article = extractArticle(url, html, charThreshold)

      when (outputFormat.lowercase()) {
        "html" -> printHtmlOutput(article)
        "text" -> printTextOutput(article)
        "json" -> printJsonOutput(article)
        "metadata" -> printMetadataOutput(article)
        "all" -> printAllOutput(article)
        else -> {
          println("Unknown output format: $outputFormat")
          println("Available formats: html, text, json, metadata, all")
        }
      }
    } catch (e: Exception) {
      System.err.println("Error: ${e.message}")
      e.printStackTrace()
    }
  }
}

fun printUsage() {
  println(
    """
    Readability4K CLI - Article Extraction Tool

    Usage: readability4k-cli <url> [format] [charThreshold]

    Arguments:
      url              The URL of the article to extract (required)
      format           Output format: html, text, json, metadata, all (default: html)
      charThreshold    Minimum characters for article detection (default: 500)

    Examples:
      readability4k-cli https://example.com/article
      readability4k-cli https://example.com/article html
      readability4k-cli https://example.com/article text 300
      readability4k-cli https://example.com/article all

    Output Formats:
      html       - Extracted HTML content only
      text       - Plain text content only
      json       - All article data in JSON format
      metadata   - Article metadata (title, author, etc.)
      all        - Everything (metadata + HTML + text)
    """.trimIndent(),
  )
}

suspend fun fetchHtml(url: String): String {
  val client = HttpClient(CIO)
  return try {
    val response = client.get(url)
    response.bodyAsText()
  } finally {
    client.close()
  }
}

fun extractArticle(url: String, html: String, charThreshold: Int): ArticleResult {
  val options = ReadabilityOptions(charThreshold = charThreshold)
  val readability = Readability4K(url, html, options)
  val article = readability.parse()

  return ArticleResult(
    url = url,
    title = article.title,
    byline = article.byline,
    content = article.content,
    textContent = article.textContent,
    length = article.length,
    excerpt = article.excerpt,
    siteName = article.siteName,
    lang = article.lang,
    publishedTime = article.publishedTime,
  )
}

fun printHtmlOutput(article: ArticleResult) {
  if (article.content != null) {
    println(article.content)
  } else {
    println("No article content extracted.")
  }
}

fun printTextOutput(article: ArticleResult) {
  if (article.textContent != null) {
    println(article.textContent)
  } else {
    println("No article content extracted.")
  }
}

fun printJsonOutput(article: ArticleResult) {
  println(
    """
    {
      "url": "${escapeJson(article.url)}",
      "title": "${escapeJson(article.title.orEmpty())}",
      "byline": "${escapeJson(article.byline.orEmpty())}",
      "siteName": "${escapeJson(article.siteName.orEmpty())}",
      "lang": "${escapeJson(article.lang.orEmpty())}",
      "publishedTime": "${escapeJson(article.publishedTime.orEmpty())}",
      "excerpt": "${escapeJson(article.excerpt.orEmpty())}",
      "length": ${article.length},
      "content": "${escapeJson(article.content.orEmpty())}",
      "textContent": "${escapeJson(article.textContent.orEmpty())}"
    }
    """.trimIndent(),
  )
}

fun printMetadataOutput(article: ArticleResult) {
  println("=".repeat(80))
  println("ARTICLE METADATA")
  println("=".repeat(80))
  println("URL:            ${article.url}")
  println("Title:          ${article.title ?: "N/A"}")
  println("Author:         ${article.byline ?: "N/A"}")
  println("Site Name:      ${article.siteName ?: "N/A"}")
  println("Language:       ${article.lang ?: "N/A"}")
  println("Published:      ${article.publishedTime ?: "N/A"}")
  println("Length:         ${article.length} characters")
  println("Excerpt:        ${article.excerpt ?: "N/A"}")
  println("=".repeat(80))
}

fun printAllOutput(article: ArticleResult) {
  printMetadataOutput(article)
  println()
  println("=".repeat(80))
  println("TEXT CONTENT")
  println("=".repeat(80))
  println(article.textContent ?: "No content extracted")
  println()
  println("=".repeat(80))
  println("HTML CONTENT")
  println("=".repeat(80))
  println(article.content ?: "No content extracted")
}

fun escapeJson(str: String): String {
  return str
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "\\r")
    .replace("\t", "\\t")
}

data class ArticleResult(
  val url: String,
  val title: String?,
  val byline: String?,
  val content: String?,
  val textContent: String?,
  val length: Int,
  val excerpt: String?,
  val siteName: String?,
  val lang: String?,
  val publishedTime: String?,
)
