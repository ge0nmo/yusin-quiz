# Admin Content API

모든 JSON 응답은 `GlobalResponse.data`로 감싸며, 모든 경로는 관리자 인증이 필요하다.

## Dashboard

- `GET /api/admin/dashboard`
- 응답: `{qualificationExamCount, subjectCount, examCount, problemCount}`

## Qualification exams

- `GET|POST /api/admin/qualification-exams`
- `GET|PUT|DELETE /api/admin/qualification-exams/{id}`
- `code`는 `APPRAISER | CPA | CUSTOMS_BROKER` enum이며, 표시 이름은 서버가 enum에서 결정한다.
- 생성: `{code,status,subjects:[{subjectId,status,displayOrder}]}`
- 수정: `{status,subjects:[...]}` (`code`와 표시 이름은 불변)

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
- `label`은 화면에 표시할 문자열 전체다. 앞뒤 공백을 제외하고 필수이며 최대 20자다.
- 각 `content`는 텍스트(`text` 또는 `spans[].text`)나 이미지(`src`)가 하나 이상 있는 JSON block 배열이어야 한다. 목록과 다른 지문 묶음 안에 중첩된 텍스트·이미지도 유효하다.
- 관리자는 완전히 빈 행과 빈 지문 묶음을 요청에서 제외해야 한다. 서버가 빈 묶음, 빈 항목 또는 라벨/내용 중 하나만 있는 항목을 받으면 `400 INVALID_STATEMENT_GROUP`으로 거부한다.
- 보기는 plain text이며 1~5번 정확히 다섯 개, 정답은 정확히 하나다.
- 문제/보기 해설은 빈 배열이어도 공개할 수 있다.

## Image upload

- `POST /api/admin/file` multipart field `file`
- 업로드 직후 사용할 presigned URL 문자열을 반환한다.
