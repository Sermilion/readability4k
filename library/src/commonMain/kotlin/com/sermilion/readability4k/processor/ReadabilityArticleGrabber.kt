package com.sermilion.readability4k.processor

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.nodes.TextNode
import com.fleeksoft.ksoup.select.Elements
import com.sermilion.readability4k.model.ArticleGrabberOptions
import com.sermilion.readability4k.model.ArticleMetadata
import com.sermilion.readability4k.model.ReadabilityObject
import com.sermilion.readability4k.model.ReadabilityOptions
import com.sermilion.readability4k.model.TopCandidateResult
import com.sermilion.readability4k.util.HtmlUtil
import com.sermilion.readability4k.util.Logger
import com.sermilion.readability4k.util.RegExUtil
import kotlin.math.floor

@Suppress("TooManyFunctions")
open class ReadabilityArticleGrabber(
  options: ReadabilityOptions,
  protected val regEx: RegExUtil = RegExUtil(),
  logger: Logger = Logger.NONE,
  protected val candidateFilters: List<CandidateFilter> = listOf(BlockquoteDescendantFilter),
) : ProcessorBase(logger), ArticleGrabber {

  override var articleByline: String? = null
    protected set

  override var articleDir: String? = null
    protected set

  override var articleLang: String? = null
    protected set

  protected val nbTopCandidates = options.nbTopCandidates
  protected val charThreshold = options.charThreshold
  protected val allowedVideoRegex = options.allowedVideoRegex
  protected val linkDensityModifier = options.linkDensityModifier

  protected val readabilityObjects = HashMap<Element, ReadabilityObject>()

  protected val readabilityDataTable = HashMap<Element, Boolean>()

  protected open fun isVideoContent(matchString: String): Boolean {
    return allowedVideoRegex?.containsMatchIn(matchString) ?: regEx.isVideo(matchString)
  }

  @Suppress("NestedBlockDepth")
  override fun grabArticle(
    doc: Document,
    metadata: ArticleMetadata,
    options: ArticleGrabberOptions,
    pageElement: Element?,
  ): Element? {
    logger.debug("**** grabArticle ****")

    val isPaging = pageElement != null
    val page = pageElement ?: doc.body()

    val pageCacheHtml = doc.html()

    val optionsSequence = generateOptionsSequence(options)
    val attempts = ArrayList<Triple<Element, Element, Int>>()

    for (currentOptions in optionsSequence) {
      page.html(pageCacheHtml)

      val result = tryExtractArticle(doc, metadata, currentOptions, page, isPaging)

      val textLength = getInnerText(result.articleContent, regEx, true).length
      attempts.add(Triple(result.articleContent, result.topCandidate, textLength))

      if (textLength >= this.charThreshold) {
        getTextDirection(result.topCandidate, doc)
        getLanguage(doc)
        return result.articleContent
      }
    }

    attempts.sortByDescending { it.third }
    return if (attempts.isNotEmpty() && attempts[0].third > 0) {
      getTextDirection(attempts[0].second, doc)
      getLanguage(doc)
      attempts[0].first
    } else {
      null
    }
  }

  private fun generateOptionsSequence(options: ArticleGrabberOptions): List<ArticleGrabberOptions> {
    val sequence = mutableListOf(options)

    if (options.stripUnlikelyCandidates) {
      sequence.add(options.copy(stripUnlikelyCandidates = false))
    }
    if (options.weightClasses) {
      sequence.add(options.copy(stripUnlikelyCandidates = false, weightClasses = false))
    }
    if (options.cleanConditionally) {
      sequence.add(
        options.copy(
          stripUnlikelyCandidates = false,
          weightClasses = false,
          cleanConditionally = false,
        ),
      )
    }

    return sequence
  }

  private data class ExtractionResult(
    val articleContent: Element,
    val topCandidate: Element,
  )

  private fun tryExtractArticle(
    doc: Document,
    metadata: ArticleMetadata,
    options: ArticleGrabberOptions,
    page: Element,
    isPaging: Boolean,
  ): ExtractionResult {
    val elementsToScore = prepareNodes(doc, options)
    val candidates = scoreElements(elementsToScore, options)
    val topCandidateResult = getTopCandidate(page, candidates, options)
    val topCandidate = topCandidateResult.candidate
    val neededToCreateTopCandidate = topCandidateResult.wasCreated

    val articleContent = createArticleContent(doc, topCandidate, isPaging)

    logger.debug("Article content pre-prep: ${articleContent.html()}")
    prepArticle(articleContent, options, metadata)
    logger.debug("Article content post-prep: ${articleContent.html()}")

    if (neededToCreateTopCandidate) {
      topCandidate.attr("id", "readability-page-1")
      topCandidate.addClass("page")
    } else {
      val div = doc.createElement("div")
      div.attr("id", "readability-page-1")
      div.addClass("page")

      ArrayList(articleContent.childNodes()).forEach { child ->
        child.remove()
        div.appendChild(child)
      }

      articleContent.appendChild(div)
    }

    logger.debug("Article content after paging: ${articleContent.html()}")

    return ExtractionResult(articleContent, topCandidate)
  }

  /*             First step: prepare nodes           */

  @Suppress("CyclomaticComplexMethod", "NestedBlockDepth", "LoopWithTooManyJumpStatements")
  protected open fun prepareNodes(doc: Document, options: ArticleGrabberOptions): List<Element> {
    val elementsToScore = ArrayList<Element>()
    var node: Element? = doc

    while (node != null) {
      val matchString = node.className() + " " + node.id()

      // Check to see if this node is a byline, and remove it if it is.
      if (checkByline(node, matchString)) {
        node = removeAndGetNext(node, "byline")
        continue
      }

      // Remove unlikely candidates
      if (options.stripUnlikelyCandidates) {
        val isUnlikely = regEx.isUnlikelyCandidate(matchString) &&
          !regEx.okMaybeItsACandidate(matchString) &&
          node.tagName() != "body" &&
          node.tagName() != "a"

        if (isUnlikely) {
          node = this.removeAndGetNext(node, "Removing unlikely candidate")
          continue
        }
      }

      // Remove DIV, SECTION, and HEADER nodes without any content
      // (e.g. text, image, video, or iframe).
      if ((
          node.tagName() == "div" ||
            node.tagName() == "section" ||
            node.tagName() == "header" ||
            node.tagName() == "h1" ||
            node.tagName() == "h2" ||
            node.tagName() == "h3" ||
            node.tagName() == "h4" ||
            node.tagName() == "h5" ||
            node.tagName() == "h6"
          ) &&
        this.isElementWithoutContent(node)
      ) {
        node = this.removeAndGetNext(node, "node without content")
        continue
      }

      if (DEFAULT_TAGS_TO_SCORE.contains(node.tagName())) {
        elementsToScore.add(node)
      }

      // Turn all divs that don't have children block level elements into p's
      if (node.tagName() == "div") {
        // Sites like http://mobile.slate.com encloses each paragraph with a DIV
        // element. DIVs with only a P element inside and no text content can be
        // safely converted into plain P elements to avoid confusing the scoring
        // algorithm with DIVs with are, in practice, paragraphs.
        if (this.hasSinglePInsideElement(node)) {
          val newNode = node.child(0)
          node.replaceWith(newNode)
          node = newNode
          elementsToScore.add(node)
        } else if (!this.hasChildBlockElement(node)) {
          setNodeTag(node, "p")
          elementsToScore.add(node)
        } else {
          // EXPERIMENTAL
          node.childNodes().forEach { childNode ->
            if (childNode is TextNode && childNode.text().trim().isNotEmpty()) {
              val p = doc.createElement("p")
              p.text(childNode.text())
              p.attr("style", "display: inline;")
              p.addClass("readability-styled")
              childNode.replaceWith(p)
            }
          }
        }
      }

      node = this.getNextNode(node)
    }

    return elementsToScore
  }

  protected open fun checkByline(node: Element, matchString: String): Boolean {
    if (this.articleByline != null) {
      return false
    }

    val rel = node.attr("rel")

    if ((rel == "author" || regEx.isByline(matchString)) && isValidByline(node.wholeText())) {
      this.articleByline = node.text().trim()
      return true
    }

    return false
  }

  /**
   * Check whether the input string could be a byline.
   * This verifies that the input is a string, and that the length
   * is less than 100 chars.
   */
  protected open fun isValidByline(text: String): Boolean {
    val byline = text.trim()

    return (byline.isNotEmpty()) && (byline.length < 100)
  }

  protected open fun isElementWithoutContent(node: Element): Boolean = node.text().isBlank() &&
    (
      node.children().isEmpty() ||
        node.children().size == node.getElementsByTag("br").size + node.getElementsByTag("hr").size
      )

  /**
   * Check if this node has only whitespace and a single P element
   * Returns false if the DIV node contains non-empty text nodes
   * or if it contains no P or more than 1 element.
   */
  protected open fun hasSinglePInsideElement(element: Element): Boolean {
    // There should be exactly 1 element child which is a P:
    if (element.children().size != 1 || element.child(0).tagName() != "p") {
      return false
    }

    // And there should be no text nodes with real content
    element.childNodes().forEach { node ->
      if (node is TextNode && regEx.hasContent(node.text())) {
        return false
      }
    }

    return true
  }

  /**
   * Determine whether element has any children block level elements.
   */
  protected open fun hasChildBlockElement(element: Element): Boolean {
    element.children().forEach { node ->
      if (DIV_TO_P_ELEMS.contains(node.tagName()) || hasChildBlockElement(node)) {
        return true
      }
    }

    return false
  }

  protected open fun setNodeTag(node: Element, tagName: String) {
    node.tagName(tagName)
  }

  /*          Second step: Score elements             */

  protected open fun scoreElements(elementsToScore: List<Element>, options: ArticleGrabberOptions): List<Element> {
    val candidates = ArrayList<Element>()

    elementsToScore.forEach { elementToScore ->
      if (elementToScore.parentNode() == null) {
        return@forEach
      }

      // If this paragraph is less than 25 characters, don't even count it.
      val innerText = this.getInnerText(elementToScore, regEx)
      if (innerText.length < 25) {
        return@forEach
      }

      // Exclude nodes with no ancestor.
      val ancestors = this.getNodeAncestors(elementToScore, 3)
      if (ancestors.isEmpty()) {
        return@forEach
      }

      var contentScore = 0.0

      // Add a point for the paragraph itself as a base.
      contentScore += 1

      // Add points for any commas within this paragraph.
      contentScore += innerText.split(',').size

      // For every 100 characters in this paragraph, add another point. Up to 3 points.
      contentScore += minOf(floor(innerText.length / 100.0), 3.0)

      // Initialize and score ancestors.
      for (level in 0..<ancestors.size) {
        val ancestor = ancestors[level]
        if (ancestor.tagName().isBlank()) {
          // with Jsoup this should never be true as we're only handling Elements
          return@forEach
        }

        if (getReadabilityObject(ancestor) == null) {
          candidates.add(ancestor)
          initializeNode(ancestor, options)
        }

        // Node score divider:
        // - parent:             1 (no division)
        // - grandparent:        2
        // - great grandparent+: ancestor level * 3
        val scoreDivider =
          when (level) {
            0 -> 1
            1 -> 2
            else -> level * 3
          }

        getReadabilityObject(ancestor)?.let { readability ->
          readability.contentScore += contentScore / scoreDivider.toDouble()
        }
      }
    }

    return candidates
  }

  /**
   * Initialize a node with the readability object. Also checks the
   * className/id for special names to add to its score.
   */
  protected open fun initializeNode(node: Element, options: ArticleGrabberOptions): ReadabilityObject {
    val readability = ReadabilityObject(0.0)
    readabilityObjects[node] = readability

    readability.contentScore += getTagScore(node.tagName())
    readability.contentScore += getClassWeight(node, options)
    readability.contentScore += getMediaBonus(node, options)

    return readability
  }

  private fun getTagScore(tagName: String): Int = when (tagName) {
    "div" -> TAG_SCORE_DIV
    "pre", "td", "blockquote" -> TAG_SCORE_CONTENT
    "address", "ol", "ul", "dl", "dd", "dt", "li", "form" -> TAG_SCORE_LIST_PENALTY
    "h1", "h2", "h3", "h4", "h5", "h6", "th" -> TAG_SCORE_HEADER_PENALTY
    else -> 0
  }

  private fun getMediaBonus(node: Element, options: ArticleGrabberOptions): Int {
    if (!options.preserveImages && !options.preserveVideos) return 0
    if (isLikelyNonContentContainer(node)) return 0

    val imageCount = if (options.preserveImages) node.getElementsByTag("img").size else 0
    val videoCount = if (options.preserveVideos) countVideoElements(node) else 0

    val textLength = getInnerText(node, regEx).length
    val hasReasonableRatio = imageCount == 0 || (textLength / imageCount.toDouble()) > 50

    return if (hasReasonableRatio) {
      (imageCount * 3) + (videoCount * 5)
    } else {
      0
    }
  }

  private fun isLikelyNonContentContainer(node: Element): Boolean {
    val matchString = node.className() + " " + node.id()
    return regEx.isNegative(matchString) ||
      matchString.contains("sidebar", ignoreCase = true) ||
      matchString.contains("related", ignoreCase = true) ||
      matchString.contains("recommend", ignoreCase = true) ||
      matchString.contains("widget", ignoreCase = true) ||
      matchString.contains("promo", ignoreCase = true)
  }

  private fun countVideoElements(node: Element): Int = node.getElementsByTag("iframe").size +
    node.getElementsByTag("video").size +
    node.getElementsByTag("embed").size

  /**
   * Get an elements class/id weight. Uses regular expressions to tell if this
   * element looks good or bad.
   */
  protected open fun getClassWeight(e: Element, options: ArticleGrabberOptions): Int {
    if (!options.weightClasses) {
      return 0
    }

    var weight = 0

    // Look for a special classname
    if (e.className().isNotBlank()) {
      if (regEx.isNegative(e.className())) {
        weight -= 25
      }

      if (regEx.isPositive(e.className())) {
        weight += 25
      }
    }

    // Look for a special ID
    if (e.id().isNotBlank()) {
      if (regEx.isNegative(e.id())) {
        weight -= 25
      }

      if (regEx.isPositive(e.id())) {
        weight += 25
      }
    }

    return weight
  }

  @Suppress("LoopWithTooManyJumpStatements")
  protected open fun getNodeAncestors(node: Element, maxDepth: Int = 0): List<Element> {
    var i = 0
    val ancestors = ArrayList<Element>()
    var next = node

    while (next.parent() != null) {
      next.parent()?.let { ancestors.add(it) }
      if (++i == maxDepth) {
        break
      }

      next = next.parent() ?: break
    }

    return ancestors
  }

  /*          Third step: Get top candidate           */

  @Suppress(
    "LongMethod",
    "CyclomaticComplexMethod",
    "NestedBlockDepth",
    "LoopWithTooManyJumpStatements",
  )
  protected open fun getTopCandidate(
    page: Element,
    candidates: List<Element>,
    options: ArticleGrabberOptions,
  ): TopCandidateResult {
    val topCandidates = ArrayList<Element>()

    candidates.forEach { candidate ->
      if (!shouldIncludeCandidate(candidate)) {
        logger.debug("Skipping candidate (filtered): $candidate")
        return@forEach
      }

      getReadabilityObject(candidate)?.let { readability ->
        // Scale the final candidates score based on link density. Good content
        // should have a relatively small link density (5% or less) and be mostly
        // unaffected by this operation.
        val candidateScore = readability.contentScore * (1 - this.getLinkDensity(candidate))
        readability.contentScore = candidateScore

        logger.debug("Candidate: $candidate with score $candidateScore")

        for (t in 0..<nbTopCandidates) {
          val aTopCandidate = if (topCandidates.size > t) topCandidates[t] else null
          val topCandidateReadability =
            if (aTopCandidate != null) getReadabilityObject(aTopCandidate) else null

          if (aTopCandidate == null ||
            (
              topCandidateReadability != null &&
                candidateScore > topCandidateReadability.contentScore
              )
          ) {
            topCandidates.add(t, candidate)

            if (topCandidates.size > this.nbTopCandidates) {
              topCandidates.removeAt(nbTopCandidates)
            }
            break
          }
        }
      }
    }

    var topCandidate = if (topCandidates.isNotEmpty()) topCandidates[0] else null
    var parentOfTopCandidate: Element?

    // If we still have no top candidate, just use the body as a last resort.
    // We also have to copy the body node so it is something we can modify.
    if (topCandidate == null || topCandidate.tagName() == "body") {
      // Move all of the page's children into topCandidate
      topCandidate = Element("div")
      // Move everything (not just elements, also text nodes etc.) into the container
      // so we even include text directly in the body:
      ArrayList(page.childNodes()).forEach { child ->
        logger.debug("Moving child out: $child")
        child.remove()
        topCandidate.appendChild(child)
      }

      page.appendChild(topCandidate)

      this.initializeNode(topCandidate, options)

      return TopCandidateResult(topCandidate, true)
    } else {
      // Find a better top candidate node if it contains (at least three) nodes which belong to
      // `topCandidates` array
      // and whose scores are quite closed with current `topCandidate` node.
      val alternativeCandidateAncestors = ArrayList<List<Element>>()

      getReadabilityObject(topCandidate)?.let { topCandidateReadability ->
        topCandidates.filter { it != topCandidate }.forEach { otherTopCandidate ->
          if ((
              (
                getReadabilityObject(otherTopCandidate)?.contentScore
                  ?: 0.0
                ) / topCandidateReadability.contentScore
              ) >= 0.75
          ) {
            alternativeCandidateAncestors.add(this.getNodeAncestors(otherTopCandidate))
          }
        }
      }

      val minimumTopCandidates = 3
      if (alternativeCandidateAncestors.size >= minimumTopCandidates) {
        parentOfTopCandidate = topCandidate.parent()

        while (parentOfTopCandidate != null && parentOfTopCandidate.tagName() != "body") {
          var listsContainingThisAncestor = 0
          var ancestorIndex = 0
          while (ancestorIndex < alternativeCandidateAncestors.size &&
            listsContainingThisAncestor < minimumTopCandidates
          ) {
            if (alternativeCandidateAncestors[ancestorIndex].contains(parentOfTopCandidate)) {
              listsContainingThisAncestor++
            }
            ancestorIndex++
          }

          if (listsContainingThisAncestor >= minimumTopCandidates) {
            topCandidate = parentOfTopCandidate
            break
          }
          parentOfTopCandidate = parentOfTopCandidate.parent()
        }
      }

      topCandidate = topCandidate!!
      if (getReadabilityObject(topCandidate) == null) {
        this.initializeNode(topCandidate, options)
      }

      // Because of our bonus system, parents of candidates might have scores
      // themselves. They get half of the node. There won't be nodes with higher
      // scores than our topCandidate, but if we see the score going *up* in the first
      // few steps up the tree, that's a decent sign that there might be more content
      // lurking in other places that we want to unify in. The sibling stuff
      // below does some of that - but only if we've looked high enough up the DOM
      // tree.
      parentOfTopCandidate = topCandidate.parent()
      var lastScore = getReadabilityObject(topCandidate)?.contentScore ?: 0.0
      // The scores shouldn't get too low.
      val scoreThreshold = lastScore / 3.0

      while (parentOfTopCandidate != null && parentOfTopCandidate.tagName() != "body") {
        val parentOfTopCandidateReadability = getReadabilityObject(parentOfTopCandidate)
        if (parentOfTopCandidateReadability == null) {
          parentOfTopCandidate = parentOfTopCandidate.parent()
          continue
        }

        val parentScore = parentOfTopCandidateReadability.contentScore
        if (parentScore < scoreThreshold) {
          break
        }
        if (parentScore > lastScore) {
          // Alright! We found a better parent to use.
          topCandidate = parentOfTopCandidate
          break
        }

        lastScore = parentOfTopCandidateReadability.contentScore
        parentOfTopCandidate = parentOfTopCandidate.parent()
      }

      // If the top candidate is the only child, use parent instead. This will help sibling
      // joining logic when adjacent content is actually located in parent's sibling node.
      topCandidate = topCandidate!!
      parentOfTopCandidate = topCandidate.parent()
      while (parentOfTopCandidate != null &&
        parentOfTopCandidate.tagName() != "body" &&
        parentOfTopCandidate.children().size == 1
      ) {
        topCandidate = parentOfTopCandidate
        parentOfTopCandidate = topCandidate.parent()
      }
      if (getReadabilityObject(topCandidate) == null) {
        this.initializeNode(topCandidate, options)
      }

      return TopCandidateResult(topCandidate, false)
    }
  }

  /**
   * Get the density of links as a percentage of the content
   * This is the amount of text that is inside a link divided by the total text in the node.
   */
  protected open fun getLinkDensity(element: Element): Double {
    val textLength = this.getInnerText(element, regEx).length
    if (textLength == 0) {
      return 0.0
    }

    var linkLength = 0.0

    element.getElementsByTag("a").forEach { linkNode ->
      val href = linkNode.attr("href")
      val coefficient = if (href.isNotEmpty() && regEx.isHashUrl(href)) 0.3 else 1.0
      linkLength += this.getInnerText(linkNode, regEx).length * coefficient
    }

    return linkLength / textLength.toDouble()
  }

  /*          Forth step: Create articleContent           */

  @Suppress("CyclomaticComplexMethod", "NestedBlockDepth", "LoopWithTooManyJumpStatements")
  protected open fun createArticleContent(doc: Document, topCandidate: Element, isPaging: Boolean): Element {
    val articleContent = doc.createElement("div")
    if (isPaging) {
      articleContent.attr("id", "readability-content")
    }

    val topCandidateReadability = getReadabilityObject(topCandidate) ?: return articleContent

    val siblingScoreThreshold = maxOf(10.0, topCandidateReadability.contentScore * 0.2)
    val parentOfTopCandidate = topCandidate.parent()
    val siblings = parentOfTopCandidate?.children() ?: Elements()

    val siblingCandidates = collectSiblingCandidates(parentOfTopCandidate, siblings)

    val elementsToAppend = ArrayList<Element>()
    ArrayList(siblingCandidates).forEach { sibling ->
      if (shouldAppendSibling(
          sibling,
          topCandidate,
          topCandidateReadability,
          siblingScoreThreshold,
        )
      ) {
        logger.debug("Will append node: $sibling")
        elementsToAppend.add(sibling)
      }
    }

    sortElementsByDocumentOrder(elementsToAppend)
    appendElementsToArticle(elementsToAppend, articleContent)

    return articleContent
  }

  private fun collectSiblingCandidates(parentOfTopCandidate: Element?, siblings: Elements): ArrayList<Element> {
    val siblingCandidates = ArrayList<Element>()
    siblingCandidates.addAll(siblings)

    val grandparent = parentOfTopCandidate?.parent() ?: return siblingCandidates
    if (grandparent.tagName() == "body") {
      return siblingCandidates
    }

    val parentClasses = parentOfTopCandidate.className().split(" ").filter { it.isNotEmpty() }
    addCousinCandidates(grandparent, parentOfTopCandidate, parentClasses, siblingCandidates)

    return siblingCandidates
  }

  private fun addCousinCandidates(
    grandparent: Element,
    parentOfTopCandidate: Element,
    parentClasses: List<String>,
    siblingCandidates: ArrayList<Element>,
  ) {
    val semanticParentClasses = filterSemanticClasses(parentClasses)
    if (semanticParentClasses.isEmpty()) {
      logger.debug("No semantic classes found, skipping cousin candidates")
      return
    }

    grandparent.children().forEach { uncle ->
      if (uncle != parentOfTopCandidate) {
        processUncleElement(uncle, semanticParentClasses, siblingCandidates)
      }
    }
  }

  private fun filterSemanticClasses(parentClasses: List<String>): List<String> {
    val utilityPrefixes = listOf(
      "flex",
      "grid",
      "text",
      "bg",
      "p-",
      "m-",
      "w-",
      "h-",
      "max",
      "min",
      "rounded",
      "border",
      "shadow",
      "prose",
    )
    return parentClasses.filter { className ->
      !utilityPrefixes.any { prefix -> className.startsWith(prefix) } && className.length > 3
    }
  }

  private fun processUncleElement(
    uncle: Element,
    semanticParentClasses: List<String>,
    siblingCandidates: ArrayList<Element>,
  ) {
    val uncleClasses = uncle.className().split(" ").filter { it.isNotEmpty() }
    val hasCommonSemanticClass = semanticParentClasses.any { pc -> uncleClasses.contains(pc) }

    if (hasCommonSemanticClass) {
      addValidCousinCandidates(uncle, siblingCandidates)
    }
  }

  private fun addValidCousinCandidates(uncle: Element, siblingCandidates: ArrayList<Element>) {
    uncle.children().forEach { cousin ->
      if (isValidCousinCandidate(cousin)) {
        siblingCandidates.add(cousin)
        logger.debug("Adding cousin candidate: $cousin")
      } else {
        logger.debug("Skipping unlikely cousin: ${cousin.className()} ${cousin.id()}")
      }
    }
  }

  private fun isValidCousinCandidate(cousin: Element): Boolean {
    val matchString = cousin.className() + " " + cousin.id()
    return !regEx.isUnlikelyCandidate(matchString) || regEx.okMaybeItsACandidate(matchString)
  }

  private fun shouldAppendSibling(
    sibling: Element,
    topCandidate: Element,
    topCandidateReadability: ReadabilityObject,
    siblingScoreThreshold: Double,
  ): Boolean {
    val siblingReadability = getReadabilityObject(sibling)
    logger.debug(
      "Looking at sibling node: $sibling with score ${siblingReadability?.contentScore ?: 0}",
    )
    logger.debug("Sibling has score ${siblingReadability?.contentScore?.toString() ?: "Unknown"}")

    if (sibling == topCandidate) {
      return true
    }

    val contentBonus = calculateContentBonus(sibling, topCandidate, topCandidateReadability)

    return when {
      isIntroSectionWithContent(sibling) -> true
      hasHighEnoughScore(siblingReadability, contentBonus, siblingScoreThreshold) -> true
      shouldKeepSiblingBasedOnContent(sibling) -> true
      else -> false
    }
  }

  private fun calculateContentBonus(
    sibling: Element,
    topCandidate: Element,
    topCandidateReadability: ReadabilityObject,
  ): Double {
    return if (sibling.className() == topCandidate.className() && topCandidate.className() != "") {
      topCandidateReadability.contentScore * 0.2
    } else {
      0.0
    }
  }

  private fun isIntroSectionWithContent(sibling: Element): Boolean {
    val isIntroSection = sibling.className().contains("intro") || sibling.tagName() == "header"
    return isIntroSection && this.getInnerText(sibling, regEx).length > 50
  }

  private fun hasHighEnoughScore(
    siblingReadability: ReadabilityObject?,
    contentBonus: Double,
    siblingScoreThreshold: Double,
  ): Boolean {
    return siblingReadability != null &&
      ((siblingReadability.contentScore + contentBonus) >= siblingScoreThreshold)
  }

  private fun shouldKeepSiblingBasedOnContent(sibling: Element): Boolean {
    if (!shouldKeepSibling(sibling)) {
      return false
    }

    val linkDensity = this.getLinkDensity(sibling)
    val nodeContent = this.getInnerText(sibling, regEx)
    val nodeLength = nodeContent.length

    return when {
      nodeLength > 80 && linkDensity < 0.25 + linkDensityModifier -> true
      nodeLength in 1..<80 && linkDensity == 0.0 && nodeContent.contains("\\.( |$)".toRegex()) -> true
      else -> false
    }
  }

  private fun sortElementsByDocumentOrder(elements: ArrayList<Element>) {
    elements.sortWith { a, b ->
      val pathA = mutableListOf<Int>()
      var current: Element? = a
      while (current != null) {
        pathA.add(0, current.siblingIndex())
        current = current.parent()
      }

      val pathB = mutableListOf<Int>()
      current = b
      while (current != null) {
        pathB.add(0, current.siblingIndex())
        current = current.parent()
      }

      for (i in 0 until minOf(pathA.size, pathB.size)) {
        val cmp = pathA[i].compareTo(pathB[i])
        if (cmp != 0) return@sortWith cmp
      }
      pathA.size.compareTo(pathB.size)
    }
  }

  private fun appendElementsToArticle(elementsToAppend: ArrayList<Element>, articleContent: Element) {
    elementsToAppend.forEach { element ->
      logger.debug("Appending node in document order: $element")

      if (!ALTER_TO_DIV_EXCEPTIONS.contains(element.tagName())) {
        logger.debug("Altering sibling: $element to div.")
        setNodeTag(element, "div")
      }

      articleContent.appendChild(element)
    }
  }

  protected open fun shouldKeepSibling(sibling: Element): Boolean = sibling.tagName() == "p"

  /*          Fifth step: Prepare article            */

  /**
   * Prepare the article node for display. Clean out any inline styles,
   * iframes, forms, strip extraneous <p> tags, etc.
   */
  @Suppress("NestedBlockDepth")
  protected open fun prepArticle(articleContent: Element, options: ArticleGrabberOptions, metadata: ArticleMetadata) {
    this.cleanStyles(articleContent)

    // Check for data tables before we continue, to avoid removing items in
    // those tables, which will often be isolated even though they're
    // visually linked to other content-ful elements (text, images, etc.).
    markDataTables(articleContent)

    // Clean out junk from the article content
    this.cleanConditionally(articleContent, "form", options)
    this.cleanConditionally(articleContent, "fieldset", options)
    this.clean(articleContent, "object")
    this.clean(articleContent, "embed")
    this.clean(articleContent, "footer")
    this.clean(articleContent, "link")

    // Clean out elements have "share" in their id/class combinations from final top candidates,
    // which means we don't remove the top candidates even they have "share".
    val shareRegex = "share".toRegex()
    articleContent.children().forEach { topCandidate ->
      cleanMatchedNodes(topCandidate, shareRegex)
    }

    removeNodes(articleContent, "h1") { h1 ->
      headerDuplicatesTitle(h1, metadata)
    }

    removeNodes(articleContent, "h2") { h2 ->
      headerDuplicatesTitle(h2, metadata)
    }

    if (!options.preserveVideos) {
      this.clean(articleContent, "iframe")
    }
    this.clean(articleContent, "input")

    removeNodes(articleContent, "figure") { figure ->
      val images = figure.select("img")
      images.isEmpty()
    }

    removeNodes(articleContent, "div") { div ->
      val text = div.text().trim()
      val hasChildren = div.children().isNotEmpty()
      text.isEmpty() && !hasChildren
    }

    this.clean(articleContent, "textarea")
    this.clean(articleContent, "select")
    this.clean(articleContent, "button")
    this.cleanHeaders(articleContent, options)

    // Do these last as the previous stuff may have removed junk
    // that will affect these
    this.cleanConditionally(articleContent, "table", options)
    this.cleanConditionally(articleContent, "ul", options)
    this.cleanConditionally(articleContent, "div", options)

    // Remove extra paragraphs
    removeNodes(articleContent, "p") { paragraph ->
      val imgCount = paragraph.getElementsByTag("img").size
      val embedCount = paragraph.getElementsByTag("embed").size
      val objectCount = paragraph.getElementsByTag("object").size
      // At this point, nasty iframes have been removed, only remain embedded video ones.
      val iframeCount = paragraph.getElementsByTag("iframe").size
      val totalCount = imgCount + embedCount + objectCount + iframeCount

      return@removeNodes totalCount == 0 &&
        getInnerText(
          paragraph,
          normalizeSpaces = false,
        ).isEmpty()
    }

    articleContent.select("br").forEach { br ->
      val next = nextElement(br.nextSibling(), regEx)
      if (next != null && next.tagName() == "p") {
        br.remove()
      }
    }
  }

  /**
   * Remove the style attribute on every e and under.
   * TODO: Test if getElementsByTagName(*) is faster.
   */
  protected open fun cleanStyles(e: Element) {
    if (e.tagName() == "svg") {
      return
    }

    if (e.className() != "readability-styled") {
      // Remove `style` and deprecated presentational attributes
      PRESENTATIONAL_ATTRIBUTES.forEach { attributeName ->
        e.removeAttr(attributeName)
      }

      if (DEPRECATED_SIZE_ATTRIBUTE_ELEMS.contains(e.tagName())) {
        e.removeAttr("width")
        e.removeAttr("height")
      }
    }

    e.children().forEach { child ->
      cleanStyles(child)
    }
  }

  protected open fun markDataTables(root: Element) {
    root.getElementsByTag("table").forEach outer@{ table ->
      val role = table.attr("role")
      if (role == "presentation") {
        setReadabilityDataTable(table, false)
        return@outer
      }
      val datatable = table.attr("datatable")
      if (datatable == "0") {
        setReadabilityDataTable(table, false)
        return@outer
      }
      val summary = table.attr("summary")
      if (summary.isNotBlank()) {
        setReadabilityDataTable(table, true)
        return@outer
      }

      val caption = table.getElementsByTag("caption")
      if (caption.isNotEmpty() && caption[0].childNodeSize() > 0) {
        setReadabilityDataTable(table, true)
        return@outer
      }

      DATA_TABLE_DESCENDANTS.forEach { tag ->
        if (table.getElementsByTag(tag).isNotEmpty()) {
          logger.debug("Data table because found data-y descendant")
          setReadabilityDataTable(table, true)
          return@outer
        }
      }

      // Nested tables indicate a layout table:
      if (table.getElementsByTag("table").isNotEmpty()) {
        setReadabilityDataTable(table, false)
        return@outer
      }

      val sizeInfo = getRowAndColumnCount(table)
      if (sizeInfo.first >= 10 || sizeInfo.second > 4) {
        setReadabilityDataTable(table, true)
        return@outer
      }

      // Now just go by size entirely:
      setReadabilityDataTable(table, sizeInfo.first * sizeInfo.second > 10)
    }
  }

  /**
   * Return an object indicating how many rows and columns this table has.
   */
  protected open fun getRowAndColumnCount(table: Element): Pair<Int, Int> {
    var rows = 0
    var columns = 0

    val trs = table.getElementsByTag("tr")
    trs.forEach { tr ->
      rows +=
        try {
          tr.attr("rowspan").toInt()
        } catch (_: Exception) {
          1
        }

      // Now look for column-related info
      var columnsInThisRow = 0
      tr.getElementsByTag("td").forEach { cell ->
        columnsInThisRow +=
          try {
            cell.attr("colspan").toInt()
          } catch (_: Exception) {
            1
          }
      }

      columns = maxOf(columns, columnsInThisRow)
    }

    return Pair(rows, columns)
  }

  @Suppress("CyclomaticComplexMethod")
  protected open fun cleanConditionally(e: Element, tag: String, options: ArticleGrabberOptions) {
    if (!options.cleanConditionally) {
      return
    }

    val isList = tag == "ul" || tag == "ol"

    // Gather counts for other typical elements embedded within.
    // Traverse backwards so we can remove nodes at the same time
    // without effecting the traversal.
    //
    // TODO: Consider taking into account original contentScore here.
    removeNodes(e, tag) { node ->
      // First check if we're in a data table, in which case don't remove us.
      val isDataTable: (Element) -> Boolean = { element ->
        getReadabilityDataTable(element)
      }

      if (hasAncestorTag(node, "table", -1, isDataTable)) {
        return@removeNodes false
      }

      val weight = getClassWeight(node, options)
      val contentScore = 0

      logger.debug("Cleaning Conditionally $node")

      if (weight + contentScore < 0) {
        return@removeNodes true
      }

      if (getCharCount(node, ',') < 10) {
        // If there are not very many commas, and the number of
        // non-paragraph elements is more than paragraphs or other
        // ominous signs, remove the element.
        val p = node.getElementsByTag("p").size
        val img = node.getElementsByTag("img").size
        val li = node.getElementsByTag("li").size - 100
        val input = node.getElementsByTag("input").size

        var embedCount = 0
        node.getElementsByTag("embed").forEach {
          if (!isVideoContent(it.attr("src"))) {
            embedCount += 1
          }
        }

        val linkDensity = getLinkDensity(node)
        val contentLength = getInnerText(node, regEx).length

        val imageCheck = if (options.preserveImages) {
          false
        } else {
          (img > 1 && p / img.toFloat() < 0.5 && !hasAncestorTag(node, "figure"))
        }

        val embedCheck = if (options.preserveVideos) {
          false
        } else {
          ((embedCount == 1 && contentLength < 75) || embedCount > 1)
        }

        val haveToRemove =
          imageCheck ||
            (!isList && li > p) ||
            (input > floor(p / 3.0)) ||
            (!isList && contentLength < 25 && img == 0 && !hasAncestorTag(node, "figure")) ||
            (!isList && weight < 25 && linkDensity > 0.2 + linkDensityModifier) ||
            (weight >= 25 && linkDensity > 0.5 + linkDensityModifier) ||
            embedCheck
        return@removeNodes haveToRemove
      }

      return@removeNodes false
    }
  }

  /**
   * Check if a given node has one of its ancestor tag name matching the
   * provided one.
   */
  protected open fun hasAncestorTag(
    node: Element,
    tagName: String,
    maxDepth: Int = 3,
    filterFn: ((Element) -> Boolean)? = null,
  ): Boolean {
    val tagNameLowerCase = tagName.lowercase()
    var parent = node
    var depth = 0

    while (parent.parent() != null) {
      if (maxDepth in 1..<depth) {
        return false
      }

      val grandParent = parent.parent()
      if (grandParent != null &&
        grandParent.tagName() == tagNameLowerCase &&
        (filterFn == null || filterFn(grandParent))
      ) {
        return true
      }

      parent = grandParent ?: break
      depth++
    }

    return false
  }

  /**
   * Get the number of times a string s appears in the node e.
   */
  protected open fun getCharCount(node: Element, c: Char = ','): Int = getInnerText(node, regEx).split(c).size - 1

  /**
   * Clean a node of all elements of type "tag".
   * (Unless it's a youtube/vimeo video. People love movies.)
   */
  protected open fun clean(e: Element, tag: String) {
    val isEmbed = EMBEDDED_NODES.contains(tag)

    removeNodes(e, tag) { element ->
      // Allow youtube and vimeo videos through as people usually want to see those.
      if (isEmbed) {
        val attributeValues = element.attributes().joinToString("|") { it.value }

        // First, check the elements attributes to see if any of them contain youtube or vimeo
        if (isVideoContent(attributeValues)) {
          return@removeNodes false
        }

        // Then check the elements inside this element for the same.
        if (isVideoContent(element.html())) {
          return@removeNodes false
        }
      }

      return@removeNodes true
    }
  }

  /**
   * Clean out elements whose id/class combinations match specific string.
   */
  protected open fun cleanMatchedNodes(e: Element, regex: Regex) {
    val endOfSearchMarkerNode = getNextNode(e, true)
    var next = getNextNode(e)

    while (next != null && next != endOfSearchMarkerNode) {
      next = if (regex.containsMatchIn(next.className() + " " + next.id())) {
        removeAndGetNext(next, regex.pattern)
      } else {
        getNextNode(next)
      }
    }
  }

  /**
   * Clean out spurious headers from an Element. Checks things like classnames and link density.
   */
  protected open fun cleanHeaders(e: Element, options: ArticleGrabberOptions) {
    listOf("h1", "h2").forEach {
      removeNodes(e, it) { header ->
        getClassWeight(header, options) < 0
      }
    }
  }

  /*          Util methods            */

  protected open fun removeAndGetNext(node: Element, reason: String = ""): Element? {
    val nextNode = this.getNextNode(node, true)
    printAndRemove(node, reason)
    return nextNode
  }

  /**
   * Traverse the DOM from node to node, starting at the node passed in.
   * Pass true for the second parameter to indicate this node itself
   * (and its kids) are going away, and we want the next node over.
   *
   * Calling this in a loop will traverse the DOM depth-first.
   */
  protected open fun getNextNode(node: Element, ignoreSelfAndKids: Boolean = false): Element? {
    // First check for kids if those aren't being ignored
    if (!ignoreSelfAndKids && node.children().isNotEmpty()) {
      return node.child(0)
    }

    // Then for siblings...
    node.nextElementSibling()?.let { return it }

    // And finally, move up the parent chain *and* find a sibling
    // (because this is depth-first traversal, we will have already
    // seen the parent nodes themselves).
    var parent = node.parent()
    while (parent != null && parent.nextElementSibling() == null) {
      parent = parent.parent()
    }

    return parent?.nextElementSibling()
  }

  protected open fun getTextDirection(topCandidate: Element, doc: Document) {
    val ancestors = mutableSetOf(topCandidate.parent(), topCandidate)
    topCandidate.parent()?.let { ancestors.addAll(getNodeAncestors(it)) }
    ancestors.add(doc.body())
    ancestors.add(doc.selectFirst("html"))

    ancestors.filterNotNull().forEach { ancestor ->
      val articleDir = ancestor.attr("dir")
      if (articleDir.isNotBlank()) {
        this.articleDir = articleDir
        return
      }
    }
  }

  protected open fun getLanguage(doc: Document) {
    val htmlElement = doc.selectFirst("html")
    if (htmlElement != null) {
      val lang = htmlElement.attr("lang")
      if (lang.isNotBlank()) {
        this.articleLang = lang
      }
    }
  }

  protected open fun headerDuplicatesTitle(node: Element, metadata: ArticleMetadata): Boolean {
    val tagName = node.tagName()
    if (tagName != "h1" && tagName != "h2") {
      return false
    }

    val title = metadata.title ?: return false
    if (title.isBlank()) {
      return false
    }

    val heading = getInnerText(node, regEx)
    val similarity = HtmlUtil.textSimilarity(heading, title)

    return similarity > 0.75
  }

  protected open fun getReadabilityObject(element: Element): ReadabilityObject? = readabilityObjects[element]

  protected open fun getReadabilityDataTable(table: Element): Boolean = this.readabilityDataTable[table] ?: false

  protected open fun setReadabilityDataTable(table: Element, readabilityDataTable: Boolean) {
    this.readabilityDataTable[table] = readabilityDataTable
  }

  protected open fun shouldIncludeCandidate(candidate: Element): Boolean = candidateFilters.all { filter ->
    filter.shouldIncludeCandidate(candidate)
  }

  companion object {
    const val TAG_SCORE_DIV = 5
    const val TAG_SCORE_CONTENT = 3
    const val TAG_SCORE_LIST_PENALTY = -3
    const val TAG_SCORE_HEADER_PENALTY = -5

    val DEFAULT_TAGS_TO_SCORE = listOf("section", "h2", "h3", "h4", "h5", "h6", "p", "td", "pre")

    val DIV_TO_P_ELEMS =
      listOf("a", "blockquote", "dl", "div", "img", "ol", "p", "pre", "table", "ul", "select")

    val ALTER_TO_DIV_EXCEPTIONS = listOf("div", "article", "section", "p")

    val PRESENTATIONAL_ATTRIBUTES = listOf(
      "align",
      "background",
      "bgcolor",
      "border",
      "cellpadding",
      "cellspacing",
      "frame",
      "hspace",
      "rules",
      "style",
      "valign",
      "vspace",
    )

    val DEPRECATED_SIZE_ATTRIBUTE_ELEMS = listOf("table", "th", "td", "hr", "pre")

    val EMBEDDED_NODES = listOf("object", "embed", "iframe")

    val DATA_TABLE_DESCENDANTS = listOf("col", "colgroup", "tfoot", "thead", "th")
  }
}
