package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.sermilion.readability4k.model.ArticleGrabberOptions
import com.sermilion.readability4k.model.ArticleMetadata

interface Preprocessor {
  fun prepareDocument(document: Document)
}

interface MetadataParser {
  fun getArticleMetadata(document: Document, disableJSONLD: Boolean = false): ArticleMetadata
}

interface ArticleGrabber {
  val articleByline: String?
  val articleDir: String?
  val articleLang: String?

  fun grabArticle(
    doc: Document,
    metadata: ArticleMetadata,
    options: ArticleGrabberOptions = ArticleGrabberOptions(),
    pageElement: Element? = null,
  ): Element?
}

interface Postprocessor {
  fun postProcessContent(
    originalDocument: Document,
    articleContent: Element,
    articleUri: String,
    additionalClassesToPreserve: Collection<String> = emptyList(),
    keepClasses: Boolean = false,
  )
}
