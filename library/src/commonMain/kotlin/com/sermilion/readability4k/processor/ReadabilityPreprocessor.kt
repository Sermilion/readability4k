package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.Node
import com.sermilion.readability4k.util.Logger
import com.sermilion.readability4k.util.RegExUtil

/**
 * Performs basic sanitization before starting the extraction process.
 */
open class ReadabilityPreprocessor(
  protected val regEx: RegExUtil = RegExUtil(),
  logger: Logger = Logger.NONE,
  protected val noscriptHandler: NoscriptHandler = ContentAwareNoscriptHandler,
) : ProcessorBase(logger), Preprocessor {

  /**
   * Prepare the HTML document for readability to scrape it.
   * This includes things like stripping javascript, CSS, and handling terrible markup.
   */
  override fun prepareDocument(document: Document) {
    logger.debug("Starting to prepare document")

    unwrapNoscriptImages(document)
    removeScripts(document)
    removeNoscripts(document)

    removeStyles(document)

    // Enhancement: Not in Mozilla's Readability.js, but improves extraction quality
    removeForms(document)

    // Enhancement: Not in Mozilla's Readability.js, but reduces parsing overhead
    removeComments(document)

    replaceBrs(document, regEx)

    replaceNodes(document, "font", "span")

    fixLazyImages(document)
    fixNextJsImages(document)
  }

  protected open fun removeScripts(document: Document) {
    removeNodes(document, "script") { scriptNode ->
      scriptNode.removeAttr("src")
      true
    }
  }

  protected open fun removeNoscripts(document: Document) {
    document.getElementsByTag("noscript").forEach { noscript ->
      if (noscriptHandler.shouldKeepNoscript(document, noscript)) {
        noscript.unwrap()
      } else {
        printAndRemove(noscript, "removeScripts('noscript')")
      }
    }
  }

  @Deprecated(
    message = "Use noscriptHandler parameter instead. " +
      "This method is kept for backward compatibility.",
    replaceWith = ReplaceWith("noscriptHandler.shouldKeepNoscript(document, noscript)"),
  )
  protected open fun shouldKeepImageInNoscriptElement(document: Document, noscript: Element): Boolean {
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

  protected open fun removeStyles(document: Document) {
    removeNodes(document, "style")
  }

  protected open fun removeForms(document: Document) {
    removeNodes(document, "form")
  }

  protected open fun removeComments(node: Node) {
    var i = 0
    while (i < node.childNodeSize()) {
      val child = node.childNode(i)
      if (child.nodeName() == "#comment") {
        printAndRemove(child, "removeComments")
      } else {
        removeComments(child)
        i++
      }
    }
  }

  /**
   * Replaces 2 or more successive <br> elements with a single <p>.
   * Whitespace between <br> elements are ignored. For example:
   *   <div>foo<br>bar<br> <br><br>abc</div>
   * will become:
   *   <div>foo<br>bar<p>abc</p></div>
   */
  @Suppress("NestedBlockDepth")
  protected open fun replaceBrs(document: Document, regEx: RegExUtil) {
    document.body().select("br").forEach { br ->
      var next: Node? = br.nextSibling()

      // Whether 2 or more <br> elements have been found and replaced with a
      // <p> block.
      var replaced = false

      // If we find a <br> chain, remove the <br>s until we hit another element
      // or non-whitespace. This leaves behind the first <br> in the chain
      // (which will be replaced with a <p> later).
      next = nextElement(next, regEx)
      while (next != null && next.nodeName() == "br") {
        replaced = true
        val brSibling = (next as? Element)?.nextSibling()
        printAndRemove(next, "replaceBrs")
        next = nextElement(brSibling, regEx)
      }

      // If we removed a <br> chain, replace the remaining <br> with a <p>. Add
      // all sibling nodes as children of the <p> until we hit another <br>
      // chain.
      if (replaced) {
        val p = br.ownerDocument()?.createElement("p") ?: return@forEach
        br.replaceWith(p)

        next = p.nextSibling()
        while (next != null) {
          // If we've hit another <br><br>, we're done adding children to this <p>.
          if (next.nodeName() == "br") {
            val nextElem = this.nextElement(next, regEx)
            if (nextElem != null && nextElem.tagName() == "br") {
              break
            }
          }

          // Otherwise, make this node a child of the new <p>.
          val sibling = next.nextSibling()
          p.appendChild(next)
          next = sibling
        }
      }
    }
  }

  @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
  protected open fun fixLazyImages(document: Document) {
    val imageExtensionPattern = Regex("\\.(jpg|jpeg|png|webp)\\s+\\d", RegexOption.IGNORE_CASE)
    val srcPattern = Regex("^\\s*\\S+\\.(jpg|jpeg|png|webp)\\S*\\s*$", RegexOption.IGNORE_CASE)

    document.select("img, picture, figure").forEach { elem ->
      if (elem.tagName() == "img") {
        val src = elem.attr("src")
        val srcset = elem.attr("srcset")

        if ((src.isEmpty() || src.startsWith("data:")) &&
          (srcset.isEmpty() || srcset.startsWith("data:"))
        ) {
          val attributesList = elem.attributes().toList()
          for (attr in attributesList) {
            if (attr.key == "src" || attr.key == "srcset") {
              continue
            }

            val copyTo = when {
              srcPattern.matches(attr.value) -> "src"
              imageExtensionPattern.containsMatchIn(attr.value) -> "srcset"
              else -> null
            }

            if (copyTo != null) {
              if (elem.tagName() == "img") {
                elem.attr(copyTo, attr.value)
              } else if (elem.tagName() == "picture") {
                val img = elem.selectFirst("img")
                if (img != null) {
                  img.attr(copyTo, attr.value)
                } else {
                  val newImg = document.createElement("img")
                  newImg.attr(copyTo, attr.value)
                  elem.appendChild(newImg)
                }
              }
            }
          }
        }
      } else if (elem.tagName() == "figure" && !isSingleImage(elem)) {
        val attributesList = elem.attributes().toList()
        for (attr in attributesList) {
          val copyTo = when {
            srcPattern.matches(attr.value) -> "src"
            imageExtensionPattern.containsMatchIn(attr.value) -> "srcset"
            else -> null
          }

          if (copyTo != null) {
            val img = document.createElement("img")
            img.attr(copyTo, attr.value)
            elem.appendChild(img)
            break
          }
        }
      }
    }
  }

  protected open fun fixNextJsImages(document: Document) {
    val images = document.select("img")
    logger.debug("[NextJS] Found ${images.size} img tags to process")

    images.forEach { img ->
      val src = img.attr("src")
      val srcset = img.attr("srcset")

      if (src.isNotBlank()) {
        logger.debug("[NextJS] Processing src: $src")
        val extractedUrl = extractRealImageUrl(src)
        if (extractedUrl != null) {
          logger.debug("[NextJS] Extracted URL: $extractedUrl")
          img.attr("src", extractedUrl)
          logger.debug("[NextJS] Updated src attribute")
        } else {
          logger.debug("[NextJS] No Next.js pattern found in src")
        }
      }

      if (srcset.isNotBlank()) {
        logger.debug("[NextJS] Processing srcset: ${srcset.take(100)}...")
        val fixedSrcset = fixNextJsSrcset(srcset)
        if (fixedSrcset != srcset) {
          logger.debug("[NextJS] Fixed srcset")
          img.attr("srcset", fixedSrcset)
        }
      }
    }
    logger.debug("[NextJS] Completed processing images")
  }

  protected open fun extractRealImageUrl(url: String): String? {
    val nextJsPattern = Regex("/_next/image\\?url=([^&]+)")
    val match = nextJsPattern.find(url)

    if (match == null) {
      return null
    }

    val encodedUrl = match.groupValues[1]
    logger.debug("[NextJS] Encoded URL: $encodedUrl")

    return try {
      val decoded = decodeUrl(encodedUrl)
      logger.debug("[NextJS] Decoded URL: $decoded")
      decoded
    } catch (e: Exception) {
      logger.debug("[NextJS] Failed to decode: $encodedUrl - ${e.message}")
      null
    }
  }

  protected open fun fixNextJsSrcset(srcset: String): String {
    val entries = srcset.split(",").map { it.trim() }
    val fixedEntries = entries.map { entry ->
      val parts = entry.split(Regex("\\s+"))
      if (parts.isEmpty()) return@map entry

      val url = parts[0]
      val descriptor = parts.drop(1).joinToString(" ")

      val extractedUrl = extractRealImageUrl(url)
      if (extractedUrl != null) {
        if (descriptor.isNotBlank()) {
          "$extractedUrl $descriptor"
        } else {
          extractedUrl
        }
      } else {
        entry
      }
    }
    return fixedEntries.joinToString(", ")
  }

  protected open fun decodeUrl(encodedUrl: String): String {
    var decoded = encodedUrl
    decoded = decoded.replace("%3A", ":")
    decoded = decoded.replace("%2F", "/")
    decoded = decoded.replace("%3F", "?")
    decoded = decoded.replace("%3D", "=")
    decoded = decoded.replace("%26", "&")
    decoded = decoded.replace("%2B", "+")
    decoded = decoded.replace("%20", " ")
    decoded = decoded.replace("%23", "#")
    decoded = decoded.replace("%25", "%")
    decoded = decoded.replace("%2C", ",")
    decoded = decoded.replace("%40", "@")
    decoded = decoded.replace("%21", "!")
    decoded = decoded.replace("%24", "$")
    decoded = decoded.replace("%27", "'")
    decoded = decoded.replace("%28", "(")
    decoded = decoded.replace("%29", ")")
    decoded = decoded.replace("%2A", "*")
    decoded = decoded.replace("%2D", "-")
    decoded = decoded.replace("%2E", ".")
    decoded = decoded.replace("%5F", "_")
    decoded = decoded.replace("%7E", "~")
    return decoded
  }

  @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
  protected open fun unwrapNoscriptImages(document: Document) {
    val imgElements = document.select("img")

    imgElements.forEach { img ->
      val src = img.attr("src")
      val srcset = img.attr("srcset")
      val dataSrc = img.attr("data-src")
      val dataSrcset = img.attr("data-srcset")

      if (src.isEmpty() && srcset.isEmpty() && dataSrc.isEmpty() && dataSrcset.isEmpty()) {
        img.remove()
      }
    }

    val noscripts = document.select("noscript")

    noscripts.forEach { noscript ->
      val tmp = document.createElement("div")
      tmp.html(noscript.html())

      if (!isSingleImage(tmp)) {
        return@forEach
      }

      val prevElement = noscript.previousElementSibling()
      if (prevElement != null && isSingleImage(prevElement)) {
        val prevImg = if (prevElement.tagName() == "img") {
          prevElement
        } else {
          prevElement.selectFirst("img")
        }

        val newImg = tmp.selectFirst("img")

        if (prevImg != null && newImg != null) {
          val attributesList = prevImg.attributes().toList()
          for (attr in attributesList) {
            if (attr.value.isEmpty()) {
              continue
            }

            if (attr.key == "src" || attr.key == "srcset" || attr.value.startsWith("data:")) {
              if (newImg.hasAttr(attr.key)) {
                newImg.attr("data-old-${attr.key}", attr.value)
              }
            }
          }

          prevElement.replaceWith(newImg.clone())
        }
      }
    }
  }
}
