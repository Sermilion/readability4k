package com.sermilion.readability4k.style

data class EmbedStyle(
  val backgroundColor: String,
  val textColor: String,
  val accentColor: String,
  val borderWidth: String = "4px",
  val borderRadius: String = "12px",
  val padding: String = "16px 20px",
  val margin: String = "20px 0",
  val fontFamily: String = "-apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif",
  val fontSize: String = "15px",
  val lineHeight: String = "1.5",
  val icon: String? = null,
)

data class EmbedStyles(
  val twitter: EmbedStyle = EmbedStyle(
    backgroundColor = "#f7f9fa",
    textColor = "#0f1419",
    accentColor = "#1DA1F2",
    icon = "𝕏",
  ),
  val instagram: EmbedStyle = EmbedStyle(
    backgroundColor = "#fafafa",
    textColor = "#262626",
    accentColor = "#833AB4",
    borderWidth = "1px",
    borderRadius = "8px",
    padding = "16px",
    icon = "📷 Instagram",
  ),
  val tiktok: EmbedStyle = EmbedStyle(
    backgroundColor = "#f1f1f2",
    textColor = "#000000",
    accentColor = "#000000",
    icon = "♪ TikTok",
  ),
  val reddit: EmbedStyle = EmbedStyle(
    backgroundColor = "#f6f7f8",
    textColor = "#1c1c1c",
    accentColor = "#FF4500",
    borderRadius = "8px",
    icon = "🔴 Reddit",
  ),
  val facebook: EmbedStyle = EmbedStyle(
    backgroundColor = "#f0f2f5",
    textColor = "#050505",
    accentColor = "#1877f2",
    borderWidth = "1px",
    borderRadius = "8px",
    padding = "16px",
    icon = "ƒ Facebook",
  ),
)

data class DarkModeStyles(
  val twitter: EmbedStyle = EmbedStyle(
    backgroundColor = "#192734",
    textColor = "#e7e9ea",
    accentColor = "#1DA1F2",
    icon = "𝕏",
  ),
  val instagram: EmbedStyle = EmbedStyle(
    backgroundColor = "#1a1a1a",
    textColor = "#fafafa",
    accentColor = "#833AB4",
    borderWidth = "1px",
    borderRadius = "8px",
    padding = "16px",
    icon = "📷 Instagram",
  ),
  val tiktok: EmbedStyle = EmbedStyle(
    backgroundColor = "#121212",
    textColor = "#ffffff",
    accentColor = "#000000",
    icon = "♪ TikTok",
  ),
  val reddit: EmbedStyle = EmbedStyle(
    backgroundColor = "#1a1a1b",
    textColor = "#d7dadc",
    accentColor = "#FF4500",
    borderRadius = "8px",
    icon = "🔴 Reddit",
  ),
  val facebook: EmbedStyle = EmbedStyle(
    backgroundColor = "#242526",
    textColor = "#e4e6eb",
    accentColor = "#1877f2",
    borderWidth = "1px",
    borderRadius = "8px",
    padding = "16px",
    icon = "ƒ Facebook",
  ),
)
