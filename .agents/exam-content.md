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

Initial public problem responses contain content and choice text only. Answer flags and all explanations are available only from check/solutions endpoints as specified in the public contract.

## Images

JSON blocks keep current rich editor/image capabilities. `/api/admin/file` keeps the S3 upload/presigned preview workflow. Managed image block URLs are re-signed on public/admin reads, including nested list blocks. Legacy HTML fields and converters are forbidden.
