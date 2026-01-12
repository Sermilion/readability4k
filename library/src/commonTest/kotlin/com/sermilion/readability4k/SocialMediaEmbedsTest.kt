package com.sermilion.readability4k

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class SocialMediaEmbedsTest : FunSpec({

  test("should preserve Twitter embed class") {
    val html = """
      <!DOCTYPE html>
      <html>
      <head><title>Article with Twitter embed</title></head>
      <body>
        <article>
          <h1>Breaking News</h1>
          <p>Check out this important announcement:</p>
          <blockquote class="twitter-tweet">
            <p lang="en" dir="ltr">This is a tweet about something important</p>
            — User (@username) <a href="https://twitter.com/username/status/123">January 1, 2026</a>
          </blockquote>
          <p>More article content here.</p>
        </article>
      </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content.orEmpty() shouldContain "twitter-tweet"
    article.content.orEmpty() shouldContain "This is a tweet about something important"
  }

  test("should preserve Instagram embed class") {
    val html = """
      <!DOCTYPE html>
      <html>
      <head><title>Article with Instagram embed</title></head>
      <body>
        <article>
          <h1>Photo Gallery</h1>
          <p>Check out this amazing photo:</p>
          <blockquote class="instagram-media" data-instgrm-permalink="https://www.instagram.com/p/ABC123/">
            <div>
              <a href="https://www.instagram.com/p/ABC123/">
                <p>Amazing photo caption</p>
              </a>
            </div>
          </blockquote>
          <p>More content here.</p>
        </article>
      </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content.orEmpty() shouldContain "instagram-media"
    article.content.orEmpty() shouldContain "Amazing photo caption"
  }

  test("should preserve TikTok embed class") {
    val html = """
      <!DOCTYPE html>
      <html>
      <head><title>Article with TikTok embed</title></head>
      <body>
        <article>
          <h1>Viral Video</h1>
          <p>This video went viral:</p>
          <blockquote class="tiktok-embed" cite="https://www.tiktok.com/@user/video/123">
            <section>
              <a href="https://www.tiktok.com/@user">@user</a>
              <p>Funny video content</p>
            </section>
          </blockquote>
          <p>Analysis of the video.</p>
        </article>
      </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content.orEmpty() shouldContain "tiktok-embed"
    article.content.orEmpty() shouldContain "Funny video content"
  }

  test("should preserve Reddit embed class") {
    val html = """
      <!DOCTYPE html>
      <html>
      <head><title>Article with Reddit embed</title></head>
      <body>
        <article>
          <h1>Community Discussion</h1>
          <p>Reddit users had this to say:</p>
          <blockquote class="reddit-embed-bq">
            <a href="https://www.reddit.com/r/example/comments/abc123/">
              Interesting discussion thread title
            </a>
          </blockquote>
          <p>More analysis here.</p>
        </article>
      </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content.orEmpty() shouldContain "reddit-embed-bq"
    article.content.orEmpty() shouldContain "Interesting discussion thread"
  }

  test("should preserve Facebook post embed class") {
    val html = """
      <!DOCTYPE html>
      <html>
      <head><title>Article with Facebook post</title></head>
      <body>
        <article>
          <h1>Official Statement</h1>
          <p>The company posted on Facebook:</p>
          <div class="fb-post" data-href="https://www.facebook.com/page/posts/123">
            <blockquote>
              <p>Official company statement here</p>
            </blockquote>
          </div>
          <p>More context about the statement.</p>
        </article>
      </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    article shouldNotBe null
    article.content.orEmpty() shouldContain "fb-post"
    article.content.orEmpty() shouldContain "Official company statement"
  }

  test("should preserve multiple social media embeds in same article") {
    val html = """
      <!DOCTYPE html>
      <html>
      <head><title>Article with multiple embeds</title></head>
      <body>
        <article>
          <h1>Social Media Roundup</h1>
          <p>First, Twitter:</p>
          <blockquote class="twitter-tweet">
            <p>Tweet content</p>
          </blockquote>
          <p>Then Instagram:</p>
          <blockquote class="instagram-media">
            <p>Instagram caption</p>
          </blockquote>
          <p>And finally TikTok:</p>
          <blockquote class="tiktok-embed">
            <p>TikTok description</p>
          </blockquote>
        </article>
      </body>
      </html>
    """.trimIndent()

    val readability = Readability4K("https://example.com/article", html)
    val article = readability.parse()

    article shouldNotBe null
    val content = article.content.orEmpty()
    content shouldContain "twitter-tweet"
    content shouldContain "instagram-media"
    content shouldContain "tiktok-embed"
    content shouldContain "Tweet content"
    content shouldContain "Instagram caption"
    content shouldContain "TikTok description"
  }
})
