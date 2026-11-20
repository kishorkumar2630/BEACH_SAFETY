# Authentication and Role-Based Access Control

## Authentication flow

1. A user registers through `POST /api/auth/register`; public self-registration always assigns `VIEWER`.
2. An administrator may create users and assign a role through `/api/users`.
3. Login checks username, enabled status, and BCrypt password match.
4. Successful register/login returns a signed JWT, username, and role.
5. Protected calls send `Authorization: Bearer <token>`. `JwtAuthenticationFilter` extracts and validates the token and populates the request security context.

The application is stateless: server-side HTTP sessions are not used. JWT lifetime comes from `jwt.expiration` in milliseconds. The signing secret is supplied by `jwt.secret`.

## Roles and permissions

Rules below reflect the path and HTTP method matchers in `SecurityConfig`.

| Resource group | ADMIN | OPERATOR | VIEWER |
|---|---:|---:|---:|
| Register/login | Public | Public | Public |
| `/api/users/**` | All methods | No | No |
| Zone GET | Yes | Yes | Yes |
| Zone POST/PUT | Yes | Yes | No |
| Zone DELETE | Yes | No | No |
| Sample GET | Yes | Yes | Yes |
| Sample POST/PUT | Yes | Yes | No |
| Sample DELETE | Yes | No | No |
| Contact GET | Yes | Yes | Yes |
| Contact POST/PUT | Yes | Yes | No |
| Contact DELETE | Yes | No | No |
| Ledger GET (including summaries) | Yes | Yes | Yes |
| Ledger POST/PUT | Yes | Yes | No |
| Ledger DELETE | Yes | No | No |
| Monitoring GET | Yes | Yes | Yes |
| Dashboard GET | Yes | Yes | Yes |
| Reports GET | Yes | Yes | Yes |
| Other unmatched paths | Authenticated | Authenticated | Authenticated |

This is role-based access to shared application data. There is no user-to-zone assignment or per-user record isolation in the current data model.

## Password and account handling

- Passwords are stored as BCrypt hashes via Spring Security's `PasswordEncoder`.
- Public registration creates enabled VIEWER users only.
- ADMIN can create users; omitted role defaults to VIEWER and omitted enabled flag defaults true.
- ADMIN can update role, enabled state, and password.
- Disabled users cannot log in.
- The currently authenticated user cannot delete their own account through the user delete endpoint.

## Bootstrap administrator

`AdminBootstrapRunner` is controlled by `app.admin.bootstrap.enabled`. If enabled, startup creates an ADMIN with configured username/password only when that username does not already exist. The password is BCrypt encoded. It skips rather than resets an existing account. Configure a unique, strong bootstrap password and turn bootstrap off when it is no longer needed.

## HTTP outcomes

- `401 Unauthorized`: missing/invalid bearer token, failed login, or disabled account.
- `403 Forbidden`: authenticated request does not satisfy the role rule.
- Registration is public and creates a VIEWER, so a new public account cannot self-assign elevated privileges.

CSRF is disabled because the API is stateless and uses bearer tokens. Do not store bearer tokens in insecure client storage. Keep the JWT secret private, sufficiently strong, and different across environments. Rotate credentials/secrets using a deployment plan that accounts for invalidating existing tokens.

## Current implementation limits

Authorization is applied by path and method in the security filter chain; no additional per-zone ownership checks are implemented. JWT claims carry the authenticated identity/role as created by the current token service. Existing issued tokens may remain usable until expiry after account/role state changes, subject to token validation behavior; re-login is not equivalent to immediate revocation. These are implementation characteristics, not configurable RBAC features.
