## [2026-10-06] Keep the bio object at ten functions
Context: Detekt reports an object once it reaches 11 functions. The strip needs several helpers.
Decision: Matchers and removal live in an internal processor object. The grabber exposes a thin protected method. Block length is computed in the heading check so the object stays at 10 functions.
Reason: An eleventh function fails the gate. The object has no suppression and no baseline entry.

## [2026-10-06] Copy a matching class and id onto an unwrapped paragraph
Context: prepareNodes replaces a div that holds a single paragraph with that paragraph and drops class and id before the strip runs at the end of prepArticle.
Decision: When that div matches a compound token or a bare author or disclaimer token, copy the class and id onto the replacement paragraph. The later strip still applies the preceding-text guard.
Reason: Boxes that also contain a heading or image were already removed. A single-paragraph author-box id and a short disclaimer class lost the attributes the strip reads, so those sentences stayed in the article.

## [2026-10-06] Remove heading blocks only when the body is longer
Context: Author-bio and disclaimer sections have to leave the extracted article, and a real body has to stay.
Decision: Compound class and id tokens are removed with no length guard. A bare author or disclaimer token, and headings About the author, About author, Author bio, and Disclaimer, are removed only when the text before them is longer than the block. Siblings are removed until the next heading of the same or higher rank. A paragraph that only contains the phrase stays.
Reason: Compound names identify the box. A wrapper whose class is author, or a short lead followed by a longer about-the-author section, can be the article itself.

## [2026-10-06] Run the bio strip at the end of prepArticle
Context: Trailing about-the-author and disclaimer blocks survive into reading mode. Class and id are still present in prepArticle, and post-processing drops classes when keepClasses is false.
Decision: Call one protected strip at the end of prepArticle, after share cleanup, on every grab attempt, including attempts that disable unlikely-candidate removal, class weighting, or conditional cleaning. There is no new public option.
Reason: A long author box matches the byline pattern and stays because a valid byline is under 100 characters. A class that contains both sidebar and author is treated as a candidate. Conditional cleaning keeps a div once it has 10 or more commas. Short metadata removal does not see these bios.

## [2026-10-06] Test the strip with synthetic pages
Context: The two publisher pages named in the issue have unknown class and id strings.
Decision: Use the listed compound tokens and the English heading phrases. Tests are synthetic common tests against example.com. Host names stay out of library code, patterns, and test URLs. Existing notebookcheck tests stay unchanged. Non-English headings stay unimplemented.
Reason: The digest fixed the token list because the markup was unknown, so this work does not fetch those URLs. A later captured page may add one generic compound token and live only as a jvm test resource.
