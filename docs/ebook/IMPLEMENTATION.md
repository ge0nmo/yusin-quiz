# Quiz Ebook: Agent Implementation Handoff

Updated: 2026-09-19. Status: EPUB feature implemented; final verification evidence below. PDF is deliberately deferred by the user.

## Resume protocol

Read this file, the target repository's AGENTS.md, and its referenced instructions before editing. Inspect Git status and preserve unrelated changes. The workspace contains sibling repositories `yusin-quiz` (backend), `quiz-admin` (web admin), and `quiz-app` (mobile). No mobile changes are needed for this feature.

User requests maintainable, clearly commented code. Comments must explain meaningful logic and invariants. The user invoked `grill-me`: ask one unresolved product question at a time, with a recommendation, and investigate code questions independently. The implementation phase is authorized; do not re-ask confirmed decisions. Keep this handoff in English and the human guide in Korean.

## Confirmed requirements

- Purpose: last-minute past-exam review, private distribution.
- One qualification per book, e.g. APPRAISER or CUSTOMS_BROKER; selectable subjects and years.
- EPUB first; architecture must allow later PDF without duplicating content fetching/composition.
- Order by year, then original question number.
- Default: correct answers under each question; overall and per-choice explanations at the end of that year.
- Answer and explanation positions are independently configurable at generation time.
- Include all stored overall/per-choice explanations. When absent, include correct answers only.
- Preserve original wording. Staff review errors, obsolete laws/rules, and final appearance manually.
- No supplementary manuscript. Do not invent introductions, author/publisher identity, or legal statements.
- Readability is the priority; KNOU/Kyobo ebooks are directional references, not supplied templates or promised reader integrations.
- Rights/source details were not supplied; this is not verified ownership or a license declaration.
- Manual creation through the existing admin and download workflow was accepted with “proceed.”
- Human documentation must explain both macOS and Windows installation, execution, and use.

## Implemented defaults and boundaries

These are implementation choices, documented to the user rather than silently attributed to explicit user answers:

- Newest year first, with an oldest-first option.
- Fully published content only across qualification/exam/mapping/subject/problem.
- Original number ascending within year. Ties: exam ID, subject display order, subject ID, problem ID. Source exam/subject labels distinguish repeated numbers without rewriting them.
- `AFTER_QUESTION`, `YEAR_END`, and `BOOK_END` for each independent placement setting.
- Editable derived title; no fabricated creator/publisher. A generated typographic SVG cover and navigation are included.
- Synchronous generation, one request per backend process at a time; no job queue, permanent files, download history, external storage, or automatic publishing.
- At most 5,000 questions and 20,000,000 serialized source-content bytes per request; browser wait limit 120 seconds. Split scope on limit errors. Browser disconnect does not guarantee backend cancellation.
- Text-only initial output. Unsupported blocks/images fail explicitly with source context, never silently disappear.
- No production DB was inspected or modified during implementation. Current-corpus completeness/performance and final reader-app review remain the team's responsibility.

## Architecture and technology decision

Use the existing Java 21 / Spring Boot backend rather than introduce a second runtime or microservice. EPUB is a standards-based ZIP/XML/CSS format; Java's built-in ZIP implementation and typed immutable records are sufficient for this scope. Rendering is isolated so a later PDF library can be chosen independently.

```text
quiz-admin /ebooks
  -> existing cookie-authenticated Next.js API proxy
  -> GET /api/admin/ebooks/catalog
  -> POST /api/admin/ebooks/epub
       -> per-process generation semaphore
       -> repeatable-read published content snapshot
       -> validate JSON blocks and create immutable BookModel
       -> BookComposer (ordering, placement, stable anchors)
       -> EpubRenderer (XHTML, navigation, OPF, OCF ZIP)
       -> binary EPUB download
```

The server reads the database once for the selected scope using a fetch graph; it does not make HTTP requests to its own API. The admin calls the authenticated generation API. No new bulk answer endpoint was added to the public mobile namespace.

### Backend files

All paths below are relative to `yusin-quiz`.

| Path | Responsibility |
| --- | --- |
| `src/main/java/com/cpa/yusin/quiz/ebook/controller/EbookDto.java` | Validated generation request and catalog DTO. |
| `.../ebook/controller/AdminEbookController.java` | Admin-only routes, binary headers, 400/429 errors. |
| `.../ebook/infrastructure/EbookRepository.java` | Published catalog aggregation, scoped count, fetch graph. |
| `.../ebook/service/EbookSnapshotService.java` | Consistent source snapshot, scope/size/content checks, detached records. |
| `.../ebook/service/BlockNormalizer.java` | Typed text, spans, lists, legacy text/listing, statement groups; rejects unsupported input. |
| `.../ebook/service/EbookGenerationService.java` | Bounded concurrent orchestration; always releases its semaphore. |
| `.../ebook/model/BookModel.java` | Immutable format-neutral content and provenance. |
| `.../ebook/model/BookSettings.java` | Independent placement and ordering enums. |
| `.../ebook/model/BookComposer.java` | Pure ordering and logical chapter/entry composition. |
| `.../ebook/render/BookRenderer.java` | Future PDF adapter boundary. |
| `.../ebook/render/EpubRenderer.java` | Offline reflowable EPUB 3 package and escaped markup. |
| `src/main/resources/application-ebook-local.yml` | Optional local-only MySQL/JWT environment profile. |
| `docs/frontend-api/admin-ebook.md` | Detailed API contract and operational limits. |
| `src/main/resources/static/asciidoc/admin-ebook.adoc` | Generated REST Docs entry point. |
| `src/test/java/com/cpa/yusin/quiz/ebook/EpubRendererTest.java` | Placement, content, links, EPUBCheck, human-review sample. |
| `src/test/java/com/cpa/yusin/quiz/content/ContentApiIntegrationTest.java` | Existing suite extended with authenticated ebook catalog/export checks. |

