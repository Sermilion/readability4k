package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * Offline regression over ~100 real publisher HTML pages for author-bio /
 * disclaimer stripping. Fixtures live under
 * `jvmTest/resources/author-bio-corpus/` (see CORPUS.md).
 */
class AuthorBioCorpusRegressionTest :
  FunSpec(
    {
      test("corpus index has about 100 real articles across many hosts") {
        AuthorBioCorpusIndex.COUNT shouldBe AuthorBioCorpusIndex.ARTICLES.size
        AuthorBioCorpusIndex.ARTICLES.size shouldBeGreaterThan 90
        AuthorBioCorpusIndex.ARTICLES.map { it.host }.toSet().size shouldBeGreaterThan 20
        AuthorBioCorpusIndex.ARTICLES.count { it.expectAbsent.isNotEmpty() } shouldBeGreaterThan 15
      }

      test("every corpus article keeps body text and drops annotated author-box phrases") {
        val failures = mutableListOf<String>()
        for (entry in AuthorBioCorpusIndex.ARTICLES) {
          val resourcePath = "author-bio-corpus/${entry.file}"
          val htmlStream = this::class.java.classLoader.getResourceAsStream(resourcePath)
          if (htmlStream == null) {
            failures += "${entry.id}: missing resource $resourcePath"
            continue
          }
          val html = htmlStream.bufferedReader().use { it.readText() }
          val article = try {
            Readability4K(entry.finalUrl, html).parse()
          } catch (error: Exception) {
            failures += "${entry.id}: parse threw ${error::class.simpleName}: ${error.message}"
            continue
          }
          val text = article.articleContent?.text().orEmpty()
          if (article.articleContent == null || text.length < 120) {
            failures += "${entry.id}: extracted text too short (${text.length})"
            continue
          }
          if (!text.contains(entry.mustKeep)) {
            failures += "${entry.id}: missing must_keep=${entry.mustKeep.take(80)}"
          }
          for (absent in entry.expectAbsent) {
            if (text.contains(absent)) {
              failures += "${entry.id}: still contains expect_absent=$absent"
            }
          }
        }
        if (failures.isNotEmpty()) {
          error(
            "Author-bio corpus regressions (${failures.size}/${AuthorBioCorpusIndex.ARTICLES.size}):\n" +
              failures.joinToString("\n"),
          )
        }
      }

      test("seed Notebookcheck ChatGPT page drops the sidebar bio") {
        val entry = AuthorBioCorpusIndex.ARTICLES.first {
          it.id == "notebookcheck-net-828e157eb8" || it.finalUrl.contains("1416940")
        }
        val html = this::class.java.classLoader
          .getResourceAsStream("author-bio-corpus/${entry.file}")!!
          .bufferedReader()
          .use { it.readText() }
        val article = Readability4K(entry.finalUrl, html).parse()
        val text = article.articleContent!!.text()
        text shouldContain entry.mustKeep
        text shouldNotContain "I write about IT security and artificial intelligence"
        text shouldNotContain "Golem.de"
        text shouldNotContain "practical AI for everyday users"
      }

      test("seed TweakTown page drops the About the author card") {
        val entry = AuthorBioCorpusIndex.ARTICLES.first {
          it.finalUrl.contains("tweaktown.com") && it.finalUrl.contains("113920")
        }
        val html = this::class.java.classLoader
          .getResourceAsStream("author-bio-corpus/${entry.file}")!!
          .bufferedReader()
          .use { it.readText() }
        val article = Readability4K(entry.finalUrl, html).parse()
        val text = article.articleContent!!.text()
        text shouldContain entry.mustKeep
        text shouldNotContain "About the author"
        article.articleContent shouldNotBe null
      }
    },
  )
