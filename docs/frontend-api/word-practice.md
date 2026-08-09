# 말문제 빠른 풀이 API Contract

말문제 빠른 풀이는 시험 종류(`subject`)의 현재 공개 말문제를 고정 순서로 푸는 별도 학습 흐름이다. 기존 시험별 문제 조회와 `StudySession` 흐름을 대체하거나 공유하지 않는다. 최종 계약은 HTTP 통합 테스트에서 생성되는 REST Docs와 OpenAPI이며, 이 문서는 React Native 연동을 위한 요약이다.

## 화면 흐름

1. 시험 목록 화면에서 `GET /subjects`로 진행률을 그린다.
2. 시험 선택 시 `POST /subjects/{subjectId}/cycle`로 최신 회차를 시작하거나 이어 푼다.
3. 문제 화면은 `GET /cycles/{cycleId}/problems?count=5|10|15`를 호출한다.
4. 앱은 각 선택을 기기에 영속 저장하고 `choices[].isAnswer`로 즉시 피드백한다.
5. 현재 묶음을 모두 풀면 같은 배열을 `POST /answers/batch`로 한 번 제출한다. 서버 동기화가 끝나기 전에는 다음 묶음이나 결과로 이동하지 않는다.
6. 마지막 배치 뒤 `status=COMPLETED`가 되면 자동으로 다음 회차를 만들지 않는다. 완료 화면의 사용자가 선택할 때만 `POST /restart`를 호출한다.

## 공통 식별 규칙

- 로그인 JWT가 유효하면 회원 기록을 사용한다. 이때 `X-Guest-Token` 헤더가 있어도 반드시 무시한다.
- 비로그인 사용자는 `X-Guest-Token`에 서버가 발급한 UUID를 보낸다. 앱은 최초 cycle 시작 응답의 `issuedGuestToken`을 기기 영속 저장소에 보관하고 이후 모든 말문제 요청에 전달한다.
- token 없이 목록을 조회해도 token은 생성되지 않는다. token 없이 최초 cycle을 시작할 때만 발급된다.
- 로그인 전 익명 기록과 로그인 회원 기록은 복사·병합·삭제하지 않는다.
- `GlobalResponse` 성공 payload는 항상 `data` 아래에 있다. 오류 응답은 HTTP status와 `message`를 확인한다.

## Endpoint 표

| 화면/용도 | Method | Path | 인증/헤더 |
| --- | --- | --- | --- |
| 과목 진행률 | GET | `/api/v2/problem/word-practice/subjects` | 선택: JWT 또는 `X-Guest-Token` |
| 회차 시작/이어풀기 | POST | `/api/v2/problem/word-practice/subjects/{subjectId}/cycle` | 선택: JWT 또는 `X-Guest-Token` |
| 다음 문제 묶음 | GET | `/api/v2/problem/word-practice/cycles/{cycleId}/problems?count={count}` | JWT 또는 guest token |
| 답안 묶음 제출 | POST | `/api/v2/problem/word-practice/cycles/{cycleId}/answers/batch` | JWT 또는 guest token |
| 단건 답안 제출(이전 앱 호환) | POST | `/api/v2/problem/word-practice/cycles/{cycleId}/answers` | JWT 또는 guest token |
| 완료 회차 재시작 | POST | `/api/v2/problem/word-practice/cycles/{cycleId}/restart` | JWT 또는 guest token |

## 1. subject 진행률

### Request

```http
GET /api/v2/problem/word-practice/subjects
X-Guest-Token: 2f10c41e-0c1b-4f29-8971-1108eff78552
```

### Response

```json
{
  "data": [{
    "subjectId": 1,
    "subjectName": "감정평가사",
    "solvedCount": 3,
    "totalCount": 120,
    "remainingCount": 117,
    "status": "IN_PROGRESS"
  }]
}
```

`status`는 `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED` 중 하나다. 회차가 있으면 그 회차의 스냅샷 문제 수를, 회차가 없으면 현재 공개 말문제 수를 `totalCount`로 사용한다.

## 2. 회차 시작 또는 이어풀기

### Request

```http
POST /api/v2/problem/word-practice/subjects/1/cycle
```

### Response (token 없는 최초 익명 시작)

```json
{
  "data": {
    "cycleId": 301,
    "subjectId": 1,
    "roundNumber": 1,
    "status": "IN_PROGRESS",
    "issuedGuestToken": "2f10c41e-0c1b-4f29-8971-1108eff78552",
    "progress": { "solvedCount": 0, "correctCount": 0, "incorrectCount": 0, "totalCount": 120, "remainingCount": 120 }
  }
}
```

이미 최신 회차가 있으면 같은 회차를 반환하며 `issuedGuestToken`은 없다. 완료 회차도 자동으로 새 회차를 생성하지 않고 완료 상태로 반환한다. 존재하지 않거나 공개되지 않은 subject는 `404`, 공개 말문제가 없으면 `409`다.

## 3. 다음 문제 묶음

`count`는 정확히 `5`, `10`, `15`만 가능하며 이외 값은 `400 Bad Request`다. 남은 문제가 요청 수보다 적으면 남은 문제만 반환한다.

### Request

