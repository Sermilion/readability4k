package com.sermilion.readability4k.style

interface StyleRenderer {
  fun render(content: String, stylesheet: String): String
}

class HtmlStyleRenderer(
  private val includeViewport: Boolean = true,
  private val baseStyles: String? = null,
) : StyleRenderer {

  override fun render(content: String, stylesheet: String): String = buildString {
    appendLine("<!DOCTYPE html>")
    appendLine("<html>")
    appendLine("<head>")
    if (includeViewport) {
      appendLine("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
    }
    appendLine("  <style>")
    if (baseStyles != null) {
      appendLine(baseStyles)
      appendLine()
    }
    appendLine(stylesheet)
    appendLine("  </style>")
    appendLine("</head>")
    appendLine("<body>")
    appendLine(content)
    appendLine("</body>")
    appendLine("</html>")
  }
}

class InlineStyleRenderer : StyleRenderer {
  override fun render(content: String, stylesheet: String): String = buildString {
    appendLine("<style>")
    appendLine(stylesheet)
    appendLine("</style>")
    appendLine(content)
  }
}

object StyleRenderers {
  val html = HtmlStyleRenderer()

  val inline = InlineStyleRenderer()

  fun html(includeViewport: Boolean = true, baseStyles: String? = null) = HtmlStyleRenderer(includeViewport, baseStyles)
}
