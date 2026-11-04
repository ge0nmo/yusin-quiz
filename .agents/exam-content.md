# Exam Content

## Model

- `QualificationExam`: `APPRAISER | CPA | CUSTOMS_BROKER | REAL_ESTATE_AGENT` enum code, enum-derived immutable display name, and editable status.
- `Subject`: global unique name and status.
- `QualificationExamSubject`: qualification/subject mapping with status and display order.
- `Exam`: belongs to QualificationExam and stores name, year and status.
- `Problem`: belongs to Exam and one mapping under the same qualification; stores number, status, content JSON blocks and optional explanation JSON blocks.
- `Choice`: two to five entries numbered 1..N with no gaps (request `@Size(min = 2, max = 5)`, service checks numbering); at least one answer, multiple answers allowed. Updates match choices by number: kept numbers update the same row (stable `id`), removed numbers are deleted, added numbers are inserted; plain text `content`, optional `imageUrl` (one image, http(s), at most 2048 chars, `INVALID_CHOICE_IMAGE` otherwise) and optional explanation JSON blocks. A choice needs non-blank `content` or an `imageUrl`; an image-only choice stores `content: ""`, and a choice with neither fails as `VALIDATION_ERROR` on `choices[i].content`.
- Problem number uniqueness is `(exam, subject mapping, number)`.

## Publication

Public reads require PUBLISHED status on qualification, subject, mapping, exam and problem. Backend order is exam year descending then problem number ascending.

## Data exposure

Public content is served by v2 only (the v1 content endpoints were removed). v2 content includes correct choice IDs and choice `imageUrl` but no explanations. v2 explanations are a separate endpoint reserved for future entitlement checks.

## Blocks

- Blocks are stored and returned as submitted maps; `JsonBlockContentValidator` checks `statementGroup`, `table` and math spans anywhere in the tree.
- `table`: `{rows: [{cells: [{content: Block[], header?, colspan?, rowspan?}]}], columnWidths?: int[], border?: "all"|"outer"|"none"}`. `columnWidths` is one 1..10000 px width per column, used as proportions; the server does not check it against the column count. `border` defaults to `all` (grid); `outer` draws only the outer frame and `none` no lines; any other value is rejected. Cells hidden by a merge are omitted (HTML table rule), so row lengths may differ; empty cells are allowed. Errors: `INVALID_TABLE`.
- Inline math: text span `{text: "<LaTeX>", math: true}`; LaTeX must be non-blank. Errors: `INVALID_MATH`. Plain-text choice content wraps math in `$...$` and is not parsed by the server.

## Images

JSON blocks keep current rich editor/image capabilities. `/api/admin/file` keeps the S3 upload/presigned preview workflow. Managed image block URLs are re-signed on public/admin reads, including nested list, statement group and table cell blocks. Choice `imageUrl` follows the same rule through `JsonBlockContentProcessor`: it is stored as submitted (like block `src`), managed S3 URLs are re-signed on admin and v2 reads, other http(s) URLs pass through, and the v2 content version hashes the object key. Legacy HTML fields and converters are forbidden.
