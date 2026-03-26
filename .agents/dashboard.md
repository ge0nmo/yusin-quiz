# Dashboard

## Purpose

- Define the admin dashboard read model.
- Keep its aggregate semantics separate from the underlying feature docs.

## Read This When

- The task changes `/api/admin/dashboard`.
- The task changes dashboard totals, operations, pending-question cards, or context behavior.
- The task changes lecture-coverage metrics or dashboard drilldown assumptions.

## Invariants

- Dashboard is an admin-only aggregate read model.
- The dashboard contract combines global totals, operational counters, pending questions, and optional context in one response.
- Stale selections are not errors. They resolve to `null` context entries.

## Current Implementation

- Endpoint:
  - `GET /api/admin/dashboard`
- Consumer:
  - Next.js admin dashboard
- Response shape:
  - `GlobalResponse<DashboardResponse>`
- Query parameters:
  - optional `subjectId`
  - optional `examId`
- Global sections are computed from active data, not from the current selection:
  - `totals`
  - `operations`
  - `pendingQuestions`
- Context section is selection-sensitive:
  - `context.subject`
  - `context.exam`
- Pending question behavior:
  - latest unanswered admin questions only
  - limit of 5
  - sorted by newest first
- `context.subject` becomes `null` when `subjectId` is missing or stale.
- `context.exam` becomes `null` when `examId` is missing, stale, or incompatible with the selected subject.
- `lectureCoverageRate`:
  - is numeric
  - returns `0.0` when active problem count is zero
  - otherwise returns a percentage rounded to one decimal place
- Soft-delete propagation matters for counts and coverage.

## Decision Rules

- Treat dashboard as its own contract, not as a casual projection of other APIs.
- If the task changes question or lecture behavior, check whether dashboard totals or metrics must also change.
- Keep stale-selection recovery behavior stable unless the API contract is intentionally changed.
- Do not move detailed question/answer rules into this doc. Link to `question-answer.md` instead.

## Change Checklist

- Update dashboard integration tests.
- Update `docs/frontend-api/dashboard.md`.
- Update `docs/frontend-api/dashboard-drilldown.md` if drilldown assumptions changed.
- Update this file if aggregate semantics or null-context rules changed.

## Verification

- Run `DashboardTest`.
- Run `./gradlew test asciidoctor openapi3` if the HTTP contract changed.

## Related Docs

- `question-answer.md`
- `exam-content.md`
- `api-contracts.md`
- `testing-and-docs.md`
- `docs/frontend-api/dashboard.md`
- `docs/frontend-api/dashboard-drilldown.md`
