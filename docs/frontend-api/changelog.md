# Frontend API Changelog

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
