# Dashboard Drill-Down API Contract

관리자 대시보드 카드 숫자 클릭 시 어떤 API로 어떤 목록 화면으로 진입해야 하는지 정리한 문서다.
최종 권위는 integration test + generated docs 이고, 이 문서는 프론트 구현 판단을 빠르게 돕는 포인터다.

## 요약

| 카드 | 사용할 API | 프론트 라우트 예시 | 비고 |
| --- | --- | --- | --- |
| 미답변 질문 | `GET /api/admin/question?status=UNANSWERED` | `/question?status=unanswered` | 신규 API 없음 |
| 오늘 등록 질문 | `GET /api/admin/question?datePreset=TODAY` | `/question?datePreset=today` | 서버 clock 기준 오늘 |
| 강의 미연결 문제 | `GET /api/v2/admin/problem/search?lectureStatus=WITHOUT_LECTURE` | `/problem?lectureStatus=withoutLecture` | 신규 검색 API |

## 핵심 정합성 규칙

- 프론트는 오늘 날짜 경계를 직접 계산하지 않는다
- `todayQuestionCount` 와 `GET /api/admin/question?datePreset=TODAY` 총건수는 같은 기준이다
- `problemsWithoutLectureCount` 와 `GET /api/v2/admin/problem/search?lectureStatus=WITHOUT_LECTURE` 총건수는 같은 기준이다
- 두 목록 API 모두 soft delete 된 상위 엔티티까지 전파해서 제외한다
- 질문 목록의 오늘 기준은 서버 `ClockHolder` 날짜 경계다
- 문제 목록의 활성 기준은 `subject.isRemoved = false`, `exam.isRemoved = false`, `problem.isRemoved = false` 인 active hierarchy 다

## 1. 미답변 질문

신규 API는 없다.
기존 관리자 질문 목록 API에 이미 `status=UNANSWERED` 가 있다.

### 요청 예시

```http
GET /api/admin/question?page=0&size=20&status=UNANSWERED
```

### 프론트 메모

- 대시보드 `unansweredQuestionCount` 카드 클릭 시 그대로 연결하면 된다
- 응답 shape 는 기존 질문 목록과 동일하다
- `page` 요청값은 0-based 이고, 응답 `pageInfo.currentPage` 는 1-based 다

## 2. 오늘 등록 질문

기존 관리자 질문 목록 API에 `datePreset=TODAY` 필터가 추가됐다.
`status`, `keyword`, `page`, `size` 와 함께 조합 가능하다.

### 요청 예시

```http
GET /api/admin/question?page=0&size=20&datePreset=TODAY
GET /api/admin/question?page=0&size=20&datePreset=TODAY&status=UNANSWERED
GET /api/admin/question?page=0&size=20&datePreset=TODAY&keyword=환급
```

### 쿼리 파라미터

- `page`: optional, 0-based, default `0`
- `size`: optional, default Spring pageable 규칙 사용
- `status`: optional, `ALL | ANSWERED | UNANSWERED`, default `ALL`
- `datePreset`: optional, `ALL | TODAY`, default `ALL`
- `keyword`: optional, title/content/username/email 대상 검색

### 응답 shape

- `GlobalResponse<List<QuestionDTO>> + pageInfo`
- 기존 `GET /api/admin/question` 와 완전히 동일한 shape

### 응답 예시

```json
{
  "data": [
    {
      "id": 201,
      "title": "오늘 등록된 질문",
      "content": "질문 내용",
      "answerCount": 0,
      "answeredByAdmin": false,
      "createdAt": "2026-03-14T09:30:00",
      "problemId": 55,
      "memberId": 10,
      "email": "user@test.com",
      "username": "tester"
    }
  ],
  "pageInfo": {
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 1,
    "pageSize": 20
  }
}
```

### 프론트 메모

- 이 카드에서는 브라우저 local time 이 아니라 서버 기준 오늘을 보여준다
- 그래서 카드 숫자 클릭 후 목록도 반드시 `datePreset=TODAY` 로 조회해야 한다
- 클라이언트에서 `createdAt` 을 다시 필터링하면 숫자와 리스트가 어긋날 수 있다

## 3. 강의 미연결 문제

대시보드 전체 집계에서 바로 진입하기 위한 신규 검색 API 다.
기존 `GET /api/v2/admin/problem?examId={examId}` 는 시험 단위 목록이라 대시보드 전역 카드와 직접 연결하면 안 된다.

### 엔드포인트

- `GET /api/v2/admin/problem/search`

### 최소 요청 예시

