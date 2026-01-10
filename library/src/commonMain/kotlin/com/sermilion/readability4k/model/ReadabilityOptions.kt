package com.sermilion.readability4k.model

import com.fleeksoft.ksoup.nodes.Element
import com.sermilion.readability4k.transformer.RedditUrlTransformer
import com.sermilion.readability4k.transformer.UrlTransformer

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
 * @property charThreshold   Minimum number of characters an article must have to return a result.
 *                           Articles with fewer characters will not be extracted. Default: 500
 *                           Note: Despite the name, this counts characters, not words.
 * @property additionalClassesToPreserve Additional CSS class names to preserve during content
 *                           cleaning. By default, only "readability-styled" and "page" are preserved.
 * @property keepClasses     Whether to preserve all CSS classes on elements. When false (default),
 *                           classes are removed except those in additionalClassesToPreserve and
 *                           built-in preserved classes. When true, all classes are kept. Default: false
 * @property disableJSONLD   Whether to disable JSON-LD metadata extraction. When false (default),
 *                           the parser will extract metadata from JSON-LD structured data.
 *                           Set to true to skip JSON-LD parsing for performance. Default: false
 * @property serializer      Custom function to serialize Element to HTML string. When null (default),
 *                           uses Element.html(). Provide a custom serializer if you need special
 *                           HTML output formatting. Default: null
 * @property allowedVideoRegex Custom regex pattern to identify video elements. When null (default),
 *                           uses built-in patterns for YouTube, Vimeo, etc. Provide a custom pattern
 *                           to support additional video platforms. Default: null
 * @property linkDensityModifier Modifier to adjust link density calculation threshold. Higher values
 *                           make the algorithm more lenient with links. Default: 0.0
 * @property urlTransformers List of URL transformers to apply before parsing. By default, includes
 *                           RedditUrlTransformer which converts new Reddit URLs to old Reddit
 *                           format for better comment extraction. Transformers are applied in order
 *                           of priority (highest first). Pass empty list to disable transformation.
 *                           Default: [RedditUrlTransformer()]
 * @property preserveImages  Whether to preserve images in the extracted content. When true (default),
 *                           images within the main article content are kept. When false, images may
 *                           be more aggressively filtered out. Default: true
 * @property preserveVideos  Whether to preserve videos and iframes in the extracted content. When true
 *                           (default), video elements and iframes within the main article content are kept.
 *                           When false, videos/iframes may be more aggressively filtered out. Default: true
 *
 * ## Example
 *
 * ```kotlin
 * val options = ReadabilityOptions(
 *     maxElemsToParse = 1000,
 *     nbTopCandidates = 3,
 *     charThreshold = 300,
 *     additionalClassesToPreserve = setOf("highlight", "important"),
 *     keepClasses = false,
 *     disableJSONLD = false,
 *     preserveImages = true,
 *     preserveVideos = true,
 *     urlTransformers = listOf(RedditUrlTransformer())
 * )
 * ```
 */
open class ReadabilityOptions(
  val maxElemsToParse: Int = DEFAULT_MAX_ELEMS_TO_PARSE,
  val nbTopCandidates: Int = DEFAULT_N_TOP_CANDIDATES,
  val charThreshold: Int = DEFAULT_CHAR_THRESHOLD,
  val additionalClassesToPreserve: Collection<String> = emptyList(),
  val keepClasses: Boolean = false,
  val disableJSONLD: Boolean = false,
  val serializer: ((Element) -> String)? = null,
  val allowedVideoRegex: Regex? = null,
  val linkDensityModifier: Double = DEFAULT_LINK_DENSITY_MODIFIER,
  val urlTransformers: List<UrlTransformer> = listOf(RedditUrlTransformer()),
  val preserveImages: Boolean = true,
  val preserveVideos: Boolean = true,
) {

  companion object {
    const val DEFAULT_MAX_ELEMS_TO_PARSE = 0
    const val DEFAULT_N_TOP_CANDIDATES = 5
    const val DEFAULT_CHAR_THRESHOLD = 500
    const val DEFAULT_LINK_DENSITY_MODIFIER = 0.0
  }
}
