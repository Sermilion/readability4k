package com.sermilion.readability4k.style

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class SimpleSocialMediaStyleTest : FunSpec({

  test("should generate default social media CSS") {
    val css = generateSocialMediaCSS()

    css shouldContain ".twitter-tweet"
    css shouldContain ".instagram-media"
    css shouldContain ".tiktok-embed"
    css shouldContain "background: #f7f9fa"
    css shouldContain "@media (prefers-color-scheme: dark)"
  }

  test("should generate CSS without dark mode") {
    val css = generateSocialMediaCSS(isDarkMode = true)

    css shouldContain ".twitter-tweet"
    css shouldNotContain "@media (prefers-color-scheme: dark)"
  }

  test("should generate themed CSS") {
    val css = generateThemedSocialMediaCSS(
      backgroundColor = "#ffffff",
      textColor = "#000000",
      accentColor = "#ff0000",
    )

    css shouldContain "background: #ffffff"
    css shouldContain "color: #000000"
    css shouldContain "color: #ff0000"
  }

  test("should generate themed CSS without custom accent") {
    val css = generateThemedSocialMediaCSS(
      backgroundColor = "#ffffff",
      textColor = "#000000",
    )

    css shouldContain "background: #ffffff"
    css shouldContain "color: #000000"
    css shouldContain "#1DA1F2"
  }

  test("should allow customization") {
    val css = generateSocialMediaCSS(
      customizeLight = {
        twitter {
          icon = "🐦"
          accentColor = "#custom"
        }
      },
    )

    css shouldContain "content: \"🐦\""
    css shouldContain "color: #custom"
  }
})
