package com.sermilion.readability4k.transformer

interface UrlTransformer {
  fun transform(url: String): String
  val priority: Int get() = 0
}
