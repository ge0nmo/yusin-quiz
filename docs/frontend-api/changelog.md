# Frontend API Changelog

## 2026-11-04 — 보기 2~5개

- 관리자 문제 생성·수정의 `choices`는 이제 2~5개다(이전에는 정확히 5개). 번호는 1..N, 정답은 하나 이상이다. 개수가 범위를 벗어나면 `400 VALIDATION_ERROR`.
- 수정으로 보기 개수를 바꿀 수 있다. 남는 번호의 보기 `id`는 그대로이고, 없어진 번호는 삭제, 늘어난 번호는 새로 만든다.
- v2 content/explanations의 `choices`는 문제마다 2~5개다. 앱은 보기 개수를 5로 가정하지 않아야 한다.

## 2026-11-04 — v1 콘텐츠 API 삭제

- `GET /api/v1/qualification-exams/{code}/subjects`, `GET .../subjects/{subjectId}/problems`, `POST .../problems/{problemId}/check`, `POST .../solutions`를 삭제했다. v1만 쓰던 예전 앱은 스토어에서 내렸고, 앱은 v2 content/explanations로만 동기화한다.
- `GET /api/v1/qualification-exams/{code}/app-version-policy`와 `GET /api/v1/hc`, `GET /api/v1/env`는 경로·응답 그대로 유지한다.
- v1 전용이던 오류 코드 `SUBJECT_MAPPING_NOT_FOUND`, `CHOICE_NOT_FOUND`를 제거했다.

## 2026-11-04 — 보기 이미지

- 관리자 문제 생성·수정 요청의 `choices[]`에 `imageUrl`(선택, http(s), 2048자 이하)을 추가했다. 잘못된 값은 `400 INVALID_CHOICE_IMAGE`.
- 보기는 `content`나 `imageUrl` 중 하나가 있으면 된다. 그림만 있는 보기는 `content: ""`이다. 둘 다 없으면 기존 빈 보기와 같은 `400 VALIDATION_ERROR`(`choices[i].content`).
- 관리자 문제 상세와 `GET /api/v2/qualification-exams/{code}/content`의 `choices[]`에 `imageUrl: string | null`을 추가했다. 관리 이미지는 응답마다 새로 서명하고, content `version`은 서명이 아닌 object key로 계산한다. 보기 이미지가 있으면 `imageUrlsExpireAt`도 채운다.
- 배포하면 모든 보기에 `imageUrl` 필드가 생기므로 content `version`이 한 번 바뀐다. v2 explanations 응답은 바뀌지 않았다.

## 2026-11-04 — 표 테두리

- `table` 블록에 선택 필드 `border`(`all` | `outer` | `none`)를 추가했다. 없으면 `all`(격자)이다. 다른 값은 `400 INVALID_TABLE`로 거부한다.
- `outer`는 바깥 테두리만, `none`은 선 없이 그린다. 이 두 값에서는 머리글 칸에 배경색을 칠하지 않고 굵게만 표시한다. 모르는 값은 `all`로 그린다.

## 2026-11-02 — 표 열 너비

- `table` 블록에 선택 필드 `columnWidths`(열별 px, 1~10000 정수 배열)를 추가했다. 화면에서는 비율로 쓰며 없으면 모든 열이 같은 너비다.
- 잘못된 값은 `400 INVALID_TABLE`로 거부한다. 이 필드를 모르는 앱은 지금처럼 같은 너비로 그린다.

## 2026-11-02 — 표 블록과 인라인 수식

- 문제 본문, 문제 해설, 보기별 해설 JSON block에 `{type:"table",rows:[{cells:[{content,header?,colspan?,rowspan?}]}]}`를 추가했다. 병합으로 가려진 칸은 `cells`에 넣지 않는다.
- text block span에 `math:true`를 추가했다. `text`에 LaTeX 원문을 두므로 `math`를 모르는 앱도 원문은 잃지 않는다.
- 보기 본문(plain text)의 수식은 `$...$`로 감싼다. 서버는 해석하지 않고 그대로 저장·반환한다.
- 잘못된 표는 `400 INVALID_TABLE`, 빈 수식이나 boolean이 아닌 `math`는 `400 INVALID_MATH`로 관리자 생성·수정 API에서 거부한다.
- API 경로와 최상위 요청·응답 구조는 바뀌지 않았다. 표와 병합·수식을 모르는 기존 앱은 표를 건너뛰거나 LaTeX 원문을 보여줄 수 있다.

## 2026-11-01 — v2 콘텐츠·해설 스냅샷

- `GET /api/v2/qualification-exams/{code}/content`를 추가했다. 공개 과목, 문제, 보기, 정답 ID를 한 응답으로 반환하고 해설은 넣지 않는다.
- `GET /api/v2/qualification-exams/{code}/explanations`를 추가했다. 공개 문제 전체의 문제·보기별 해설을 반환한다. 향후 이용권 확인을 붙일 엔드포인트이며 지금은 로그인 없이 호출한다.
- 두 응답은 각자 `version`을 `ETag`로 내려주고, `If-None-Match`가 같으면 `304`로 본문 없이 응답한다.
- 관리 이미지가 있는 응답은 `imageUrlsExpireAt`을 내려준다.
- v1 엔드포인트의 경로, 요청, 응답은 변경하지 않았다.

## 2026-08-14 — 앱 버전 정책

- `GET /api/v1/qualification-exams/{code}/app-version-policy?platform=android|ios`를 추가했다.
- 최신 버전, 최소 지원 버전, 플랫폼별 스토어 URL은 서버 환경변수로 관리한다.
- 미출시 iOS 기본 정책은 최소 버전 `0.0.0`으로 강제 업데이트를 비활성화한다.

## 2026-08-13 — 지문 묶음 블록

- 문제 JSON block 배열에 `{type:"statementGroup",items:[{label,content}]}` 구조를 추가했다.
- 라벨은 앞뒤 공백 제외 필수·최대 20자이며, 각 항목 내용에는 의미 있는 텍스트 또는 이미지가 필요하다.
- 빈 묶음이나 부분 입력 항목은 관리자 생성·수정 API에서 `400 INVALID_STATEMENT_GROUP`으로 거부한다.
- 기존 API 경로, 최상위 요청·응답 구조와 JSON 저장 방식은 변경하지 않았다.

## 2026-08-09 — clean rebuild

- 자격시험, 전역 과목, 자격시험-과목 연결, 시험 회차, 문제, 보기 계층으로 전면 재구축했다.
- 모바일 API를 자격시험 코드로 스코프된 공개·무상태 API로 교체했다.
- 최초 문제 응답에서 정답과 해설을 제거하고 채점/해설 API를 분리했다.
- V1 HTML 문제 모델을 삭제하고 JSON block 모델 하나만 유지했다.
- 사용자 인증, Google 로그인, 북마크, 질문/답변, 학습 세션/로그, 말문제 API를 삭제했다.
- Member는 향후 확장을 위해 남기되 현재는 loginId/passwordHash/role 기반 관리자 인증만 제공한다.
- 자격시험 `code`를 `APPRAISER | CPA | CUSTOMS_BROKER` enum으로 제한하고, 표시 이름을 서버 enum에서 결정하도록 변경했다.
