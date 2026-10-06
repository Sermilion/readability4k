package com.sermilion.readability4k

import com.sermilion.readability4k.processor.AuthorBioDisclaimerExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class AuthorBioDisclaimerRemovalTest : FunSpec({

  test("default parse keeps a bio the extension would remove") {
    val html = articleHtml(
      body = longBody(),
      extras = tweakTownCard(),
    )
    val article = Readability4K(PAGE_URL, html).parse()
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldContain TWEAKTOWN_BIO
    markup shouldContain TWEAKTOWN_BIO
    article.byline shouldBe "About the author"
  }

  test("injected extension removes a paragraph author label and keeps the next section") {
    val html = articleHtml(
      body = longBody(),
      extras = tweakTownCard() + nextSection(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldContain NEXT_SECTION_HEADING
    text shouldContain NEXT_SECTION_BODY
    text shouldNotContain TWEAKTOWN_BIO
    text shouldNotContain "About the author"
    markup shouldContain NEXT_SECTION_HEADING
    markup shouldNotContain TWEAKTOWN_BIO
  }

  test("long dense bio with a label is removed despite length and commas") {
    val html = articleHtml(
      body = longBody() + longerPrecedingBody(),
      extras = """
        <h2>About the author</h2>
        <p>$LONG_DENSE_BIO</p>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldNotContain LONG_DENSE_BIO.take(80)
    text shouldNotContain "About the author"
    markup shouldContain BODY_MARKER
    markup shouldNotContain "About the author"
  }

  test("bio that is most of the article is kept by the fraction veto") {
    val html = articleHtml(
      body = SHORT_BODY,
      extras = """
        <div class="author-bio">
          <p>About the author</p>
          <p>$FRACTION_BIO</p>
        </div>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain FRACTION_BIO
    markup shouldContain FRACTION_BIO
  }

  test("author-bio wrapper that is most of the text stays while an inner bio is removed") {
    val html = articleHtml(
      body = "",
      extras = """
        <div class="author-bio">
          <p>$BODY_ONE</p>
          <p>$BODY_TWO</p>
          <p>$BODY_THREE</p>
          <aside class="author-box journalist" rel="author" itemtype="http://schema.org/Person">
            <p>$INNER_BIO</p>
          </aside>
        </div>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldNotContain INNER_BIO
    markup shouldContain BODY_MARKER
    markup shouldNotContain INNER_BIO
  }

  test("short disclaimer with a label is removed and the next heading stays") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <h2>Disclaimer</h2>
        <p>$SHORT_DISCLAIMER</p>
        <h2>$NEXT_SECTION_HEADING</h2>
        <p>$NEXT_SECTION_BODY</p>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldContain NEXT_SECTION_HEADING
    text shouldNotContain SHORT_DISCLAIMER
    markup shouldContain NEXT_SECTION_HEADING
    markup shouldNotContain SHORT_DISCLAIMER
  }

  test("substantive disclaimer with commas is kept") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <div class="disclaimer">
          <p>Disclaimer</p>
          <p>$SUBSTANTIVE_DISCLAIMER</p>
        </div>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain SUBSTANTIVE_DISCLAIMER
    markup shouldContain SUBSTANTIVE_DISCLAIMER
  }

  test("person-profile block that is most of the article is kept") {
    val html = articleHtml(
      body = SHORT_BODY,
      extras = """
        <aside class="journalist" rel="author" itemtype="http://schema.org/Person">
          <p>$PROFILE_BIO</p>
        </aside>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain PROFILE_BIO
    markup shouldContain PROFILE_BIO
  }

  test("trailing person block with rel, Person, and journalist is removed") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <aside class="journalist" rel="author" itemtype="http://schema.org/Person">
          <p>$INNER_BIO</p>
        </aside>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldNotContain INNER_BIO
    markup shouldContain BODY_MARKER
    markup shouldNotContain INNER_BIO
  }

  test("heading bio span stops before the next equal heading") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <h2>About the author</h2>
        <p>$TWEAKTOWN_BIO</p>
        <h2>$NEXT_SECTION_HEADING</h2>
        <p>$NEXT_SECTION_BODY</p>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain NEXT_SECTION_HEADING
    text shouldContain NEXT_SECTION_BODY
    text shouldNotContain TWEAKTOWN_BIO
    markup shouldContain "<h2>$NEXT_SECTION_HEADING</h2>"
    markup shouldNotContain TWEAKTOWN_BIO
  }

  test("nested qualifying cards remove the outer node once") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <div class="author-box">
          <div class="author-bio">
            <p>$INNER_BIO</p>
          </div>
        </div>
        <p>$AFTER_CARD</p>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldContain AFTER_CARD
    text shouldNotContain INNER_BIO
    markup shouldContain AFTER_CARD
    markup shouldNotContain INNER_BIO
  }

  test("unwrapped div snapshot attributes still remove the bio") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <div class="author-bio" itemtype="http://schema.org/Person">
          <p>$UNWRAP_BIO</p>
        </div>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldNotContain UNWRAP_BIO
    markup shouldContain BODY_MARKER
    markup shouldNotContain UNWRAP_BIO
  }

  test("bio after a negative-weight heading is still removed") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <h2 class="hidden">About the author</h2>
        <p>$TWEAKTOWN_BIO</p>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldNotContain TWEAKTOWN_BIO
    markup shouldContain BODY_MARKER
    markup shouldNotContain TWEAKTOWN_BIO
  }

  test("short page still drops the bio on the weightClasses retry") {
    val html = articleHtml(
      body = SHORT_RETRY_BODY,
      extras = """
        <div class="author-bio">
          <p>$SHORT_RETRY_BIO</p>
        </div>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent?.text().orEmpty()
    val markup = article.articleContent?.html().orEmpty()
    article.articleContent shouldNotBe null
    text shouldContain "four-hour window"
    text shouldNotContain SHORT_RETRY_BIO
    markup shouldNotContain SHORT_RETRY_BIO
  }

  test("one extension instance does not leak removal into the next parse") {
    val extension = AuthorBioDisclaimerExtension()
    val first = Readability4K(
      uri = PAGE_URL,
      html = articleHtml(body = longBody(), extras = tweakTownCard()),
      contentExtension = extension,
    ).parse()
    first.articleContent!!.text() shouldNotContain TWEAKTOWN_BIO

    val second = Readability4K(
      uri = PAGE_URL,
      html = articleHtml(
        body = SHORT_BODY,
        extras = """
          <div class="author-bio">
            <p>$COMPOUND_ONLY_BIO</p>
          </div>
          <p>$BODY_ONE</p>
          <p>$BODY_TWO</p>
          <p>$BODY_THREE</p>
        """.trimIndent(),
      ),
      contentExtension = extension,
    ).parse()
    val text = second.articleContent!!.text()
    val markup = second.articleContent!!.html()
    text shouldContain COMPOUND_ONLY_BIO
    markup shouldContain COMPOUND_ONLY_BIO
  }

  test("parse and parseAsync return the same text for an injected fixture") {
    val html = articleHtml(body = longBody(), extras = tweakTownCard() + nextSection())
    val extension = AuthorBioDisclaimerExtension()
    val sync = Readability4K(uri = PAGE_URL, html = html, contentExtension = extension).parse()
    val async = Readability4K(uri = PAGE_URL, html = html, contentExtension = extension).parseAsync()
    sync.articleContent!!.text() shouldBe async.articleContent!!.text()
    sync.articleContent!!.html() shouldBe async.articleContent!!.html()
  }

  test("a body sentence that only contains the author phrase is kept") {
    val html = articleHtml(
      body = """
        <p>$BODY_ONE</p>
        <p>$BODY_TWO</p>
        <p>Editors later added a note about the author in the margin of the printed edition.</p>
        <p>$BODY_THREE</p>
      """.trimIndent(),
      extras = "",
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain "note about the author in the margin"
    markup shouldContain "note about the author in the margin"
  }

  test("author div retagged to a paragraph is still removed") {
    val html = articleHtml(
      body = """
        <div>
          ${longBody()}
          <div class="author-bio" itemtype="http://schema.org/Person">$UNWRAP_BIO</div>
        </div>
      """.trimIndent(),
      extras = "",
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain BODY_MARKER
    text shouldNotContain UNWRAP_BIO
    markup shouldContain BODY_MARKER
    markup shouldNotContain UNWRAP_BIO
  }

  test("span whose text equals a label does not remove a bare author block") {
    val html = articleHtml(
      body = longBody(),
      extras = """
        <div class="author">
          <span>Disclaimer</span>
          <p>$UNWRAP_BIO</p>
        </div>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain UNWRAP_BIO
    markup shouldContain UNWRAP_BIO
  }

  test("label plus descendant rel and link density does not remove a trailing block") {
    val html = articleHtml(
      body = SHORT_BODY,
      extras = """
        <h2>About the author</h2>
        <p>
          <a rel="author" href="/jane">$AUTHOR_LINK</a>
          <a href="/tw">Twitter profile</a>
          <a href="/fb">Facebook page</a>
          <a href="/ig">Instagram</a>
        </p>
        <h2>$NEXT_SECTION_HEADING</h2>
        <p>$BODY_ONE</p>
        <p>$BODY_TWO</p>
        <p>$BODY_THREE</p>
      """.trimIndent(),
    )
    val article = parseWithExtension(html)
    val text = article.articleContent!!.text()
    val markup = article.articleContent!!.html()
    text shouldContain "About the author"
    text shouldContain "Twitter profile"
    markup shouldContain "About the author"
  }
})

