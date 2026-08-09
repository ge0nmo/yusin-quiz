# API Contracts

## Namespaces

- Public mobile API: `/api/v1/qualification-exams/{code}/**`
- Admin API: `/api/admin/**`
- Health API: `/api/v1/hc`, `/api/v1/env`

JSON success responses use `GlobalResponse<T>` and expose payloads under `data`. The admin file endpoint intentionally returns a URL string.

## Public contract

- `GET /api/v1/qualification-exams/{code}/subjects`
- `GET /api/v1/qualification-exams/{code}/subjects/{subjectId}/problems`
- `POST /api/v1/qualification-exams/{code}/problems/{problemId}/check`
- `POST /api/v1/qualification-exams/{code}/solutions`

Problem catalog responses never contain answer flags or explanations. Check returns correctness only. Solutions accepts one to five distinct IDs and is separate for future entitlement enforcement.

Admin auth endpoints are `POST /api/admin/login`, `/api/admin/refresh`, and `/api/admin/logout`. Admin content CRUD paths are documented in `docs/frontend-api/admin-content.md`.

## Maintenance

External changes require integration tests, `docs/frontend-api`, REST Docs, and OpenAPI regeneration together.
