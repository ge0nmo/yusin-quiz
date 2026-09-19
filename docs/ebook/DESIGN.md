---
name: Quiz EPUB Reading
description: Quiet Korean exam review in a reader-controlled, reflowable book.
colors:
  cover-paper: "#f4f1e8"
  cover-green: "#254b3e"
  cover-ink: "#203d33"
  reading-rule: "#939b94"
typography:
  body:
    fontFamily: '"Noto Serif CJK KR", "Batang", serif'
    fontSize: "1em"
    fontWeight: 400
    lineHeight: 1.85
  chapter:
    fontSize: "1.8em"
    lineHeight: 1.4
  question:
    fontSize: "1.28em"
    lineHeight: 1.4
  source:
    fontFamily: "sans-serif"
    fontSize: ".82em"
    lineHeight: 1.6
---

# Design System: Quiz EPUB Reading

## Overview

Mode: Read. This scoped system describes the generated EPUB, not the admin website or a standalone reader application. Its original, Kyobo-inspired treatment uses restrained typography, generous reading rhythm, and a cream-and-green cover. Kyobo branding and reader integration are not part of the artifact.

Ground truth: `src/main/resources/ebook/reading.css`, `EpubRenderer.java`, and `EpubCover.java`. Operational details and verification records live in [IMPLEMENTATION.md](IMPLEMENTATION.md).

## Colors

The fixed SVG cover uses cream paper, green rules and a left spine strip, and dark green lettering. Body foreground and background are unset so the reading app can supply its theme. Thin reading rules use `reading-rule`; the chapter underline and link focus outline use `currentColor`. Hierarchy also relies on size, weight, spacing, and borders. Source span colors, when supplied, remain source content rather than book-wide theme tokens.

## Typography

The body requests Korean serif fallbacks without bundling fonts. Relative sizes preserve reader enlargement: body `1em`, chapter `1.8em`, question `1.28em`, subsection `1em`, source `.82em`, and answer/choice-explanation labels `.92em`. Body line height is `1.85`; headings use `1.4`. Source labels use sans-serif with line height `1.6`. Original line breaks and semantic emphasis are retained; source headings become subordinate `h3` elements.

Cover lettering is sans-serif SVG artwork. Its title wraps and scales down for long supplied titles; it does not share the body's adjustable text size.

## Layout

The book reflows with a `5%` body margin and word wrapping. It imposes no fixed page height or CSS columns. Metadata declares reflowable layout and `rendition:spread=auto`; the reading app decides pagination and whether to show facing pages.

Consecutive entries with the same source question ID form one group. Groups have `2.6em` bottom spacing; later groups start after a thin rule and `1.8em` top padding. Heading/source blocks resist page splits and separation from following text, while long questions can flow across pages. Widow and orphan targets are two lines. Inline answers also resist splitting; reader support determines final pagination.

## Elevation & Depth

Flat editorial composition, with no shadows. Whitespace, type hierarchy, and horizontal rules separate content.

## Shapes

Straight rules and square statement frames. The cover is a `900 × 1260` SVG with a narrow green left strip. Its displayed image scales to available width, capped at `32em`, with automatic height.

## Components

- **Cover:** prints only the supplied title, selected years (or their range when lengthy), and total question count. No fabricated author or publisher.
- **Question:** number and year/exam/subject source line precede the original text and ordered choices. Distinct source identities remain distinguishable even when numbers repeat.
- **Answer:** a compact bold text strip between thin rules. Adjacent answers omit repeated number/source headings; standalone answers retain them.
- **Explanation:** adjacent explanations start with a short heading; per-choice explanations have subordinate labels. Placement follows the independently chosen generation settings.
- **Statements:** a bordered definition list preserves labels and their associated content.
- **Contents:** the EPUB TOC links to the cover and logical chapters using quiet ruled rows and inherited text color. Links have a visible focus outline. All per-question forward, return, and answer-check links are removed.
- **Reader controls:** macOS Books owns its toolbar, type-size controls, contents, search, and navigation. These are not EPUB widgets and no custom toolbar is generated.

## Do's and Don'ts

- Do preserve reader theme and font enlargement, source wording, grouping, and useful chapter navigation.
- Do regenerate and reopen an EPUB when evaluating changes; previously imported copies do not update.
- Don't force spreads, page counts, body colors, or fixed-height question containers.
- Don't add decorative buttons, repeat inline source headings, or invent publication identity.
- Don't equate offline preview with native reader certification: offline renders were reviewed and all nine placement combinations passed EPUBCheck; native Books visual review remains blocked by pending computer-use permissions.
