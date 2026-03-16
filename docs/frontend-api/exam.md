# Exam API Contract

시험은 soft delete 와 별도로 `status` 를 가진다.

- `PUBLISHED`: 사용자 앱과 관리자에서 모두 조회
- `DRAFT`: 관리자에서만 조회

기존 데이터 호환 정책은 `status = null` legacy row 를 `DRAFT` 로 간주하는 것이다.

## 엔드포인트

- `GET /api/v1/exam?subjectId={subjectId}&year={year}`
- `GET /api/v1/exam/year?subjectId={subjectId}`
- `GET /api/admin/subject/{subjectId}/exam`
- `POST /api/admin/exam?subjectId={subjectId}`
- `PATCH /api/admin/exam/{examId}`

## 사용자 앱 규칙

- `/api/v1/exam` 은 상위 과목이 `PUBLISHED` 이고 시험도 `PUBLISHED` 인 데이터만 내려준다
- `/api/v1/exam/year` 은 사용자에게 실제로 노출되는 `PUBLISHED` 시험이 하나라도 있는 연도만 내려준다
- 공개 과목 아래 `DRAFT` 시험만 존재하면 `/api/v1/exam` 과 `/api/v1/exam/year` 는 200 응답과 빈 목록을 반환한다
- 사용자 응답의 `status` 는 항상 `PUBLISHED` 다
- `DRAFT` 시험은 문제, 질문, 답변, 북마크, 학습 시작의 사용자 진입점으로 사용할 수 없다

## 관리자 규칙

- `/api/admin/subject/{subjectId}/exam` 은 `isRemoved = false` 인 시험을 모두 내려준다
- 관리자 응답은 각 시험의 현재 `status` 를 포함한다
- `POST /api/admin/exam`, `PATCH /api/admin/exam/{examId}` 에서 `status` 를 지정할 수 있다
- `status` 를 생략하면 `DRAFT` 로 저장한다
- 관리자 대시보드와 관리자 문제/질문/답변 화면은 `DRAFT` 시험도 계속 노출한다

## 관리자 요청 예시

```json
{
  "name": "2026 모의고사",
  "year": 2026,
  "status": "DRAFT"
}
```

## 관리자 응답 예시

```json
[
  {
    "id": 10,
    "name": "1차",
    "year": 2025,
    "status": "PUBLISHED"
  },
  {
    "id": 11,
    "name": "모의고사",
    "year": 2026,
    "status": "DRAFT"
  }
]
```

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/exam/controller/ExamController.java`
- `src/main/java/com/cpa/yusin/quiz/exam/controller/AdminExamController.java`
- `src/main/java/com/cpa/yusin/quiz/exam/service/ExamServiceImpl.java`
- `src/test/java/com/cpa/yusin/quiz/exam/integration/ExamTest.java`
