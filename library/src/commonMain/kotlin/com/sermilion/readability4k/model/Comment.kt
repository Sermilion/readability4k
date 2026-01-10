package com.sermilion.readability4k.model

data class Comment(
  val id: String? = null,
  val author: String? = null,
  val body: String? = null,
  val bodyHtml: String? = null,
  val timestamp: String? = null,
  val score: String? = null,
  val replies: List<Comment> = emptyList(),
)
