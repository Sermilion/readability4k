package com.sermilion.readability4k.model

import com.fleeksoft.ksoup.nodes.Element

data class TopCandidateResult(
  val candidate: Element,
  val wasCreated: Boolean,
)