Important repository invariants retained: use Spring transactions, ClockHolder and qualified `systemUuidHolder`, explicit DTOs, canonical JSON blocks, no server-rendered MVC or JPA schema additions. There are two UuidHolder beans; retain the explicit qualifier. Historical docs sometimes say one answer/five choices; current ebook normalization supports 2–5 choices and all correct flags, matching current service intent. Do not “fix” unrelated historical inconsistencies as a side effect.

### Admin files

All paths below are relative to `quiz-admin`.

- `src/app/(dashboard)/ebooks/page.tsx`: form using incumbent components and tokens.
- `src/features/ebook/useEbookPageModel.ts`: scope, loading, latest-only requests, duplicate-submit guard, timeout, object-URL cleanup, download retry.
- `src/services/ebook.ts`: endpoints and validated blob transport.
- `src/types/ebook.ts`: typed UI/API contract.
- `src/lib/http/client.ts` and `types.ts`: `blob` response mode; errors retain shared JSON/auth handling.
- `src/components/layout/adminNavigation.ts` and `src/proxy.ts`: navigation and protected route inclusion. The existing server dashboard layout also protects access.
- Hook/service tests and the proxy's binary response test exercise failure/race/download behavior.

## Data correctness and EPUB design

The snapshot count and joined query execute in one repeatable-read transaction. Rendering receives only immutable records and runs after transaction completion. Every selected year/subject must have a matching eligible question in combined scope; reject stale/foreign selections rather than treating them as “all.” No pagination means no partial-page success case.

Source identities, not human-readable question numbers, produce stable element IDs. Composition emits each question, answer, and nonempty explanation exactly once. Multiple correct options are not reduced to one. Blank explanations are omitted, while content without a required answer/body is an error.

Text is XML-escaped; illegal XML control characters fail instead of being deleted. Allowed semantic styles are preserved. Source heading tags are normalized to subordinate headings inside question content. A future format adapter must consume typed blocks rather than parse EPUB XHTML. Nested content is bounded at 24 levels.

The EPUB uses a stored first `mimetype` entry, OCF container, OPF metadata/manifest/spine, navigation, separate year/solution XHTML documents, relative TOC links, a registered SVG cover image, and local CSS. No scripts, remote images/fonts, live answer checks, or fixed page-size assumptions. Korean language metadata and source line breaks are retained; reader font-size controls remain usable.

`META-INF/generation.json` stores schema version, capture time, publication identifier, source question IDs, scope, and settings. It contains no credentials. The EPUB already contains rendered content; this JSON is provenance, not a raw snapshot backup/re-import system.

## Local operation

Full Korean guide: [USER_GUIDE.ko.md](USER_GUIDE.ko.md).

`ebook-local` is opt-in; existing deployment profiles stay intact. It binds `127.0.0.1`, takes EBOOK_DB_URL/USERNAME/PASSWORD and EBOOK_JWT_SECRET, and defaults to JPA validation. Explicit EBOOK_DDL_AUTO=update is documented only for an empty dedicated local DB. Existing ADMIN_BOOTSTRAP_LOGIN_ID/PASSWORD creates a first admin only if none exists. Dummy S3 configuration initializes existing beans but does not support image uploads; ebook rendering makes no S3 calls.

Frontend uses the existing SOURCE_API_URL proxy; defaults to localhost:8080. The guide uses webpack for local execution because the sandbox's Turbopack CSS subprocess could not bind a port. No package script was globally changed.

## Verification record

Implementation environment: macOS, Amazon Corretto 21.0.8, Node 22.21.0.

