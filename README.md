# Municipal Smart Public Beach Safety & Water Quality Monitoring System

A Spring Boot REST backend for managing beach zones, water quality samples, public contacts, operational ledger entries, monitoring views, dashboards, reports, and authenticated user access.

## Problem and objectives

Beach operators need a consistent place to record sample measurements, understand the latest status by zone, and maintain related contacts and financial entries. This backend provides REST endpoints and persistent storage for those records, plus aggregate monitoring and reporting responses.

Project objectives:

- Maintain beach zones and their current green/red flag state.
- Record E. coli and enterococci sample counts and mark samples safe or unsafe on creation.
- Expose latest readings, historical readings, zone status, dashboards, and filtered reports.
- Track permit income and laboratory expenses by zone and contact.
- Authenticate users with JWT and enforce ADMIN, OPERATOR, and VIEWER permissions in the backend.

## Scope

This repository contains the backend only. It does not include a web or mobile frontend. API consumers must use the endpoint contracts documented in [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md). Dashboard presentation and user interface choices belong to a future client; authorization is enforced by this service.

## Technology

- Java 21
- Spring Boot 4.1.1 (from `pom.xml`)
- Spring MVC, Spring Security, Spring Data JPA / Hibernate
- MySQL Connector/J
- JJWT 0.12.6 for JWT handling
- Maven Wrapper
- Integration tests with Spring Boot test support and Spring Security Test

## Architecture

The application follows a layered structure: REST controllers accept requests, services apply domain rules, Spring Data repositories load and persist entities, and MySQL stores the records. A stateless Spring Security filter validates bearer JWTs before protected requests reach controllers. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Project structure

```text
src/main/java/com/example/beach_safety_water_quality/
├── controller/   REST endpoints
├── dto/          Authentication and user request/response objects
├── model/        JPA entities and enums
├── repo/         Spring Data repositories
├── security/     JWT, filter chain, bootstrap admin
└── service/      Domain operations, monitoring, dashboards, reports
src/main/resources/application.properties
src/test/java/com/example/beach_safety_water_quality/
docs/
```

## Setup and run

Requirements: JDK 21, a running MySQL server, and Windows PowerShell for the commands below (the Unix Maven wrapper is also included).

1. Create or allow the configured database `beach_safety_db` on local MySQL.
2. Review `src/main/resources/application.properties`. Configure the datasource username/password, a strong JWT secret, JWT expiration, and bootstrap administrator credentials for your environment. Never deploy the sample development credentials or commit production secrets.
3. From the project directory run:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

The API uses the Spring Boot default port unless overridden by configuration. The current datasource URL targets `localhost:3306`; `createDatabaseIfNotExist=true` asks MySQL to create the database if the configured account has permission. Detailed setup and test notes are in [docs/SETUP_AND_TESTING.md](docs/SETUP_AND_TESTING.md).

## Authentication overview

`POST /api/auth/register` creates an enabled VIEWER account and returns a JWT. `POST /api/auth/login` authenticates an enabled account and returns a JWT. Send protected requests with `Authorization: Bearer <token>`. The bootstrap runner can create the configured ADMIN user if enabled and if that username does not already exist. Role access is detailed in [docs/SECURITY_RBAC.md](docs/SECURITY_RBAC.md).

## Documentation

- [API reference](docs/API_DOCUMENTATION.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Database entities](docs/DATABASE_ENTITIES.md)
- [Authentication and RBAC](docs/SECURITY_RBAC.md)
- [Business rules](docs/BUSINESS_RULES.md)
- [Setup and testing](docs/SETUP_AND_TESTING.md)
- [Project structure](docs/PROJECT_STRUCTURE.md)

## Important implementation notes

- Schema generation is configured as Hibernate `ddl-auto=update`; it updates tables based on entities and is not a versioned migration system.
- Samples are classified using the thresholds implemented in the service: E. coli greater than 235 or enterococci greater than 104 makes a newly created sample unsafe.
- Financial report `from`/`to` parameters are accepted and ordered validation is applied, but ledger entries have no date field, so these parameters cannot filter ledger records. See the API and business-rule docs.
- A zone's current flag is updated on sample creation. Updating or deleting an existing sample does not recalculate the zone flag in the current service implementation.
- API error status can depend on the specific controller/service path; refer to the endpoint notes rather than assuming every exception has the same response shape.
