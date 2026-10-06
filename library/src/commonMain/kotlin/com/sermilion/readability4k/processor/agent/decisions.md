## [2026-10-06] Component name attributes are name signals
Context: Once utility classes stopped counting, the GamesRadar comment widget leaked a sign-in prompt into the article. In 0.2.0 it had been dropped only because a utility class happened to contain `comment` (`rounded-b-[var(--comment-widget-border-radius,0)]`). The widget's only real name is `data-component-name="Viafoura:Comments"`.
Decision: `data-component` and `data-component-name` join the hook attributes, so they feed the same match string and class-weight slot as test hooks.
Reason: Component-based sites (Future plc, Hearst, and others) name regions such as `Footer`, `PostArticleLinks`, and `article-content` in these attributes. That is the same kind of signal as test hooks.
Alternatives considered: Treating `overflow-hidden` or bracketed tokens as names again (brings back the NEWS-144 false signals).

## [2026-10-06] `carousel` is negative vocabulary
Context: Future plc articles (PC Gamer, GamesRadar) embed a JW Player carousel mid-article. Its static HTML is a "Latest Videos From <site>" header and an empty player host. 0.2.0 dropped it only because of `overflow-hidden`. Its real name is `data-component-name="JwPlayer:Carousel"`.
Decision: Add `carousel` to `NEGATIVE_DEFAULT_PATTERN`.
Reason: A carousel is a navigation or recirculation widget, like the `promo`, `related`, and `widget` names already in the list. A negative weight does not strip the element. It only makes the conditional cleaner judge it more strictly. Across 206 pages from the Readian feed, the 20 pages from the adb run, and the 109 jvmTest pages, extraction matches 0.2.0 except the recovered inline word on lifehacker.ru and a spacing change on IGN.
Alternatives considered: Unlikely vocabulary (would strip galleries outright on the first attempt). `player` (would penalise kept video embeds).

