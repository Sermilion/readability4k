package com.sermilion.readability4k.model

data class ArticleGrabberOptions(
  val stripUnlikelyCandidates: Boolean = true,
  val weightClasses: Boolean = true,
  val cleanConditionally: Boolean = true,
)
