package com.sermilion.readability4k.style

import com.sermilion.readability4k.Readability4K
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class SocialMediaStyleTest : FunSpec({

  test("should generate default CSS stylesheet") {
    val styles = socialMediaStyles()
    val css = styles.generateCSS()

    css shouldContain ".twitter-tweet"
    css shouldContain ".instagram-media"
    css shouldContain ".tiktok-embed"
    css shouldContain ".reddit-embed-bq"
    css shouldContain ".fb-post"
    css shouldContain "background: #f7f9fa"
    css shouldContain "color: #1DA1F2"
    css shouldContain "@media (prefers-color-scheme: dark)"
  }

  test("should customize Twitter styles") {
    val styles = socialMediaStyles {
      lightMode {
        twitter {
          backgroundColor = "#ffffff"
          accentColor = "#000000"
          icon = "🐦"
        }
      }
    }

    val css = styles.generateCSS()
    css shouldContain "background: #ffffff"
    css shouldContain "color: #000000"
    css shouldContain "content: \"🐦\""
  }

  test("should disable dark mode") {
    val styles = socialMediaStyles {
      disableDarkMode()
    }

    val css = styles.generateCSS()
    css shouldNotContain "@media (prefers-color-scheme: dark)"
  }

  test("should render full HTML with styles") {
    val html = """
      <article>
        <h1>Article Title</h1>
        <blockquote class="twitter-tweet">
          <p>Tweet content</p>
        </blockquote>
      </article>
    """.trimIndent()

    val styles = socialMediaStyles()
    val rendered = styles.renderHtml(html)

    rendered shouldContain "<!DOCTYPE html>"
    rendered shouldContain "<style>"
    rendered shouldContain ".twitter-tweet"
    rendered shouldContain "<article>"
    rendered shouldContain "viewport"
  }

  test("should render inline styles") {
    val html = """
      <blockquote class="twitter-tweet">
        <p>Tweet content</p>
      </blockquote>
    """.trimIndent()

    val styles = socialMediaStyles()
    val rendered = styles.renderHtml(html, StyleRenderers.inline)

    rendered shouldContain "<style>"
    rendered shouldContain ".twitter-tweet"
    rendered shouldContain "Tweet content"
    rendered shouldNotContain "<!DOCTYPE html>"
  }

  test("should work with Readability4K extraction") {
    val htmlSource = """
      <!DOCTYPE html>
      <html>
      <head><title>News Article</title></head>
      <body>
        <article>
          <h1>Breaking News</h1>
          <p>See the official statement:</p>
          <blockquote class="twitter-tweet">
            <p>Official announcement text</p>
            — Source (@source) January 1, 2026
          </blockquote>
          <p>More analysis here.</p>
        </article>
      </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", htmlSource)
    val article = readability.parse()

    article shouldNotBe null

    val styles = socialMediaStyles()
    val styledHtml = styles.renderHtml(article.content.orEmpty())

    styledHtml shouldContain "<!DOCTYPE html>"
    styledHtml shouldContain ".twitter-tweet"
    styledHtml shouldContain "Official announcement text"
  }

  test("should customize all platforms") {
    val styles = socialMediaStyles {
      lightMode {
        twitter {
          accentColor = "#custom1"
        }
        instagram {
          accentColor = "#custom2"
        }
        tiktok {
          accentColor = "#custom3"
        }
        reddit {
          accentColor = "#custom4"
        }
        facebook {
          accentColor = "#custom5"
        }
      }
    }

    val css = styles.generateCSS()
    css shouldContain "#custom1"
    css shouldContain "#custom2"
    css shouldContain "#custom3"
    css shouldContain "#custom4"
    css shouldContain "#custom5"
  }

  test("should handle custom base styles") {
    val baseStyles = """
      body {
        font-family: serif;
        max-width: 800px;
        margin: 0 auto;
      }
    """.trimIndent()

    val html = "<p>Content</p>"
    val styles = socialMediaStyles()
    val rendered = styles.renderHtml(
      html,
      StyleRenderers.html(baseStyles = baseStyles),
    )

    rendered shouldContain "font-family: serif"
    rendered shouldContain "max-width: 800px"
    rendered shouldContain ".twitter-tweet"
  }
})
