# Exam Content

## Purpose

- Define the quiz content model and the high-risk rules around problem delivery.
- Keep V1/V2 problem behavior, lecture rules, bookmark shape, and file handling in one place.

## Read This When

- The task changes subject, exam, problem, or choice behavior.
- The task changes bookmark behavior.
- The task changes file upload, image handling, or lecture fields.
- The task changes admin problem search, save, or read behavior.

## Invariants

- The main content hierarchy is:
  - `Subject -> Exam -> Problem -> Choice`
- User-visible content must respect parent publication state.
- `Exam.subjectId` is a scalar field, not a JPA relation.
- Problem work must start by deciding whether the task is V1 HTML or V2 block JSON.
- Bookmark problem payloads must stay aligned with the V2 problem response shape.
- Lecture data is optional and can be absent.

## Current Implementation

- `Subject` has:
  - unique `name`
  - `status` with `DRAFT` and `PUBLISHED`
  - soft delete via `isRemoved`
- `Exam` has:
  - `name`
  - `year`
  - scalar `subjectId`
  - `status` with `DRAFT` and `PUBLISHED`
  - soft delete via `isRemoved`
- `Problem` stores both models:
  - V1: `content`, `explanation`
  - V2: `contentJson`, `explanationJson`
- V2 block types currently used in the repo are:
  - `text`
  - `image`
  - `list`
  - `listItem`
- `Choice` uses `(problem_id, number)` uniqueness.
- User content endpoints include:
  - `/api/v1/subject`
  - `/api/v1/exam`
  - `/api/v1/problem`
  - `/api/v2/problem`
  - `/api/v1/bookmarks`
- Admin content endpoints include:
  - `/api/admin/subject`
  - `/api/admin/exam`
  - `/api/admin/problem/{problemId}` for V1 delete only
  - `/api/v2/admin/problem` for V2 create or update
  - `/api/admin/file`
- Lecture behavior:
  - `lecture = null` clears existing lecture fields
  - `lecture.youtubeUrl` can exist without `lecture.startTimeSecond`
  - `YoutubeLectureUrlProcessor` normalizes URLs to canonical YouTube watch URLs
- Image and file pipeline:
  - V1 save path parses embedded base64 images and stores them in S3
  - V2 save path keeps block image URLs as provided
  - read paths convert stored S3 object locations into presigned URLs
  - `AdminFileController` stores the raw object and returns a presigned preview URL immediately
- `ProblemDTO`, `ProblemV2Response`, and bookmark problem payloads can all carry nullable lecture data.
- User `/api/v1/exam` responses include `questionCount`, and that count must stay aligned with active problem visibility rules.

## Decision Rules

- Decide V1 vs V2 before touching any problem logic.
- If the change touches lecture fields, check both problem and bookmark behavior.
- If the change touches image handling, check:
  - save path
  - read path
  - admin file upload path
- If the change touches user-visible problem payloads, review bookmark impact because bookmarks reuse the V2 problem shape.
- Do not assume deleting content always means hard delete. Subject, exam, and problem use soft-delete paths.

## Change Checklist

- Update relevant integration tests.
- Update relevant files in `docs/frontend-api/`:
  - `subject.md`
  - `exam.md`
  - `problem-v2.md`
  - `bookmark.md`
- Regenerate REST Docs and OpenAPI if the HTTP contract changed.
- Update this file if V1/V2 rules, lecture rules, or content visibility rules changed.

## Verification

- Run `ProblemTest`.
- Run `BookmarkTest`.
- Run relevant subject and exam integration tests.
- Run relevant problem and file service tests.

## Related Docs

- `api-contracts.md`
- `question-answer.md`
- `testing-and-docs.md`
- `docs/frontend-api/problem-v2.md`
- `docs/frontend-api/bookmark.md`
- `docs/frontend-api/exam.md`
- `docs/frontend-api/subject.md`
