package com.sermilion.readability4k

import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class FooterDisclosureExtractionTest {

  @Test
  fun `long article body is kept when a data-test footer has text- utility classes`() {
    val html = pageHtml(
      bodyBlock = paragraphBlock(ARTICLE_SENTENCE, ARTICLE_FILLER, count = 3),
      footerBlock = paragraphBlock(DISCLOSURE_SENTENCE, FOOTER_FILLER, count = 6),
    )

    val text = Readability4K(PAGE_URL, html).parse().articleContent?.text()

    text shouldContain ARTICLE_SENTENCE
    text shouldNotContain DISCLOSURE_SENTENCE
  }

  @Test
  fun `short article body is kept when a longer data-test footer would win on retry`() {
    val html = pageHtml(
      bodyBlock = paragraphBlock(ARTICLE_SENTENCE, ARTICLE_FILLER, count = 2),
      footerBlock = paragraphBlock(DISCLOSURE_SENTENCE, FOOTER_FILLER, count = 4),
    )

    val text = Readability4K(PAGE_URL, html).parse().articleContent?.text()

    text shouldContain ARTICLE_SENTENCE
    text shouldNotContain DISCLOSURE_SENTENCE
  }

  private fun pageHtml(bodyBlock: String, footerBlock: String): String = """
    <!DOCTYPE html>
    <html>
      <head>
        <title>Economy news</title>
      </head>
      <body>
        <div>
          $bodyBlock
        </div>
        <div data-test="footer" class="text-sm text-gray-500">
          $footerBlock
        </div>
      </body>
    </html>
  """.trimIndent()

  private fun paragraphBlock(lead: String, filler: String, count: Int): String =
    List(count) { "<p>$lead $filler</p>" }.joinToString("\n")

  companion object {
    private const val PAGE_URL = "https://example.com/economy-news"
    private const val ARTICLE_SENTENCE =
      "Former Anthropic researcher Jacob Coxon will testify at a New York City AI hearing."
    private const val DISCLOSURE_SENTENCE =
      "Fusion Media may be compensated by advertisers that appear on this website."
    private const val ARTICLE_FILLER =
      "Readers following the hearing can expect detail about model safety practices in production systems."
    private const val FOOTER_FILLER =
      "This regulatory notice is provided for informational purposes and does not constitute investment advice."
  }
}