```http
GET /api/v2/admin/problem/search?page=0&size=20&lectureStatus=WITHOUT_LECTURE
```

### 필터 조합 예시

```http
GET /api/v2/admin/problem/search?page=0&size=20&lectureStatus=WITHOUT_LECTURE&subjectId=1
GET /api/v2/admin/problem/search?page=0&size=20&lectureStatus=WITHOUT_LECTURE&year=2026
GET /api/v2/admin/problem/search?page=0&size=20&lectureStatus=WITHOUT_LECTURE&subjectId=1&year=2026&examId=10
GET /api/v2/admin/problem/search?page=0&size=20&lectureStatus=WITH_LECTURE
GET /api/v2/admin/problem/search?page=0&size=20&lectureStatus=ALL
```

### 쿼리 파라미터

- `page`: required in practice, 0-based
- `size`: required in practice
- `lectureStatus`: `ALL | WITH_LECTURE | WITHOUT_LECTURE`
- `subjectId`: optional
- `year`: optional
- `examId`: optional

### 응답 shape

- `GlobalResponse<List<AdminProblemSearchResponse>> + pageInfo`

### 응답 필드

- `id`: 문제 ID
- `number`: 문제 번호
- `subjectId`: 과목 ID
- `subjectName`: 과목명
- `examId`: 시험 ID
- `examName`: 시험명
- `examYear`: 시험 연도
- `lecture`: 해설 강의 정보. 강의 미연결 문제면 `null`
- `choiceCount`: 선택지 개수
- `answerChoiceCount`: 정답 선택지 개수
- `contentPreviewText`: 짧은 문제 미리보기 텍스트

### 응답 예시

```json
{
  "data": [
    {
      "id": 301,
      "number": 7,
      "subjectId": 2,
      "subjectName": "세법",
      "examId": 18,
      "examName": "2차",
      "examYear": 2026,
      "lecture": null,
      "choiceCount": 4,
      "answerChoiceCount": 1,
      "contentPreviewText": "강의가 아직 연결되지 않은 세법 문제입니다"
    }
  ],
  "pageInfo": {
    "totalElements": 53,
    "totalPages": 3,
    "currentPage": 1,
    "pageSize": 20
  }
}
```

### lecture 필드 shape

강의가 연결된 경우 `lecture` 는 아래 shape 를 쓴다.

```json
{
  "youtubeUrl": "https://www.youtube.com/watch?v=abc123XYZ09",
  "startTimeSecond": 430,
  "playbackUrl": "https://www.youtube.com/watch?v=abc123XYZ09&t=430s"
}
```

### 프론트 메모

- 대시보드 `problemsWithoutLectureCount` 카드 클릭 시 첫 진입은 `lectureStatus=WITHOUT_LECTURE` 로 고정한다
- 추가 필터 UI 가 있더라도 첫 로딩 총건수는 대시보드 카드 숫자와 같아야 한다
- `lecture = null` 여부로 UI badge 를 바로 그리면 된다
- `contentPreviewText` 는 목록 row 미리보기 용도다. 상세 본문 대체 데이터가 아니다

## 추천 라우팅 규칙

- 미답변 질문 카드 클릭: `/question?status=unanswered`
- 오늘 등록 질문 카드 클릭: `/question?datePreset=today`
- 강의 미연결 문제 카드 클릭: `/problem?lectureStatus=withoutLecture`

프론트 내부 쿼리 문자열은 자유롭게 설계해도 되지만, 서버 요청으로는 아래 값으로 매핑해야 한다.

- `unanswered` -> `UNANSWERED`
- `today` -> `TODAY`
- `withoutLecture` -> `WITHOUT_LECTURE`
- `withLecture` -> `WITH_LECTURE`

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/dashboard/controller/AdminDashboardController.java`
- `src/main/java/com/cpa/yusin/quiz/question/controller/AdminQuestionController.java`
- `src/main/java/com/cpa/yusin/quiz/problem/controller/AdminProblemV2Controller.java`
- `src/main/java/com/cpa/yusin/quiz/dashboard/infrastructure/DashboardJpaRepository.java`
- `src/main/java/com/cpa/yusin/quiz/question/infrastructure/QuestionJpaRepository.java`
- `src/main/java/com/cpa/yusin/quiz/problem/infrastructure/ProblemJpaRepository.java`
- `src/test/java/com/cpa/yusin/quiz/dashboard/integration/DashboardTest.java`
- `src/test/java/com/cpa/yusin/quiz/question/integration/AdminQuestionTest.java`
- `src/test/java/com/cpa/yusin/quiz/problem/integration/ProblemTest.java`
