# Exam Content

## Model

- `QualificationExam`: `APPRAISER | CPA | CUSTOMS_BROKER | REAL_ESTATE_AGENT` enum code, enum-derived immutable display name, and editable status.
- `Subject`: global unique name and status.
- `QualificationExamSubject`: qualification/subject mapping with status and display order.
- `Exam`: belongs to QualificationExam and stores name, year and status.
- `Problem`: belongs to Exam and one mapping under the same qualification; stores number, status, content JSON blocks and optional explanation JSON blocks.
- `Choice`: exactly five entries numbered 1 through 5; exactly one answer; plain text content and optional explanation JSON blocks.
- Problem number uniqueness is `(exam, subject mapping, number)`.

## Publication

Public reads require PUBLISHED status on qualification, subject, mapping, exam and problem. Backend order is exam year descending then problem number ascending.

## Data exposure

v1 problem responses contain content and choice text only; answer flags and explanations come only from v1 check/solutions. v2 content includes correct choice IDs but no explanations. v2 explanations are a separate endpoint reserved for future entitlement checks.

## Blocks

- Blocks are stored and returned as submitted maps; `JsonBlockContentValidator` checks `statementGroup`, `table` and math spans anywhere in the tree.
- `table`: `{rows: [{cells: [{content: Block[], header?, colspan?, rowspan?}]}], columnWidths?: int[]}`. `columnWidths` is one 1..10000 px width per column, used as proportions; the server does not check it against the column count. Cells hidden by a merge are omitted (HTML table rule), so row lengths may differ; empty cells are allowed. Errors: `INVALID_TABLE`.
- Inline math: text span `{text: "<LaTeX>", math: true}`; LaTeX must be non-blank. Errors: `INVALID_MATH`. Plain-text choice content wraps math in `$...$` and is not parsed by the server.

## Images

JSON blocks keep current rich editor/image capabilities. `/api/admin/file` keeps the S3 upload/presigned preview workflow. Managed image block URLs are re-signed on public/admin reads, including nested list, statement group and table cell blocks. Legacy HTML fields and converters are forbidden.