private const val PAGE_URL = "https://example.com"

private const val BODY_MARKER = "hour-long waits and missed shifts"

private const val BODY_ONE =
  "The city council voted on Tuesday to expand overnight bus service after residents " +
    "described hour-long waits and missed shifts at the food warehouse on the north side of town."

private const val BODY_TWO =
  "Transit staff said the extra runs would use existing depots and would not require a new " +
    "bond issue before the winter schedule is printed for riders in the eastern precincts."

private const val BODY_THREE =
  "Neighborhood groups asked for paper timetables at libraries so shift workers without " +
    "smartphones could still plan the late trip home after the warehouse overtime window."

private const val SHORT_BODY =
  "The council met at noon and adjourned before the storm reached the river district."

private const val TWEAKTOWN_BIO =
  "Jane Fairchild has covered PC hardware, chip foundries, and retail pricing for more than " +
    "a decade and previously edited a weekly newsletter for boutique system builders."

private const val INNER_BIO =
  "I write about IT security and artificial intelligence for everyday users and still " +
    "publish weekly explainers that walk through practical settings on phones, laptops, " +
    "and home routers without assuming a computer-science background."

private const val UNWRAP_BIO =
  "Marcus Hale is a veteran journalist covering semiconductor fabrication, yield reports, " +
    "and foundry contracts, and he previously spent six years on a factory floor in Oregon."

