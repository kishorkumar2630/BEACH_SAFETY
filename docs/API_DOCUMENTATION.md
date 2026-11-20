# API Documentation

Base path: `/api`. The service does not configure a separate API version prefix. JSON is used for request and response bodies unless stated otherwise. IDs are generated numeric identifiers. Most protected endpoints require `Authorization: Bearer <JWT>`.

## Authentication and authorization behavior

- `POST /api/auth/register` and `POST /api/auth/login` are public.
- Every other request requires authentication unless permitted by a more specific rule.
- Missing/invalid authentication normally yields `401`; authenticated users without sufficient role normally receive `403`.
- Role rules are defined in [SECURITY_RBAC.md](SECURITY_RBAC.md).

## Authentication

### Register

`POST /api/auth/register` — public; returns `201 Created`.

Request:

```json
{"username":"viewer1","password":"change-me"}
```

Creates a VIEWER account with an encoded password and enabled status. Duplicate/blank username and blank password are rejected with `400`. Response contains `token`, `username`, and `role`.

### Login

`POST /api/auth/login` — public; returns `200 OK` on success.

Request:

```json
{"username":"admin_sys","password":"configured-password"}
```

Response:

```json
{"token":"<jwt>","username":"admin_sys","role":"ADMIN"}
```

Invalid credentials or disabled users are rejected with `401`.

## User management (ADMIN)

All routes use `/api/users` and require ADMIN.

| Method | Path | Result |
|---|---|---|
| GET | `/api/users` | `200`, list of user responses |
| GET | `/api/users/{id}` | `200`, one user; missing ID is `404` |
| POST | `/api/users` | `201`, create user |
| PUT | `/api/users/{id}` | `200`, update role, enabled flag, and/or password |
| DELETE | `/api/users/{id}` | `204`; deleting the current authenticated user is rejected with `400` |

Create body: `{"username":"operator1","password":"change-me","role":"OPERATOR","enabled":true}`. `role` and `enabled` may be omitted (defaults VIEWER and true). Update body accepts optional `role`, `enabled`, and `password`. Responses omit password and include user ID, username, role, enabled, and creation time.

## Beach zones

Base path `/api/beach/zones`. Request/response entity fields: `id`, `name`, `currentFlagStatus` (`GREEN` or `RED`), and `analyticAccountCode`.

| Method | Path | Result |
|---|---|---|
| POST | `/api/beach/zones/create` | `201`, create zone |
| GET | `/api/beach/zones/getAll` | `200`, list |
| GET | `/api/beach/zones/get/{id}` | `200`, zone |
| PUT | `/api/beach/zones/update/{id}` | `200`, updated zone |
| DELETE | `/api/beach/zones/delete/{id}` | `204` |

Create/update JSON example: `{"name":"North Beach","currentFlagStatus":"GREEN","analyticAccountCode":"NB-001"}`. Account code is unique and required by the entity.

## Water samples

Base path `/api/beach/samples`. Fields: `id`, `zoneId`, `enterococciCount`, `ecoliCount`, `sampledAt` (ISO local date-time), `isSafe`.

| Method | Path | Result |
|---|---|---|
| POST | `/api/beach/samples/create` | `201`, create sample and update zone flag |
| GET | `/api/beach/samples/getAll` | `200`, list |
| GET | `/api/beach/samples/get/{id}` | `200`, sample |
| PUT | `/api/beach/samples/update/{id}` | `200`, update sample |
| DELETE | `/api/beach/samples/delete/{id}` | `204` |

Example: `{"zoneId":1,"enterococciCount":40,"ecoliCount":80,"sampledAt":"2026-09-25T09:30:00","isSafe":true}`. The create service sets `isSafe` based on the thresholds; callers should treat that server result as authoritative. Both counts and sampled time are required by the entity.

## Contacts

Base path `/api/contacts`. Fields: `id`, `name`, `type` (`VENDOR`, `CUSTOMER`, `INTERNAL`).

| Method | Path | Result |
|---|---|---|
| POST | `/api/contacts/create` | `201` |
| GET | `/api/contacts/getAll` | `200` |
| GET | `/api/contacts/get/{id}` | `200` |
| PUT | `/api/contacts/update/{id}` | `200` |
| DELETE | `/api/contacts/delete/{id}` | `204` |

Example body: `{"name":"Regional Water Lab","type":"VENDOR"}`.

## Ledger

Base path `/api/ledger`. Fields: `id`, `zoneId`, `contactId`, `amount` (decimal), `type` (`INCOME_PERMIT` or `EXPENSE_LAB`), and optional `description`.

| Method | Path | Result |
|---|---|---|
| POST | `/api/ledger/create` | `201` |
| GET | `/api/ledger/getAll` | `200` |
| GET | `/api/ledger/get/{id}` | `200` |
| PUT | `/api/ledger/update/{id}` | `200` |
| DELETE | `/api/ledger/delete/{id}` | `204` |
| GET | `/api/ledger/summary` | `200`, total income, expense, and net balance |
| GET | `/api/ledger/summary/zone/{zoneId}` | `200`, same summary for one zone |

Create/update example: `{"zoneId":1,"contactId":2,"amount":1250.00,"type":"INCOME_PERMIT","description":"Permit fee"}`. A valid existing zone and contact are required. Summary totals use `INCOME_PERMIT` as income and `EXPENSE_LAB` as expense; net balance is income minus expense.

## Monitoring

All routes are GET and require authentication. Base path `/api/beach/monitoring`.

| Path | Result |
|---|---|
| `/latest/{zoneId}` | Latest sample by sampled time (ID breaks ties); `404` if zone or sample is absent |
| `/status/{zoneId}` | Zone current flag and latest sample fields; latest fields are null if no sample exists |
| `/history/{zoneId}` | Samples for zone in descending sampled-time order |
| `/zone/{zoneId}` | Zone ID/name/account code/current flag, latest sample (nullable), and history |

## Dashboard

Authenticated GET endpoints, base path `/api/beach/dashboard`.

| Path | Result |
|---|---|
| `/api/beach/dashboard` | Overall dashboard response |
| `/api/beach/dashboard/zone/{zoneId}` | Dashboard response for a zone |

Response fields follow `OverallDashboardResponse` and `ZoneDashboardResponse` in the service package; dashboards are aggregate summaries and do not enforce per-user zone assignments.

## Reports

Authenticated GET endpoints, base path `/api/beach/reports`.

| Path | Optional query parameters | Result |
|---|---|---|
| `/samples` | `zoneId`, `from`, `to`, `safe` | Matching sample list |
| `/water-quality` | `zoneId`, `from`, `to` | Count and average measurement statistics |
| `/financial` | `zoneId`, `from`, `to`, `type` | Totals and matching entry count |
| `/ledger` | `zoneId`, `contactId`, `type` | Matching ledger entries |

`from`/`to` use ISO date-time, for example `2026-09-01T00:00:00`. `safe` is Boolean. `type` is `INCOME_PERMIT` or `EXPENSE_LAB`. Sample report date/zone filters are applied. Financial report date arguments are validated (`from` must not be later than `to`) but cannot filter entries because ledger entities do not store dates. Invalid date order returns `400`; unknown referenced zone/contact IDs are rejected.

## Common status codes

`200 OK` successful reads/updates/login; `201 Created` successful creates/register; `204 No Content` successful deletions; `400 Bad Request` invalid request or domain validation; `401 Unauthorized` missing/invalid token or failed login; `403 Forbidden` authenticated but insufficient role; `404 Not Found` missing resource in service paths that map absence to 404. Error body formats are not uniform across all controllers.
