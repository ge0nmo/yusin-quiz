# API Contracts

## Purpose

- Define the cross-cutting HTTP namespace map.
- Define how API contract changes must be maintained.

## Read This When

- The task changes a controller.
- The task changes a request or response DTO.
- The task changes pagination, response wrapping, or nullability.
- The task affects frontend consumers.

## Invariants

- The API surface is split into four main namespace families:
  - `/api/v1/**`
  - `/api/admin/**`
  - `/api/v2/problem`
  - `/api/v2/admin/problem`
- Generated API artifacts come from tests, not from manual documents.
- Integration tests are the final executable truth for external contract behavior.
- Thin frontend-facing docs in `docs/frontend-api/` are guides, not the final authority.

## Current Implementation

- Health endpoints live under `/api/v1`:
  - `/api/v1/hc`
  - `/api/v1/env`
- Most JSON responses use `GlobalResponse<T>`.
- `pageInfo` is nullable and is present only when needed.
- Some admin endpoints return raw JSON lists instead of `GlobalResponse<T>`.
- Shared validation and exception responses are translated by `ExceptionAdvice`.
- REST Docs HTML is generated into `build/docs/asciidoc/`.
- OpenAPI JSON is generated into `build/api-spec/openapi3.json`.

## Decision Rules

- Start with the relevant feature doc before changing a controller or DTO.
- Do not assume all admin endpoints are wrapped in `GlobalResponse<T>`.
- Do not put long payload catalogs in `.agents/`. Keep examples in `docs/frontend-api/` and generated docs.
- If the change is frontend-visible, check nullability and optional-field behavior explicitly.
- If the change touches auth failures or access rules, also read `security-auth.md`.

## Change Checklist

- Update relevant integration tests.
- Update relevant files in `docs/frontend-api/`.
- Regenerate REST Docs and OpenAPI artifacts.
- Update the relevant `.agents/*.md` file if the rule or contract changed.
- Update frontend type or mapping layers if the contract is consumed outside this repo.

## Verification

- Run `./gradlew test asciidoctor openapi3`.
- Review the generated outputs in:
  - `build/docs/asciidoc/`
  - `build/api-spec/openapi3.json`
- Re-read the relevant integration tests after changing a contract.

## Related Docs

- `architecture.md`
- `security-auth.md`
- `testing-and-docs.md`
- `docs/frontend-api/README.md`
