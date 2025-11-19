@file:Suppress("MatchingDeclarationName")

package com.sermilion.readability4k

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.sermilion.readability4k.util.RegExUtil
import com.sermilion.readability4k.util.VisibilityUtil
import kotlin.math.sqrt

private val defaultRegEx = RegExUtil()

data class ReadableCheckOptions(
  val minScore: Int = 20,
  val minContentLength: Int = 140,
  val visibilityChecker: (Element) -> Boolean = VisibilityUtil::isNodeVisible,
)

@Suppress("CyclomaticComplexMethod", "LoopWithTooManyJumpStatements", "MaxLineLength")
fun isProbablyReaderable(
  doc: Document,
  options: ReadableCheckOptions = ReadableCheckOptions(),
  regEx: RegExUtil = defaultRegEx,
): Boolean {
  val unlikelyPattern = Regex(
    "-ad-|banner|combx|comment|community|cover-wrap|disqus|extra|footer|gdpr|" +
      "header|legends|menu|related|remark|replies|rss|shoutbox|sidebar|skyscraper|" +
      "social|sponsor|supplemental|ad-break|agegate|pagination|pager|popup|yom-remote",
    RegexOption.IGNORE_CASE,
  )
  val okMaybePattern = Regex(
    "article|body|content|entry|hentry|h-entry|main|page|pagination|post|text|blog|story",
    RegexOption.IGNORE_CASE,
  )

  val nodes = doc.select("p, pre, article")

  val brNodes = doc.select("div > br")
  brNodes.forEach { br ->
    br.parent()?.let { nodes.add(it) }
  }

  var score = 0.0

  for (node in nodes) {
    if (!options.visibilityChecker(node)) {
      continue
    }

    val matchString = node.className() + " " + node.id()
    if (unlikelyPattern.containsMatchIn(matchString) && !okMaybePattern.containsMatchIn(matchString)) {
      continue
    }

    if (node.parent()?.tagName() == "li" && node.tagName() == "p") {
      continue
    }

    val textContent = regEx.normalize(node.text().trim())
    val textContentLength = textContent.length

    if (textContentLength < options.minContentLength) {
      continue
    }

    score += sqrt((textContentLength - options.minContentLength).toDouble())

    if (score > options.minScore) {
      return true
    }
  }

  return false
}
