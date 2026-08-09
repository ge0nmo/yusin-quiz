# Frontend API Contract Guide

기존 사용자 인증, 북마크, 질문/답변, 학습 세션, 말문제 API는 모두 제거되었다.
현재 외부 계약은 아래 세 문서와 integration test를 기준으로 한다.

- `public-content.md`: 로그인 없는 모바일 콘텐츠 API
- `admin-content.md`: 관리자 콘텐츠 CRUD API
- `admin-auth.md`: 관리자 아이디 로그인

모든 JSON 성공 응답은 `{ "data": ... }` 형태다. 파일 업로드만 기존과 같이 URL 문자열을 반환한다.

생성 문서와 OpenAPI는 다음 명령으로 갱신한다.

```bash
./gradlew test asciidoctor openapi3
```

- REST Docs HTML: `build/docs/asciidoc/`
- OpenAPI JSON: `build/api-spec/openapi3.json`

이 릴리스는 데이터/스키마 하위호환이 없는 clean rebuild다. 기존 운영 DB를 재사용하지 말고 최초 배포 전에 빈 스키마로 초기화해야 한다.
