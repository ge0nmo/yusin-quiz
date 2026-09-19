---
name: Quiz EPUB Reading
description: Warm ivory, light-only Korean exam review with reflowable typography.
colors:
  paper: "#faf7f0"
  explanation: "#f0eadc"
  ink: "#2f302b"
  heading: "#315c4e"
  secondary: "#696253"
  answer-paper: "#ede2c8"
  answer-ink: "#76531f"
  rule: "#d5ccba"
typography:
  body:
    fontFamily: '"Noto Sans CJK KR", "Noto Sans KR", "Apple SD Gothic Neo", "Malgun Gothic", sans-serif'
    fontSize: "1em"
    fontWeight: 400
    lineHeight: 1.8
  chapter:
    fontSize: "1.9em"
    lineHeight: 1.4
  question:
    fontSize: "1.5em"
    lineHeight: 1.4
  source:
    fontSize: ".85em"
    lineHeight: 1.6
---

# Design System: Quiz EPUB Reading

## Scope and confirmed direction

Mode: Read. This system describes the generated EPUB, not the admin website or a separate reader app. The user chose an extensive readability revision: all devices in portrait and landscape, balanced density, Korean sans-serif body, light-only warm ivory paper, and a slightly deeper ivory explanation surface. Printed workbook references establish information hierarchy, not a fixed two-column page template. These choices supersede the earlier serif and reader-controlled default color design.

Ground truth: `src/main/resources/ebook/reading.css`, `EpubRenderer.java`, and `EpubCover.java`. The existing cream-and-green cover remains. No invented author, publisher, editorial facts, or additional source text.

## Color and typography

Paper and ink are explicitly paired on the root/body, explanation panel, and answer strip. There is no authored dark theme or dark media query. External reader themes and font preferences may override the book; no `!important` rules attempt to lock them out.

The body requests local Korean sans-serif fonts with no bundled fonts or network requests. Relative sizes keep reader enlargement available. Body and answers are `1em`; choice explanation labels are `1em` bold; inline explanation titles are `1.1em`; question numbers are `1.5em`; chapters are `1.9em`. The question unit and standalone entry kind are `.67em` of the question heading. Source metadata is `.85em`. Body line height is `1.8`, choices `1.75`, metadata `1.6`, and headings `1.4`.

Dark green identifies chapters, question numbers, and explanation headings. Warm brown identifies answers; muted warm ink identifies source metadata. Hierarchy also uses size, weight, spacing, lines, and explicit labels. Original wording, authored line breaks, and semantic/source span emphasis remain intact. Generated explanation headings have dedicated classes so source-authored `h3` blocks do not receive the section-label treatment.

## Reading measure and rhythm

Body width is `90%`, capped at `38em`, horizontally centered with `1.6em` vertical margins. It specifies no fixed page height, device width, or CSS columns. The EPUB remains reflowable with `rendition:spread=auto`; reader software controls pagination and facing-page display.

Paragraph margins are `.65em`; options use `.65em` vertical separation and hanging list markers. Question groups have `2.8em` bottom space; subsequent groups start after a thin rule with `2em` padding. Headings/source lines stay visually attached, with `1em` before the body. Answers follow questions after `1.4em`; explanations follow after `1.2em`.

Avoid page breaks inside short source headings and answers and after headings. Permit breaks within long questions, options, statements, and explanation panels. Widow/orphan targets are two lines. Reader support determines actual page boundaries; an offline browser preview is not native EPUB pagination certification.

## Components

- **Question:** large green original number, quieter number unit, compact source line, original content, and numbered choices. Repeated numbers retain distinct source identities.
- **Answer:** full-width, compact warm ochre strip with dark brown bold text and thin top/bottom rules. Multiple correct answers remain visible. No inline source repetition or answer-check controls.
- **Explanation:** one warm ivory surface, `.9em` horizontal and `1em` vertical padding. Inline entries begin with an underlined green `해설` label. Standalone entries include their original number and source inside the same surface. Per-choice explanations remain inside it and use bold labels with light horizontal separators, never individual nested cards. Missing explanations are omitted.
- **Statements:** square, thin-bordered frame with `.7em` horizontal padding. Each `dt`/`dd` pair retains the existing hanging-indent fix: `2.2em` label column, `2.4em` content indent, `.55em` between rows. First/last child margins reset so labels align with the first text line. Long entries may paginate.
- **Contents:** quiet ruled rows, green links, and visible keyboard focus. Navigation belongs to the EPUB TOC and the reader; no per-question return/forward buttons.
- **Cover:** existing typographic SVG containing the supplied title, selected years, and question count, scaled to available width up to `32em`.

## Verification and use

Review both `build/ebook-samples/sample-review.epub` (year-end explanations) and `sample-reading-inline.epub` (inline explanations). Their long Korean sample content is explicitly fictional, not actual exam or legal material.

Use the updated backend to regenerate EPUBs. Existing downloaded/imported copies do not update. Prefer a distinct review title so readers can distinguish versions. Test light-theme rendering and reader text enlargement on the intended apps; external app appearance overrides cannot be prohibited by the EPUB.
