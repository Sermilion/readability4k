package com.sermilion.readability4k

/**
 * Exception thrown when document parsing exceeds the configured limits.
 *
 * @property message Description of the parsing error
 * @property cause The underlying cause of the exception, if any
 */
open class ReadabilityException(message: String, cause: Throwable? = null) :
  Exception(message, cause)

/**
 * Exception thrown when the document exceeds the maximum element limit.
 *
 * @property elementCount The actual number of elements in the document
 * @property maxElements The configured maximum number of elements allowed
 */
class MaxElementsExceededException(val elementCount: Int, val maxElements: Int) :
  ReadabilityException(
    "Aborting parsing document; $elementCount elements found, but maxElemsToParse is set to $maxElements",
  )
