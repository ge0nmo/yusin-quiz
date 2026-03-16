# Subject API Contract

과목은 soft delete 와 별도로 `status` 를 가진다.

- `PUBLISHED`: 사용자 앱과 관리자에서 모두 조회
- `DRAFT`: 관리자에서만 조회

기존 데이터 호환을 위해 `status = null` 인 legacy row 는 사용자 앱에서 `PUBLISHED` 로 간주한다.

## 엔드포인트

- `GET /api/v1/subject?page={page}&size={size}`
- `GET /api/admin/subject`
- `POST /api/admin/subject`
- `PATCH /api/admin/subject/{subjectId}`

## 사용자 앱 규칙

- `/api/v1/subject` 는 `status = PUBLISHED` 인 과목만 내려준다
- 응답 항목의 `status` 는 항상 `PUBLISHED` 다
- `DRAFT` 과목은 사용자 앱 과목 목록, 시험 목록, 문제 목록의 진입점으로 사용할 수 없다

## 관리자 규칙

- `/api/admin/subject` 는 `isRemoved = false` 인 과목을 모두 내려준다
- 관리자 응답은 각 과목의 현재 `status` 를 포함한다
- `POST /api/admin/subject`, `PATCH /api/admin/subject/{subjectId}` 에서 `status` 를 지정할 수 있다
- `status` 를 생략하면 기존 호환을 위해 `PUBLISHED` 로 저장한다

## 관리자 요청 예시

```json
{
  "name": "세법",
  "status": "DRAFT"
}
```

## 관리자 응답 예시

```json
[
  {
    "id": 1,
    "name": "회계학",
    "status": "PUBLISHED"
  },
  {
    "id": 2,
    "name": "임시 과목",
    "status": "DRAFT"
  }
]
```

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/subject/controller/SubjectController.java`
- `src/main/java/com/cpa/yusin/quiz/subject/controller/AdminSubjectController.java`
- `src/main/java/com/cpa/yusin/quiz/subject/service/SubjectServiceImpl.java`
- `src/test/java/com/cpa/yusin/quiz/subject/integration/SubjectTest.java`
