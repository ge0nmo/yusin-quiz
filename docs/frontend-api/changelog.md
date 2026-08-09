# Frontend API Changelog

## 2026-08-09

- 인증된 사용자의 즉시 탈퇴 API `DELETE /api/v1/members/me`를 추가했다. 성공은 body 없는 `204 No Content`다.
- 탈퇴 시 개인 학습 데이터는 삭제하고 공개 질문·답변은 작성자를 `탈퇴한 사용자`로 익명화해 유지한다.
- 탈퇴 이메일은 재가입에 다시 사용할 수 있으며 탈퇴 전 access/refresh token은 새 계정에 사용할 수 없다.
- 신규 토큰에 회원 ID 바인딩을 추가하고, 회원 말문제 쓰기·기존 회원 소셜 로그인·refresh 발급을 탈퇴와 회원 행 잠금으로 직렬화했다.
- Google 로그인에서 잘못되거나 만료된 외부 토큰은 `401 INVALID_SOCIAL_TOKEN`, 잘못된 프로필은 `400 INVALID_SOCIAL_PROFILE` 로 반환한다.
- 공통 오류 응답에 항상 `code` 를 포함하고 예상하지 못한 500 응답은 내부 예외 메시지를 노출하지 않는다.
- 사용자 질문/답변 페이지 번호를 0 기반으로 통일하고, 답변 정렬을 `createdAt DESC` 로 고정했다.
- 답변 응답에 `isAdmin` 을 추가하고 본인 답변 수정 API `PATCH /api/v1/answer/{answerId}` 를 추가했다.
- 질문/답변 삭제는 body 없는 `204 No Content` 로 통일했다.
- 사용자 시험 종류 목록은 페이지네이션 없이 전체 공개 목록을 반환한다.
- 말문제 빠른 풀이 API를 추가했다. 계약과 guest token 처리 규칙은 `word-practice.md`를 기준으로 한다.
- 새 경로는 `/api/v2/problem/word-practice/**`이며, 기존 `GET /api/v2/problem?examId=...` 및 기존 `StudySession` 계약은 변경하지 않았다.

## 2026-03-21

- `POST /api/v1/bookmarks/status` 추가. 로그인 사용자가 현재 화면의 problemIds 중 북마크된 ID만 가볍게 조회할 수 있음
- 보호된 `/api/v1/**` 사용자 API의 비인증 응답을 `401 SecurityErrorResponse`로 정렬. 공개 GET 과 `/api/v1/auth/**` 는 그대로 유지

## 2026-03-14

- `GET /api/admin/question` 에 `datePreset=ALL|TODAY` 추가
- `GET /api/v2/admin/problem/search` 추가
- 관리자 대시보드 카드 클릭 진입용 포인터 문서 `docs/frontend-api/dashboard-drilldown.md` 추가
- `todayQuestionCount` 와 질문 목록의 `datePreset=TODAY` 가 같은 서버 날짜 경계를 사용하도록 정렬
- `problemsWithoutLectureCount` 와 문제 검색의 `lectureStatus=WITHOUT_LECTURE` 가 같은 active hierarchy 기준을 사용하도록 정렬

## 2026-03-10

- `GET /api/v1/problem/{problemId}`, `GET /api/v1/problem`, `GET /api/v2/problem`, `GET /api/v2/admin/problem`, `GET /api/v2/admin/problem/{problemId}` 응답에 `lecture` 객체 추가
- `GET /api/v1/bookmarks/problems` 응답의 각 문제에 `lecture` 객체 추가
- `POST /api/v2/admin/problem` 요청에 `lecture.youtubeUrl`, `lecture.startTimeSecond` 추가
- 문제 응답에 `lecture.playbackUrl` 추가. 프론트는 유튜브 재생 링크를 직접 조합하지 않아야 함
- API 계약 문서의 소스 오브 트루스를 integration test + REST Docs + OpenAPI 3 산출물로 표준화
