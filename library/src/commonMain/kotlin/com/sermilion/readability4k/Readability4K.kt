package com.sermilion.readability4k

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.sermilion.readability4k.model.ArticleMetadata
import com.sermilion.readability4k.model.ReadabilityOptions
import com.sermilion.readability4k.processor.ArticleGrabber
import com.sermilion.readability4k.processor.MetadataParser
import com.sermilion.readability4k.processor.Postprocessor
import com.sermilion.readability4k.processor.Preprocessor
import com.sermilion.readability4k.processor.ReadabilityArticleGrabber
import com.sermilion.readability4k.processor.ReadabilityMetadataParser
import com.sermilion.readability4k.processor.ReadabilityPostprocessor
import com.sermilion.readability4k.processor.ReadabilityPreprocessor
import com.sermilion.readability4k.util.Logger
import com.sermilion.readability4k.util.RegExUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Readability4K - Extracts the main readable content and metadata from web pages.
 *
 * This is the main entry point for the Readability4K library. It implements Mozilla's
 * Readability algorithm to extract article content from HTML documents.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * val readability = Readability4K(url, html)
 * val article = readability.parse()
 * println(article.title)
 * println(article.content)
 * ```
 *
 * ## With Custom Options
 *
 * ```kotlin
 * val options = ReadabilityOptions(
 *     maxElemsToParse = 1000,
 *     charThreshold = 500
 * )
 * val readability = Readability4K(url, html, options)
 * ```
 *
 * ## With Logging
 *
 * ```kotlin
 * val logger = object : Logger {
 *     override fun debug(message: String) = println(message)
 *     override fun error(message: String, throwable: Throwable?) = println(message)
 * }
 * val readability = Readability4J(url, html, logger = logger)
 * ```
 *
 * @see Article
 * @see ReadabilityOptions
 * @see Logger
 */
open class Readability4K {

  protected var uri: String

  protected var document: Document

  protected var options: ReadabilityOptions

  protected var regEx: RegExUtil

  protected var logger: Logger

  protected var preprocessor: Preprocessor

  protected var metadataParser: MetadataParser

  protected var articleGrabber: ArticleGrabber

  protected var postprocessor: Postprocessor

  // for Java interoperability
  /**
   * Calls Readability(String, String, ReadabilityOptions) with default
   * ReadabilityOptions and no logging
   */
  constructor(uri: String, html: String) : this(uri, html, ReadabilityOptions())

  constructor(
    uri: String,
    html: String,
    options: ReadabilityOptions = ReadabilityOptions(),
    logger: Logger = Logger.NONE,
    regExUtil: RegExUtil = RegExUtil(),
    preprocessor: Preprocessor = ReadabilityPreprocessor(regExUtil, logger),
    metadataParser: MetadataParser = ReadabilityMetadataParser(regExUtil),
    articleGrabber: ArticleGrabber = ReadabilityArticleGrabber(options, regExUtil, logger),
    postprocessor: Postprocessor = ReadabilityPostprocessor(logger),
  ) : this(
    uri,
    Ksoup.parse(
      html = html,
      baseUri = uri,
    ),
    options, logger, regExUtil, preprocessor, metadataParser, articleGrabber, postprocessor,
  )

  // for Java interoperability
  /**
   * Calls Readability(String, Document, ReadabilityOptions) with default ReadabilityOptions
   * and no logging
   */
  constructor(uri: String, document: Document) : this(uri, document, ReadabilityOptions())

  constructor(
    uri: String,
    document: Document,
    options: ReadabilityOptions = ReadabilityOptions(),
    logger: Logger = Logger.NONE,
    regExUtil: RegExUtil = RegExUtil(),
    preprocessor: Preprocessor = ReadabilityPreprocessor(regExUtil, logger),
    metadataParser: MetadataParser = ReadabilityMetadataParser(regExUtil),
    articleGrabber: ArticleGrabber = ReadabilityArticleGrabber(options, regExUtil, logger),
    postprocessor: Postprocessor = ReadabilityPostprocessor(logger),
  ) {
    this.uri = uri
    this.document = document
    this.options = options
    this.logger = logger

    this.regEx = regExUtil
    this.preprocessor = preprocessor
    this.metadataParser = metadataParser
    this.articleGrabber = articleGrabber
    this.postprocessor = postprocessor
  }

  /**
   * Parses the HTML document and extracts the main article content.
   *
   * This method implements the core Readability algorithm:
   * 1. Prepares the document by removing scripts, CSS, and other non-content elements
   * 2. Builds Readability's DOM tree
   * 3. Identifies and extracts the article content using scoring algorithms
   * 4. Post-processes the content (fixes relative URLs, cleans up markup)
   * 5. Extracts metadata (title, author, excerpt, etc.)
   *
   * **Note**: This is a blocking operation that may take time for large documents.
   * For Android/coroutine-based applications, consider using [parseAsync] which
   * automatically runs on the IO dispatcher to avoid blocking the main thread.
   *
   * @return An [Article] object containing the extracted content and metadata
   * @throws MaxElementsExceededException if the document exceeds [ReadabilityOptions.maxElemsToParse]
   *
   * @see parseAsync
   * @see Article
   * @see ReadabilityOptions
   */
  open fun parse(): Article {
    if (options.maxElemsToParse > 0) {
      val allElements = document.getAllElements()
      val numTags = allElements.size
      if (numTags > options.maxElemsToParse) {
        throw MaxElementsExceededException(numTags, options.maxElemsToParse)
      }
    }

    val metadata = metadataParser.getArticleMetadata(document, options.disableJSONLD)

    preprocessor.prepareDocument(document)

    val articleContent = articleGrabber.grabArticle(document, metadata)
    logger.debug("Grabbed: $articleContent")

    articleContent?.let {
      postprocessor.postProcessContent(
        document,
        articleContent,
        uri,
        options.additionalClassesToPreserve,
        options.keepClasses,
      )
    }

    val finalMetadata = buildArticleMetadata(metadata, articleContent)

    val serializedContent = articleContent?.let { element ->
      options.serializer?.invoke(element)
    }

    return Article(
      uri = uri,
      title = finalMetadata.title,
      articleContent = articleContent,
      excerpt = finalMetadata.excerpt,
      byline = finalMetadata.byline,
      dir = articleGrabber.articleDir,
      charset = finalMetadata.charset,
      lang = articleGrabber.articleLang,
      siteName = finalMetadata.siteName,
      publishedTime = finalMetadata.publishedTime,
      serializedContent = serializedContent,
    )
  }

  /**
   * Suspending variant of [parse] that automatically runs on the IO dispatcher.
   *
   * This method is the recommended way to parse HTML in coroutine-based applications
   * (Android, Kotlin/Native, etc.) as it prevents blocking the main thread during
   * potentially expensive DOM parsing and traversal operations.
   *
   * **Example usage:**
   * ```kotlin
   * // In a ViewModel or Repository
   * suspend fun extractArticle(url: String, html: String): Article {
   *     val readability = Readability4J(url, html)
   *     return readability.parseAsync() // Automatically runs on IO dispatcher
   * }
   * ```
   *
   * Internally, this method wraps [parse] with `withContext(Dispatchers.IO)`,
   * ensuring the blocking HTML parsing operations don't block the calling thread.
   *
   * @return An [Article] object containing the extracted content and metadata
   * @throws MaxElementsExceededException if the document exceeds [ReadabilityOptions.maxElemsToParse]
   *
   * @see parse
   * @see Article
   * @see ReadabilityOptions
   */
  open suspend fun parseAsync(): Article = withContext(Dispatchers.IO) {
    parse()
  }

  protected open fun buildArticleMetadata(metadata: ArticleMetadata, articleContent: Element?): ArticleMetadata {
    val excerpt = if (metadata.excerpt.isNullOrBlank()) {
      articleContent?.getElementsByTag("p")?.first()?.text()?.trim()
    } else {
      metadata.excerpt
    }

    val byline = if (metadata.byline.isNullOrBlank()) {
      articleGrabber.articleByline
    } else {
      metadata.byline
    }

    return ArticleMetadata(
      title = metadata.title,
      excerpt = excerpt,
      byline = byline,
      charset = metadata.charset,
      dir = metadata.dir,
      lang = metadata.lang,
      siteName = metadata.siteName,
      publishedTime = metadata.publishedTime,
    )
  }
}
