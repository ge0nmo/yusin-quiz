# Bookmark API Contract

북마크 문제 조회는 `ProblemV2Response` 기반 응답을 사용한다.
즉, 문제 본문 / 해설 / 보기와 함께 `lecture` 객체도 동일하게 포함된다.

## 엔드포인트

- `GET /api/v1/bookmarks/problems?subjectId={optional}&page={page}&size={size}`
- `POST /api/v1/bookmarks/status`

## 화면 기준 사용처

- 사용자 북마크 문제 목록
- 특정 과목 필터가 있는 북마크 화면
- 로그인 직후 또는 현재 화면 문제 목록의 북마크 상태 동기화

## 응답 규칙

- 응답은 `GlobalResponse<SliceResponse<ProblemV2Response>>` 형태
- `data.content[]` 각 항목은 `ProblemV2Response`
- 각 항목은 `lecture.youtubeUrl`, `lecture.startTimeSecond`, `lecture.playbackUrl`를 포함할 수 있음
- `lecture = null`이면 해설강의가 연결되지 않은 문제
- `currentPage`, `size`, `hasNext`로 무한 스크롤 또는 다음 페이지 버튼 구성 가능

## 응답 예제

```json
{
  "data": {
    "content": [
      {
        "id": 1,
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
            "id": 1,
            "number": 1,
            "content": "보기 1",
            "isAnswer": true
          }
        ]
      }
    ],
    "currentPage": 0,
    "size": 20,
    "hasNext": false
  }
}
```

## 북마크 상태 조회 계약

- 요청: `POST /api/v1/bookmarks/status`
- 인증: 필수
- 목적: 현재 로그인 사용자가 전달한 `problemIds` 중 어떤 문제를 북마크했는지 ID만 빠르게 확인
- 응답: `GlobalResponse<{ bookmarkedIds: Long[] }>`
- 서버는 입력 `problemIds`를 dedupe하며, 응답 `bookmarkedIds`는 가능한 한 입력 순서를 유지한다
- 존재하지 않는 문제 ID는 조용히 무시한다
- 이 API는 목록 조회가 아니므로 페이지네이션, 과목 필터, 문제 payload를 반환하지 않는다

### Request JSON

```json
{
  "problemIds": [104, 999, 104, 101]
}
```

### Success Response JSON

```json
{
  "data": {
    "bookmarkedIds": [104, 101]
  }
}
```

### Validation Error Examples

`problemIds` 누락:

```json
{
  "status": 400,
  "message": "Bad Request",
  "valueErrors": [
    {
      "descriptor": "problemIds",
      "rejectedValue": "",
      "reason": "problemIds는 필수입니다"
    }
  ]
}
```

`null` 원소 포함:

```json
{
  "status": 400,
  "message": "Bad Request",
  "valueErrors": [
    {
      "descriptor": "problemIds[1]",
      "rejectedValue": "",
      "reason": "problemIds에는 null을 포함할 수 없습니다"
    }
  ]
}
```

500개 초과:

```json
{
  "status": 400,
  "message": "Bad Request",
  "valueErrors": [
    {
      "descriptor": "problemIds",
      "rejectedValue": "[...]",
      "reason": "problemIds는 최대 500개까지 허용합니다"
    }
  ]
}
```

### Security Error Example

비인증 요청은 `SecurityErrorResponse` 형식을 사용한다.

```json
{
  "status": 401,
  "code": "AUTH_REQUIRED",
  "message": "로그인이 필요합니다.",
  "path": "/api/v1/bookmarks/status"
}
```

## 프론트 구현 메모

- 북마크 화면은 별도 lecture 타입을 만들지 말고 problem 공통 타입을 재사용해야 함
- `subjectId`는 optional query parameter
- 유튜브 재생은 `lecture.playbackUrl`을 그대로 사용해야 함
- 북마크 탭의 목록 조회는 계속 `GET /api/v1/bookmarks/problems`를 사용하고, 문제 카드별 북마크 배지는 `POST /api/v1/bookmarks/status`로 분리해서 사용해야 함

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/bookmark/controller/BookmarkController.java`
- `src/main/java/com/cpa/yusin/quiz/bookmark/service/GetBookmarkStatusServiceImpl.java`
- `src/main/java/com/cpa/yusin/quiz/bookmark/service/GetBookmarkedProblemsServiceImpl.java`
- `src/test/java/com/cpa/yusin/quiz/bookmark/integration/BookmarkTest.java`
