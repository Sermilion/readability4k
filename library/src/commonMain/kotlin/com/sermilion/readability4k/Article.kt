package com.sermilion.readability4k

import com.fleeksoft.ksoup.nodes.Element

/**
 * Represents an article extracted from a web page.
 *
 * Contains the extracted content, metadata, and various convenience properties
 * for accessing the article data in different formats.
 *
 * This class is open to allow library consumers to extend it with custom functionality.
 *
 * @property uri Original URI that was passed to the Readability4J constructor
 * @property title Article title extracted from the document
 * @property articleContent The Ksoup Element containing the processed article content
 * @property excerpt Article description or short excerpt from the content
 * @property byline Author name extracted from metadata or byline
 * @property dir Text direction of the content (e.g., "ltr" or "rtl")
 * @property charset Character encoding of the original document
 * @property lang Language of the article content (e.g., "en", "fr")
 * @property siteName Name of the website/publication
 * @property publishedTime Publication date/time of the article
 */
open class Article(
  val uri: String,
  val title: String? = null,
  val articleContent: Element? = null,
  val excerpt: String? = null,
  val byline: String? = null,
  val dir: String? = null,
  val charset: String? = null,
  val lang: String? = null,
  val siteName: String? = null,
  val publishedTime: String? = null,
  private val serializedContent: String? = null,
) {

  /**
   * HTML string of processed article content in a `<div>` element.
   *
   * Note: No encoding is applied to this content. For content with explicit
   * UTF-8 encoding, see [contentWithUtf8Encoding]. For more details, see
   * [https://github.com/dankito/Readability4J/issues/1].
   *
   * If a custom serializer was provided via options, it was applied during parsing.
   */
  val content: String?
    get() = serializedContent ?: articleContent?.html()

  /**
   * Content with explicit UTF-8 encoding.
   *
   * The [content] property returns a `<div>` element without encoding information.
   * Since HTML requires a `<head><meta charset="">` tag to specify encoding,
   * non-ASCII characters may display incorrectly when using [content] alone.
   *
   * This property wraps [content] in a complete HTML structure with UTF-8 encoding:
   * ```html
   * <html>
   *   <head>
   *     <meta charset="utf-8"/>
   *   </head>
   *   <body>
   *     <!-- content -->
   *   </body>
   * </html>
   * ```
   *
   * See [https://github.com/dankito/Readability4J/issues/1] for more information.
   */
  val contentWithUtf8Encoding: String?
    get() = getContentWithEncoding("utf-8")

  /**
   * Content wrapped with the document's original charset, or UTF-8 if not specified.
   *
   * Returns the content wrapped in an HTML structure with charset set to the document's
   * original charset. If the charset is not available, defaults to UTF-8.
   *
   * @see contentWithUtf8Encoding for more details
   */
  val contentWithDocumentsCharsetOrUtf8: String?
    get() = getContentWithEncoding(charset ?: "utf-8")

  /**
   * Plain text content of the article without HTML markup
   */
  val textContent: String?
    get() = articleContent?.text()

  /**
   * Length of article in characters (based on [textContent])
   */
  val length: Int
    get() = textContent?.length ?: -1

  /**
   * Returns the article content wrapped in a complete HTML structure with the specified encoding.
   *
   * Wraps [content] in:
   * ```html
   * <html>
   *   <head>
   *     <meta charset="[encoding]"/>
   *   </head>
   *   <body>
   *     <!-- content -->
   *   </body>
   * </html>
   * ```
   *
   * @param encoding The character encoding to use (e.g., "utf-8", "iso-8859-1")
   * @return The wrapped HTML content, or null if [content] is null
   * @see contentWithUtf8Encoding
   */
  fun getContentWithEncoding(encoding: String): String? {
    content?.let { content ->
      return "<html>\n  <head>\n    <meta charset=\"$encoding\"/>\n  </head>\n  <body>\n    " +
        "$content\n  </body>\n</html>"
    }

    return null
  }
}
