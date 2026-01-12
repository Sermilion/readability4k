package com.sermilion.readability4k.style

class SocialMediaStyleBuilder {
  private var lightModeStyles: EmbedStyles = EmbedStyles()
  private var darkModeStyles: DarkModeStyles? = DarkModeStyles()
  private var stylesheet: EmbedStylesheet = DefaultEmbedStylesheet()

  fun lightMode(block: EmbedStylesBuilder.() -> Unit) {
    lightModeStyles = EmbedStylesBuilder().apply(block).build()
  }

  fun darkMode(block: DarkModeStylesBuilder.() -> Unit) {
    darkModeStyles = DarkModeStylesBuilder().apply(block).build()
  }

  fun disableDarkMode() {
    darkModeStyles = null
  }

  fun customStylesheet(stylesheet: EmbedStylesheet) {
    this.stylesheet = stylesheet
  }

  fun generateCSS(): String = stylesheet.generate(lightModeStyles, darkModeStyles)

  fun renderHtml(content: String, renderer: StyleRenderer = StyleRenderers.html): String =
    renderer.render(content, generateCSS())
}

class EmbedStylesBuilder {
  private var twitter: EmbedStyle = EmbedStyles().twitter
  private var instagram: EmbedStyle = EmbedStyles().instagram
  private var tiktok: EmbedStyle = EmbedStyles().tiktok
  private var reddit: EmbedStyle = EmbedStyles().reddit
  private var facebook: EmbedStyle = EmbedStyles().facebook

  fun twitter(block: EmbedStyleBuilder.() -> Unit) {
    twitter = EmbedStyleBuilder(twitter).apply(block).build()
  }

  fun instagram(block: EmbedStyleBuilder.() -> Unit) {
    instagram = EmbedStyleBuilder(instagram).apply(block).build()
  }

  fun tiktok(block: EmbedStyleBuilder.() -> Unit) {
    tiktok = EmbedStyleBuilder(tiktok).apply(block).build()
  }

  fun reddit(block: EmbedStyleBuilder.() -> Unit) {
    reddit = EmbedStyleBuilder(reddit).apply(block).build()
  }

  fun facebook(block: EmbedStyleBuilder.() -> Unit) {
    facebook = EmbedStyleBuilder(facebook).apply(block).build()
  }

  fun build() = EmbedStyles(twitter, instagram, tiktok, reddit, facebook)
}

class DarkModeStylesBuilder {
  private var twitter: EmbedStyle = DarkModeStyles().twitter
  private var instagram: EmbedStyle = DarkModeStyles().instagram
  private var tiktok: EmbedStyle = DarkModeStyles().tiktok
  private var reddit: EmbedStyle = DarkModeStyles().reddit
  private var facebook: EmbedStyle = DarkModeStyles().facebook

  fun twitter(block: EmbedStyleBuilder.() -> Unit) {
    twitter = EmbedStyleBuilder(twitter).apply(block).build()
  }

  fun instagram(block: EmbedStyleBuilder.() -> Unit) {
    instagram = EmbedStyleBuilder(instagram).apply(block).build()
  }

  fun tiktok(block: EmbedStyleBuilder.() -> Unit) {
    tiktok = EmbedStyleBuilder(tiktok).apply(block).build()
  }

  fun reddit(block: EmbedStyleBuilder.() -> Unit) {
    reddit = EmbedStyleBuilder(reddit).apply(block).build()
  }

  fun facebook(block: EmbedStyleBuilder.() -> Unit) {
    facebook = EmbedStyleBuilder(facebook).apply(block).build()
  }

  fun build() = DarkModeStyles(twitter, instagram, tiktok, reddit, facebook)
}

class EmbedStyleBuilder(base: EmbedStyle) {
  var backgroundColor: String = base.backgroundColor
  var textColor: String = base.textColor
  var accentColor: String = base.accentColor
  var borderWidth: String = base.borderWidth
  var borderRadius: String = base.borderRadius
  var padding: String = base.padding
  var margin: String = base.margin
  var fontFamily: String = base.fontFamily
  var fontSize: String = base.fontSize
  var lineHeight: String = base.lineHeight
  var icon: String? = base.icon

  fun build() = EmbedStyle(
    backgroundColor,
    textColor,
    accentColor,
    borderWidth,
    borderRadius,
    padding,
    margin,
    fontFamily,
    fontSize,
    lineHeight,
    icon,
  )
}

fun socialMediaStyles(block: SocialMediaStyleBuilder.() -> Unit = {}): SocialMediaStyleBuilder =
  SocialMediaStyleBuilder().apply(block)
