# Frontend API Changelog

## 2026-08-09 — clean rebuild

- 자격시험, 전역 과목, 자격시험-과목 연결, 시험 회차, 문제, 보기 계층으로 전면 재구축했다.
- 모바일 API를 자격시험 코드로 스코프된 공개·무상태 API로 교체했다.
- 최초 문제 응답에서 정답과 해설을 제거하고 채점/해설 API를 분리했다.
- V1 HTML 문제 모델을 삭제하고 JSON block 모델 하나만 유지했다.
- 사용자 인증, Google 로그인, 북마크, 질문/답변, 학습 세션/로그, 말문제 API를 삭제했다.
- Member는 향후 확장을 위해 남기되 현재는 loginId/passwordHash/role 기반 관리자 인증만 제공한다.
