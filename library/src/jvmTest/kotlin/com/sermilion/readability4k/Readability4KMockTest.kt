package com.sermilion.readability4k

import com.fleeksoft.ksoup.Ksoup
import com.sermilion.readability4k.model.ArticleMetadata
import com.sermilion.readability4k.model.ReadabilityOptions
import com.sermilion.readability4k.processor.ArticleGrabber
import com.sermilion.readability4k.processor.MetadataParser
import com.sermilion.readability4k.processor.Postprocessor
import com.sermilion.readability4k.processor.Preprocessor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class Readability4KMockTest : FunSpec({

  context("Readability4K with mocked processors") {
    test("should call preprocessor to prepare document") {
      val html = "<html><body><article><p>Test content</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Extracted content</p>")

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(
        title = "Test Title",
        byline = "Test Author",
      )
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns null
      every { articleGrabber.articleDir } returns "ltr"
      every { articleGrabber.articleLang } returns "en"

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      readability.parse()

      verify(exactly = 1) { preprocessor.prepareDocument(document) }
    }

    test("should call metadataParser to extract metadata") {
      val html = "<html><body><article><p>Test content</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Extracted content</p>")

      val expectedMetadata = ArticleMetadata(
        title = "Metadata Title",
        byline = "Metadata Author",
        excerpt = "Test excerpt",
      )

      every { metadataParser.getArticleMetadata(any(), any()) } returns expectedMetadata
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns null
      every { articleGrabber.articleDir } returns "ltr"
      every { articleGrabber.articleLang } returns "en"

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      val article = readability.parse()

      verify(exactly = 1) { metadataParser.getArticleMetadata(document, false) }
      article.title shouldBe "Metadata Title"
      article.byline shouldBe "Metadata Author"
    }

    test("should call articleGrabber to extract content") {
      val html = "<html><body><article><p>Test content</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Grabbed article content</p>")

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(title = "Test")
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns "Grabber Byline"
      every { articleGrabber.articleDir } returns "rtl"
      every { articleGrabber.articleLang } returns "ar"

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      val article = readability.parse()

      verify(exactly = 1) { articleGrabber.grabArticle(any(), any(), any(), any()) }
      article.byline shouldBe "Grabber Byline"
      article.dir shouldBe "rtl"
      article.lang shouldBe "ar"
    }

    test("should call postprocessor to clean content") {
      val html = "<html><body><article><p>Test content</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Content to postprocess</p>")

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(title = "Test")
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns null
      every { articleGrabber.articleDir } returns null
      every { articleGrabber.articleLang } returns null

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      readability.parse()

      verify(exactly = 1) {
        postprocessor.postProcessContent(
          originalDocument = document,
          articleContent = articleContent,
          articleUri = "https://example.com",
          additionalClassesToPreserve = emptyList(),
          keepClasses = false,
        )
      }
    }

    test("should handle null article content from grabber") {
      val html = "<html><body><p>Minimal content</p></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(title = "Test")
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns null
      every { articleGrabber.articleByline } returns null
      every { articleGrabber.articleDir } returns null
      every { articleGrabber.articleLang } returns null

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      val article = readability.parse()

      article.articleContent shouldBe null
      article.content shouldBe null
      verify(exactly = 0) { postprocessor.postProcessContent(any(), any(), any(), any(), any()) }
    }

    test("should use custom options for disableJSONLD") {
      val html = "<html><body><article><p>Test</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Content</p>")

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(title = "Test")
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns null
      every { articleGrabber.articleDir } returns null
      every { articleGrabber.articleLang } returns null

      val options = ReadabilityOptions(disableJSONLD = true)

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        options = options,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      readability.parse()

      verify(exactly = 1) { metadataParser.getArticleMetadata(document, true) }
    }

    test("should pass keepClasses option to postprocessor") {
      val html = "<html><body><article><p>Test</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Content</p>")

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(title = "Test")
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns null
      every { articleGrabber.articleDir } returns null
      every { articleGrabber.articleLang } returns null

      val options = ReadabilityOptions(keepClasses = true)

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        options = options,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      readability.parse()

      verify(exactly = 1) {
        postprocessor.postProcessContent(
          originalDocument = document,
          articleContent = articleContent,
          articleUri = "https://example.com",
          additionalClassesToPreserve = emptyList(),
          keepClasses = true,
        )
      }
    }
  }

  context("processor integration") {
    test("should prefer metadata byline over articleGrabber byline") {
      val html = "<html><body><article><p>Test</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Content</p>")

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(
        title = "Test",
        byline = "Metadata Byline",
      )
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns "Grabber Byline"
      every { articleGrabber.articleDir } returns null
      every { articleGrabber.articleLang } returns null

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      val article = readability.parse()

      article.byline shouldBe "Metadata Byline"
    }

    test("should fall back to metadata byline when grabber byline is null") {
      val html = "<html><body><article><p>Test</p></article></body></html>"
      val document = Ksoup.parse(html, "https://example.com")

      val preprocessor = mockk<Preprocessor>(relaxUnitFun = true)
      val metadataParser = mockk<MetadataParser>()
      val articleGrabber = mockk<ArticleGrabber>()
      val postprocessor = mockk<Postprocessor>(relaxUnitFun = true)

      val articleContent = document.createElement("div")
      articleContent.html("<p>Content</p>")

      every { metadataParser.getArticleMetadata(any(), any()) } returns ArticleMetadata(
        title = "Test",
        byline = "Metadata Byline",
      )
      every { articleGrabber.grabArticle(any(), any(), any(), any()) } returns articleContent
      every { articleGrabber.articleByline } returns null
      every { articleGrabber.articleDir } returns null
      every { articleGrabber.articleLang } returns null

      val readability = Readability4K(
        uri = "https://example.com",
        document = document,
        preprocessor = preprocessor,
        metadataParser = metadataParser,
        articleGrabber = articleGrabber,
        postprocessor = postprocessor,
      )

      val article = readability.parse()

      article.byline shouldBe "Metadata Byline"
    }
  }
})
