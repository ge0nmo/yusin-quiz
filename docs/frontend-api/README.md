# Frontend API Contract Guide

이 디렉터리는 프론트엔드 코딩 에이전트와 프론트 개발자가 백엔드 API 계약을 빠르게 이해하기 위한 얇은 가이드 레이어다.
최종 권위는 수동 문서가 아니라 integration test로 검증된 계약이다.

## 읽는 순서

1. `docs/frontend-api/*.md`
2. generated REST Docs / AsciiDoc
3. Controller 주석 + DTO
4. integration test
5. `quiz-admin` 타입 정의 및 매핑 레이어

## generated docs 위치

- REST Docs / AsciiDoc HTML: `build/docs/asciidoc/*.html`
- OpenAPI 3 JSON: `build/api-spec/openapi3.json`

## 생성 명령

```bash
./gradlew test asciidoctor openapi3
```

- 로컬 셸 기본 JDK가 21보다 높으면 Gradle 설정 단계가 깨질 수 있음
- 이 저장소에서는 Gradle 실행 JDK를 21로 맞추는 것을 기본 전제로 함

## 운영 규칙

- 프론트 영향이 있는 API 변경은 integration test 문서화와 함께 수정해야 함
- `docs/frontend-api` 문서는 generated docs를 읽기 전에 빠르게 판단하는 용도여야 함
- nullable / optional 정책은 문서에 명시해야 함
- 예제 JSON은 integration test payload와 응답 shape를 기준으로 관리해야 함
- 문서 원본은 backend repo 하나에만 두고, frontend repo에는 포인터 문서만 둬야 함

## 현재 1차 적용 범위

- `problem-v2.md`
- `bookmark.md`
- `dashboard.md`
- `dashboard-drilldown.md`
- `examples/problem-v2/*`
- `changelog.md`
