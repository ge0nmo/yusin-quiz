# Bookmark API Contract

북마크 문제 조회는 `ProblemV2Response` 기반 응답을 사용한다.
즉, 문제 본문 / 해설 / 보기와 함께 `lecture` 객체도 동일하게 포함된다.

## 엔드포인트

- `GET /api/v1/bookmarks/problems?subjectId={optional}&page={page}&size={size}`

## 화면 기준 사용처

- 사용자 북마크 문제 목록
- 특정 과목 필터가 있는 북마크 화면

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

## 프론트 구현 메모

- 북마크 화면은 별도 lecture 타입을 만들지 말고 problem 공통 타입을 재사용해야 함
- `subjectId`는 optional query parameter
- 유튜브 재생은 `lecture.playbackUrl`을 그대로 사용해야 함

## 관련 백엔드 소스

- `src/main/java/com/cpa/yusin/quiz/bookmark/controller/BookmarkController.java`
- `src/main/java/com/cpa/yusin/quiz/bookmark/service/GetBookmarkedProblemsServiceImpl.java`
- `src/test/java/com/cpa/yusin/quiz/bookmark/integration/BookmarkTest.java`
