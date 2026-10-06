package com.sermilion.readability4k

import com.sermilion.readability4k.processor.AuthorBioDisclaimerExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

/**
 * Offline regression over ~100 real publisher HTML pages for scored author-bio /
 * disclaimer cleanup. Fixtures live under `jvmTest/resources/author-bio-corpus/`
 * (see CORPUS.md). Parses with [AuthorBioDisclaimerExtension].
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

      test("every corpus article keeps body text; calibration and seed hosts drop annotated bios") {
        val sortedHosts = AuthorBioCorpusIndex.ARTICLES.map { it.host }.distinct().sorted()
        val missingBody = mutableListOf<String>()
        val absentHits = mutableListOf<String>()
        val reportedValidationAbsent = mutableListOf<String>()
        for (entry in AuthorBioCorpusIndex.ARTICLES) {
          val resourcePath = "author-bio-corpus/${entry.file}"
          val htmlStream = this::class.java.classLoader.getResourceAsStream(resourcePath)
          if (htmlStream == null) {
            missingBody += "${entry.host}/${entry.id}: missing resource $resourcePath"
            continue
          }
          val html = htmlStream.bufferedReader().use { it.readText() }
          val article = try {
            Readability4K(
              uri = entry.finalUrl,
              html = html,
              contentExtension = AuthorBioDisclaimerExtension(),
            ).parse()
          } catch (error: Exception) {
            missingBody +=
              "${entry.host}/${entry.id}: parse threw ${error::class.simpleName}: ${error.message}"
            continue
          }
          val text = article.articleContent?.text().orEmpty()
          if (article.articleContent == null || text.length < 120) {
            missingBody += "${entry.host}/${entry.id}: extracted text too short (${text.length})"
            continue
          }
          for (phrase in entry.mustKeep) {
            if (!text.contains(phrase)) {
              missingBody += "${entry.host}/${entry.id}: missing must_keep=${phrase.take(80)}"
            }
          }
          for (absent in entry.expectAbsent) {
            if (text.contains(absent)) {
              val line = "${entry.host}/${entry.id}: still contains expect_absent=$absent"
              if (isHardFailAbsentHost(entry.host, sortedHosts)) {
                absentHits += line
              } else {
                reportedValidationAbsent += line
              }
            }
          }
        }
        if (reportedValidationAbsent.isNotEmpty()) {
          println(
            "Validation-host expect_absent hits (reported, not failing):\n" +
              reportedValidationAbsent.joinToString("\n"),
          )
        }
        val groupedMissing = missingBody.groupBy { it.substringBefore("/") }
        val groupedAbsent = absentHits.groupBy { it.substringBefore("/") }
        if (missingBody.isNotEmpty() || absentHits.isNotEmpty()) {
          error(
            "Author-bio corpus regressions: missing body phrases=${missingBody.size}, " +
              "expect_absent hits=${absentHits.size}\n" +
              "missing by host: $groupedMissing\n" +
              "absent hits by host: $groupedAbsent\n" +
              (missingBody + absentHits).joinToString("\n"),
          )
        }
      }
    },
  )

internal fun isValidationHost(host: String, sortedHosts: List<String>): Boolean {
  val index = sortedHosts.indexOf(host)
  return host == "notebookcheck.net" ||
    host == "tweaktown.com" ||
    (index >= 0 && index % 3 == 0)
}

private fun isHardFailAbsentHost(host: String, sortedHosts: List<String>): Boolean {
  if (host == "notebookcheck.net" || host == "tweaktown.com") {
    return true
  }
  return !isValidationHost(host, sortedHosts)
}
