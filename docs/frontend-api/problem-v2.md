# Problem API Contract

문제 조회와 저장 계약은 integration test에서 문서화하고, 이 문서는 프론트 통합에 필요한 핵심 규칙만 요약한다.

## 엔드포인트

- `GET /api/v1/problem/{problemId}`
- `GET /api/v1/problem?examId={examId}`
- `GET /api/v2/problem?examId={examId}`
- `GET /api/v2/admin/problem?examId={examId}`
- `GET /api/v2/admin/problem/{problemId}`
- `POST /api/v2/admin/problem?examId={examId}`

## 화면 기준 사용처

- 관리자 문제 목록: `GET /api/v2/admin/problem?examId={examId}`
- 관리자 문제 수정 초기 로딩: `GET /api/v2/admin/problem/{problemId}`
- 관리자 문제 생성/수정 저장: `POST /api/v2/admin/problem?examId={examId}`
- 사용자 시험 화면: `GET /api/v2/problem?examId={examId}`
- 레거시 HTML 기반 화면: `GET /api/v1/problem*`

## 핵심 계약 규칙

- `lecture = null` 이면 기존 해설강의 링크를 제거해야 함
- `lecture.youtubeUrl`만 있어도 저장 가능
- `lecture.startTimeSecond`는 nullable
- 응답의 `lecture.youtubeUrl`은 canonical watch URL로 내려감
- 응답의 `lecture.playbackUrl`은 프론트가 그대로 사용해야 함
- 입력 URL은 `youtu.be`, `youtube.com/watch`, `youtube.com/shorts`, `youtube.com/embed`를 허용함
- 입력 URL의 `t`, `start`, `si` 등 부가 쿼리는 저장하지 않음

## 관리자 저장 요청 예제

예제 원본: `docs/frontend-api/examples/problem-v2/save-request.json`

```json
{
  "number": 3,
  "content": [
    {
      "type": "text",
      "tag": "p",
      "content": "문제 본문"
    }
  ],
  "explanation": [
    {
      "type": "text",
      "tag": "p",
      "content": "해설 본문"
    }
  ],
  "lecture": {
    "youtubeUrl": "https://youtu.be/abc123XYZ09?t=430",
    "startTimeSecond": 430
  },
  "choices": []
}
```

## 문제 상세 응답 예제

예제 원본: `docs/frontend-api/examples/problem-v2/detail-response.json`

```json
{
  "data": {
    "id": 31,
    "number": 1,
    "content": [
      {
        "type": "text",
        "tag": "p",
        "content": "문제 본문"
      }
    ],
    "explanation": [
      {
        "type": "text",
        "tag": "p",
        "content": "해설 본문"
      }
    ],
    "lecture": {
      "youtubeUrl": "https://www.youtube.com/watch?v=abc123XYZ09",
      "startTimeSecond": 430,
      "playbackUrl": "https://www.youtube.com/watch?v=abc123XYZ09&t=430s"
    },
    "choices": [
      {
        "id": 41,
        "number": 1,
        "content": "A",
        "isAnswer": true
      }
    ]
  }
}
```

## nullable 규칙

- `lecture` 자체는 nullable
- `lecture = null` 저장 요청은 링크 제거 의미
- `lecture.youtubeUrl`은 `lecture`가 존재할 때 필수
- `lecture.startTimeSecond`는 nullable
- `lecture.startTimeSecond = null`이면 응답의 `lecture.playbackUrl == lecture.youtubeUrl`
- lecture 데이터가 없는 문제 조회 응답은 `lecture = null`

## 대표 오류 케이스

- `400 Bad Request`
  - 유효하지 않은 유튜브 링크
  - video id 추출 실패
  - `lecture.youtubeUrl` 없이 `startTimeSecond`만 전달
  - `startTimeSecond < 0`
- `404 Not Found`
  - 존재하지 않는 `problemId`
  - 존재하지 않는 `examId`
- `409 Conflict`
  - 동일 시험 내 문제 번호 중복

예제 원본: `docs/frontend-api/examples/problem-v2/validation-error.json`

```json
{
  "status": 400,
  "message": "유효한 유튜브 해설 링크를 입력해야 합니다."
}
```

## 프론트 구현 메모

- 저장 화면에서는 빈 문자열을 그대로 보내지 말고 `lecture = null` 또는 `lecture.youtubeUrl`을 명시적으로 구성해야 함
- 유튜브 버튼/링크는 `lecture.playbackUrl`을 그대로 써야 함
- 편집 화면에 canonical URL이 다시 표시될 수 있으므로, 입력 URL 원문 보존을 기대하면 안 됨
- V1 / V2 / 북마크가 모두 같은 `lecture` shape를 쓰므로 공통 타입으로 관리해야 함

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/problem/controller/ProblemController.java`
- `src/main/java/com/cpa/yusin/quiz/problem/controller/ProblemV2Controller.java`
- `src/main/java/com/cpa/yusin/quiz/problem/controller/AdminProblemV2Controller.java`
- `src/main/java/com/cpa/yusin/quiz/problem/controller/dto/request/ProblemSaveV2Request.java`
- `src/main/java/com/cpa/yusin/quiz/problem/controller/dto/response/ProblemLectureResponse.java`
- `src/main/java/com/cpa/yusin/quiz/problem/service/CreateProblemV2ServiceImpl.java`
- `src/main/java/com/cpa/yusin/quiz/problem/service/GetProblemV2ServiceImpl.java`
- `src/main/java/com/cpa/yusin/quiz/problem/service/YoutubeLectureUrlProcessor.java`
- `src/test/java/com/cpa/yusin/quiz/problem/integration/ProblemTest.java`
