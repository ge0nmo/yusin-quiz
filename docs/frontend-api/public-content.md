# Public Content API

모든 API는 로그인 없이 호출하며 `{code}`가 가리키는 공개 자격시험 범위 안에서만 데이터를 반환한다.

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
