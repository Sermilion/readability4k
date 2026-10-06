## [2026-10-06] Remove data-test footer on every extract attempt
Context: The Fusion Media disclosure is marked only data-test="footer". The short-body retry turns class and id stripping off, and the match string is class plus id, so unlikely-candidate removal never sees that attribute.
Decision: On every grab attempt, delete a non-body element whose own data-test equals footer, ignoring case on the whole value. Leave ancestors. Leave values such as footer-legal. Reuse the existing removal walk.
Reason: Removal only while stripUnlikelyCandidates is true still returns the disclosure on the retry. A substring match would delete unrelated nodes. Skipping body keeps the document root.
Alternatives considered: Editing the unlikely-candidate pattern was rejected because that path stays behind the strip flag.

## [2026-10-06] Drop non-positive text- tokens before the class bonus
Context: A Tailwind class that begins with text- matches the positive text pattern and scores as content, so a long disclosure outranks a short article.
Decision: Keep one positive bonus and one negative penalty per class string and per id. Ignore a whitespace-delimited token that starts with text-, case-insensitively, when the remainder is not positive. Score the negative pattern on the full class string and the full id. Leave the shared patterns unchanged. weightClasses false still returns zero.
Reason: text-sm and text-gray-500 should add nothing. text-content still gains the bonus because the remainder matches. article-text still gains it because the token does not start with text-. Case-insensitive prefix matching follows the positive check, so TEXT-sm does not keep the bonus.
Alternatives considered: Editing the shared positive pattern was declined so the change stays in prepareNodes and getClassWeight.

## [2026-10-06] Treat conditional removal of footer text-sm as intended
Context: Conditional cleaning and header cleaning use class weight as the whole score and remove a node when that weight is below zero.
Decision: A class of footer plus text-sm keeps the negative penalty and loses the text- bonus, so those cleaners can remove the node.
Reason: Before this change that class netted to zero. Keeping the penalty is why the negative pattern still runs on the full string.

## [2026-10-06] Prove the footer bug with a synthetic sibling fixture
Context: The live Investing.com DOM is not in the repo. Exact utility names beyond a text- class, compound data-test values, and the Reuters body length are unknown.
Decision: One commonTest compares a neutral article container with a sibling data-test=footer block classed text-sm text-gray-500. Cover a body over 500 characters and a body under 500 with a longer disclosure. Assert the article sentence is present and the disclosure is absent. Do not add an Investing.com fixture.
Reason: The unfixed short page can return on the first attempt when the disclosure is at least 500 characters, so the second case fails only if removal is skipped once stripping is off. The grabber still returns the longest non-empty attempt when every attempt stays under 500, so the short article stays observable after the footer is gone.

## [2026-10-06] Keep footer removal and the text- rule on the default parse
Context: Reading Mode uses the default parse. Grabber options already default stripping, class weights, and conditional cleaning on, and the character threshold stays 500.
Decision: No options field turns the data-test removal or the text- token rule off. generateOptionsSequence, ArticleGrabberOptions, and ReadabilityOptions stay unchanged. Callers stay source compatible. The README gains no flag.
Reason: A switch would leave the broken default in place. The CLI stays a wrapper, and cutting a release is outside this change.

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
