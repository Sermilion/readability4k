package com.sermilion.readability4k

import com.sermilion.readability4k.util.RegExUtil
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class NameSignalTest : FunSpec({

  test("utility class vocabulary matches presentational tokens only") {
    val regEx = RegExUtil()

    listOf(
      "text-sm",
      "text-xs",
      "text-gray-500",
      "text-[#5B616E]",
      "text-[18px]",
      "md:hidden",
      "hover:text-blue-600",
      "[&>p]:mb-4",
      "overflow-hidden",
      "overflow-x-auto",
      "scroll-smooth",
      "scroll-mt-4",
      "content-center",
      "justify-content-between",
      "place-content-center",
    ).forEach { regEx.isUtilityClass(it) shouldBe true }

    listOf(
      "article-text",
      "storytext",
      "content",
      "content-wrapper",
      "entry-content",
      "hidden",
      "footer",
      "site-footer",
      "overflow-menu",
      "scroll-container",
    ).forEach { regEx.isUtilityClass(it) shouldBe false }
  }

  test("investing.com footer marked only by data-test and a text- utility does not replace the article") {
    val text = Readability4K(INVESTING_URL, investingPage()).parse().articleContent?.text().orEmpty()

    text shouldContain "Former Anthropic researcher Jacob Coxon will testify"
    text shouldContain "Reuters could not immediately verify the report."
    text shouldNotContain "Fusion Media"
    text shouldNotContain "Risk Disclosure"
  }

  test("chrome named only by a test hook is stripped like a class name") {
    val html = """
      <html>
        <body>
          <div class="css-1x2y3z">
            ${paragraphs(ARTICLE_SENTENCE, count = 5)}
          </div>
          <div class="css-9q8w7e" data-testid="related-stories">
            ${paragraphs(RELATED_SENTENCE, count = 8)}
          </div>
        </body>
      </html>
    """.trimIndent()

    val text = Readability4K("https://example.com/story", html).parse().articleContent?.text().orEmpty()

    text shouldContain ARTICLE_SENTENCE
    text shouldNotContain RELATED_SENTENCE
  }
})

private const val INVESTING_URL =
  "https://www.investing.com/news/economy-news/" +
    "former-anthropic-researcher-coxon-to-testify-at-new-york-city-ai-hearing-bloomberg-news-reports-4930955"

private const val ARTICLE_SENTENCE =
  "The council published the hearing agenda, listing witnesses, topics, and the order of testimony for the session."

private const val RELATED_SENTENCE =
  "Read next: markets, rates, earnings, and the week ahead, plus more stories, picks, and analysis you may like."

private fun paragraphs(sentence: String, count: Int): String = List(count) { "<p>$sentence</p>" }.joinToString("\n")

private fun investingPage(): String = """
  <!DOCTYPE html>
  <html lang="en">
    <head>
      <title>Former Anthropic researcher Coxon to testify at New York City AI hearing, Bloomberg News reports By Reuters</title>
    </head>
    <body>
      <div id="__next">
        <div class="flex flex-col">
          <h1 id="articleTitle" class="text-xl/7 font-bold sm:text-3xl/8">
            Former Anthropic researcher Coxon to testify at New York City AI hearing, Bloomberg News reports
          </h1>
          <div class="article_WYSIWYG__O0uhw article_articlePage__UMz3q text-[18px] leading-8">
            <p>Oct 4 (Reuters) - Former Anthropic researcher Jacob Coxon will testify at a New York City hearing
            on artificial intelligence, Bloomberg News reported on Sunday, citing people familiar with the matter.</p>
            <p>Coxon will appear at the request of New York City Council Speaker Julie Menin, who has urged AI
            whistleblowers to testify as city lawmakers weigh a package of bills aimed at establishing safeguards
            around the technology, the report said.</p>
            <p>Coxon quit Anthropic last month, issuing warnings that the "people building AI earnestly believe that
            it could kill us all by the end of the decade" and accusing his former employer and OpenAI of
            "gambling with our lives."</p>
            <p>Reuters could not immediately verify the report.</p>
          </div>
        </div>
        <div data-test="footer" class="mx-auto max-w-[1280px] px-4 text-xs text-[#5B616E]">
          <p>Risk Disclosure: Trading in financial instruments and/or cryptocurrencies involves high risks
          including the risk of losing some, or all, of your investment amount, and may not be suitable for all
          investors. Prices of cryptocurrencies are extremely volatile and may be affected by external factors such
          as financial, regulatory or political events. Trading on margin increases the financial risks.</p>
          <p>Before deciding to trade in financial instrument or cryptocurrencies you should be fully informed of
          the risks and costs associated with trading the financial markets, carefully consider your investment
          objectives, level of experience, and risk appetite, and seek professional advice where needed.</p>
          <p>Fusion Media would like to remind you that the data contained in this website is not necessarily
          real-time nor accurate. The data and prices on the website are not necessarily provided by any market or
          exchange, but may be provided by market makers, and so prices may not be accurate and may differ from the
          actual price at any given market, meaning prices are indicative and not appropriate for trading purposes.
          Fusion Media and any provider of the data contained in this website will not accept liability for any loss
          or damage as a result of your trading, or your reliance on the information contained within this
          website.</p>
          <p>It is prohibited to use, store, reproduce, display, modify, transmit or distribute the data contained
          in this website without the explicit prior written permission of Fusion Media and/or the data provider.
          All intellectual property rights are reserved by the providers and/or the exchange providing the data
          contained in this website.</p>
          <p>Fusion Media may be compensated by the advertisers that appear on the website, based on your
          interaction with the advertisements or advertisers.</p>
        </div>
      </div>
    </body>
  </html>
""".trimIndent()
