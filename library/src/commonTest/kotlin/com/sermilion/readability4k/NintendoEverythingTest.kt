package com.sermilion.readability4k

import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class NintendoEverythingTest {

  @Test
  fun `should extract Xenoblade article from nintendoeverything com`() {
    val html = """
<!DOCTYPE HTML>
<html lang="en">
<head>
  <title>All four Xenoblade games need Switch 2 Editions</title>
  <meta name="description" content="We look at each Xenoblade Chronicles game's performance on Nintendo Switch and how each one would be impacted by a Switch 2 Edition or patch." />
  <meta property="og:title" content="Why all four Xenoblade Chronicles games need Nintendo Switch 2 Editions" />
  <meta name="author" content="Ethan" />
</head>
<body>
  <div class="newsbanner">
    <div class="large-6 columns text-left">
      <a href="https://nintendoeverything.com/contact-us/"><p>Submit a news tip</p></a>
    </div>
  </div>

  <div class="navbar">
    <div class="nav">
      <div class="logo">
        <a href="https://nintendoeverything.com"><img src="logo.png" alt="Nintendo Everything logo"></a>
      </div>
      <div class="navigation">
        <ul id="menu-header-menu">
          <li><a href="/">Home</a></li>
          <li><a href="/features/">Features</a></li>
          <li><a href="/news/">News</a></li>
        </ul>
      </div>
    </div>
  </div>

  <div class="main">
    <div class="row collapse">
      <div class="large-8 columns">
        <div class="content">
          <div class="large-12 columns">
            <h1>Why all four Xenoblade Chronicles games need Nintendo Switch 2 Editions</h1>
            <div class="extra-single">
              Posted on November 16, 2025 by <a href="/author/cloudnine987/">Ethan</a>
            </div>
          </div>

          <div class="large-12 columns">
            <p>If you're at all familiar with JRPGs, then you've definitely at least heard of the Xenoblade Chronicles series. And if you've only heard of them and haven't played them, you should definitely change that as soon as possible – or, potentially, wait just a little longer. Recently, Monolith Soft shared a recruitment video on Twitter, and fans noticed that the included footage of Xenoblade Chronicles 2 looked a little bit different than they were used to.</p>

            <p>Starting with Xenoblade Chronicles: Definitive Edition, this game definitely needs some help. It's a massive visual improvement over the original Wii version, with much more expressive and dynamic-looking characters and super-detailed and vast environments. In docked mode, it generally runs at around 720p and in handheld mode it runs at around 540p.</p>

            <div class="advertisement">
              <div id="ad-slot-1">Advertisement content</div>
            </div>

            <p>Xenoblade Chronicles 2 currently fares much worse on Nintendo Switch and Nintendo Switch 2. No matter which console you play it on, it's terribly blurry – especially in handheld mode, where it supposedly renders in 360p. This is a huge shame, because once you get past that filter the game's characters and especially environments look great.</p>

            <p>Xenoblade Chronicles 3 could benefit from its own Nintendo Switch 2 Edition – it's definitely the most polished of all four games on Nintendo Switch, but that doesn't mean it couldn't use a few improvements. The game runs at a dynamic resolution that can go from 720p all the way down to 378p in portable mode.</p>

            <div class="comments">
              <h3>Comments</h3>
              <p>Comment 1</p>
              <p>Comment 2</p>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar">
        <h3>Related Posts</h3>
        <ul>
          <li><a href="#">Related article 1</a></li>
          <li><a href="#">Related article 2</a></li>
        </ul>
      </div>
    </div>
  </div>

  <div class="footer">
    <p>Footer content</p>
  </div>
</body>
</html>
    """.trimIndent()

    val readability =
      Readability4K(
        "https://nintendoeverything.com/xenoblade-chronicles-games-switch-2-editions/",
        html,
      )
    val article = readability.parse()

    article.title shouldNotBe null
    article.title shouldContain "Xenoblade"

    val content = article.articleContent?.text() ?: ""

    content shouldContain "Xenoblade Chronicles series"
    content shouldContain "Definitive Edition"
    content shouldContain "Xenoblade Chronicles 2"
    content shouldContain "Xenoblade Chronicles 3"
    content shouldContain "720p"
    content shouldContain "540p"

    content shouldNotContain "Submit a news tip"
    content shouldNotContain "Advertisement content"
    content shouldNotContain "Comment 1"
    content shouldNotContain "Comment 2"
    content shouldNotContain "Related article"
    content shouldNotContain "Footer content"
  }
}
