package com.sermilion.readability4k.style

interface EmbedStylesheet {
  fun generate(lightMode: EmbedStyles = EmbedStyles(), darkMode: DarkModeStyles? = DarkModeStyles()): String
}

class DefaultEmbedStylesheet : EmbedStylesheet {

  override fun generate(lightMode: EmbedStyles, darkMode: DarkModeStyles?): String = buildString {
    appendLine("/* Social Media Embed Styles */")
    appendLine()

    appendEmbedStyle("twitter-tweet", lightMode.twitter)
    appendEmbedStyle("instagram-media", lightMode.instagram)
    appendEmbedStyle("tiktok-embed", lightMode.tiktok)
    appendEmbedStyle("reddit-embed-bq", lightMode.reddit)
    appendEmbedStyle("fb-post", lightMode.facebook)
    appendEmbedStyle("fb-video", lightMode.facebook)

    if (darkMode != null) {
      appendLine()
      appendLine("/* Dark Mode */")
      appendLine("@media (prefers-color-scheme: dark) {")
      appendEmbedStyle("  twitter-tweet", darkMode.twitter, indent = "  ")
      appendEmbedStyle("  instagram-media", darkMode.instagram, indent = "  ")
      appendEmbedStyle("  tiktok-embed", darkMode.tiktok, indent = "  ")
      appendEmbedStyle("  reddit-embed-bq", darkMode.reddit, indent = "  ")
      appendEmbedStyle("  fb-post", darkMode.facebook, indent = "  ")
      appendEmbedStyle("  fb-video", darkMode.facebook, indent = "  ")
      appendLine("}")
    }
  }

  private fun StringBuilder.appendEmbedStyle(className: String, style: EmbedStyle, indent: String = "") {
    appendLine("$indent.$className {")
    appendLine("$indent  border-left: ${style.borderWidth} solid ${style.accentColor};")
    appendLine("$indent  padding: ${style.padding};")
    appendLine("$indent  margin: ${style.margin};")
    appendLine("$indent  background: ${style.backgroundColor};")
    appendLine("$indent  border-radius: ${style.borderRadius};")
    appendLine("$indent  font-family: ${style.fontFamily};")
    appendLine("$indent  font-size: ${style.fontSize};")
    appendLine("$indent  line-height: ${style.lineHeight};")
    appendLine("$indent  color: ${style.textColor};")
    appendLine("$indent}")
    appendLine()

    appendLine("$indent.$className p {")
    appendLine("$indent  margin: 0 0 12px 0;")
    appendLine("$indent}")
    appendLine()

    appendLine("$indent.$className a {")
    appendLine("$indent  color: ${style.accentColor};")
    appendLine("$indent  text-decoration: none;")
    appendLine("$indent}")
    appendLine()

    if (style.icon != null) {
      appendLine("$indent.$className::before {")
      appendLine("$indent  content: \"${style.icon}\";")
      appendLine("$indent  font-size: ${if (style.icon.length > 2) "14px" else "20px"};")
      appendLine("$indent  font-weight: ${if (style.icon.length > 2) "600" else "normal"};")
      appendLine("$indent  color: ${style.accentColor};")
      appendLine("$indent  display: block;")
      appendLine("$indent  margin-bottom: 8px;")
      appendLine("$indent}")
      appendLine()
    }
  }
}
