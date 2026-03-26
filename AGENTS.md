# AGENTS.md

## Project Snapshot

- `yusin-quiz` is a Java 21 / Spring Boot 3.3.x API server for a quiz platform.
- The root package is `com.cpa.yusin.quiz`.
- The admin consumer is a Next.js app that uses `/api/admin/**` and `/api/v2/admin/problem`.
- The repo also contains thin frontend-facing docs in `docs/frontend-api/` and generated API artifacts from REST Docs/OpenAPI.

## Hard Invariants

- The runtime is API-only. Do not add server-rendered MVC flows or Thymeleaf runtime behavior.
- An empty `src/main/java/com/cpa/yusin/quiz/web/` source directory may exist. The real invariant is: no active classes, no MVC controllers, and no Thymeleaf runtime there.
- Every JPA entity must extend `BaseEntity`.
- Use Spring `@Transactional`, not `jakarta.transaction.Transactional`.
- Use `ClockHolder` for application time and `UuidHolder` for application-generated UUID values.
- Determine the HTTP namespace before editing:
  - user API: `/api/v1/**`
  - admin API: `/api/admin/**`
  - user problem V2 API: `/api/v2/problem`
  - admin problem V2 API: `/api/v2/admin/problem`
- Determine the problem model before editing: V1 HTML fields vs V2 block JSON fields.
- External contract changes must update tests and docs together.

## Task Router

- Start here for unfamiliar or multi-domain work: `.agents/architecture.md`
- Read `.agents/api-contracts.md` for controller, DTO, pagination, or response-shape work
- Read `.agents/security-auth.md` for login, JWT, roles, permit-all rules, or CORS
- Read `.agents/exam-content.md` for subject, exam, problem, choice, bookmark, lecture, image, or file-upload work
- Read `.agents/study-runtime.md` for study sessions, submitted answers, scoring, idempotency, or study logs
- Read `.agents/question-answer.md` for question, answer, or admin moderation work
- Read `.agents/dashboard.md` for admin dashboard metrics, pending questions, or dashboard drilldown behavior
- Read `.agents/testing-and-docs.md` for test strategy, REST Docs, OpenAPI, and verification workflow

## Verification Commands

- `./gradlew test`
- `./gradlew test asciidoctor openapi3`
- Use JDK 21 for Gradle tasks.
