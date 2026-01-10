package com.sermilion.readability4k.transformer

class RedditUrlTransformer : UrlTransformer {
  override val priority: Int = 100

  override fun transform(url: String): String = when {
    url.contains("old.reddit.com") -> url
    url.contains("www.reddit.com") -> url.replace("www.reddit.com", "old.reddit.com")
    url.contains("reddit.com") -> url.replace("reddit.com", "old.reddit.com")
    else -> url
  }
}
