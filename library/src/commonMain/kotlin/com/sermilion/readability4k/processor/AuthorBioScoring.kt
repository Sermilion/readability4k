package com.sermilion.readability4k.processor

internal object AuthorBioScoring {
  const val THRESHOLD = 4
  const val FRACTION = 0.45
  const val DISCLAIMER_COMMA_KEEP = 10
  const val DISCLAIMER_LENGTH_KEEP = 600
  const val BIO_LABEL = 2
  const val COMPOUND = 3
  const val BARE_AUTHOR = 1
  const val REL_AUTHOR = 2
  const val PERSON = 2
  const val PRECEDING = 2
  const val DISCLAIMER_LABEL = 2
  const val DISCLAIMER_TOKEN = 2

  const val VETO_ARTICLE_ROOT = "article_root"
  const val VETO_FRACTION = "fraction"
  const val VETO_DISCLAIMER_COMMAS = "disclaimer_commas"
  const val VETO_DISCLAIMER_LENGTH = "disclaimer_length"
  const val VETO_BELOW_THRESHOLD = "below_threshold"
  const val VETO_NESTED = "nested"
  const val VETO_DETACHED = "detached"

  const val FEATURE_BIO_LABEL = "bio_label"
  const val FEATURE_COMPOUND = "compound"
  const val FEATURE_BARE_AUTHOR = "bare_author"
  const val FEATURE_REL_AUTHOR = "rel_author"
  const val FEATURE_PERSON = "person"
  const val FEATURE_PRECEDING = "preceding"
  const val FEATURE_DISCLAIMER_LABEL = "disclaimer_label"
  const val FEATURE_DISCLAIMER_TOKEN = "disclaimer_token"
}