- Backend `./gradlew test asciidoctor openapi3`: 66 tests passed (0 failures/errors); AsciiDoc and OpenAPI include both ebook endpoints. Passed after resolving EPUBCheck's transitive `slf4j-nop` conflict (excluded) and selecting the correct UuidHolder bean. Existing tests remained intact.
- EPUBCheck 5.4.0 validated all nine answer/explanation placement combinations with no validation errors/warnings (`doValidate() == 0`). Version pinned in test dependencies; it is not bundled in the production runtime.
- Link checks verify target documents and IDs; sample includes Korean text, XML-reserved characters, statement groups, modern/legacy lists, repeated question numbers across subjects, multiple answers, and a question with no explanations.
- `build/ebook-samples/sample-review.epub` is generated by the renderer test. It contains explicitly fictional test content, not user quiz data.
- Admin `npm run test`: 98 tests passed in the final verification run. `npm run lint`: passed. `npm run build -- --webpack`: passed.
- Default `npm run build` encountered a local Turbopack process/port sandbox error; webpack build succeeded. Do not claim that the default build was verified in this environment.
- Local Chrome browser check with a synthetic API catalog: desktop (1440px) and mobile (390px) screenshots inspected; no horizontal overflow or page errors; real EPUB blob downloaded via the admin/proxy path. Backend authentication/data retrieval was verified separately by integration tests. This is not a production end-to-end data run.
- The optional ebook-local profile was boot-tested against an isolated H2 database with no production credentials or S3 requests. Real MySQL connectivity still depends on the user's local settings.
- The actual browser-downloaded EPUB also passed the validateEpub task with zero fatal errors/errors/warnings/information messages.
- Extracted EPUB XHTML was inspected offline in Chrome at normal and enlarged (24px / 390px viewport) text sizes, with no horizontal overflow. This is supplementary visual evidence, not an EPUB reader compatibility guarantee.
- Independent UI finishing review returned `ship` with no material fixes for the existing-admin extension.
- Windows instructions are supplied but not executed on Windows. Reader-specific Books/calibre visual QA and full production-corpus load testing are not claimed.

Commands:

```sh
# Backend (JDK 21)
./gradlew test asciidoctor openapi3
./gradlew validateEpub -PepubFile=/absolute/path/book.epub
# Admin
npm run lint
npm run test
npm run build -- --webpack
```

Existing integration tests rely on the local ignored `src/test/resources/application-test.properties`; do not substitute production settings for this file. Test logs and generated reports are under build/, not source control.

## Next work, only when requested

1. Team checks a real selected qualification on their actual EPUB readers and reports content/layout issues.
2. Choose PDF page size, print vs tablet use, font licensing/embedding, pagination rules, and engine. Implement another BookRenderer consuming the existing Composition; add PDF-specific pagination/visual validation.
3. If measured corpus/deployment limits require it, replace synchronous orchestration with durable jobs and bounded artifact retention; keep source and rendering layers unchanged.
4. If images become required, design offline asset acquisition and supported image semantics explicitly rather than skipping them.

Do not add storefront/DRM integration, automatic editorial rewriting, AI-generated explanations, permanent generation history, or public answer export without a new requirement.

## Standards and reader references

- [W3C EPUB 3.3](https://www.w3.org/TR/epub-33/)
- [W3C EPUBCheck](https://www.w3.org/publishing/epubcheck/)
- [EPUBCheck 5.4.0 release](https://github.com/w3c/epubcheck/releases/tag/v5.4.0)
- [Apple Books import guide](https://support.apple.com/en-ae/guide/books/ibkseed72068/mac)
- [calibre viewer](https://manual.calibre-ebook.com/viewer.html)

## Reading design revision — 2026-09-19

The user requested a Kyobo-like, quiet ebook reading experience and removal of redundant buttons. This is a refinement of the EPUB artifact, not a new reader application. Never imply that EPUB can install a custom toolbar into Apple Books or reproduce Kyobo DRM/storefront behavior.

- `src/main/resources/ebook/reading.css` owns book typography, grouping, rules, spacing, and reader-safe wrapping. Body foreground/background stay reader-controlled; no fixed page heights or CSS columns. Font sizes are relative. No fonts are bundled.
- `EpubCover.java` generates an original cream/forest-green typographic SVG, registered as `cover-image` in OPF. Only supplied title, selected years, and question count are printed. No Kyobo logo or invented author/publisher. Cover typography is fixed artwork; the body remains reflowable.
- Consecutive entries for one source question share a visual group. Inline answers do not repeat their number/source. Standalone answer/explanation sections retain source identity, including repeated question numbers.
- All per-question forward/back/answer-check links are removed at the user's request. The EPUB TOC remains functional. Do not reintroduce `문제로 돌아가기` or decorative controls.
- `rendition:spread=auto` permits reader-managed layout, not forced facing pages. On Mac, Books window width and column preferences determine one/two-page view; its own top toolbar controls type size, TOC, and search/page navigation. Pagination changes with reader settings.
- Apple Books native visual testing was attempted but computer-use permissions were pending. Offline browser rendering supplements EPUBCheck; it is not a native reader certification.
- Existing downloaded/imported books do not update automatically. Regenerate with the updated backend and open the new EPUB; use a distinct title while reviewing to distinguish copies.

Revision verification: 67 backend tests passed (0 failures/errors), including all nine placement combinations validated by EPUBCheck and long/escaped cover-title cases. Offline light/dark/390px enlarged XHTML renders had no horizontal overflow; scoped independent review returned ship. No API or admin UI contract changed.
