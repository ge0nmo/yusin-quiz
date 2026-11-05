# Security and Auth

- Public mobile content APIs are stateless and permit all requests.
- `POST /api/admin/login`, `/api/admin/refresh`, and `/api/admin/logout` are public.
- Every other `/api/admin/**` endpoint requires `ROLE_ADMIN`.
- Admin login request is `{loginId,password}`; email and Google login are not supported.
- JWT subject is immutable `Member.loginId` and the token also carries `memberId` and `tokenType`.
- Admin refresh accepts a refresh token in the request body, validates token type/expiry/account/ADMIN role, and rotates both tokens.
- Access tokens are accepted through `Authorization: Bearer` or the httpOnly `JWT_TOKEN` cookie.
- Allowed CORS origins come from `app.security.cors.allowed-origins`; credentialed wildcard CORS is forbidden.
- The first admin is created only when none exists and both `ADMIN_BOOTSTRAP_LOGIN_ID` and `ADMIN_BOOTSTRAP_PASSWORD` are set.
- Never log passwords or JWT values.
