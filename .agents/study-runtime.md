# Study Runtime

## Purpose

- Define study-session execution, scoring, idempotency, and study-log behavior.
- Capture the correctness and concurrency rules that matter in production.

## Read This When

- The task changes study start, answer save, or finish behavior.
- The task changes scoring, progress restore, or session reuse.
- The task changes study logs or async event flow.
- The task changes locking or concurrency behavior.

## Invariants

- The main study entities are:
  - `StudySession`
  - `SubmittedAnswer`
  - `DailyStudyLog`
- `StudySession.examId` is a scalar field, not a JPA relation.
- One `SubmittedAnswer` row should exist per `(study_session_id, problem_id)`.
- One `DailyStudyLog` row should exist per `(member_id, date)`.
- Session completion summary must be derived on the server from persisted data.

## Current Implementation

- `startSession`:
  - locks the member row
  - verifies the exam is published
  - reuses an existing `IN_PROGRESS` session for the same member, exam, and mode
  - snapshots active problem count into `plannedProblemCount`
- `saveAnswer`:
  - loads the session with `PESSIMISTIC_WRITE`
  - updates `lastIndex`
  - validates problem and choice ownership
  - computes correctness from `Choice.isAnswer`
  - upserts `SubmittedAnswer`
  - publishes a solved event on first submit in `PRACTICE` mode
- `completeSession`:
  - loads the session with lock
  - builds summary from persisted answers
  - returns the same summary for already-completed sessions
  - marks the session complete only on the first completion path
  - publishes a solved event for `EXAM` mode using `answeredCount`
- Finish response fields:
  - `correctCount`
  - `totalCount`
  - `answeredCount`
  - `unansweredCount`
  - deprecated `finalScore`, kept as a compatibility alias for correct-count style behavior
- `StudyEventListener` handles `StudySolvedEvent` with `@TransactionalEventListener(AFTER_COMMIT)` and `@Async`.
- `StudyLogService.recordActivity` uses an upsert path keyed by member and date.
- Log accumulation semantics:
  - `PRACTICE`: first submit for a problem in a session increments activity
  - changing the same answer later does not increment again
  - `EXAM`: completion increments by `answeredCount`
  - repeated finish calls do not increment again

## Decision Rules

- If the task changes correctness or scoring, inspect both `saveAnswer` and `completeSession`.
- If the task changes logging, inspect the full path:
  - session service
  - event publication
  - async listener
  - log service
- Do not move correctness decisions to the client. Server-side correctness is authoritative.
- Do not assume the current active problem count is always the finish denominator. `plannedProblemCount` exists to stabilize the summary.

## Change Checklist

- Update study integration tests when session lifecycle behavior changes.
- Update docs if finish-response fields or session payloads change.
- Recheck idempotency and concurrency behavior after modifying persistence or locking logic.
- Update this file if session reuse, finish summary, or log rules change.

## Verification

- Run `StudyApiTest`.
- Run `StudyConcurrencyTest`.
- Run `StudySessionServiceTest`.
- Run `StudyLogServiceTest`.

## Related Docs

- `exam-content.md`
- `api-contracts.md`
- `testing-and-docs.md`
- `docs/frontend-api/study.md`
