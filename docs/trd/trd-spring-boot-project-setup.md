# TRD – Spring Boot Project Setup: Car Rental System

## Document Information

| Field | Details |
|---|---|
| **Feature Name** | Spring Boot Project Setup |
| **Module** | Platform / Foundation |
| **Author** | @copilot |
| **Date** | 2026-03-07 |
| **Version** | 1.0 |

---

## Table of Contents

1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Maven Dependency Design](#maven-dependency-design)
   - [Core Dependencies](#core-dependencies)
   - [Database and Persistence](#database-and-persistence)
   - [Security](#security)
   - [Testing](#testing)
   - [Utilities](#utilities)
5. [Layered Architecture](#layered-architecture)
   - [Controller Layer](#controller-layer)
   - [Service Layer](#service-layer)
   - [Repository Layer](#repository-layer)
   - [DTO Design](#dto-design)
6. [Configuration Design](#configuration-design)
   - [@ConfigurationProperties Approach](#configurationproperties-approach)
   - [Environment Variable Strategy](#environment-variable-strategy)
   - [Application Properties Structure](#application-properties-structure)
7. [Exception Handling Design](#exception-handling-design)
   - [Global Error Handler](#global-error-handler)
   - [Error Response Schema](#error-response-schema)
   - [Standard Error Codes](#standard-error-codes)
8. [Security Design](#security-design)
   - [Authentication](#authentication)
   - [Authorization and Roles](#authorization-and-roles)
   - [Security Filter Chain](#security-filter-chain)
9. [Logging Conventions](#logging-conventions)
10. [Reactive Programming Model](#reactive-programming-model)
11. [Database Migration Conventions](#database-migration-conventions)
12. [Testing Strategy](#testing-strategy)
    - [Unit Tests](#unit-tests)
    - [Integration Tests](#integration-tests)
    - [Test Configuration](#test-configuration)
13. [Non-Functional Requirements](#non-functional-requirements)
14. [Open Questions](#open-questions)

---

## Overview

This document defines the technical design for the foundational Spring Boot project setup for the Car Rental System backend. It establishes the conventions, architectural patterns, dependency choices, and configuration strategies that all feature teams must follow when implementing backend services.

All feature-level TRDs (vehicle onboarding, lifecycle management, etc.) reference this document as the **baseline implementation standard**. Any deviation from these conventions requires sign-off from the architecture owner.

---

## Technology Stack

| Concern | Technology | Version |
|---|---|---|
| Language | Java | 25 |
| Framework | Spring Boot | 3.x (latest stable) |
| Web layer | Spring WebFlux (Reactive) | Included in Spring Boot 3.x |
| Database | PostgreSQL | 16.x |
| DB Connectivity | R2DBC (reactive) | `r2dbc-postgresql` 1.x |
| DB Migrations | Flyway | 10.x |
| Build Tool | Maven | 3.9.x |
| Security | Spring Security (WebFlux) | Included in Spring Boot 3.x |
| API Documentation | SpringDoc OpenAPI | 2.x |
| Logging | SLF4J + Logback | Included in Spring Boot 3.x |
| Testing | JUnit 5, Mockito, Spring Boot Test, Testcontainers | JUnit 5.x, Testcontainers 1.x |

> **Note:** JPA/Hibernate is **not** used. All database access is via **R2DBC** for non-blocking reactive database connectivity. This is a deliberate architectural constraint.

---

## Project Structure

The project follows a domain-first, feature-oriented package layout. Each domain module contains its own controller, service, repository, DTO, and entity sub-packages.

```
car-rental-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── carrental/
│   │   │           ├── CarRentalApplication.java          # Application entry point
│   │   │           ├── config/                            # Global configuration beans
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── R2dbcConfig.java
│   │   │           │   └── OpenApiConfig.java
│   │   │           ├── common/                            # Shared utilities and cross-cutting concerns
│   │   │           │   ├── exception/                     # Global exception handling
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── CarRentalException.java
│   │   │           │   │   └── ErrorResponse.java
│   │   │           │   ├── dto/                           # Shared DTO types (pagination, etc.)
│   │   │           │   │   └── PageResponse.java
│   │   │           │   └── validation/                    # Custom validators
│   │   │           ├── vehicle/                           # Vehicle domain (Car Management)
│   │   │           │   ├── controller/
│   │   │           │   │   └── VehicleController.java
│   │   │           │   ├── service/
│   │   │           │   │   └── VehicleService.java
│   │   │           │   ├── repository/
│   │   │           │   │   └── VehicleRepository.java
│   │   │           │   ├── dto/
│   │   │           │   │   ├── VehicleRegistrationRequest.java
│   │   │           │   │   └── VehicleResponse.java
│   │   │           │   └── entity/
│   │   │           │       └── Vehicle.java
│   │   │           └── location/                          # Location domain
│   │   │               └── ...
│   │   └── resources/
│   │       ├── application.yml                            # Base configuration
│   │       ├── application-local.yml                      # Local developer overrides (not committed)
│   │       ├── application-test.yml                       # Test profile configuration
│   │       └── db/
│   │           └── migration/                             # Flyway SQL migrations
│   │               ├── V1__create_vehicles.sql
│   │               ├── V2__create_vehicle_insurance.sql
│   │               └── V3__create_vehicle_status_history.sql
│   └── test/
│       └── java/
│           └── com/
│               └── carrental/
│                   ├── vehicle/
│                   │   ├── controller/
│                   │   │   └── VehicleControllerTest.java
│                   │   └── service/
│                   │       └── VehicleServiceTest.java
│                   └── integration/
│                       └── VehicleIntegrationTest.java
├── pom.xml
└── README.md
```

**Naming Rules:**

| Artifact | Convention | Example |
|---|---|---|
| Java package | `com.carrental.<domain>.<layer>` | `com.carrental.vehicle.service` |
| Controller class | `<Domain>Controller` | `VehicleController` |
| Service class | `<Domain>Service` | `VehicleService` |
| Repository class | `<Domain>Repository` | `VehicleRepository` |
| Request DTO | `<Action>Request` | `VehicleRegistrationRequest` |
| Response DTO | `<Domain>Response` | `VehicleResponse` |
| Entity class | `<Domain>` (singular) | `Vehicle` |
| Flyway migration | `V<version>__<description>.sql` | `V1__create_vehicles.sql` |

---

## Maven Dependency Design

The `pom.xml` uses `spring-boot-starter-parent` as the parent POM. All Spring Boot–managed dependency versions are inherited from the BOM; explicit versions are only specified for non-Boot-managed libraries.

### Core Dependencies

| Group ID | Artifact ID | Scope | Purpose |
|---|---|---|---|
| `org.springframework.boot` | `spring-boot-starter-webflux` | compile | Reactive HTTP endpoints using Spring WebFlux and Netty |
| `org.springframework.boot` | `spring-boot-starter-validation` | compile | Bean validation (`@Valid`, `@NotNull`, `@Size`, etc.) |
| `org.springdoc` | `springdoc-openapi-starter-webflux-ui` | compile | OpenAPI / Swagger UI for API documentation |

### Database and Persistence

| Group ID | Artifact ID | Scope | Purpose |
|---|---|---|---|
| `org.springframework.boot` | `spring-boot-starter-data-r2dbc` | compile | Reactive repository support using Spring Data R2DBC |
| `org.postgresql` | `r2dbc-postgresql` | compile | R2DBC driver for PostgreSQL |
| `org.postgresql` | `postgresql` | runtime | JDBC driver used exclusively by Flyway for schema migrations |
| `org.flywaydb` | `flyway-core` | compile | Database migration management |
| `org.flywaydb` | `flyway-database-postgresql` | compile | Flyway PostgreSQL dialect support |

> **Note:** Flyway uses the synchronous JDBC `postgresql` driver because Flyway does not support R2DBC. At application startup, Flyway runs migrations synchronously before the reactive pipeline initialises. This is a one-time startup concern and does not affect runtime request handling.

### Security

| Group ID | Artifact ID | Scope | Purpose |
|---|---|---|---|
| `org.springframework.boot` | `spring-boot-starter-security` | compile | Spring Security reactive (WebFlux) support |

### Testing

| Group ID | Artifact ID | Scope | Purpose |
|---|---|---|---|
| `org.springframework.boot` | `spring-boot-starter-test` | test | JUnit 5, Mockito, AssertJ, Spring Boot Test |
| `io.projectreactor` | `reactor-test` | test | `StepVerifier` for testing reactive streams |
| `org.testcontainers` | `postgresql` | test | PostgreSQL container for integration tests |
| `org.testcontainers` | `r2dbc` | test | R2DBC Testcontainers support |
| `org.testcontainers` | `junit-jupiter` | test | JUnit 5 integration for Testcontainers lifecycle |

### Utilities

| Group ID | Artifact ID | Scope | Purpose |
|---|---|---|---|
| `org.springframework.boot` | `spring-boot-configuration-processor` | optional | Generates IDE metadata for `@ConfigurationProperties` classes |

---

## Layered Architecture

The backend follows a three-layer architecture: **Controller → Service → Repository**. Each layer has a single responsibility and must not bypass the layer above or below it.

```
HTTP Request
     │
     ▼
┌──────────────┐
│  Controller  │  Handles HTTP I/O, validates input, delegates to Service
└──────┬───────┘
       │  calls
       ▼
┌──────────────┐
│   Service    │  Contains all business logic; calls Repository
└──────┬───────┘
       │  calls
       ▼
┌──────────────┐
│  Repository  │  Database access only; no business logic
└──────────────┘
```

### Controller Layer

**Responsibilities:**
- Map HTTP routes to handler methods.
- Accept request DTOs; reject invalid input using `@Valid` before it reaches the Service.
- Return response DTOs wrapped in reactive types (`Mono<ResponseEntity<T>>` or `Flux<T>`).
- Must **never** expose entity objects directly in responses.
- Must **never** contain business logic.

**Conventions:**
- Annotate with `@RestController` and `@RequestMapping("/api/v1/<resource>")`.
- Use constructor-based injection for the Service dependency.
- Method return types use `Mono<ResponseEntity<T>>` for single-resource operations.
- Pagination endpoints return `Mono<ResponseEntity<PageResponse<T>>>`.

**Example method signature pattern (pseudo-code):**

```
POST /api/v1/vehicles
  accepts: VehicleRegistrationRequest (validated with @Valid)
  returns: Mono<ResponseEntity<VehicleResponse>>
  on success: 201 Created with body VehicleResponse
```

### Service Layer

**Responsibilities:**
- Enforce all business rules and domain logic.
- Orchestrate calls to one or more Repositories.
- Map entities to/from DTOs (using a dedicated mapper method or a mapper utility).
- Return reactive types (`Mono<T>` or `Flux<T>`).
- Throw domain exceptions (`CarRentalException` and its subtypes) on business rule violations.

**Conventions:**
- Annotate with `@Service`.
- Use constructor-based injection for Repository dependencies.
- Keep methods small and single-responsibility; each method should do one logical thing.
- Log at `INFO` level on successful significant operations; log at `WARN` for handled failures.

### Repository Layer

**Responsibilities:**
- Provide reactive CRUD and query methods against the database.
- Must **not** contain business logic.

**Conventions:**
- Extend `ReactiveCrudRepository<Entity, UUID>` from Spring Data R2DBC.
- Custom queries use `@Query` annotations with parameterised SQL; **never** concatenate user input into query strings.
- Return types are always `Mono<T>` or `Flux<T>`.
- Repository interfaces are placed in the `repository` sub-package of their domain.

### DTO Design

DTOs separate the API contract from the internal data model.

| DTO Type | Purpose | Validation |
|---|---|---|
| Request DTO | Carries inbound data from client to Controller | Bean Validation annotations (`@NotNull`, `@Size`, `@Pattern`, etc.) |
| Response DTO | Carries outbound data from Service to client | No validation annotations needed |

**Rules:**
- DTOs are immutable Java Records (preferred) or classes with no public setters.
- Entity objects must **never** be serialised directly into HTTP responses.
- Nested response objects are allowed (e.g., `InsuranceDetails` embedded inside `VehicleResponse`).
- Field naming uses camelCase to match JSON conventions.

---

## Configuration Design

### @ConfigurationProperties Approach

All externally configurable parameters are grouped into typed configuration classes annotated with `@ConfigurationProperties`. This avoids scattered `@Value` annotations and provides compile-time safety.

**Convention:**
- Each logical configuration group has one `@ConfigurationProperties` class.
- Prefix follows the domain: `car-rental.<domain>.<property>`.
- Configuration classes are placed in `com.carrental.config`.
- Annotate the configuration class with `@Validated` to enforce constraints at startup.

**Configuration groups:**

| Class | Prefix | Purpose |
|---|---|---|
| `DatabaseProperties` | `car-rental.database` | Connection pool sizes, timeouts |
| `SecurityProperties` | `car-rental.security` | JWT secret reference, token TTL, CORS origins |
| `InsuranceAlertProperties` | `car-rental.insurance-alert` | Expiry warning threshold (days), notification channels |

### Environment Variable Strategy

Sensitive values (credentials, secrets, keys) must **never** be hard-coded or committed to version control.

| Property | Environment Variable | Description |
|---|---|---|
| DB host | `DB_HOST` | PostgreSQL hostname |
| DB port | `DB_PORT` | PostgreSQL port (default: 5432) |
| DB name | `DB_NAME` | Database name |
| DB username | `DB_USERNAME` | Database user |
| DB password | `DB_PASSWORD` | Database password — injected by secret manager |
| JWT secret | `JWT_SECRET` | Secret key for token signing — injected by secret manager |

### Application Properties Structure

`application.yml` uses Spring profiles to separate environment-specific configuration.

**Profile strategy:**

| Profile | File | Purpose |
|---|---|---|
| (default) | `application.yml` | Shared defaults; no secrets |
| `local` | `application-local.yml` | Developer local overrides; **not committed to version control** |
| `test` | `application-test.yml` | Test profile; points to Testcontainers or H2 |
| `staging` | Injected at deploy time | Staging environment |
| `prod` | Injected at deploy time | Production environment |

**`application-local.yml` must be added to `.gitignore`.**

---

## Exception Handling Design

### Global Error Handler

A single `@ControllerAdvice` class (`GlobalExceptionHandler`) handles all exceptions and translates them into a consistent JSON error response. This class is placed in `com.carrental.common.exception`.

For Spring WebFlux, the global error handler implements `WebExceptionHandler` (or extends `DefaultErrorWebExceptionHandler`) in addition to `@ControllerAdvice`, to capture errors from both the handler pipeline and the router function pipeline.

**Exception hierarchy:**

```
CarRentalException (base, unchecked)
├── ResourceNotFoundException     → HTTP 404
├── DuplicateResourceException    → HTTP 409
├── BusinessRuleViolationException → HTTP 422
└── UnauthorisedException         → HTTP 403
```

**Mapping rules:**

| Exception Class | HTTP Status | Error Code |
|---|---|---|
| `ResourceNotFoundException` | 404 Not Found | `RESOURCE_NOT_FOUND` |
| `DuplicateResourceException` | 409 Conflict | `DUPLICATE_RESOURCE` |
| `BusinessRuleViolationException` | 422 Unprocessable Entity | `BUSINESS_RULE_VIOLATION` |
| `UnauthorisedException` | 403 Forbidden | `UNAUTHORIZED` |
| `WebExchangeBindException` (validation) | 400 Bad Request | `VALIDATION_ERROR` |
| `Throwable` (catch-all) | 500 Internal Server Error | `INTERNAL_ERROR` |

### Error Response Schema

All error responses conform to a single JSON schema regardless of error type.

| Field | Type | Description |
|---|---|---|
| `timestamp` | String (ISO 8601) | UTC time when the error occurred |
| `status` | Integer | HTTP status code |
| `errorCode` | String | Machine-readable error code (see table above) |
| `message` | String | Human-readable description of the error |
| `path` | String | The request URI that triggered the error |
| `fieldErrors` | Array | Present only for `VALIDATION_ERROR`; each item has `field` and `message` |

**Example error response (validation failure):**

```json
{
  "timestamp": "2026-03-07T10:00:00Z",
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/vehicles",
  "fieldErrors": [
    { "field": "vin", "message": "must be exactly 17 alphanumeric characters" },
    { "field": "purchaseCost", "message": "must be greater than 0" }
  ]
}
```

### Standard Error Codes

| Error Code | Description |
|---|---|
| `VALIDATION_ERROR` | One or more request fields failed bean validation |
| `RESOURCE_NOT_FOUND` | A referenced resource (vehicle, location, etc.) does not exist |
| `DUPLICATE_RESOURCE` | A uniqueness constraint would be violated (VIN, license plate, etc.) |
| `BUSINESS_RULE_VIOLATION` | A domain business rule was violated (invalid status transition, etc.) |
| `UNAUTHORIZED` | The caller lacks the required role or the token is invalid |
| `INTERNAL_ERROR` | An unexpected server-side error occurred |

---

## Security Design

### Authentication

- The system uses **JWT-based bearer token authentication**.
- Tokens are issued by an external Identity Provider (IdP) or a dedicated Auth Service (to be defined in a separate TRD).
- Every protected endpoint requires an `Authorization: Bearer <token>` header.
- The application validates the JWT signature, expiry, and issuer claim on each request. No session state is stored server-side (stateless).

### Authorization and Roles

Roles are encoded as claims inside the JWT. Spring Security's `@PreAuthorize` annotation enforces role checks at the method level in the Controller.

| Role | Description |
|---|---|
| `FLEET_MANAGER` | Full access to vehicle management operations |
| `AGENT` | Read access to vehicle catalogue; booking operations |
| `ADMIN` | Platform administration; user management |

### Security Filter Chain

For Spring WebFlux, the security configuration defines a `SecurityWebFilterChain` bean:

- Public endpoints (health check, OpenAPI docs): permit all.
- All other endpoints: require a valid JWT.
- CSRF protection: disabled (stateless API; not applicable to REST APIs with bearer tokens).
- CORS: configured via `car-rental.security.cors-origins` property; default permits only explicitly whitelisted origins.
- Sensitive headers (`X-Powered-By`, `Server`) must be removed from all responses.

---

## Logging Conventions

All logging uses **SLF4J** with **Logback** (the default Spring Boot logging backend). `System.out.println` is **prohibited**.

**Rules:**

| Level | When to Use |
|---|---|
| `ERROR` | Unrecoverable errors; unexpected exceptions bubbling to the top |
| `WARN` | Recoverable issues; handled business exceptions (e.g., duplicate resource attempted) |
| `INFO` | Significant application lifecycle events; successful completion of important operations |
| `DEBUG` | Diagnostic data useful for development; disabled in production |

**Logger declaration:**

Every class that logs must declare a logger as a private static final field:

```
private static final Logger log = LoggerFactory.getLogger(ClassName.class);
```

(Or use Lombok's `@Slf4j` annotation if Lombok is added to the project.)

**Prohibited logging practices:**
- Do not log sensitive data: passwords, full JWT tokens, PII (customer names, IDs outside of audit contexts), payment data.
- Do not log entire request/response bodies at `INFO` level. Use `DEBUG` for request tracing in development.

**Structured logging:**

In production, logs are emitted in JSON format to support log aggregation systems. The Logback configuration for the `prod` profile must use a JSON encoder (e.g., Logstash encoder or a structured format appender).

---

## Reactive Programming Model

The entire backend is built on **Project Reactor** with Spring WebFlux. The following rules apply to all reactive code.

**Mono and Flux usage:**

| Return Type | When to Use |
|---|---|
| `Mono<T>` | Exactly 0 or 1 result (single resource fetch, create, update, delete) |
| `Flux<T>` | 0 or more results (list/paginated queries) |
| `Mono<Void>` | Operations with no response body (delete, fire-and-forget) |

**Rules:**
- Never block inside a reactive chain. `block()`, `blockFirst()`, and `blockLast()` are **prohibited** in production code paths.
- Use `switchIfEmpty(Mono.error(...))` to handle not-found cases reactively.
- Use `flatMap` for reactive transformations that return `Mono`/`Flux`; use `map` for synchronous transformations.
- Use `onErrorMap` to translate low-level exceptions (R2DBC constraint violations, etc.) into domain exceptions before they reach the Controller.
- Scheduler pinning: database operations run on a bounded elastic scheduler; CPU-bound work runs on the default parallel scheduler. Do not perform blocking I/O on the default scheduler.

**Pagination:**

Paginated list endpoints accept `page` (0-based) and `size` query parameters. The service layer constructs a `PageRequest` and returns a `PageResponse<T>` containing:

| Field | Type | Description |
|---|---|---|
| `content` | Array | Items on the current page |
| `page` | Integer | Current page index (0-based) |
| `size` | Integer | Page size requested |
| `totalElements` | Long | Total number of matching records |
| `totalPages` | Integer | Total number of pages |

---

## Database Migration Conventions

All schema changes are managed by **Flyway**. The following conventions are mandatory:

**File location:** `src/main/resources/db/migration/`

**Naming pattern:** `V<version>__<description>.sql`
- `version` is a monotonically increasing integer (e.g., `1`, `2`, `3`).
- `description` uses underscores to separate words (e.g., `create_vehicles`).
- Examples: `V1__create_vehicles.sql`, `V2__create_vehicle_insurance.sql`

**One table per file rule:**
- Each SQL file creates exactly one table, including all column definitions, primary key constraint, unique constraints, check constraints, and indices for that table.
- Foreign key constraints referencing another table may be included in the same file as the dependent table.

**DDL vs DML separation:**
- DDL (CREATE TABLE, ALTER TABLE, CREATE INDEX) lives in versioned migration files.
- Reference/seed DML (INSERT INTO for lookup data) lives in separate versioned migration files clearly named (e.g., `V10__seed_locations.sql`).

**Immutability:** Once a migration file is merged to the main branch, its content must **never** be changed. New schema changes must always be new migration files.

**Flyway configuration in `application.yml`:**

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false
    validate-on-migrate: true
```

---

## Testing Strategy

### Unit Tests

Unit tests cover individual classes (primarily Service classes) in isolation using mocks.

**Framework:** JUnit 5 (`@ExtendWith(MockitoExtension.class)`) + Mockito + `reactor-test` (`StepVerifier`).

**Coverage targets:**
- Service layer: ≥ 80% line coverage.
- Utility/validation classes: ≥ 90% line coverage.
- Controller layer: covered by integration tests (see below).

**Conventions:**
- Test class name: `<ClassName>Test` (e.g., `VehicleServiceTest`).
- Test method name: `<methodName>_<scenario>_<expected>` (e.g., `registerVehicle_duplicateVin_throwsDuplicateResourceException`).
- Use `StepVerifier.create(...).expectError(...).verify()` for testing reactive error paths.

**Example test flow (pseudo-code):**

```
// Given
when(vehicleRepository.findByVin("DUPVIN")).thenReturn(Mono.just(existingVehicle));

// When
Mono<VehicleResponse> result = vehicleService.registerVehicle(requestWithDuplicateVin);

// Then
StepVerifier.create(result)
    .expectError(DuplicateResourceException.class)
    .verify();
```

### Integration Tests

Integration tests start a real application context and verify the full request/response pipeline end-to-end, including the database.

**Framework:** Spring Boot Test (`@SpringBootTest`) + Testcontainers (PostgreSQL) + `WebTestClient`.

**Conventions:**
- Integration test class name: `<Domain>IntegrationTest` (e.g., `VehicleIntegrationTest`).
- Each integration test class starts a single Testcontainers PostgreSQL instance, shared via `@Container` and a static shared container approach to minimise startup time.
- Flyway migrations run automatically before tests (using the same migration files as production).
- Each test cleans up its own data using `@AfterEach` or wraps tests in a transaction that is rolled back.

### Test Configuration

- `application-test.yml` activates the `test` profile.
- Test profile overrides database URL to use the Testcontainers-managed PostgreSQL instance.
- The test profile disables any external service calls (stubbed with `@MockBean`).

---

## Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-1 | All service endpoints must respond within 500 ms at the 95th percentile under normal load. |
| NFR-2 | The application must start up cleanly and pass health checks within 30 seconds. |
| NFR-3 | No secrets or credentials may appear in logs, application properties committed to version control, or error responses. |
| NFR-4 | All Flyway migrations must be idempotent-safe and reversible (down-migration scripts or rollback procedures documented). |
| NFR-5 | API responses must include appropriate caching headers; mutable resources must not be cached by default. |
| NFR-6 | All public-facing APIs must be documented in OpenAPI (available at `/swagger-ui.html` and `/v3/api-docs` in non-production environments). |

---

## Open Questions

| # | Question | Owner |
|---|---|---|
| OQ-1 | Which external Identity Provider (IdP) is used for JWT issuance? Auth0, Keycloak, or a custom auth service? | Architecture Owner |
| OQ-2 | Is a service mesh (e.g., Istio) planned? This affects mutual TLS and inter-service auth design. | Architecture Owner |
| OQ-3 | What is the target deployment platform (Kubernetes, ECS, bare metal)? This affects environment variable injection and secret management strategy. | DevOps Lead |
| OQ-4 | Should the OpenAPI UI be accessible in the staging environment, or only in local/dev? | Product Owner |
| OQ-5 | Is there a centralised logging platform (e.g., ELK Stack, Datadog)? This affects the Logback configuration for structured JSON logging. | Platform Team |
