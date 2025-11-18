package com.sermilion.readability4k.util

/**
 * Simple logging interface for Readability4K.
 *
 * By default, logging is disabled. To enable logging, provide your own implementation
 * and pass it to Readability4J constructor.
 *
 * Example with console logging:
 * ```kotlin
 * val logger = object : Logger {
 *   override fun debug(message: String) = println("DEBUG: $message")
 *   override fun error(message: String, throwable: Throwable?) = println("ERROR: $message ${throwable?.message ?: ""}")
 * }
 * val readability = Readability4J(url, html, logger = logger)
 * ```
 */
interface Logger {
  fun debug(message: String)
  fun error(message: String, throwable: Throwable? = null)

  companion object {
    /**
     * No-op logger that discards all log messages.
     * This is the default logger used if none is provided.
     */
    val NONE: Logger = object : Logger {
      override fun debug(message: String) {}
      override fun error(message: String, throwable: Throwable?) {}
    }
  }
}
