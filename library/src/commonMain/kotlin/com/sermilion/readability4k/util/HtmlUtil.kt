package com.sermilion.readability4k.util

object HtmlUtil {

  private val htmlEntities = mapOf(
    "&quot;" to "\"",
    "&amp;" to "&",
    "&apos;" to "'",
    "&lt;" to "<",
    "&gt;" to ">",
    "&nbsp;" to " ",
    "&iexcl;" to "¡",
    "&cent;" to "¢",
    "&pound;" to "£",
    "&curren;" to "¤",
    "&yen;" to "¥",
    "&brvbar;" to "¦",
    "&sect;" to "§",
    "&uml;" to "¨",
    "&copy;" to "©",
    "&ordf;" to "ª",
    "&laquo;" to "«",
    "&not;" to "¬",
    "&shy;" to "\u00AD",
    "&reg;" to "®",
    "&macr;" to "¯",
    "&deg;" to "°",
    "&plusmn;" to "±",
    "&sup2;" to "²",
    "&sup3;" to "³",
    "&acute;" to "´",
    "&micro;" to "µ",
    "&para;" to "¶",
    "&middot;" to "·",
    "&cedil;" to "¸",
    "&sup1;" to "¹",
    "&ordm;" to "º",
    "&raquo;" to "»",
    "&frac14;" to "¼",
    "&frac12;" to "½",
    "&frac34;" to "¾",
    "&iquest;" to "¿",
  )

  private val numericEntityPattern = Regex("&#(\\d+);")
  private val hexEntityPattern = Regex("&#[xX]([0-9a-fA-F]+);")

  fun unescapeHtmlEntities(text: String?): String? {
    var result = text ?: return null

    htmlEntities.forEach { (entity, replacement) ->
      result = result.replace(entity, replacement)
    }

    result = numericEntityPattern.replace(result) { matchResult ->
      val codePoint = matchResult.groupValues[1].toIntOrNull()
      codePoint?.toChar()?.toString().orEmpty()
    }

    result = hexEntityPattern.replace(result) { matchResult ->
      val codePoint = matchResult.groupValues[1].toIntOrNull(16)
      codePoint?.toChar()?.toString().orEmpty()
    }

    return result
  }

  fun textSimilarity(textA: String, textB: String): Double {
    val tokensA = textA.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val tokensB = textB.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }

    if (tokensA.isEmpty() || tokensB.isEmpty()) {
      return 0.0
    }

    val uniqueTokensA = tokensA.toSet()
    val uniqueTokensB = tokensB.toSet()

    val commonTokens = uniqueTokensA.intersect(uniqueTokensB).size
    val totalUniqueTokens = uniqueTokensA.union(uniqueTokensB).size

    if (totalUniqueTokens == 0) {
      return 0.0
    }

    return commonTokens.toDouble() / totalUniqueTokens.toDouble()
  }
}
