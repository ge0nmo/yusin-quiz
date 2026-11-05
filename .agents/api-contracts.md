# API Contracts

## Namespaces

- Public mobile content API: `/api/v2/qualification-exams/{code}/**`
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

Admin auth endpoints are `POST /api/admin/login`, `/api/admin/refresh`, and `/api/admin/logout`. Admin content CRUD paths are documented in `docs/frontend-api/admin-content.md`.

## Maintenance

External changes require integration tests, `docs/frontend-api`, REST Docs, and OpenAPI regeneration together.
