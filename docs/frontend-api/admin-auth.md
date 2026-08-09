# Admin Authentication

## POST /api/admin/login

인증 없이 호출한다.

```json
{
  "loginId": "admin",
  "password": "password"
}
```

성공 시 access/refresh token을 응답하고 `JWT_TOKEN`, `REFRESH_TOKEN` httpOnly 쿠키도 설정한다.

```json
{
  "data": {
    "id": 1,
    "loginId": "admin",
    "role": "ADMIN",
    "accessToken": "eyJ...",
    "refreshToken": "eyJ..."
  }
}
```

`POST /api/admin/logout`은 두 쿠키를 만료시키고 `204 No Content`를 반환한다. 그 외 `/api/admin/**`는 `ROLE_ADMIN`이 필요하다.

`POST /api/admin/refresh`는 `{ "refreshToken": "eyJ..." }`를 받아 ADMIN 계정과 refresh token 종류·만료를 검증하고 `{data:{accessToken,refreshToken}}` 및 새 쿠키를 반환한다. 기존 사용자용 `/api/v1/auth/refresh`는 없다.

최초 관리자는 ADMIN 계정이 하나도 없을 때만 `ADMIN_BOOTSTRAP_LOGIN_ID`, `ADMIN_BOOTSTRAP_PASSWORD` 환경값으로 생성된다.
