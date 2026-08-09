# Google Login Error Contract

Google 로그인 엔드포인트는 `POST /api/v1/auth/login/google` 이다. 성공 응답 계약은
기존과 같고, 실패 응답에는 모바일이 분기할 수 있는 안정적인 `code` 가 포함된다.

## 요청

```json
{
  "idToken": "google-id-token"
}
```

## 실패 코드

| HTTP | code | 의미 |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | `idToken` 이 비어 있음 |
| 400 | `INVALID_SOCIAL_PROFILE` | 검증된 토큰에 이메일이 없거나, 이메일이 인증되지 않았거나, 이메일 형식이 잘못됨 |
| 401 | `INVALID_SOCIAL_TOKEN` | 토큰이 잘못되었거나 만료되었거나 파싱·서명 검증에 실패함 |

```json
{
  "status": 401,
  "message": "유효하지 않거나 만료된 소셜 로그인 토큰입니다.",
  "code": "INVALID_SOCIAL_TOKEN"
}
```

토큰과 검증 라이브러리의 상세 오류는 응답 및 서버 로그에 포함하지 않는다. 검증 오류 외의
예상하지 못한 서버 오류는 `500 INTERNAL_SERVER_ERROR` 와 일반화된 메시지로 반환한다.

## 공통 오류 응답

- `CustomException`의 `code`는 `ExceptionMessage` enum 이름이다.
- 요청 검증 실패의 `code`는 `VALIDATION_ERROR`다.
- 그 밖의 공통 오류는 HTTP 상태 이름을 기본 코드로 사용한다.
- 비밀번호·token·secret 필드의 거부 값은 `valueErrors.rejectedValue`에서 `[REDACTED]`로 표시한다.
