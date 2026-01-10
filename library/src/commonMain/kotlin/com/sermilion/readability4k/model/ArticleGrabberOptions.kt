package com.sermilion.readability4k.model

data class ArticleGrabberOptions(
  val stripUnlikelyCandidates: Boolean = true,
  val weightClasses: Boolean = true,
  val cleanConditionally: Boolean = true,
  val preserveImages: Boolean = true,
  val preserveVideos: Boolean = true,
)
