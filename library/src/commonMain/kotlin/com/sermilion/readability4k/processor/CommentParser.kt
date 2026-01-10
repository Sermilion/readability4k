package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.sermilion.readability4k.model.Comment

interface CommentParser {
  fun parseComments(doc: Document): List<Comment>
}
