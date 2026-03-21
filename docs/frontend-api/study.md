# Study API Contract

## 엔드포인트

- `POST /api/v1/study/exam/start`
- `POST /api/v1/study/answer`
- `POST /api/v1/study/finish`
- `GET /api/v1/study-logs/yearly?year={year}`
- `GET /api/v1/study-logs/streak`

## 세션 시작 규칙

- `POST /api/v1/study/exam/start` 의 역할은 그대로다
- 서버는 새 타이머 필드를 추가하지 않는다
- 응답은 `sessionId`, `status`, `lastIndex`, `submittedAnswers` 만 사용한다
- 기존 진행 중 세션이 있으면 이어풀기 응답을 반환한다
- `submittedAnswers` 는 배열이며, resume 케이스의 각 item 은 반드시 아래 shape 를 따른다
  - `problemId: number`
  - `choiceId: number`
  - `isCorrect: boolean`
- 첫 시작이면 `submittedAnswers` 는 빈 배열 `[]` 이다

## 세션 시작 응답 예시

```json
{
  "data": {
    "sessionId": 123,
    "status": "IN_PROGRESS",
    "lastIndex": 2,
    "submittedAnswers": [
      {
        "problemId": 101,
        "choiceId": 1001,
        "isCorrect": false
      }
    ]
  }
}
```

## 학습 종료 규칙

- `POST /api/v1/study/finish` 의 authoritative 필드는 아래 counts 다
  - `correctCount`
  - `totalCount`
  - `answeredCount`
  - `unansweredCount`
- `finalScore` 는 deprecated 하위호환 필드이며 현재 `correctCount` 와 같은 숫자를 유지한다
- 서버는 클라이언트가 보낸 값이 아니라 세션에 저장된 답안과 정답 기준으로 summary 를 계산한다
- 같은 세션에 대한 중복 finish 요청은 가능한 한 같은 요약을 다시 반환한다
- `totalCount` 는 세션 시작 시점의 문제 수 snapshot 을 사용하므로, 시험이 중간에 수정되어도 같은 세션 결과가 흔들리지 않는다

## 학습 로그 규칙

- 로그인 사용자 기준으로 `practice`, `real/exam` 모두 학습 로그에 포함된다
- `practice` 는 세션 내 같은 문제의 첫 제출만 로그에 반영된다
- 같은 세션에서 같은 문제의 답을 여러 번 바꿔도 추가 집계하지 않는다
- `real/exam` 은 finish 시점에 `answeredCount` 만큼 한 번만 반영된다
- 이 로그는 하루 unique 문제 수나 전체 기간 unique 문제 수가 아니다
- 세션 시도 기반 누적 로그라고 이해해야 한다

## 종료 응답 예시

```json
{
  "data": {
    "finalScore": 31,
    "correctCount": 31,
    "totalCount": 40,
    "answeredCount": 38,
    "unansweredCount": 2
  }
}
```

## 연간 로그 예시

```json
{
  "data": [
    {
      "date": "2026-03-15",
      "count": 2
    }
  ]
}
```

## streak 예시

```json
{
  "data": {
    "streak": 5
  }
}
```
