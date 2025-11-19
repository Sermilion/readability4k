package com.sermilion.readability4k.util

import com.fleeksoft.ksoup.nodes.Element

object VisibilityUtil {
  fun isNodeVisible(node: Element): Boolean {
    val style = node.attr("style")
    if (style.contains("display:none", ignoreCase = true) ||
      style.contains("display: none", ignoreCase = true)
    ) {
      return false
    }
    if (style.contains("visibility:hidden", ignoreCase = true) ||
      style.contains("visibility: hidden", ignoreCase = true)
    ) {
      return false
    }
    if (node.hasAttr("hidden")) {
      return false
    }
    if (node.hasAttr("aria-hidden") && node.attr("aria-hidden") == "true") {
      val className = node.className()
      if (!className.contains("fallback-image")) {
        return false
      }
    }
    return true
  }
}
