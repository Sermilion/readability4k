package com.sermilion.readability4k.model

/**
 * Configuration options for the Readability parser.
 *
 * Use this class to customize the behavior of the article extraction algorithm.
 *
 * @property maxElemsToParse Maximum number of elements to parse. Set to 0 for unlimited.
 *                           Use this to prevent parsing very large documents. Default: 0 (unlimited)
 * @property nbTopCandidates Number of top candidate nodes to consider when analyzing how
 *                           competitive the extraction is. Higher values may improve accuracy
 *                           but increase processing time. Default: 5
 * @property wordThreshold   Minimum number of words an article must have to return a result.
 *                           Articles with fewer words will not be extracted. Default: 500
 * @property additionalClassesToPreserve Additional CSS class names to preserve during content
 *                           cleaning. By default, only "readability-styled" and "page" are preserved.
 *
 * ## Example
 *
 * ```kotlin
 * val options = ReadabilityOptions(
 *     maxElemsToParse = 1000,
 *     nbTopCandidates = 3,
 *     wordThreshold = 300,
 *     additionalClassesToPreserve = setOf("highlight", "important")
 * )
 * ```
 */
open class ReadabilityOptions(
  val maxElemsToParse: Int = DEFAULT_MAX_ELEMS_TO_PARSE,
  val nbTopCandidates: Int = DEFAULT_N_TOP_CANDIDATES,
  val wordThreshold: Int = DEFAULT_WORD_THRESHOLD,
  val additionalClassesToPreserve: Collection<String> = emptyList(),
) {

  companion object {
    // Max number of nodes supported by this parser. Default: 0 (no limit)
    const val DEFAULT_MAX_ELEMS_TO_PARSE = 0

    // The number of top candidates to consider when analysing how
    // tight the competition is among candidates.
    const val DEFAULT_N_TOP_CANDIDATES = 5

    // The default number of words an article must have in order to return a result
    const val DEFAULT_WORD_THRESHOLD = 500
  }
}
