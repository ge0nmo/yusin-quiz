---
name: Quiz EPUB Reading
description: Restrained Korean exam reading with one subtle number accent and continuous text.
colors:
  question-number: "#45584d"
  secondary: "#666666"
  rule: "#bdbdbd"
typography:
  body:
    fontFamily: '"Noto Sans CJK KR", "Noto Sans KR", "Apple SD Gothic Neo", "Malgun Gothic", sans-serif'
    fontSize: "1em"
    fontWeight: 400
    lineHeight: 1.7
  chapter:
    fontSize: "1.65em"
  question:
    fontSize: "1.35em"
  source:
    fontSize: ".85em"
    lineHeight: 1.5
---

# Design System: Quiz EPUB Reading

## Current direction

Mode: Read. This is the generated EPUB, not an admin UI or custom reader. The user's native-reader screenshots rejected the earlier ivory panels, ochre answer strips, repeated colored headings, and oversized gaps as overly decorated. This revision supersedes the warm-panel design. The book should read as continuous typeset text, with a restrained accent only on original question numbers.

Keep the accepted Korean sans-serif default, adjustable type, original content, and reflow across phones/tablets/PC in both orientations. The intended viewing mode is light. There is no authored dark theme. Leave background painting and page margins to the reader: explicit body background colors appeared as disconnected rectangles in Apple Books instead of coloring the whole page. External reader font/theme overrides remain possible.

Ground truth: `src/main/resources/ebook/reading.css`, `EpubRenderer.java`, and the unchanged `EpubCover.java`.

## Typography and color

Use the local Korean sans-serif fallback stack, without font downloads. Body and answer text are `1em`; subsection labels are `1em` bold; original numbers are `1.35em`; chapter titles are `1.65em`. Unit/kind labels are `.74em` of the question heading. Source metadata is `.85em`, neutral gray. Body line height is `1.7`, metadata/answers `1.5`, and headings `1.35`.

Only `.problem-number` receives the muted green accent. Body, answer and explanation text inherit the reader foreground. No answer strips, explanation fills, colored underlines, selection tint or per-choice separator rules. Chapter titles and statement frames retain thin neutral lines. Stored source emphasis and span colors remain original content; do not delete those as book-theme decoration.

## Reflow and spacing

Body width is automatic, capped at `38em`, with `margin: 0 auto`. Reader-provided margins handle the page edge; the EPUB adds no percentage inset or vertical page padding. No fixed height, fixed columns, forced per-question page break, or full-question keep-together.

Question groups have zero bottom margin. The next group uses one `2em` top gap, increased from `1.4em` after the user found question boundaries too tight. The rejected design combined `2.8em` bottom margin with `2em` next-group padding and a border; do not reinstate that stack. Paragraph margins are `.5em`; options use `.4em`; heading-to-body spacing is `.55em`; inline answer and explanation gaps are `.75em`.

Only short source headings and answer lines resist splitting. Headings request attachment to following content; long questions/options/explanations may fragment. Widow/orphan targets remain two lines. Reader pagination can still leave some page-end space when the next heading and opening lines do not fit; eliminating every blank line would sacrifice readability. Source headers are compact to reduce this pressure.

## Components

- **Question:** original number with subtle green accent, smaller unit, compact neutral source line, original body and hanging numbered choices.
- **Answer:** a short bold line, with no fill, frame or colored text. Multiple correct choices are preserved.
- **Explanation:** plain paragraphs. Show the generated `해설` heading for an inline overall explanation; when only per-choice explanations exist, start directly with `N번 보기 해설`. Standalone entries keep their question number/source. Per-choice headings use weight and spacing only.
- **Statements:** thin neutral frame; `2.2em` label column and `2.4em` hanging indent, `.4em` between rows. Preserve label/first-line alignment and allow long rows to fragment.
- **Contents:** neutral ruled rows, inherited link color, visible keyboard focus. No per-question return/answer-check buttons.
- **Cover:** existing cream/green typographic SVG remains unchanged; the revision concerns reading pages.

## Verification boundary

`./gradlew test` validates all nine placement combinations and generates fictional review samples. A separate local preview, `build/ebook-samples/appraiser-quiet-preview.epub`, applies the same styles and generated-title change to the user's existing downloaded book. Original content/answer blocks were compared for equality; the preview has a distinct publication identifier and review title to avoid reader caching.

Native Apple Books review is now available: both the rejected original and revised real-content preview were inspected in the same two-page window. The revised view removes the colored rectangles and allows the next question to begin partway down a page. This does not certify every device or EPUB reader, and some app-specific page-break behavior remains.

Regenerate books with the updated backend. Imported copies never update automatically. No production server or source exam data was changed.
