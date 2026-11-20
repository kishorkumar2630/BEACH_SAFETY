# Database Entities

Persistence uses Spring Data JPA/Hibernate with MySQL. Table names below are explicit where declared; the `users` table name is explicit. Primary keys are generated identity `Long` values. Enums are persisted as strings. Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`.

## `beach_zones`

| Field | Mapping | Notes |
|---|---|---|
| `id` | BIGINT identity, primary key | Generated |
| `name` | String, not null | Zone display name |
| `current_flag_status` | Enum string, not null | `GREEN` or `RED`; defaults GREEN in entity |
| `analytic_account_code` | String, not null, unique | Zone's analytic account code |

## `water_samples`

| Field | Mapping | Notes |
|---|---|---|
| `id` | BIGINT identity, primary key | Generated |
| `zone_id` | BIGINT, not null | Scalar reference to beach zone |
| `enterococci_count` | Integer, not null | Measured count |
| `ecoli_count` | Integer, not null | Measured count |
| `sampled_at` | LocalDateTime, not null | Sample time |
| `is_safe` | Boolean, not null | Safe classification |

The entity does not declare a JPA relationship to `BeachZone`; service logic validates the zone on sample creation. Sample update copies the supplied zone ID and measurements/classification but does not recalculate zone flag state.

## `contacts`

| Field | Mapping | Notes |
|---|---|---|
| `id` | BIGINT identity, primary key | Generated |
| `name` | String, not null | Contact name |
| `type` | Enum string, not null | `VENDOR`, `CUSTOMER`, or `INTERNAL` |

## `ledger_entries`

| Field | Mapping | Notes |
|---|---|---|
| `id` | BIGINT identity, primary key | Generated |
| `zone_id` | BIGINT, not null | Scalar reference to beach zone |
| `contact_id` | BIGINT, not null | Scalar reference to contact |
| `amount` | DECIMAL(12,2), not null | Monetary value |
| `type` | Enum string, not null | `INCOME_PERMIT` or `EXPENSE_LAB` |
| `description` | String, nullable | Free-form detail |

There is no transaction date/time column. Current code does not declare JPA relationships or explicit foreign-key mappings for these scalar IDs.

## `users`

| Field | Mapping | Notes |
|---|---|---|
| `id` | BIGINT identity, primary key | Generated |
| `username` | String, not null, unique | Login identifier |
| `password` | String, not null | BCrypt-encoded password hash |
| `role` | Enum string, not null | `ADMIN`, `OPERATOR`, or `VIEWER` |
| `enabled` | Boolean, not null | Defaults true |
| `created_at` | LocalDateTime, not null, not updated | Initialized before first insert |

The user entity is not exposed directly by user-management responses; `UserResponse` excludes the stored password.

## Enum values

- `FlagStatus`: `GREEN`, `RED`
- `ContactType`: `VENDOR`, `CUSTOMER`, `INTERNAL`
- `LedgerEntryType`: `INCOME_PERMIT`, `EXPENSE_LAB`
- `Role`: `ADMIN`, `OPERATOR`, `VIEWER`

## Persistence and operational notes

- Datasource URL is configured for `localhost:3306/beach_safety_db` with database creation requested if absent.
- The configured account must have sufficient database/schema privileges.
- `ddl-auto=update` is convenient for this project but does not replace reviewed production migrations or backups.
- `show-sql=true` logs SQL statements; assess logging policy and operational data exposure before production use.
- Database credentials in properties are local development values and should be overridden outside local development.
