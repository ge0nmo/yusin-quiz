# Admin Content API

모든 JSON 응답은 `GlobalResponse.data`로 감싸며, 모든 경로는 관리자 인증이 필요하다.

## Dashboard

- `GET /api/admin/dashboard`
- 응답: `{qualificationExamCount, subjectCount, examCount, problemCount, openReportCount}`
- `openReportCount`는 전체 미처리(`RECEIVED`, `IN_REVIEW`) 신고·문의 수다. 신고·문의 API는 `admin-report.md`.

## Qualification exams

- `GET|POST /api/admin/qualification-exams`
- `GET|PUT|DELETE /api/admin/qualification-exams/{id}`
- `code`는 `APPRAISER | CPA | CUSTOMS_BROKER | REAL_ESTATE_AGENT` enum이며, 표시 이름은 서버가 enum에서 결정한다.
- 생성: `{code,status,subjects:[{subjectId,status,displayOrder}]}`
- 수정: `{status,subjects:[...]}` (`code`와 표시 이름은 불변)
- 삭제: 시험 회차나 신고·문의가 남아 있으면 `409 CONTENT_IN_USE`.

## Global subjects

- `GET|POST /api/admin/subjects`
- `GET|PUT|DELETE /api/admin/subjects/{id}`
- 저장: `{name,status}`

## Exams

- `GET /api/admin/exams?qualificationExamId=`
- `GET|PUT|DELETE /api/admin/exams/{id}`
- `POST /api/admin/exams`
- 저장: `{qualificationExamId,name,year,status}`

## Problems

- `GET /api/admin/problems?qualificationExamId=&examId=&subjectId=`
- `GET /api/admin/problems/next-number?examId=&subjectId=` → `{nextNumber}`
- `GET|PUT|DELETE /api/admin/problems/{id}`
- `POST /api/admin/problems`
- 문제는 해당 Exam의 QualificationExam에 연결된 Subject만 선택할 수 있다.
- 문제를 삭제하면 그 문제를 가리키는 신고·문의의 문제 연결을 끊고(스냅숏은 남김) 지운다. 신고가 있어도 삭제는 막히지 않는다.
- `content`, 문제 해설, 보기별 해설은 JSON block 배열이다.
- JSON block 배열의 어느 위치에서든 다음 `statementGroup` 블록을 중첩해 사용할 수 있다.

```json
{
  "type": "statementGroup",
  "items": [
    {
      "label": "(가)",
      "content": [
        {"type": "text", "spans": [{"text": "보고기간말 이전에"}]}
      ]
    },
    {
      "label": "ㄴ.",
      "content": [
        {"type": "image", "src": "https://example.com/condition.png"}
      ]
    }
  ]
}
```

- `items`는 하나 이상이어야 하며 최대 개수 제한은 없다.
- `label`은 화면에 표시할 문자열 전체이며 항상 문자열로 보낸다. 앞뒤 공백을 제외하고 최대 20자다.
- 항목이 2개 이상이면 모든 `label`이 앞뒤 공백을 제외하고 비어 있으면 안 된다. 항목이 정확히 1개인 묶음만 `label: ""`(라벨 없음)을 쓸 수 있다. 필드를 빼지 않는다.
- 각 `content`는 텍스트(`text` 또는 `spans[].text`)나 이미지(`src`)가 하나 이상 있는 JSON block 배열이어야 한다. 목록과 다른 지문 묶음 안에 중첩된 텍스트·이미지도 유효하다.
- 관리자는 완전히 빈 행과 빈 지문 묶음을 요청에서 제외해야 한다. 서버가 빈 묶음, 빈 항목, 내용 없는 항목 또는 항목이 2개 이상인데 라벨이 빈 항목을 받으면 `400 INVALID_STATEMENT_GROUP`으로 거부한다.
- 표는 `table` 블록이며 JSON block 배열의 어느 위치에서든 쓸 수 있다.

