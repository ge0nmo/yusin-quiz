# Public Content API

모든 API(v1, v2)는 로그인 없이 호출하며 `{code}`가 가리키는 공개 자격시험 범위 안에서만 데이터를 반환한다.

## GET /api/v1/qualification-exams/{code}/app-version-policy?platform=android|ios

대상 앱과 플랫폼의 최신·최소 지원 버전, 스토어 URL을 반환한다. 강제 업데이트 여부는 설치 버전이 `minimumVersion`보다 낮은지 semantic version으로 비교해 결정한다. 정책은 서버 환경변수로 관리한다.

```json
{"data":{"latestVersion":"2.0.0","minimumVersion":"2.0.0","storeUrl":"https://play.google.com/store/apps/details?id=com.yusin.quiz"}}
```

미출시 플랫폼은 `minimumVersion`을 `0.0.0`으로 두고 빈 `storeUrl`을 반환해 강제 업데이트를 비활성화한다.

## GET /api/v1/qualification-exams/{code}/subjects

연결 순서대로 공개 과목과 실제 공개 문제 수를 반환한다.

```json
{"data":[{"id":1,"name":"회계학","problemCount":120}]}
```

## GET /api/v1/qualification-exams/{code}/subjects/{subjectId}/problems

최신 시험 연도 우선, 같은 연도는 원본 문제 번호 오름차순이다. `content`는 JSON block 배열이다. 정답 플래그와 모든 해설은 포함하지 않는다.

문제 본문에는 일반 블록과 함께 `{type:"statementGroup",items:[{label,content}]}`가 중첩될 수 있다. `label`은 `(가)`, `ㄱ.`, `A)`처럼 그대로 표시할 문자열이고, 각 `content`도 JSON block 배열이다.

```json
{"data":[{"id":10,"number":41,"content":[{"type":"statementGroup","items":[{"label":"(가)","content":[{"type":"text","spans":[{"text":"보고기간말 이전에"}]}]}]}],"exam":{"id":3,"name":"2025년 1차","year":2025},"choices":[{"id":101,"number":1,"content":"보기"}]}]}
```

## POST /api/v1/qualification-exams/{code}/problems/{problemId}/check

요청은 `{"selectedChoiceId":101}`, 응답은 `{"data":{"correct":true}}`다. 정답 ID는 반환하지 않는다.

## POST /api/v1/qualification-exams/{code}/solutions

서로 다른 문제 ID를 최대 5개 전달한다.

```json
{"problemIds":[10,11]}
```

응답은 입력 순서를 유지하며 정답과 문제/보기별 해설을 반환한다. 미등록 해설은 빈 배열이다.

```json
{"data":[{"problemId":10,"correctChoiceId":102,"explanation":[],"choices":[{"choiceId":101,"explanation":[]}]}]}
```

## v2 공통

v2는 앱이 시작할 때마다 전체 스냅샷을 확인하고 기기에서 채점하도록 만든 API다. v1은 이미 설치된 앱을 위해 그대로 둔다.

- 응답 헤더 `ETag`는 `data.version`을 따옴표로 감싼 값이고, `Cache-Control`은 `no-cache`다.
- 저장해 둔 `ETag`를 `If-None-Match`로 보내 값이 같으면 본문 없이 `304 Not Modified`를 반환한다.
- 콘텐츠와 해설은 version을 따로 계산한다. 해설만 바뀌면 콘텐츠는 `304`로 끝난다.
- 관리 이미지 URL은 요청마다 새로 서명되지만 version에는 영향을 주지 않는다. 응답에 관리 이미지가 있으면 `imageUrlsExpireAt`(UTC ISO-8601)을 내려주고, 없으면 `null`이다. 이 시각이 지나면 `If-None-Match` 없이 다시 받는다.
- 모르는 코드나 비공개 자격시험은 `404 QUALIFICATION_EXAM_NOT_FOUND`다.

## GET /api/v2/qualification-exams/{code}/content

공개 과목 전체와 각 과목의 문제, 보기, 정답 ID를 반환한다. 과목 순서와 문제 순서는 v1과 같다. 공개 문제가 없는 공개 과목도 빈 `problems`로 포함한다. 해설 필드는 없다. 정답은 `correctChoiceIds` 하나로만 표현하고 보기에 정답 플래그를 두지 않는다.

```json
{"data":{"qualificationExamCode":"APPRAISER","version":"sha256:9f2c…","generatedAt":"2026-11-01T09:00:00Z","imageUrlsExpireAt":null,"subjects":[{"id":1,"name":"회계학","problemCount":1,"problems":[{"id":10,"number":41,"exam":{"id":3,"name":"2025년 1차","year":2025},"content":[{"type":"text","spans":[{"text":"…"}]}],"correctChoiceIds":[102],"choices":[{"id":101,"number":1,"content":"보기"}]}]}]}}
```

## GET /api/v2/qualification-exams/{code}/explanations

공개 문제 전체의 문제 해설과 보기별 해설을 `problemId` 오름차순으로 반환한다. 해설이 없으면 빈 배열이다. 정답 ID는 포함하지 않는다.

```json
{"data":{"qualificationExamCode":"APPRAISER","version":"sha256:41ab…","generatedAt":"2026-11-01T09:00:01Z","imageUrlsExpireAt":null,"problems":[{"problemId":10,"explanation":[],"choices":[{"choiceId":101,"explanation":[]}]}]}}
```

- 향후 이용권 확인을 붙일 엔드포인트다. 지금은 로그인 없이 호출한다. 앱은 `401` 또는 `403`을 받으면 해설만 빼고 나머지를 정상 동작시켜야 한다.
- 앱은 두 응답을 `problemId`로 합친다. 사라진 문제 정리는 콘텐츠 응답만 기준으로 하고, 해설 응답에만 있는 문제는 버린다.
