package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element

fun interface NoscriptHandler {
  fun shouldKeepNoscript(document: Document, noscript: Element): Boolean
}

/**
 * Default noscript handler that preserves substantial text content and unique images.
 *
 * ## Keeps noscript if either condition is true:
 *
 * 1. Contains ≥100 characters of text content, OR
 * 2. Contains unique images (not found elsewhere in document)
 *
 * ## Why this approach
 *
 * Some websites (e.g., wowhead.com) render article content via JavaScript and provide
 * the full article text in `<noscript>` tags as a fallback. Without this handler,
 * extraction would only capture titles and navigation elements.
 *
 * ## When this handler helps
 *
 * - **JavaScript-rendered content**: Sites that put real article content in noscript
 * - **Maximum extraction**: Captures all possible article content
 * - **Modern web compatibility**: Handles contemporary web development patterns
 *
 * ## Example affected sites
 *
 * - wowhead.com - Article content in noscript (fixed from 306 to 4495 characters)
 *
 * ## Creating custom handlers
 *
 * To customize noscript handling behavior:
 * ```kotlin
 * object MyNoscriptHandler : NoscriptHandler {
 *   override fun shouldKeepNoscript(document: Document, noscript: Element): Boolean {
 *     // Your custom logic
 *     return noscript.text().length > 50
 *   }
 * }
 *
 * val preprocessor = Preprocessor(noscriptHandler = MyNoscriptHandler)
 * ```
 */
object ContentAwareNoscriptHandler : NoscriptHandler {
  private const val MIN_CONTENT_LENGTH = 100

  override fun shouldKeepNoscript(document: Document, noscript: Element): Boolean {
    val textContent = noscript.text().trim()
    if (textContent.length >= MIN_CONTENT_LENGTH) {
      return true
    }

    val images = noscript.select("img")
    if (images.isNotEmpty()) {
      val imagesToKeep = ArrayList(images)

      images.forEach { image ->
        val source = image.attr("src")
        if (source.isNotBlank() && document.select("img[src=$source]").isNotEmpty()) {
          imagesToKeep.remove(image)
        }
      }

      return imagesToKeep.isNotEmpty()
    }

    return false
  }
}
