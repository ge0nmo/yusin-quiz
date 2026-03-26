# Security and Auth

## Purpose

- Define authentication, authorization, JWT, and CORS behavior.
- Prevent incorrect assumptions about public endpoints and failure semantics.

## Read This When

- The task changes login or refresh flows.
- The task changes Google login behavior.
- The task changes permit-all rules, role checks, or admin access.
- The task changes JWT parsing, token creation, or CORS settings.

## Invariants

- Security is stateless for user and admin APIs.
- `/api/admin/login` is the only public admin login endpoint.
- `/api/v2/admin/problem` is protected by the admin security chain.
- JWT subject is the member email.
- Allowed CORS origins come from `app.security.cors.allowed-origins`.
- Do not combine credentialed CORS with wildcard origins.

## Current Implementation

- User API security chain covers `/api/v1/**`.
- Admin security chain covers `/api/admin/**` and `/api/v2/admin/**`.
- User API rules:
  - `/api/v1/auth/**` is public
  - `GET /api/v1/**` is generally public
  - `/api/v1/bookmarks/**` requires authentication
  - other `/api/v1/**` requests require authentication
- Admin API rules:
  - `/api/admin/login` is public
  - other admin endpoints require `ROLE_ADMIN`
- `SecurityFilter` reads JWT from:
  - `Authorization: Bearer ...`
  - `JWT_TOKEN` cookie
- User protected API unauthenticated failures return `401 SecurityErrorResponse`.
- Admin unauthenticated failures currently resolve to Spring Security `403`.
- Authenticated-but-forbidden requests remain `403`.
- `AuthenticationServiceImpl.loginAsAdmin` reuses normal login and then enforces `Role.ADMIN`.
- Social login flow is:
  - `SocialLoadService`
  - `GoogleTokenVerifier`
  - `AuthenticationServiceImpl.socialLogin`
- New social users are auto-created with a generated nickname and generated password.
- Refresh flow validates refresh-token type and expiry, then issues a new access token and refresh token.

## Decision Rules

- When changing permit-all rules, compare `SecurityConfig` with real controller mappings before editing.
- When changing token behavior, inspect `SecurityFilter`, `JwtServiceImpl`, and `AuthenticationServiceImpl` together.
- When changing admin access, check both `/api/admin/**` and `/api/v2/admin/**`.
- Do not assume user and admin unauthenticated failures have the same response format.

## Change Checklist

- Update security-focused tests.
- Update frontend auth docs if login or token behavior changed.
- Update CORS tests if allowed-origin behavior changed.
- Update the relevant `.agents/*.md` file if public endpoint or auth semantics changed.

## Verification

- Run `SecurityFilterTest`.
- Run `AdminApiSecurityIntegrationTest`.
- Run relevant member auth tests, especially around login and refresh behavior.

## Related Docs

- `architecture.md`
- `api-contracts.md`
- `testing-and-docs.md`
- `docs/frontend-api/admin-auth.md`