private const val COMPOUND_ONLY_BIO =
  "Compound-only bio copy that must remain when the class is the sole author signal and " +
    "no second source of points is present on this trailing box."

private const val AFTER_CARD =
  "Retailers confirmed the restock would arrive before Friday's advertised discount window."

private const val NEXT_SECTION_HEADING = "What comes next"
private const val NEXT_SECTION_BODY =
  "Engineers will publish the firmware changelog after the holiday freeze lifts next month."

private const val SHORT_DISCLAIMER =
  "This article is for general information and is not legal advice for readers."

private const val SHORT_RETRY_BODY =
  "The warehouse on the north side posted overtime for Friday night only after the " +
    "storm delayed the morning trucks and the night crew asked for a four-hour window. " +
    "The dock stayed open for the late trucks."

private const val SHORT_RETRY_BIO =
  "Pat Lumen edits gadget roundups, once worked a retail service desk, and still " +
    "answers reader mail about chargers every weekday afternoon."

private const val AUTHOR_LINK =
  "Jane Fairchild, senior correspondent for hardware and retail pricing coverage " +
    "across North America and Europe since 2014"

private const val FRACTION_BIO =
  "This profile follows the inventor from the first garage prototype through the " +
    "public offering, quoting lab notebooks, supplier invoices, and interviews with " +
    "former colleagues who still remember the overnight board spins in 2009. The " +
    "narrative stays with that career and does not return to a separate news lede."

private const val PROFILE_BIO =
  "Alex Rivera reports on person-centered technology policy and has spent twenty years " +
    "interviewing engineers, regulators, and patients about how medical software is " +
    "reviewed. This page is that reporter's own profile, not a news article with a " +
    "trailing sidebar, and it remains the primary text on the document."

private val LONG_DENSE_BIO = buildString {
  append(
    "She has written columns, essays, reviews, notes, letters, reports, briefs, memos, " +
      "features, explainers, and weekend magazines for newspapers, weeklies, monthlies, ",
  )
  repeat(8) {
    append(
      "trade journals, newsletters, and radio scripts, always returning to chip supply, " +
        "retail pricing, and factory safety, ",
    )
  }
  append("before joining the present desk.")
}

private val SUBSTANTIVE_DISCLAIMER = buildString {
  append(
    "Readers should treat prices, availability, shipping estimates, tax notes, rebate " +
      "paperwork, warranty language, return windows, restocking fees, financing offers, ",
  )
  append(
    "and regional exclusions as examples only, because merchants change terms, " +
      "bundles, and inventory without notice, and this explainer cannot replace " +
      "the contract, invoice, or counsel that applies to a specific purchase.",
  )
}

private fun longBody(): String = """
  <p>$BODY_ONE</p>
  <p>$BODY_TWO</p>
  <p>$BODY_THREE</p>
""".trimIndent()

private fun longerPrecedingBody(): String = buildString {
  repeat(6) {
    append("<p>$BODY_ONE $BODY_TWO $BODY_THREE</p>")
  }
}

private fun tweakTownCard(): String = """
  <div class="tt-author-cards">
    <p class="tt-author-cards__label">About the author</p>
    <p>$TWEAKTOWN_BIO</p>
  </div>
""".trimIndent()

private fun nextSection(): String = """
  <h2>$NEXT_SECTION_HEADING</h2>
  <p>$NEXT_SECTION_BODY</p>
""".trimIndent()

private fun articleHtml(body: String, extras: String): String = """
  <html>
    <head><title>Overnight buses and warehouse shifts</title></head>
    <body>
      <article>
        <h1>Overnight buses and warehouse shifts</h1>
        $body
        $extras
      </article>
    </body>
  </html>
""".trimIndent()

private fun parseWithExtension(html: String) = Readability4K(
  uri = PAGE_URL,
  html = html,
  contentExtension = AuthorBioDisclaimerExtension(),
).parse()
