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
- 보기는 plain text이며 1~5번 정확히 다섯 개, 정답은 정확히 하나다.
- 문제/보기 해설은 빈 배열이어도 공개할 수 있다.

## Image upload

- `POST /api/admin/file` multipart field `file`
- 업로드 직후 사용할 presigned URL 문자열을 반환한다.