```json
{
  "type": "table",
  "columnWidths": [120, 80],
  "rows": [
    {"cells": [{"header": true, "colspan": 2, "content": [{"type": "text", "spans": [{"text": "구분"}]}]}]},
    {"cells": [
      {"content": [{"type": "text", "spans": [{"text": "\\frac{1}{2}", "math": true}]}]},
      {"content": []}
    ]}
  ]
}
```

- `rows`는 하나 이상이고 표 전체에 칸이 하나 이상 있어야 한다. 병합으로 가려진 칸은 `cells`에 넣지 않으므로(HTML table 규칙) 행마다 칸 수가 다를 수 있고 빈 행도 허용한다.
- 각 칸의 `content`는 JSON block 배열이며 빈 배열(빈 칸)도 허용한다. `header`는 boolean, `colspan`·`rowspan`은 1~1000 정수이며 생략하면 각각 `false`, `1`이다. 관리자 편집기는 1이면 생략한다.
- `columnWidths`는 생략할 수 있다. 있으면 열마다 하나씩 1~10000 정수(px)인 배열이며 길이는 열 수와 같게 보낸다. 화면에서는 비율로만 쓰며, 생략하면 모든 열이 같은 너비다.
- `border`는 생략할 수 있다. 있으면 `"all"`(모든 칸에 선, 기본값), `"outer"`(바깥 테두리만), `"none"`(선 없음) 중 하나여야 한다. 관리자 편집기는 `all`이면 생략한다.
- 구조가 맞지 않으면 `400 INVALID_TABLE`로 거부한다. 표 칸 안의 관리 이미지도 조회할 때 다시 서명한다.
- 인라인 수식은 text block의 span에 `{"text": "<LaTeX>", "math": true}`로 저장한다. `math`는 boolean이며, `true`이면 `text`가 공백이 아닌 LaTeX 원문이어야 한다. 아니면 `400 INVALID_MATH`.
- 보기 본문은 plain text이므로 수식을 `$...$`로 감싸 저장한다. 서버는 보기 본문의 수식을 해석하거나 검사하지 않는다.
- 보기는 2~5개이며 1번부터 빠짐없이 번호를 붙이고, 정답은 하나 이상이다(복수 정답 허용). 개수가 2~5가 아니면 `400 VALIDATION_ERROR`, 번호가 1..N이 아니거나 정답이 없으면 `400 INVALID_PROBLEM_CHOICES`.
- 수정할 때 보기 개수를 바꿀 수 있다. 남아 있는 번호의 보기는 같은 `id`를 유지하고, 없어진 번호는 삭제되며, 늘어난 번호는 새 `id`로 만들어진다.
- 보기는 `{number, content, imageUrl?, isAnswer, explanation}`이다. `content`는 plain text, `imageUrl`은 보기 이미지 하나다. `imageUrl`이 없거나 `null`이거나 빈 문자열이면 이미지 없음이다.
- `imageUrl`은 `POST /api/admin/file`이 돌려준 URL 또는 http(s) URL이며 2048자 이하다. 아니면 `400 INVALID_CHOICE_IMAGE`. 서버는 보낸 URL을 그대로 저장하고, 관리 이미지면 조회할 때마다 새로 서명해 돌려준다.
- 보기는 `content`가 비어 있지 않거나 `imageUrl`이 있어야 한다. 그림만 있는 보기는 `"content": ""`로 보낸다. 둘 다 없으면 `400 VALIDATION_ERROR`(`valueErrors[].descriptor`가 `choices[i].content`)로 거부한다.
- `GET /api/admin/problems/{id}`의 `choices[]`는 `{id, number, content, imageUrl, isAnswer, explanation}`이며 이미지가 없으면 `imageUrl: null`이다.
- 문제/보기 해설은 빈 배열이어도 공개할 수 있다.

## Image upload

- `POST /api/admin/file` multipart field `file`
- 업로드 직후 사용할 presigned URL 문자열을 반환한다.
