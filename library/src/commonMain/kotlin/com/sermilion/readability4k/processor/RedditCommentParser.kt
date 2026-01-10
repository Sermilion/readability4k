package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.sermilion.readability4k.model.Comment

open class RedditCommentParser(
  protected val maxDepth: Int = DEFAULT_MAX_DEPTH,
) : ProcessorBase(), CommentParser {

  companion object {
    const val DEFAULT_MAX_DEPTH = 50
  }

  fun extractPostContent(doc: Document): Element? {
    val postElement = doc.select("div.thing[data-type=link]").firstOrNull()
    val selfTextBody = postElement?.select("div.usertext-body div.md")?.firstOrNull()
    return selfTextBody
  }

  fun renderCommentsAsHtml(comments: List<Comment>): String {
    if (comments.isEmpty()) return ""

    val sb = StringBuilder()
    sb.append("<section class=\"reddit-comments\">")
    sb.append("<h2>Comments</h2>")

    comments.forEach { comment ->
      renderCommentAsHtml(comment, sb, 0)
    }

    sb.append("</section>")
    return sb.toString()
  }

  private fun renderCommentAsHtml(comment: Comment, sb: StringBuilder, depth: Int) {
    if (depth >= maxDepth) return

    val indent = depth * 20
    val style = "margin-left: ${indent}px; padding: 10px 0; border-left: 2px solid #ccc; padding-left: 10px;"
    sb.append("<div class=\"comment\" style=\"$style\">")

    comment.author?.let { author ->
      sb.append("<div class=\"comment-author\" style=\"font-weight: bold; color: #0066cc;\">")
      sb.append(author)
      sb.append("</div>")
    }

    comment.bodyHtml?.let { body ->
      sb.append("<div class=\"comment-body\" style=\"margin: 5px 0;\">")
      sb.append(body)
      sb.append("</div>")
    }

    sb.append("</div>")

    comment.replies.forEach { reply ->
      renderCommentAsHtml(reply, sb, depth + 1)
    }
  }

  override fun parseComments(doc: Document): List<Comment> {
    val comments = mutableListOf<Comment>()

    val topLevelContainer = doc.select("div.sitetable.nestedlisting").firstOrNull()
      ?: doc.select("div[id^=siteTable_t3_]").firstOrNull()
      ?: return emptyList()

    val allComments = topLevelContainer.select("> div[data-type=comment]")

    allComments.forEach { commentElement ->
      val comment = parseCommentElement(commentElement)
      if (comment != null) {
        comments.add(comment)
      }
    }

    return comments
  }

  protected open fun parseCommentElement(element: Element, depth: Int = 0): Comment? {
    if (depth >= maxDepth) return null

    try {
      val id = extractCommentId(element)
      val author = extractAuthor(element)
      val body = extractBody(element)
      val bodyHtml = extractBodyHtml(element)
      val timestamp = extractTimestamp(element)
      val score = extractScore(element)
      val replies = extractReplies(element, depth + 1)

      if (body.isNullOrBlank() && bodyHtml.isNullOrBlank()) {
        return null
      }

      return Comment(
        id = id,
        author = author,
        body = body,
        bodyHtml = bodyHtml,
        timestamp = timestamp,
        score = score,
        replies = replies,
      )
    } catch (_: Exception) {
      return null
    }
  }

  protected open fun extractCommentId(element: Element): String? {
    return element.attr("data-fullname").ifBlank { null }
      ?: element.attr("id").ifBlank { null }
  }

  protected open fun extractAuthor(element: Element): String? {
    return element.attr("data-author").ifBlank { null }
      ?: element.select("a.author").first()?.text()?.trim()
  }

  protected open fun extractBody(element: Element): String? {
    val bodyElement = element.select("div.usertext-body > div.md").firstOrNull()
    return bodyElement?.text()?.trim()
  }

  protected open fun extractBodyHtml(element: Element): String? {
    val bodyElement = element.select("div.usertext-body > div.md").firstOrNull()
    return bodyElement?.html()?.trim()
  }

  protected open fun extractTimestamp(element: Element): String? {
    val timeElement = element.select("time").firstOrNull()
    return timeElement?.attr("datetime")
      ?: timeElement?.attr("title")
  }

  protected open fun extractScore(element: Element): String? {
    val scoreElements = element.select("span.score.unvoted")
    return scoreElements.firstOrNull()?.attr("title")?.ifBlank { null }
  }

  protected open fun extractReplies(element: Element, depth: Int): List<Comment> {
    if (depth >= maxDepth) return emptyList()

    val replies = mutableListOf<Comment>()

    val childContainer = element.select("> div.child > div.sitetable > div.thing.comment[data-type=comment]")

    childContainer.forEach { replyElement ->
      val reply = parseCommentElement(replyElement, depth)
      if (reply != null) {
        replies.add(reply)
      }
    }

    return replies
  }
}
