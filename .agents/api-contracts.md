# API Contracts

## Namespaces

- Public mobile content API: `/api/v2/qualification-exams/{code}/**`
- Public app report API (anonymous, `X-Device-Id` header): `/api/v2/qualification-exams/{code}/reports/**`
- App version policy (installed-app force-update gate, stays on v1): `/api/v1/qualification-exams/{code}/app-version-policy`
- Admin API: `/api/admin/**`
- Health API: `/api/v1/hc`, `/api/v1/env`

JSON success responses use `GlobalResponse<T>` and expose payloads under `data`. The admin file endpoint intentionally returns a URL string.

## Public contract

- `GET /api/v1/qualification-exams/{code}/app-version-policy?platform=android|ios`

The v1 content endpoints (subjects, problems, check, solutions) were removed on 2026-11-04 after the v1-only app was withdrawn; the app syncs through v2 only.

## Public contract v2

- `GET /api/v2/qualification-exams/{code}/content`
- `GET /api/v2/qualification-exams/{code}/explanations`

Content is the full published snapshot with `correctChoiceIds` and never contains explanations. Each choice carries `imageUrl` (`null` when absent). Explanations are a separate snapshot and are the endpoint for future entitlement enforcement; they are public until in-app purchase lands. Each endpoint has its own SHA-256 `version`, sent as `ETag`; a matching `If-None-Match` returns `304` without a body. Signed image URLs (image blocks and choice `imageUrl`) are excluded from the hash in favour of their object keys, and `imageUrlsExpireAt` tells the app when to refetch.

## Reports

- App: `POST|GET /api/v2/qualification-exams/{code}/reports`, `GET .../reports/{id}`. No login; `X-Device-Id` (UUID, stored lowercase) is required and checked before body validation (`400 INVALID_DEVICE_ID`). Duplicate open report for the same device + problem + category is `409 REPORT_DUPLICATED`; more than 20 reports per device in a rolling 24 hours is `429 REPORT_RATE_LIMITED`. The app only sees `answer`/`answeredAt` while the status is closed.
- Admin: `GET /api/admin/reports` (paged, `pageInfo` with 1-based `currentPage`, `SAME_PROBLEM` sort ordered in the query before paging), `GET /api/admin/reports/summary`, `GET|PUT /api/admin/reports/{id}`. Details: `docs/frontend-api/app-report.md`, `docs/frontend-api/admin-report.md`.
- Report date-times are `LocalDateTime` truncated to seconds.

Admin auth endpoints are `POST /api/admin/login`, `/api/admin/refresh`, and `/api/admin/logout`. Admin content CRUD paths are documented in `docs/frontend-api/admin-content.md`.

## Maintenance

External changes require integration tests, `docs/frontend-api`, REST Docs, and OpenAPI regeneration together.