```http
GET /api/v2/problem/word-practice/cycles/301/problems?count=5
X-Guest-Token: 2f10c41e-0c1b-4f29-8971-1108eff78552
```

### Response

```json
{
  "data": {
    "cycleId": 301,
    "requestedCount": 5,
    "returnedCount": 5,
    "hasMore": true,
    "status": "IN_PROGRESS",
    "progress": { "solvedCount": 0, "correctCount": 0, "incorrectCount": 0, "totalCount": 120, "remainingCount": 120 },
    "problems": [{ "id": 1001, "number": 1, "requiresCalculation": false, "content": [], "explanation": [], "lecture": null, "choices": [{ "id": 4004, "number": 1, "content": "보기 1", "isAnswer": true, "explanation": [] }] }
  }
}
```

`problems` item은 기존 `ProblemV2Response`와 동일한 shape다. 따라서 현재 계약상 `choices[].isAnswer`와 `explanation`을 포함한다. React Native는 배열의 첫 문제부터 순서대로 로컬 답안을 쌓아야 한다. 완료 회차 조회는 빈 `problems`, `returnedCount=0`, `hasMore=false`를 반환한다.

## 4. 답안 묶음 제출

### Request

```http
POST /api/v2/problem/word-practice/cycles/301/answers/batch
Content-Type: application/json
X-Guest-Token: 2f10c41e-0c1b-4f29-8971-1108eff78552

{
  "answers": [
    { "problemId": 1001, "choiceId": 4004 },
    { "problemId": 1002, "choiceId": 4010 }
  ]
}
```

`answers`는 서버가 내려준 현재 문제 순서와 같아야 한다. 보통 정확히 5개이며 마지막 묶음만 남은 수에 따라 1~4개를 허용한다. 빈 배열, 5개 초과, 중복 문제, 중간 묶음의 부분 제출은 `400`이고 서버 cursor와 다른 순서는 `409`다.

### Response

```json
{
  "data": {
    "answers": [
      { "problemId": 1001, "choiceId": 4004, "isCorrect": true, "sequence": 1 },
      { "problemId": 1002, "choiceId": 4010, "isCorrect": false, "sequence": 2 }
    ],
    "status": "IN_PROGRESS",
    "progress": { "solvedCount": 2, "correctCount": 1, "incorrectCount": 1, "totalCount": 120, "remainingCount": 118 }
  }
}
```

서버는 묶음 전체의 소유권, 문제 순서, 보기 소속, 공개 문제 여부를 먼저 검증하고 한 트랜잭션으로 저장한다. 한 항목이라도 실패하면 아무 답안도 저장하지 않는다. 동일한 문제·보기 배열을 재전송하면 기존 결과를 `200 OK`로 반환하며 학습 기록도 중복 집계하지 않는다. 이미 저장한 묶음의 보기를 바꾸거나 일부만 겹치는 payload는 `409 Conflict`다.

앱은 각 선택 직후 전체 미전송 배열을 AsyncStorage에 덮어쓰고 로컬 `isAnswer`로 피드백한다. 서버 응답을 확인하고 outbox 삭제까지 성공한 뒤에만 다음 묶음 또는 결과로 이동한다. 통신 실패 시 저장된 동일 payload를 그대로 재시도한다. 오답 다시 풀기는 메모리에서만 수행하며 원래 배치 점수와 서버 진행률을 변경하지 않는다.

이전 `/answers` 단건 API는 구버전 앱 호환용으로 남아 있다. 신규 앱은 배치 API만 사용한다.

## 5. 다음 회차 재시작

### Request

```http
POST /api/v2/problem/word-practice/cycles/301/restart
X-Guest-Token: 2f10c41e-0c1b-4f29-8971-1108eff78552
```

### Response

```json
{
  "data": {
    "cycleId": 302,
    "subjectId": 1,
    "roundNumber": 2,
    "status": "IN_PROGRESS",
    "progress": { "solvedCount": 0, "correctCount": 0, "incorrectCount": 0, "totalCount": 121, "remainingCount": 121 }
  }
}
```

최신의 완료 회차만 재시작할 수 있다. 진행 중 회차, 이미 오래된 회차 ID, 다른 사용자의 회차는 각각 `409`, `409`, `403`이다. 새 회차는 호출 당시 현재 공개 카탈로그로 새 순서를 만들며, 이전 회차와 답안 이력은 보존한다.

## 오류 처리 요약

| 상황 | Status |
| --- | --- |
| 잘못된 guest token | 401 |
| 다른 참여자의 cycle 접근 | 403 |
| count가 5·10·15 외 값 | 400 |
| 존재하지 않는 cycle/비공개 subject | 404 |
| 문제 없는 subject, 답안 변경, 순서 위반, 진행 중 restart, 오래된 cycle restart | 409 |
| 빈/5개 초과/중복 답안, 마지막이 아닌 부분 배치, 문제와 보기 불일치 | 400 |

## 관련 자료

- REST Docs: `build/docs/asciidoc/word-practice.html`
- OpenAPI: `build/api-spec/openapi3.json`
- 기존 시험별 V2 문제 계약: `docs/frontend-api/problem-v2.md`
