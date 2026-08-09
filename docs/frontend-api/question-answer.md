# Question and Answer API Contract

질문과 답변 목록의 `page` 는 Spring 표준과 같은 0 기반이다. 응답의
`pageInfo.currentPage` 만 사용자 표시를 위해 1 기반으로 내려간다.

## 엔드포인트

- `GET /api/v1/problem/{problemId}/question?page={page}&size={size}`
- `GET /api/v1/question/{questionId}/answer?page={page}&size={size}`
- `PATCH /api/v1/answer/{answerId}`
- `DELETE /api/v1/question/{questionId}`
- `DELETE /api/v1/answer/{answerId}`

## 목록 규칙

- 질문과 답변 모두 요청의 `page` 를 그대로 사용한다.
- 답변은 서버가 `createdAt DESC` 로 정렬한다. 클라이언트의 `sort` 값은 적용하지 않는다.
- 관리자가 작성한 답변은 `isAdmin: true` 이며 정렬 우선권은 없다.

## 수정과 삭제

- 일반 사용자는 본인이 작성한 답변만 수정할 수 있다.
- 답변 수정 성공 응답은 갱신된 `AnswerDTO` 를 `GlobalResponse.data` 에 담는다.
- 질문과 답변 삭제 성공 응답은 body 없는 `204 No Content` 다.

## AnswerDTO 예시

```json
{
  "id": 12,
  "content": "관리자 답변입니다.",
  "createdAt": "2026-08-09T12:00:00",
  "memberId": 3,
  "username": "관리자",
  "isAdmin": true
}
```

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/question/controller/QuestionController.java`
- `src/main/java/com/cpa/yusin/quiz/answer/controller/AnswerController.java`
- `src/test/java/com/cpa/yusin/quiz/question/integration/QuestionTest.java`
- `src/test/java/com/cpa/yusin/quiz/answer/integration/AnswerTest.java`
