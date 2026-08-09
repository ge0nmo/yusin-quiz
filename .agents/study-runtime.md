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
  - `WordPractice`: 회원이 현재 5문제 묶음(마지막은 1~4개)을 원자 저장했을 때 실제 답안 수를 이벤트 한 건으로 기록한다
  - 동일 배치 재전송, 미완성 배치, 로컬 오답 복습, 비회원 답안은 증가하지 않는다
  - 완료 후 명시적으로 재시작한 새 말문제 round의 배치는 새로운 학습으로 다시 집계한다
- `WordPracticeCycle`은 기존 `StudySession`과 분리된 말문제 빠른 풀이 회차다. 문제 순서와 최초 답안은 회차에 고정하고, 최대 5개 답안을 원자 저장하며 동일 payload 재전송은 200으로 멱등 처리한다.
- 말문제는 완료 후 자동 재시작하지 않으며, 최신 완료 회차에 대한 명시적 restart만 다음 round를 만든다.
- 회원 탈퇴 시 해당 회원의 `SubmittedAnswer`, `StudySession`, `DailyStudyLog`와 회원형 말문제 참여자·회차·답안을 한 트랜잭션에서 삭제한다.
- 회원형 말문제 participant 생성 및 cycle/answer 쓰기는 member 행을 먼저 잠근다. 말문제 쓰기가 먼저면 탈퇴가 기다린 뒤 모두 삭제하고, 탈퇴가 먼저면 이후 쓰기는 회원 잠금 조회에서 실패한다.

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
