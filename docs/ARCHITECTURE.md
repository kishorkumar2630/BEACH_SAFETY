# Architecture

## Overview

The application is a Java 21 Spring Boot REST service. Its main request path is:

```text
HTTP client
  -> Spring Security filter chain (JWT authentication / role rules)
  -> REST controller
  -> service interface and implementation (domain behavior)
  -> Spring Data JPA repository
  -> Hibernate / MySQL
```

Controllers are thin adapters that map HTTP methods and paths to service calls. Services apply business rules and coordinate repositories. Entities are persisted by Spring Data JPA. Response DTOs are used for authentication and user administration; domain resources are generally serialized as entities.

## Packages

- `controller`: authentication, users, zones, samples, contacts, ledger, monitoring, dashboard, and reports.
- `dto`: login/register requests, auth response, and user create/update/response objects.
- `model`: JPA entities (`BeachZone`, `WaterSample`, `Contact`, `LedgerEntry`, `User`) and enums.
- `repo`: Spring Data repositories and filtered/aggregate queries.
- `security`: stateless security configuration, JWT service/filter, and optional admin bootstrap.
- `service`: CRUD/domain operations, monitoring, dashboard aggregation, water-quality and financial reports.

## Security boundary

Spring Security disables CSRF for this stateless API, uses stateless sessions, and installs `JwtAuthenticationFilter` before the username/password filter. Public access is limited to registration and login. Other requests are checked by path and HTTP method against roles; unmatched paths still require authentication. Passwords are encoded with BCrypt. See [SECURITY_RBAC.md](SECURITY_RBAC.md).

## Data and persistence

Spring Data JPA maps entities to MySQL. The configured Hibernate mode is `update`; this can create/update schema based on entity mappings but does not provide audited, ordered schema migrations. `spring.jpa.open-in-view=false` avoids keeping the persistence context open through response serialization. Query and connection settings are in `src/main/resources/application.properties`.

## Cross-entity references

The current model stores IDs in scalar fields (`WaterSample.zoneId`, `LedgerEntry.zoneId`, `LedgerEntry.contactId`) rather than declaring JPA object relationships. Services verify zone/contact existence for relevant create/update operations. Referential behavior therefore comes from service validation and database column constraints, not ORM cascades.

## Reporting components

`MonitoringController` composes latest readings and history. `DashboardController` delegates aggregate response construction to `DashboardService`. `ReportController` delegates filtering and aggregation to water-quality and financial report services. A ledger row has no timestamp; consequently date filters cannot be applied to ledger reports in this implementation.

## Runtime components

The application entry point is `BeachSafetyWaterQualityApplication`. `AdminBootstrapRunner` optionally inserts an initial ADMIN user at startup if enabled and if its configured username is absent. Existing users are not overwritten by bootstrap.

## Constraints and extension points

There is no frontend, API schema generator, centralized API versioning, or migration framework visible in the current project configuration. No per-user beach-zone assignment is modeled; authenticated roles access shared data subject to route permissions. Keep API contract/security changes in the relevant controllers and `SecurityConfig`, and update these docs when the implementation changes.
