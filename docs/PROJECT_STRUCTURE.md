# Project Structure

```text
beach_safety_water_quality/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/example/beach_safety_water_quality/
│   │   │   ├── BeachSafetyWaterQualityApplication.java
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── model/
│   │   │   ├── repo/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/application.properties
│   └── test/java/com/example/beach_safety_water_quality/
├── docs/
│   ├── API_DOCUMENTATION.md
│   ├── ARCHITECTURE.md
│   ├── BUSINESS_RULES.md
│   ├── DATABASE_ENTITIES.md
│   ├── PROJECT_STRUCTURE.md
│   ├── SECURITY_RBAC.md
│   └── SETUP_AND_TESTING.md
└── README.md
```

## Package responsibilities

- `controller`: HTTP route declarations and HTTP status mapping.
- `dto`: login/register payloads and user administration payloads/responses.
- `model`: persistent entities plus enum values.
- `repo`: data access via Spring Data JPA.
- `security`: stateless JWT request filter, signing/claims service, route authorization, and startup admin bootstrap.
- `service`: business operations and response aggregation for monitoring, dashboards, and reports.

## Tests

The test package contains application smoke, beach-zone integration, module integration, and security/auth integration tests. Test class names are listed in [SETUP_AND_TESTING.md](SETUP_AND_TESTING.md).
