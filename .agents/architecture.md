# Architecture

## Runtime

- Java 21 / Spring Boot 3.3 API-only server.
- Root package: `com.cpa.yusin.quiz`.
- Every JPA entity extends `BaseEntity`.
- Application time and generated UUIDs use `ClockHolder` and `UuidHolder` when needed.
- Controllers return explicit DTOs, normally wrapped in `GlobalResponse<T>`.

## Content model

```text
QualificationExam -> Exam
QualificationExam <-> QualificationExamSubject <-> global Subject
Exam + QualificationExamSubject -> Problem -> Choice
```

- A Problem's subject mapping must belong to the Exam's QualificationExam.
- JSON blocks are the only rich content representation.
- There is no active server-rendered MVC package.

## Active capabilities

- Public stateless content API
- Admin login and content CRUD
- Admin dashboard content counts
- Admin S3 image upload

End-user auth, bookmark, Q&A, study and word-practice runtimes were removed in the clean rebuild.
