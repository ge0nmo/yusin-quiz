# Testing and Docs

## Purpose

- Define the repo-wide verification strategy.
- Define how REST Docs and OpenAPI artifacts are produced and maintained.

## Read This When

- The task changes an external contract.
- The task adds a new entity or architectural rule.
- The task changes concurrency, persistence, or security behavior.
- The task is non-trivial and needs clear verification scope.

## Invariants

- Contract documentation is driven by tests.
- Integration tests are the executable source of truth for API behavior.
- Architecture rules are enforced by dedicated tests and should stay enforced by tests.
- Gradle tasks should run on JDK 21.

## Current Implementation

- Main test styles in this repo:
  - fake-container and unit-style tests using `TestContainer` and fake repositories
  - Spring Boot integration tests with `MockMvc`
  - concurrency integration tests for study behavior
  - architecture tests for API-only structure and `BaseEntity` inheritance
- REST Docs snippets are written under `build/generated-snippets`.
- AsciiDoc HTML is generated to `build/docs/asciidoc/`.
- OpenAPI JSON is generated to `build/api-spec/openapi3.json`.
- Thin frontend-consumer docs live in `docs/frontend-api/`.

## Decision Rules

- Prefer fake-container or narrow unit tests for isolated service logic.
- Prefer Spring integration tests for:
  - HTTP contracts
  - security behavior
  - persistence filtering
  - soft-delete propagation
- Prefer concurrency tests when the change touches:
  - locks
  - upserts
  - idempotency
  - async study logging
- If the task changes an external API contract, treat docs updates as part of the same change.

## Change Checklist

- For API contract changes:
  - update integration tests
  - update `docs/frontend-api/`
  - regenerate AsciiDoc and OpenAPI
  - update the relevant `.agents/*.md`
- For new entities:
  - inherit `BaseEntity`
  - keep architecture tests passing
- For security changes:
  - update security tests
- For concurrency-sensitive study changes:
  - rerun study concurrency coverage

## Verification

- Main commands:
  - `./gradlew test`
  - `./gradlew test asciidoctor openapi3`
- Key architecture tests:
  - `ApiOnlyArchitectureTest`
  - `BaseEntityArchitectureTest`
- Key security test:
  - `AdminApiSecurityIntegrationTest`
- Key study concurrency test:
  - `StudyConcurrencyTest`

## Related Docs

- `architecture.md`
- `api-contracts.md`
- `security-auth.md`
- `docs/frontend-api/README.md`
