# Question and Answer

## Purpose

- Define user question/answer behavior and admin moderation behavior.
- Keep ownership, answer-count, and admin-answer rules explicit.

## Read This When

- The task changes question CRUD.
- The task changes answer CRUD.
- The task changes admin moderation behavior.
- The task changes answer counts, `answeredByAdmin`, or pending-question semantics.

## Invariants

- The main moderation model is:
  - `Member -> Question -> Answer`
- Question ownership and answer ownership allow:
  - the original member
  - or an admin
- `Question` tracks both `answerCount` and `answeredByAdmin`.
- `Question` uses soft delete.
- `Answer` is deleted directly, not soft-deleted.

## Current Implementation

- User question endpoints live under `/api/v1`:
  - create question for problem
  - update question
  - get question
  - list questions by problem
  - delete question
- User answer endpoints live under `/api/v1`:
  - create answer for question
  - list answers for question
  - delete answer
- Admin moderation endpoints live under:
  - `/api/admin/question`
  - `/api/admin/question/{questionId}/answer`
  - `/api/admin/answer/{answerId}`
- Admin answer request DTOs live in `answer.controller.dto.request`.
- Creating a normal answer increments `answerCount`.
- Creating an admin answer also marks the question as answered by admin.
- Deleting an answer decrements `answerCount`.
- Deleting the last admin answer resets `answeredByAdmin` to `false`.
- User-side question and answer reads validate the parent published exam path.
- Admin-side reads can bypass user visibility rules for moderation use cases.

## Decision Rules

- If the task changes `answeredByAdmin`, inspect create-answer and delete-answer paths together.
- If the task changes answer counts, inspect both user and admin answer creation paths.
- If the task changes visibility rules, check whether the change should apply only to user reads or also to admin moderation flows.
- Do not mix bookmark behavior into this doc. Bookmark behavior belongs to `exam-content.md`.

## Change Checklist

- Update relevant question and answer tests.
- Update frontend docs if moderation or list-response contracts changed.
- Update this file if ownership, `answerCount`, or `answeredByAdmin` behavior changed.

## Verification

- Run `QuestionTest`.
- Run `AdminQuestionTest`.
- Run `AnswerTest`.
- Run `AnswerServiceTest`.
- Run controller tests for admin answer flows if endpoint behavior changed.

## Related Docs

- `dashboard.md`
- `exam-content.md`
- `api-contracts.md`
- `testing-and-docs.md`