## [2026-10-06] Name-based removal never cuts words out of a sentence
Context: Unlikely-candidate stripping and the share cleaner remove any element whose name matches, including inline wrappers inside a paragraph. Investing.com wraps linked company names in `<span class="aqPopupWrapper">`, so "Former Anthropic researcher" lost "Anthropic".
Decision: Both removals skip an element that is phrasing content (Mozilla's phrasing list, plus `a`, `del`, and `ins`, with every descendant phrasing) when its parent has non-blank text of its own. The share cleaner now reads the same match string as the other name checks.
Reason: An inline element in running text is part of the sentence whatever it is named. Block-level chrome and inline chrome without surrounding text are still removed.
Alternatives considered: Dropping `popup` from the unlikely vocabulary (keeps the bug for every other name). Reusing `hasChildBlockElement` (it counts `a` and `img` children as blocks, so link wrappers never qualify).

## [2026-10-06] Name checks read semantic class tokens, id, and test hooks
Context: Utility-class sites (Tailwind, Bootstrap) name their regions in test hook attributes rather than class or id, and their class attributes are mostly presentational tokens. Investing.com marks its Fusion Media footer only with data-test="footer", and its text-xs class matched the positive `text` pattern, so the footer outscored the Reuters body.
Decision: Every grabber name check reads one match string: class tokens that are not atomic CSS utilities, the id, and test hook values. The vocabulary, strip and weight flags, and retry sequence are unchanged, so hook names are stripped, scored, and relaxed exactly like class names.
Reason: This extends the existing signal source instead of adding a removal path keyed to one attribute value. A data-testid="related-stories" block is stripped by the same `related` rule that strips a class of that name.
Alternatives considered: Deleting data-test="footer" on every attempt (a special case that bypasses the strip flag). Stripping only `text-` tokens inside class weight (leaves overflow-hidden, md:hidden, and every other check unchanged).

## [2026-10-06] Utility vocabulary targets collisions, and is value-aware where names are shared
Context: Substring patterns such as `text`, `content`, `hidden`, and `scroll` also occur inside utility tokens such as text-sm, content-center, overflow-hidden, and scroll-smooth.
Decision: `UTILITY_CLASS_DEFAULT_PATTERN` covers variant (`md:hidden`) and arbitrary-value (`text-[18px]`) forms, any `text-*`, overflow and scroll utilities with their CSS values, and content-alignment utilities. The pattern is a RegExUtil constructor parameter like the other vocabularies.
Reason: content-wrapper, overflow-menu, and scroll-container are semantic names, so only their utility values count as presentational. A `text-` prefix is almost always a utility (size, alignment, or colour, including custom theme colours), so the whole prefix counts; a rare text-content class loses only its +25.

## [2026-10-06] Test hook names share the class-name weight slot
Context: Scoring hook names as a third weight source next to class and id let an element reach +75. On the jvmTest corpus that pulled Nature's related-articles and subjects lists into the body, and it also merged Wired's chunked deals body.
Decision: Hook names join the semantic class name before positive and negative matching, so class weight stays -50..+50.
Reason: Hook names describe the same component as its class. They add a signal where the class has none (utility or hashed classes) without rescaling Mozilla's calibrated weights.
Revisit when: A fixture shows a hashed-class site whose body is named only by its hooks and still loses to chrome.

## [2026-10-06] Change weights only when a body phrase is lost
Context: The 100-page corpus splits calibration hosts from validation hosts. Keeping body text outranks catching every bio.
Decision: Edit `AuthorBioScoring` only, and only after a `must_keep` sentence disappears. Validation hosts other than the TweakTown and Notebookcheck seeds stay printed misses.
Reason: Fitting those validation hosts would spend the hosts that are supposed to stay untouched. A missed ancillary block is acceptable when fixing it would drop a body sentence.
Revisit when: A seed page stops clearing the frozen formula, or a calibration host loses a body sentence.

## [2026-10-06] Leave contentScore and link density out of the total
Context: Top-candidate selection overwrites `contentScore` with a link-adjusted value, and long bios already score well as prose.
Decision: Copy the pre-adjustment score onto the decision. Record link density and a descendant rel=author link. Leave all three out of both track totals.
Reason: A keep vote from `contentScore` would protect the long bios this cleanup is meant to remove. A byline link must not finish a weak class match.
Alternatives considered: The live `contentScore` was rejected because selection overwrites it and some nodes have no stored score.

## [2026-10-06] Ignore weightClasses in author class rules
Context: Short pages retry with `weightClasses` false when no attempt reaches the character threshold.
Decision: Class and id matches still count on that retry. Capture runs inside the winning attempt.
Reason: If class rules followed the flag, the retry would keep the bio and could win because the page looks longer.

## [2026-10-06] Keep disclaimer vetoes off the bio track
Context: Bios and disclaimers need separate calibration. A long dense bio should still be removable.
Decision: Score both tracks on one candidate. Remove when either track reaches 4 and the hard stops allow it. The 10-comma and 600-character keeps apply only to the disclaimer track.
Reason: Sharing those keeps with bios would preserve the long Notebookcheck and TweakTown blocks. Hard stops still keep the article root, stop before the next equal-or-higher heading, and keep a candidate longer than 45 percent of the article.

## [2026-10-06] Treat only exact English labels as section labels
Context: TweakTown marks "About the author" with a paragraph, and a body sentence can contain the same words.
Decision: Accept about the author, about author, author bio, and disclaimer only when the full trimmed text equals the label, aside from one trailing colon. Paragraph labels run until the next heading. Heading labels stop before the next heading of equal or higher rank. Non-English labels stay unimplemented.
Reason: Heading-only discovery misses the TweakTown block. A substring match would delete body sentences that merely mention the phrase.

## [2026-10-06] Recapture source attributes on every extract attempt
Context: Retries rebuild the DOM from cached HTML. `prepareNodes` unwraps some divs and can drop a negative-weight heading.
Decision: Capture before `prepareNodes` inside each extract attempt. Copy `contentScore` after scoring and before top-candidate selection. Apply after article prep and before the readability page wrap. Store an unwrapped parent's class, id, rel, and itemtype on the surviving child.
Reason: A snapshot from an earlier attempt points at detached nodes. The unwrap drops the parent, so the child's live attributes are not enough. Cleanup has to finish before later processing drops classes.

## [2026-10-06] Read Person from itemtype, not JSON-LD
Context: Schema Person was a requested signal. Preprocessing already removes script.
Decision: Award Person points only when the candidate itemtype contains Person. Leave JSON-LD unread. One point source does not delete. A trailing Person box with a longer body ahead can reach 4. A Person box that is most of the article stays.
Reason: Script is gone before apply, so JSON-LD is not there to read. Treating Person plus position as one signal would either delete profile articles or keep the small trailing box.

## [2026-10-06] Keep publisher names out of commonMain
Context: Calibration and validation are split by host, and the library stays Kotlin Multiplatform.
Decision: The JVM test sorts corpus hosts. Validation hosts are notebookcheck.net, tweaktown.com, and every host whose sorted index is divisible by 3. The split, the corpus, and the seed absences stay in jvmTest.
Reason: Host names in the scorer would turn a portable score into a site list. A missing body sentence fails on every host. An expect-absent hit fails on calibration hosts and the two seeds, and only prints on the other validation hosts.

## [2026-10-06] Place contentExtension before articleGrabber
Context: Both long `Readability4K` constructors default `articleGrabber` to a grabber that must receive the extension.
Decision: `contentExtension` is the parameter immediately before `articleGrabber`, default null. The grabber takes it last. The two-argument constructors stay as they are. Leave `@JvmOverloads` off.
Reason: A Kotlin default expression can only read parameters declared before it, so the extension cannot be last on the facade. A caller who passes `articleGrabber` has already chosen that grabber's extension.
Alternatives considered: `@JvmOverloads` was rejected because it would multiply JVM overloads. Java callers of the long constructors need a recompile. The two-argument constructors do not.

## [2026-10-06] Keep author cleanup off the default parse
Context: Ordinary Readability has to stay free of author-bio deletion. The previous strip lived inside article prep and the grabber.
Decision: One optional `ArticleContentExtension`. Null skips capture and apply. `ReadabilityOptions` and `ArticleGrabberOptions` do not hold it. The grabber does not name `AuthorBioDisclaimerExtension`.
Reason: A plugin list or an options field would make every parse depend on this cleanup. One parameter is enough for the first caller.
Revisit when: A second cleanup needs this same lifecycle and cannot share the parameter.

## [2026-10-06] Keep the extension instance stateless
Context: The grabber is reused across parses, and one extension instance can see two documents in a row.
Decision: `decisions()` returns a fresh list and `apply` deletes from that same evaluation. Per-parse decisions and snapshots stay off the instance.
Reason: Stored parse state would leak a removal into the next document. Two parses can share one instance.

## [2026-10-06] Author-card labels can become the byline
Context: With the extension absent, bio prose stays in the article, but a short label whose class contains author can match the existing byline pattern.
Decision: Treat that byline move as existing metadata behavior. The extension is not required for it, and the bio prose stays in the article on the default parse.
Reason: The byline pattern already matches classes such as tt-author-cards. Changing that pattern was outside this cleanup.
