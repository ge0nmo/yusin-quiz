# Admin Report API (신고·문의 관리)

모든 경로는 관리자 인증이 필요하다. JSON 응답은 `GlobalResponse.data`로 감싼다. 유형(`category`)과 상태(`status`) 값은 `app-report.md`와 같다.

- 상태 묶음: 미처리 = `RECEIVED` + `IN_REVIEW`, 종료 = `RESOLVED` + `REJECTED`.
- 유형 묶음: `PROBLEM_ERROR` = `WRONG_ANSWER`, `EXPLANATION`, `TYPO`, `BROKEN_MEDIA`, `OUTDATED`, `OTHER_ERROR` / `FEEDBACK` = `SUGGESTION`, `INQUIRY`.
- 시각은 기존 `LocalDateTime` 형식(타임존 없음, 서버 시간 KST)이며 초 단위까지만 내려간다. 예: `"2026-11-03T21:14:00"`.

## GET /api/admin/reports

쿼리:

- `statusGroup`: `OPEN`(기본) | `RESOLVED` | `REJECTED` | `ALL`
- `qualificationExamId`: 선택
- `categoryGroup`: 선택, `PROBLEM_ERROR` | `FEEDBACK`
- `sort`: `SAME_PROBLEM`(기본) | `LATEST` | `OLDEST`
- `page`: 1부터(기본 1), `size`: 기본 20, 1~100

`page`·`size`가 범위를 벗어나면 `400 VALIDATION_ERROR`, 모르는 enum 값은 `400`이다. 마지막 페이지를 넘으면 빈 `data`와 `pageInfo`를 준다.

정렬은 DB에서 페이지를 나누기 전에 적용한다.

- `SAME_PROBLEM`: `sameProblemOpenCount` 내림차순 → `createdAt` 내림차순 → `id` 내림차순. 문제가 없거나 삭제된 건은 0으로 본다.
- `LATEST`: `createdAt` 내림차순, `OLDEST`: `createdAt` 오름차순(같으면 `id` 같은 방향).

응답은 `AdminReportSummary[]`와 `pageInfo`다.

```json
{
  "data": [
    {
      "id": 128,
      "status": "RECEIVED",
      "category": "WRONG_ANSWER",
      "qualificationExam": { "id": 1, "name": "감정평가사" },
      "problem": { "id": 123, "number": 12, "examId": 7, "examName": "제35회", "examYear": 2024, "subjectId": 3, "subjectName": "민법" },
      "content": "3번도 맞는 설명 아닌가요?",
      "sameProblemOpenCount": 3,
      "createdAt": "2026-11-03T21:14:00"
    }
  ],
  "pageInfo": { "totalElements": 61, "totalPages": 4, "currentPage": 1, "pageSize": 20 }
}
```

- `problem`의 `number`, `examName`, `examYear`, `subjectName`은 신고할 때 저장한 스냅숏이다. `examId`, `subjectId`(전역 과목 id)는 현재 문제에서 채운다. 문제가 삭제됐으면 `id`, `examId`, `subjectId`가 `null`이고, 문제 없이 보낸 건의·문의는 `problem: null`이다.
- `sameProblemOpenCount`: 같은 문제의 미처리 신고 수(자기 자신이 미처리면 포함). 문제가 없거나 삭제됐으면 0. 종료된 신고에도 그 문제의 현재 미처리 수가 나온다.
- `pageInfo.currentPage`는 1부터 센다.

## GET /api/admin/reports/summary?qualificationExamId=

```json
{ "data": { "openCount": 8, "resolvedCount": 41, "rejectedCount": 12, "totalCount": 61 } }
```

`qualificationExamId`를 빼면 전체다. 사이드바 배지는 전체 `openCount`를 쓴다.

## GET /api/admin/reports/{id}

`AdminReportSummary`의 모든 필드에 다음을 더한다. 없으면 `404 REPORT_NOT_FOUND`.

```json
{
  "selectedChoiceNumbers": [3],
  "appVersion": "2.0.0",
  "platform": "ANDROID",
  "deviceIdMasked": "a3f9…c21",
  "deviceReportCount": 4,
  "deviceRejectedCount": 0,
  "answer": null,
  "answeredAt": null,
  "updatedAt": "2026-11-03T21:14:00",
  "sameProblemOpenReports": [
    { "id": 127, "status": "RECEIVED", "category": "WRONG_ANSWER", "content": "정답이 3번 같아요.", "createdAt": "2026-11-03T18:02:00" }
  ]
}
```

- `deviceIdMasked`: 기기 ID 앞 4자 + `…` + 뒤 3자. 기기 ID 원문은 내려주지 않는다.
- `deviceReportCount`: 그 기기의 전체 신고 수(자기 포함, 모든 자격시험). `deviceRejectedCount`: 그 기기의 `REJECTED` 수.
- `answer`, `answeredAt`: 상태와 관계없이 저장된 값이다(앱에는 종료 상태일 때만 보인다).
- `sameProblemOpenReports`: 같은 문제의 다른 미처리 신고, 자기 제외, 최신순. 문제가 없거나 삭제됐으면 `[]`.
- 문제 본문은 넣지 않는다. `GET /api/admin/problems/{id}`로 가져온다.

## PUT /api/admin/reports/{id}

```json
{ "status": "REJECTED", "answer": "확인해 보니 정답은 2번이 맞아요.", "applyToSameProblemOpen": true }
```

- `status` 필수. `RESOLVED`/`REJECTED`면 `answer`가 앞뒤 공백을 뺀 뒤 1자 이상이어야 한다. 모든 상태에서 `answer`는 공백을 뺀 뒤 최대 1000자다. 어기면 `400 VALIDATION_ERROR`(`valueErrors[].descriptor`가 `answer`).
- `answer`는 앞뒤 공백을 빼고 저장하며, `null`이나 빈 문자열은 `null`로 저장한다.
- 종료 상태로 저장할 때마다 `answeredAt`을 지금으로 바꾼다. 미처리로 저장하면 `answeredAt`을 `null`로 만들고 `answer`는 보낸 값으로 저장한다.
- `applyToSameProblemOpen`(선택, 기본 `false`)이 `true`면 같은 문제의 다른 미처리 신고에도 같은 `status`, `answer`를 적용한다. 문제 없는 건이나 문제가 삭제된 건이면 무시한다.
- 없는 신고는 `404 REPORT_NOT_FOUND`.

```json
{ "data": { "updatedIds": [128, 127, 126] } }
```

`updatedIds`는 요청 대상이 첫 번째이고, 나머지는 함께 처리한 신고의 최신순이다.

## 문제·자격시험 삭제와의 관계

- 문제를 삭제하면 그 문제를 가리키는 신고의 문제 연결만 끊고 스냅숏은 남긴다. 신고가 있어도 문제 삭제는 막히지 않는다(외래 키도 `ON DELETE SET NULL`).
- 신고·문의가 남아 있는 자격시험은 회차가 없어도 `409 CONTENT_IN_USE`로 지우지 않는다.
