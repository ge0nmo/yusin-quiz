# App Report API (문제 오류 신고 · 건의/문의)

앱에는 회원 로그인이 없다. 신고·문의는 익명 기기 ID로 받고, 같은 기기에서 보낸 내역만 다시 볼 수 있다.

## 공통

- 경로: `/api/v2/qualification-exams/{code}/reports/**`. `{code}`는 `APPRAISER | CPA | CUSTOMS_BROKER | REAL_ESTATE_AGENT`다. 모르는 코드나 등록되지 않은 자격시험은 `404 QUALIFICATION_EXAM_NOT_FOUND`(공개 여부는 보지 않는다).
- 인증은 없다. 모든 요청에 `X-Device-Id` 헤더가 필요하다. 앱이 처음 실행할 때 만든 UUID(대소문자 무관, 하이픈 포함 36자)를 보낸다. 서버는 소문자로 바꿔 저장·비교한다.
- `X-Device-Id`가 없거나 형식이 틀리면 본문 검증보다 먼저 `400 INVALID_DEVICE_ID`("기기 정보를 확인할 수 없어요. 앱을 다시 실행해 주세요.")다.
- 시각(`createdAt`, `updatedAt`, `answeredAt`)은 기존 `LocalDateTime` 직렬화 형식(ISO-8601, 타임존 없음, 서버 시간 KST)이며 초 단위까지만 내려간다. 예: `"2026-11-04T10:20:00"`.
- 오류 본문은 `{status, message, code, valueErrors?}`다. 앱은 `message`를 그대로 보여 주면 된다.

## 유형과 상태

| `category` | 묶음 | 문제(`problemId`) | 내용(`content`) |
|---|---|---|---|
| `WRONG_ANSWER` | 문제 오류 | 필수 | 필수 |
| `EXPLANATION` | 문제 오류 | 필수 | 선택 |
| `TYPO` | 문제 오류 | 필수 | 선택 |
| `BROKEN_MEDIA` | 문제 오류 | 필수 | 선택 |
| `OUTDATED` | 문제 오류 | 필수 | 선택 |
| `OTHER_ERROR` | 문제 오류 | 필수 | 필수 |
| `SUGGESTION` | 건의·문의 | 선택 | 필수 |
| `INQUIRY` | 건의·문의 | 선택 | 필수 |

`status`는 `RECEIVED`(접수) → `IN_REVIEW`(확인 중) → `RESOLVED` 또는 `REJECTED`(앱에서는 둘 다 "답변 완료")다. 앞의 둘이 미처리, 뒤의 둘이 종료다.

## POST /api/v2/qualification-exams/{code}/reports → 201

```json
{
  "category": "WRONG_ANSWER",
  "problemId": 123,
  "content": "3번도 맞는 설명 아닌가요?",
  "selectedChoiceNumbers": [3],
  "appVersion": "2.0.0",
  "platform": "ANDROID"
}
```

검증(어기면 `400 VALIDATION_ERROR`, `valueErrors[].descriptor`에 필드 이름):

- `category`: 위 8개 중 하나, 필수. `platform`: `ANDROID | IOS`, 필수. 모르는 enum 값은 `400`.
- `content`: 항상 문자열(`null` 불가). 앞뒤 공백을 빼고 저장하며 최대 1000자. 내용 필수 유형은 공백을 뺀 뒤 5자 이상, 선택 유형은 `""` 허용.
- `problemId`: 문제 오류 6종은 필수. 건의·문의는 `null`이어도 되고 문제를 붙여도 된다.
- `selectedChoiceNumbers`: 정수 배열(`null` 불가), 0~5개, 각 값 1~5, 중복 없음. 고르지 않았으면 `[]`. `problemId`가 `null`이면 반드시 `[]`. 서버는 오름차순으로 저장한다.
- `appVersion`: 공백이 아닌 1~20자, 필수.

그 밖의 오류:

- `problemId`의 문제가 없거나 `{code}`가 아닌 자격시험의 문제면 `404 PROBLEM_NOT_FOUND`. 문제의 공개 여부는 보지 않는다.
- 같은 기기 + 같은 문제 + 같은 유형의 미처리 신고가 있으면 `409 REPORT_DUPLICATED`("이미 신고한 문제예요. 확인 후 답변 드릴게요."). 문제 없는 건의·문의에는 적용하지 않는다.
- 같은 기기가 최근 24시간(지금부터 거꾸로 센 rolling 창) 동안 20건을 보냈으면 `429 REPORT_RATE_LIMITED`("오늘은 더 보낼 수 없어요. 내일 다시 시도해 주세요."). 모든 자격시험을 합쳐 센다.

응답은 `GlobalResponse<MyReport>`다.

## GET /api/v2/qualification-exams/{code}/reports → 200

이 기기가 이 자격시험에 보낸 신고·문의를 최신순(`createdAt` 내림차순)으로 최대 100건 반환한다. 페이지와 `pageInfo`는 없다. 응답은 `GlobalResponse<MyReport[]>`다.

## GET /api/v2/qualification-exams/{code}/reports/{id} → 200

이 기기의 것이 아니거나 자격시험이 다르면 `404 REPORT_NOT_FOUND`("문의 내역을 찾을 수 없어요."). 응답은 `GlobalResponse<MyReport>`다.

## MyReport

```json
{
  "id": 128,
  "category": "WRONG_ANSWER",
  "status": "RESOLVED",
  "problem": { "id": 123, "number": 12, "examName": "제35회", "examYear": 2024, "subjectName": "민법" },
  "content": "3번도 맞는 설명 아닌가요?",
  "selectedChoiceNumbers": [3],
  "answer": "확인해 보니 정답은 2번이 맞아요.",
  "answeredAt": "2026-11-04T10:20:00",
  "createdAt": "2026-11-03T21:14:00",
  "updatedAt": "2026-11-04T10:20:00"
}
```

- `problem`은 신고할 때 저장한 스냅숏이다. 문제를 붙이지 않은 건의·문의는 `null`이다. 나중에 문제가 삭제되면 `id`만 `null`이고 나머지 값은 그대로다.
- `answer`, `answeredAt`은 종료 상태(`RESOLVED`, `REJECTED`)일 때만 값이 있다. 관리자가 미처리로 되돌리면 둘 다 `null`이 된다.
- `answeredAt`은 관리자가 종료 상태로 저장할 때마다 새 시각이 된다. 앱은 본 시점의 `answeredAt`과 다르면 "새 답변"으로 본다.
