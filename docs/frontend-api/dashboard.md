# Dashboard API Contract

관리자 대시보드 화면은 이 API 하나로 전체 집계, 운영 지표, 최신 미답변 질문, 선택된 과목/시험 컨텍스트를 함께 가져온다.
최종 권위는 integration test + generated docs 이고, 이 문서는 프론트 구현 판단을 빠르게 돕는 포인터다.

## 엔드포인트

- `GET /api/admin/dashboard?subjectId={optional}&examId={optional}`

## 화면 기준 사용처

- `quiz-admin` 관리자 대시보드 초기 로딩
- 과목/시험 선택 상태가 있는 대시보드 새로고침

## 핵심 계약 규칙

- 응답은 `GlobalResponse<DashboardResponse>` 형태
- `totals`, `operations`, `pendingQuestions` 는 전체 활성 데이터 기준 집계다
- soft delete 는 상위 엔티티까지 전파해서 제외한다
- `todayQuestionCount` 는 `ClockHolder` 가 제공하는 현재 날짜 경계를 기준으로 계산한다
- `pendingQuestions` 는 관리자 미답변 질문만 포함하고 최대 5건, 정렬은 `createdAt DESC`, tie-break 는 `id DESC`
- `context.subject` 는 `subjectId` 가 없거나 stale selection 이면 `null`
- `context.exam` 는 `examId` 가 없거나 stale selection 이면 `null`
- `subjectId` 가 함께 전달되면 `exam` context 는 해당 subject 에 속한 시험일 때만 내려온다
- `lectureCoverageRate` 는 숫자 타입이며, 활성 문제가 없으면 `0`, 그 외에는 소수 1자리 반올림 퍼센트 값이다

## 응답 예제

```json
{
  "data": {
    "totals": {
      "subjectCount": 3,
      "examCount": 12,
      "problemCount": 248,
      "questionCount": 41
    },
    "operations": {
      "todayQuestionCount": 2,
      "unansweredQuestionCount": 7,
      "problemsWithoutLectureCount": 53
    },
    "pendingQuestions": [
      {
        "id": 101,
        "title": "정답 근거가 궁금합니다",
        "username": "tester",
        "createdAt": "2026-03-14T09:12:00",
        "answerCount": 0,
        "problemId": 55
      }
    ],
    "context": {
      "subject": {
        "id": 1,
        "name": "회계학",
        "examCount": 4,
        "problemCount": 80
      },
      "exam": {
        "id": 10,
        "name": "1차",
        "year": 2025,
        "problemCount": 20,
        "questionCount": 5,
        "unansweredQuestionCount": 2,
        "lectureCoverageRate": 75.0
      }
    }
  }
}
```

## 프론트 구현 메모

- stale selection 이 와도 API 는 `200 OK` 이므로, 프론트는 `context.subject === null`, `context.exam === null` 을 정상 상태로 처리해야 한다
- 운영 지표 카드와 pending list 는 전체 기준이고, 선택된 과목/시험은 context 카드에 반영된다
- `lectureCoverageRate` 는 문자열 포맷팅 없이 숫자로 내려오므로 프론트에서 바로 `%` UI 만 붙이면 된다
- 카드 클릭 후 어떤 목록 API로 들어가야 하는지는 `docs/frontend-api/dashboard-drilldown.md` 를 기준으로 구현하면 된다

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/dashboard/controller/AdminDashboardController.java`
- `src/main/java/com/cpa/yusin/quiz/dashboard/service/DashboardServiceImpl.java`
- `src/main/java/com/cpa/yusin/quiz/dashboard/infrastructure/DashboardJpaRepository.java`
- `src/test/java/com/cpa/yusin/quiz/dashboard/integration/DashboardTest.java`
