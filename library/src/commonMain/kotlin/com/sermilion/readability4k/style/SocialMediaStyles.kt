package com.sermilion.readability4k.style

fun generateSocialMediaCSS(
  isDarkMode: Boolean = false,
  customizeLight: (EmbedStylesBuilder.() -> Unit)? = null,
  customizeDark: (DarkModeStylesBuilder.() -> Unit)? = null,
): String {
  val builder = SocialMediaStyleBuilder()

  if (isDarkMode) {
    builder.disableDarkMode()
  }

  customizeLight?.let { builder.lightMode(it) }
  customizeDark?.let { builder.darkMode(it) }

  return builder.generateCSS()
}

fun generateThemedSocialMediaCSS(backgroundColor: String, textColor: String, accentColor: String? = null): String =
  socialMediaStyles {
    lightMode {
      twitter {
        this.backgroundColor = backgroundColor
        this.textColor = textColor
        accentColor?.let { this.accentColor = it }
      }
      instagram {
        this.backgroundColor = backgroundColor
        this.textColor = textColor
        accentColor?.let { this.accentColor = it }
      }
      tiktok {
        this.backgroundColor = backgroundColor
        this.textColor = textColor
        accentColor?.let { this.accentColor = it }
      }
      reddit {
        this.backgroundColor = backgroundColor
        this.textColor = textColor
        accentColor?.let { this.accentColor = it }
      }
      facebook {
        this.backgroundColor = backgroundColor
        this.textColor = textColor
        accentColor?.let { this.accentColor = it }
      }
    }
    disableDarkMode()
  }.generateCSS()
