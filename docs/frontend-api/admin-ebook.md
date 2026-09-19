# Admin ebook API

All endpoints require the existing ADMIN session. Public mobile catalogs and solution endpoints are unchanged. Only content published at every level (qualification, exam, subject mapping, subject, problem) is eligible.

## GET /api/admin/ebooks/catalog

Returns `GlobalResponse.data`, a list of available combinations:

```json
{
  "data": [
    {
      "qualificationCode": "APPRAISER",
      "qualificationName": "감정평가사",
      "subjectId": 1,
      "subjectName": "민법",
      "year": 2025,
      "questionCount": 40
    }
  ]
}
```

Counts are advisory: generation reads a fresh, consistent snapshot. No answers or question text appear in this selection catalog.

## POST /api/admin/ebooks/epub

```json
{
  "qualificationCode": "APPRAISER",
  "subjectIds": [1],
  "years": [2025, 2024],
  "title": "감정평가사 기출 복습",
  "yearOrder": "NEWEST_FIRST",
  "answerPlacement": "AFTER_QUESTION",
  "explanationPlacement": "YEAR_END"
}
```

All fields are required. Lists must be nonempty and contain at most 200 entries. Subject IDs must be positive; years must be 1900–3000. Title is 1–160 characters, excluding an all-whitespace title. Qualifications use the existing enum. Each selected subject/year must have at least one eligible question within the combined scope; a completely empty or stale scope fails rather than returning an incomplete-looking book.

- `yearOrder`: `NEWEST_FIRST` or `OLDEST_FIRST`.
- Both placement fields independently accept `AFTER_QUESTION`, `YEAR_END`, or `BOOK_END`.
- Same-year questions sort by original number ascending, then exam ID, subject display order, subject ID, and problem ID. Original numbers are not rewritten; source metadata disambiguates collisions.
- Missing explanations yield an answer without an empty explanation section. Multiple correct choices are preserved.
- The generation captures selected content in a repeatable-read transaction with fetch joins, then renders detached records outside the transaction.

Success is **binary**, not `GlobalResponse`:

```http
HTTP/1.1 200 OK
Content-Type: application/epub+zip
Content-Disposition: attachment; filename="appraiser.epub"
Cache-Control: no-store
```

Download the response as bytes/blob. Errors remain ordinary JSON errors:

| Status | Code | Meaning |
| --- | --- | --- |
| 400 | EBOOK_INVALID_CONTENT | Empty/stale scope, missing answer/text, invalid blocks, unsupported image, or content size limit. Message includes source problem context where available. |
| 400 | Existing validation response | Invalid/missing request fields. |
| 401 / 403 | Existing auth contract | Authentication/ADMIN authorization required. |
| 429 | EBOOK_BUSY | One generation is already running on this server; `Retry-After: 10`. |

Limits: 5,000 questions, 20,000,000 serialized source-content bytes, and one concurrent generation per backend process. A large single database row can still be loaded before the content-size check; these are operational guards, not a guarantee of constant memory. No new database tables, background queue, history, or permanent artifact storage are introduced. The admin browser stops waiting after 120 seconds; a disconnected browser does not guarantee cancellation of work already running on the server.

The EPUB includes internal `META-INF/generation.json` provenance (schema version, capture time, identifier, selected scope, source problem IDs, placement settings). It contains no credentials. It is not a separately restorable database backup.

## Verification and documentation

`ContentApiIntegrationTest` documents catalog and binary export, verifies authentication, draft exclusion, and invalid selections. `EpubRendererTest` checks all nine placement combinations using EPUBCheck 5.4.0, content preservation, ZIP structure, and internal links. Contract artifacts are generated with `./gradlew test asciidoctor openapi3`.

EPUBCheck is a test/CLI dependency only. Generation does not run the full validator on each request; use the `validateEpub` Gradle task for a downloaded production book.
