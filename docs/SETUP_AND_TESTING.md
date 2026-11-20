# Setup, Configuration, and Testing

## Requirements

- JDK 21 (the Maven compiler release is configured from Java 21)
- MySQL available to the application
- Windows PowerShell or another shell for the included Maven wrapper

## Database setup

The current local datasource configuration points to `jdbc:mysql://localhost:3306/beach_safety_db` and includes `createDatabaseIfNotExist=true`. Ensure MySQL is running and the configured account can connect and create/use the database. Properties are in `src/main/resources/application.properties`.

## Configuration reference

| Property | Purpose |
|---|---|
| `spring.datasource.url` | JDBC URL, database name, and driver options |
| `spring.datasource.username` / `spring.datasource.password` | MySQL account |
| `spring.jpa.hibernate.ddl-auto` | Current schema behavior (`update`) |
| `spring.jpa.show-sql` | Whether Hibernate logs SQL (`true` currently) |
| `spring.jpa.open-in-view` | Persistence context through response serialization (`false`) |
| `jwt.secret` | Signing secret used by JWT service |
| `jwt.expiration` | Token lifetime in milliseconds; current value is 86,400,000 (24 hours) |
| `app.admin.bootstrap.enabled` | Whether startup may create the first administrator |
| `app.admin.bootstrap.username` / `.password` | Bootstrap account values |

The checked-in properties currently contain local development credentials and a configured bootstrap account. Before using another environment, supply environment-specific strong values and do not reuse or publish these sample secrets. Bootstrap creates an administrator only if enabled and the username does not already exist.

## Run locally

From the project root in PowerShell:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Use `./mvnw clean test` and `./mvnw spring-boot:run` in a Unix-like shell. The default HTTP port is Spring Boot's standard port unless overridden. Stop the running process with Ctrl+C.

## Automated tests

Run the repository's existing test suite with:

```powershell
.\mvnw.cmd clean test
```

The test sources currently include:

- `BeachSafetyWaterQualityApplicationTests`: application context smoke test.
- `BeachZoneIntegrationTest`: zone endpoint/integration coverage.
- `ModulesIntegrationTest`: integration coverage across core modules.
- `SecurityAuthIntegrationTest`: authentication and role access integration coverage.

Maven reports the test count, failures, errors, and final `BUILD SUCCESS`/`BUILD FAILURE` status. Test count can change as test sources change; use the actual output for the run being reported.

## Manual API smoke test

1. Start the service after MySQL is ready.
2. Log in using the bootstrap admin credentials (if bootstrap is enabled) or register a VIEWER.
3. Copy the returned JWT and send it as `Authorization: Bearer <token>` to protected endpoints.
4. Verify a viewer can read zones and receives `403` for write operations; verify an unauthenticated protected request receives `401`.
5. Use an ADMIN token for user administration and delete operations.

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) and [SECURITY_RBAC.md](SECURITY_RBAC.md) for endpoint and permission details.

## Operational notes

- Hibernate `update` is not a substitute for tracked schema migrations. Back up important data and use an explicit migration process for production schema changes.
- SQL logging is enabled in current properties; review whether that is appropriate for the deployment.
- The application depends on MySQL at runtime. Tests may use test-specific configuration; consult actual test output when the environment cannot reach a production-style database.
