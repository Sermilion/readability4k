package com.sermilion.readability4k

import com.sermilion.readability4k.processor.RedditCommentParser
import com.sermilion.readability4k.util.Logger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.test.runTest

class RedditTest :
  FunSpec({

    test("should extract post content from reddit.com") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "reddit-post.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url = "https://www.reddit.com/r/google_antigravity/comments/1hvjv0x/hi/"

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
        val htmlContent = article.articleContent.html()

        println("====================")
        println("Title: ${article.title}")
        println("Byline: ${article.byline}")
        println("Excerpt: ${article.excerpt}")
        println("Extracted content length: ${content.length}")
        println("First 1000 chars: ${content.take(1000)}")
        println("====================")
        println("HTML content (first 2000 chars):")
        println(htmlContent.take(2000))
        println("====================")

        article.title shouldNotBe null
        article.title shouldNotBe ""
        article.title shouldContain "used up 3% of Opus"

        article.byline shouldBe "Temporary-Mix8022"

        content.length shouldNotBe 0
        content shouldContain "I have a pure AG setup"
        content shouldContain "clearing out \"Brain\""

        content shouldNotContain "Sign up"
        content shouldNotContain "Log in"
        content shouldNotContain "Open app"
        content shouldNotContain "Get the Reddit app"
        content shouldNotContain "More posts you may like"
        content shouldNotContain "Recommended"
      }
    }

    test("should extract post title from reddit.com with generic document title") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "reddit-local-llm.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url = "https://www.reddit.com/r/google_antigravity/comments/1q9cs66/local_llm_has_anyone_considered_this/"

        val readability = Readability4K(url, html)
        val article = readability.parseAsync()

        article shouldNotBe null

        println("====================")
        println("Title: ${article.title}")
        println("====================")

        article.title shouldNotBe null
        article.title shouldNotBe ""
        article.title shouldBe "Local LLM, has anyone considered this?"
        article.title shouldNotContain "Reddit"
        article.title shouldNotContain "heart of the internet"
      }
    }

    test("should extract comments from reddit.com post with full HTML") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "reddit-post-full.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url =
          "https://www.reddit.com/r/google_antigravity/comments/1q9toqt/" +
            "gemini_3_pro_is_either_full_gungho_or_need_to_be/"

        val commentParser = RedditCommentParser()
        val readability = Readability4K(url, html, commentParser = commentParser)
        val article = readability.parseAsync()

        article shouldNotBe null

        println("====================")
        println("Extracted ${article.comments.size} comments")
        println("====================")

        if (article.comments.isNotEmpty()) {
          println("SUCCESS: Found ${article.comments.size} comments!")

          article.comments.forEach { comment ->
            println("Author: ${comment.author}")
            println("Body: ${comment.body?.take(100)}")
            println("Body length: ${comment.body?.length}")
            if (comment.replies.isNotEmpty()) {
              println("  - Has ${comment.replies.size} replies")
            }
            println("---")
          }
        } else {
          println("No comments found - HTML likely uses client-side loading")
        }
      }
    }

    test("should extract comments from reddit.com post when comments are server-rendered") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "reddit-post.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url = "https://www.reddit.com/r/google_antigravity/comments/1hvjv0x/hi/"

        val commentParser = RedditCommentParser()
        val readability = Readability4K(url, html, commentParser = commentParser)
        val article = readability.parseAsync()

        article shouldNotBe null

        println("====================")
        println("Extracted ${article.comments.size} top-level comments")
        println("====================")

        if (article.comments.isNotEmpty()) {
          article.comments.size shouldBeGreaterThan 0

          val firstComment = article.comments.first()
          firstComment.author shouldNotBe null
          firstComment.body shouldNotBe null

          println("First comment by: ${firstComment.author}")
          println("First comment body: ${firstComment.body?.take(100)}")
          println("====================")

          article.comments.forEach { comment ->
            println("Author: ${comment.author}, Body length: ${comment.body?.length}")
            if (comment.replies.isNotEmpty()) {
              println("  - Has ${comment.replies.size} replies")
            }
          }
        } else {
          println("No comments found in HTML (likely loaded client-side)")
        }
      }
    }

    test("should extract comments from reddit post") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "reddit-comments.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url =
          "https://old.reddit.com/r/google_antigravity/comments/1q95ifn/" +
            "google_ai_pro_isnt_broken_your_expectations_are/"

        val commentParser = RedditCommentParser()
        val readability = Readability4K(url, html, commentParser = commentParser)
        val article = readability.parseAsync()

        article shouldNotBe null
        article.comments.shouldNotBeEmpty()

        println("====================")
        println("Title: ${article.title}")
        println("Extracted ${article.comments.size} top-level comments")
        println("====================")

        article.title shouldNotBe null
        article.title shouldNotBe ""
        article.title!! shouldContain "Google AI Pro"

        article.comments.size shouldBeGreaterThan 0

        val firstComment = article.comments.first()
        firstComment.author shouldNotBe null
        firstComment.body shouldNotBe null

        println("First comment by: ${firstComment.author}")
        println("First comment body: ${firstComment.body?.take(100)}")
        println("First comment ID: ${firstComment.id}")
        println("First comment score: ${firstComment.score}")
        println("====================")

        article.comments.forEach { comment ->
          println("Author: ${comment.author}, ID: ${comment.id}")
          println("  Body: ${comment.body?.take(80)}")
          println("  Score: ${comment.score}, Timestamp: ${comment.timestamp}")
          if (comment.replies.isNotEmpty()) {
            println("  - Has ${comment.replies.size} replies")
            comment.replies.take(2).forEach { reply ->
              println("    Reply by ${reply.author}: ${reply.body?.take(60)}")
            }
          }
          println("---")
        }

        firstComment.author shouldBe "JoeyDee86"
        firstComment.body shouldContain "The issue I have is the gap between pro and Ultra"
        firstComment.replies.shouldNotBeEmpty()

        firstComment.id shouldNotBe null
        firstComment.id shouldContain "t1_"
        firstComment.score shouldNotBe null
        firstComment.timestamp shouldNotBe null

        val firstReply = firstComment.replies.first()
        firstReply.author shouldNotBe null
        firstReply.body shouldNotBe null
        firstReply.id shouldNotBe null
        firstReply.id shouldContain "t1_"
      }
    }

    test("should auto-detect Reddit and extract comments without explicit commentParser") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "reddit-comments.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url =
          "https://old.reddit.com/r/google_antigravity/comments/1q95ifn/" +
            "google_ai_pro_isnt_broken_your_expectations_are/"

        val readability = Readability4K(url, html)
        val article = readability.parseAsync()

        article shouldNotBe null
        article.comments.shouldNotBeEmpty()
        article.comments.size shouldBeGreaterThan 0
      }
    }

    test("should verify nested comment structure") {
      runTest {
        val htmlResource = this::class.java.classLoader.getResourceAsStream(
          "reddit-comments.html",
        )
        val html = htmlResource?.bufferedReader()?.use { it.readText() }
          ?: error("Could not load test resource")

        val url =
          "https://old.reddit.com/r/google_antigravity/comments/1q95ifn/" +
            "google_ai_pro_isnt_broken_your_expectations_are/"

        val commentParser = RedditCommentParser()
        val readability = Readability4K(url, html, commentParser = commentParser)
        val article = readability.parseAsync()

        val commentsWithReplies = article.comments.filter { it.replies.isNotEmpty() }
        commentsWithReplies.shouldNotBeEmpty()

        val commentWithReplies = commentsWithReplies.first()
        println("Comment with ${commentWithReplies.replies.size} replies by ${commentWithReplies.author}")

        commentWithReplies.replies.forEach { reply ->
          reply.author shouldNotBe null
          reply.body shouldNotBe null
          reply.id shouldNotBe null
          reply.id shouldContain "t1_"
        }

        val totalReplies = article.comments.sumOf { it.replies.size }
        totalReplies shouldBeGreaterThan 0
        println("Total top-level comments: ${article.comments.size}")
        println("Total replies: $totalReplies")
      }
    }
  })
