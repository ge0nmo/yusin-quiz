# Architecture

## Purpose

- Provide shared structural truth for the repo.
- Give agents one place to learn system shape before reading feature docs.

## Read This When

- The task is unfamiliar.
- The task touches multiple domains.
- The task changes shared infrastructure, package layout, or application-wide behavior.

## Invariants

- The root package is `com.cpa.yusin.quiz`.
- The backend runtime is API-only.
- No active legacy MVC controllers or Thymeleaf runtime behavior should exist.
- An empty `src/main/java/com/cpa/yusin/quiz/web/` directory may exist, but it must remain functionally unused.
- Every JPA entity must extend `BaseEntity`.
- Shared application time should come from `ClockHolder`.
- Shared application-generated UUID values should come from `UuidHolder`.
- Prefer explicit response types over wildcard `ResponseEntity<?>`.

## Current Implementation

- Major top-level packages:
  - `answer`, `bookmark`, `choice`, `dashboard`, `exam`, `file`, `member`, `problem`, `question`, `study`, `subject`: business capabilities
  - `common`: shared controller DTOs and infrastructure abstractions
  - `config`: security, async executor, AOP, and infrastructure wiring
  - `global`: JWT, filters, exception handling, logging, security helpers, and utilities
- Typical flow is:
  - controller
  - `controller.port`
  - service implementation
  - `service.port`
  - repository implementation
  - JPA repository
- `YusinQuizApplication` enables JPA auditing and async execution.
- `AppConfig` registers the `taskExecutor` and propagates MDC into async work.
- `BaseEntity` provides `createdAt` and `updatedAt`.
- `ExceptionAdvice` is the shared exception translation layer for REST responses.
- `TraceIdFilter` manages request trace IDs.
- `PerformanceLoggingAspect` logs controller and service execution time.

## Decision Rules

- If the task changes endpoints, DTOs, or HTTP behavior, read `api-contracts.md`.
- If the task changes login, token handling, access rules, or CORS, read `security-auth.md`.
- If the task changes quiz content, read `exam-content.md`.
- If the task changes study execution or scoring, read `study-runtime.md`.
- If the task changes question or answer behavior, read `question-answer.md`.
- If the task changes dashboard metrics or dashboard response semantics, read `dashboard.md`.
- Do not assume package names alone define change boundaries. Some critical rules cross package boundaries.

## Change Checklist

- Preserve API-only runtime behavior.
- Preserve `BaseEntity` inheritance on all entities.
- Keep time-dependent logic on `ClockHolder`.
- Keep app-generated UUID logic on `UuidHolder`.
- Check whether architecture tests need to stay green after the change.

## Verification

- Run `ApiOnlyArchitectureTest` for API-only invariants.
- Run `BaseEntityArchitectureTest` for entity inheritance invariants.
- Run relevant feature tests after shared-structure changes.

## Related Docs

- `api-contracts.md`
- `security-auth.md`
- `testing-and-docs.md`
