# Admin Authentication

관리자 프론트가 사용하는 인증 계약 문서다.
공식 로그인 요청 필드는 `identifier` 이며, 값으로 이메일 또는 관리자 `loginId` 를 보낼 수 있다.

## POST /api/admin/login

- 인증: 불필요
- 목적: 관리자 이메일 또는 `loginId` 식별자로 JWT access/refresh token 발급

## Request

```json
{
  "identifier": "admin",
  "password": "password"
}
```

### 필드 규칙

- `identifier`
  - 이메일 형식이면 email 기준 조회
  - 이메일 형식이 아니면 `loginId` 기준 조회
  - blank, null, 공백-only 금지
- `password`
  - 관리자 비밀번호

### 배포 전환 메모

- 공식 필드는 `identifier`
- 서버는 rollout 기간 동안 legacy `email` 필드도 임시 허용
- 관리자 프론트는 새 코드부터 반드시 `identifier` 를 사용해야 함

## Success Response

```json
{
  "data": {
    "id": 1,
    "email": "admin@test.com",
    "role": "ADMIN",
    "username": "admin",
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

## Error Contract

- `400 Bad Request`
  - `identifier`, `password` 누락 또는 blank
- `404 Not Found`
  - 존재하지 않는 email/loginId
  - 비밀번호 불일치
  - OAuth2 전용 계정 로그인 시도
- `403 Forbidden`
  - 인증은 성공했지만 `ADMIN` 권한이 아닌 계정
