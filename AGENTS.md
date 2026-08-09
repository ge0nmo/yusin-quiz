# AGENTS.md

## Project snapshot

- Java 21 / Spring Boot 3.3 API-only quiz backend.
- Root package: `com.cpa.yusin.quiz`.
- Mobile is public/stateless; only admin authentication remains.

## Hard invariants

- No server-rendered MVC or Thymeleaf runtime.
- Every JPA entity extends `BaseEntity`.
- Use Spring `@Transactional`.
- Use `ClockHolder` and `UuidHolder` for application time/UUID values.
- Public APIs live under `/api/v1/qualification-exams/{code}/**`.
- Admin APIs live under `/api/admin/**`.
- JSON blocks are the single problem/explanation content model; do not add HTML compatibility fields.
- External contract changes update tests and docs together.

## Task router

- `.agents/architecture.md`
- `.agents/api-contracts.md`
- `.agents/security-auth.md`
- `.agents/exam-content.md`
- `.agents/testing-and-docs.md`

## Verification

- `./gradlew test`
- `./gradlew test asciidoctor openapi3`
