# API Contracts

## Namespaces

- Public mobile API: `/api/v1/qualification-exams/{code}/**`, `/api/v2/qualification-exams/{code}/**`
- Admin API: `/api/admin/**`
- Health API: `/api/v1/hc`, `/api/v1/env`

JSON success responses use `GlobalResponse<T>` and expose payloads under `data`. The admin file endpoint intentionally returns a URL string.

## Public contract

- `GET /api/v1/qualification-exams/{code}/app-version-policy?platform=android|ios`
- `GET /api/v1/qualification-exams/{code}/subjects`
- `GET /api/v1/qualification-exams/{code}/subjects/{subjectId}/problems`
- `POST /api/v1/qualification-exams/{code}/problems/{problemId}/check`
- `POST /api/v1/qualification-exams/{code}/solutions`

v1 problem catalog responses never contain answer flags or explanations. Check returns correctness only. Solutions accepts one to five distinct IDs.

## Public contract v2

- `GET /api/v2/qualification-exams/{code}/content`
- `GET /api/v2/qualification-exams/{code}/explanations`

Content is the full published snapshot with `correctChoiceIds` and never contains explanations. Explanations are a separate snapshot and are the endpoint for future entitlement enforcement; they are public until in-app purchase lands. Each endpoint has its own SHA-256 `version`, sent as `ETag`; a matching `If-None-Match` returns `304` without a body. Signed image URLs are excluded from the hash, and `imageUrlsExpireAt` tells the app when to refetch.

v1 Solutions remains public as well. Gating explanations later requires gating v1 Solutions and requiring an app update through the version policy.

Admin auth endpoints are `POST /api/admin/login`, `/api/admin/refresh`, and `/api/admin/logout`. Admin content CRUD paths are documented in `docs/frontend-api/admin-content.md`.

## Maintenance

External changes require integration tests, `docs/frontend-api`, REST Docs, and OpenAPI regeneration together.
