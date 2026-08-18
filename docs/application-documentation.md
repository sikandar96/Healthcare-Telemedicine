# Healthcare Telemedicine Platform

**Application documentation**
**Technology:** Spring Boot, Java 21, MongoDB, Spring Security, JWT, SpringDoc OpenAPI
**Current application port:** `9008`

## 1. Application overview

Healthcare Telemedicine is a MongoDB-backed Spring Boot backend for a healthcare platform that combines doctor discovery, consultations, appointments, pharmacy operations, medicine orders, preventive reminders, clinical records, prescriptions, consent management, sponsored health campaigns, payments, revenue reporting, and audit tracking.

The application exposes REST APIs under `/api`. Every application response is intended to use the `HealthApiResponse` envelope, while authentication failures and authorization failures are serialized through the security exception handlers using the same response model.

The platform uses stateless JWT authentication. User records and their roles are stored in MongoDB; the application does not use an in-memory user store for production authentication.

## 2. Technology stack

| Area | Technology |
|---|---|
| Language and runtime | Java 21 |
| Application framework | Spring Boot |
| Build | Maven and Maven Wrapper (`./mvnw`) |
| Persistence | Spring Data MongoDB |
| Database | MongoDB |
| Authentication | Spring Security with JWT |
| Password hashing | BCrypt |
| API documentation | SpringDoc OpenAPI 3 and Swagger UI |
| Logging | SLF4J/Logback with correlation IDs |
| Validation | Jakarta Bean Validation |
| Tests | Spring Boot Test, JUnit, Mockito |
| Observability | Spring Boot Actuator: health, info, and metrics |

## 3. Architecture

The application follows a conventional layered Spring Boot architecture:

```mermaid
flowchart TD
    Client[Web or mobile client] --> Security[Security filter chain]
    Security --> JWT[JWT authentication filter]
    JWT --> Controllers[REST controllers]
    Controllers --> Services[Application services]
    Services --> Repositories[MongoDB repositories]
    Repositories --> Mongo[(MongoDB)]
    Services --> Audit[Audit and notification persistence]
    Correlation[Correlation ID filter] --> Controllers
    Actuator[Actuator] --> Application[Application health and metrics]
```

The main dependency direction is:

```text
rest.controller -> services -> repositories -> MongoDB
                 -> dtos
                 -> entities/enums
```

Controllers are responsible for HTTP concerns, request validation, authentication context extraction, and response mapping. Services contain business rules, ownership checks, transaction boundaries, audit events, and notification orchestration. Repositories abstract persistence. DTOs define API contracts, while entities represent MongoDB documents.

## 4. Package structure

The source tree uses standard package boundaries under `com.health.care`.

| Package | Responsibility |
|---|---|
| `config` | Security, rate limiting, correlation IDs, MongoDB transactions, and application configuration |
| `dtos` | REST request and response records, including validation annotations |
| `entities` | MongoDB document models and persisted domain objects |
| `enums` | Roles, statuses, consultation types, payment states, reminder types, and other controlled values |
| `repositories` | Spring Data MongoDB repository interfaces |
| `security` | JWT service, JWT filter, MongoDB user-details service, and authentication support |
| `services` | Business operations and SOLID application contracts |
| `rest.controller` | REST endpoint adapters |
| `exceptions` | Domain exceptions and `GlobalExceptionHandler` |

The former consolidated `com.health.care.healthcare` package is no longer used.

## 5. Running the application locally

### Prerequisites

Install Java 21, Git, Maven or use the included Maven Wrapper, and MongoDB. MongoDB must be reachable using the configured URI. MongoDB transactions require a replica set or sharded cluster; a standalone MongoDB process is sufficient only for non-transactional development flows.

### Start MongoDB for basic local development

```bash
docker run -d --name healthcare-mongodb -p 27017:27017 mongo:latest
```

For testing transaction-enabled workflows, use a MongoDB replica set or a MongoDB service that supports transactions.

### Build and test

```bash
./mvnw clean test
```

### Run the application

```bash
./mvnw spring-boot:run
```

Alternatively, build and run the packaged JAR:

```bash
./mvnw clean package
java -jar target/Healthcare-Telemedicine-0.0.1-SNAPSHOT.jar
```

The default base URL is `http://localhost:9008`.

## 6. Configuration

The application reads configuration from `src/main/resources/application.yaml`. Environment variables are preferred for secrets and deployment-specific values.

| Variable | Default | Purpose |
|---|---|---|
| `MONGO_URI` | `mongodb://localhost:27017/Healthcare-Telemedicine` | MongoDB connection URI |
| `JWT_SECRET` | Development placeholder | HMAC signing secret; replace in every deployed environment |
| `JWT_EXPIRATION_MS` | `86400000` | JWT lifetime in milliseconds; default is 24 hours |
| `JWT_HEADER` | `Authorization` | HTTP header containing the token |
| `JWT_PREFIX` | `Bearer` | Token prefix |
| `JWT_ISSUER` | `healthcare-telemedicine` | JWT issuer claim |
| `APP_DEFAULT_ADMIN_CREATE` | `false` | Enables optional bootstrap administrator creation |
| `APP_DEFAULT_ADMIN_USERNAME` | Empty | Bootstrap administrator username |
| `APP_DEFAULT_ADMIN_PASSWORD` | Empty | Bootstrap administrator password |
| `APP_REMINDERS_CRON` | `0 0 8 * * *` | Preventive reminder scheduled-job expression |

A production deployment should set a long, random `JWT_SECRET`, keep default-admin creation disabled after bootstrap, use a managed secret store, and avoid placing credentials in source control.

## 7. Authentication and authorization

### JWT flow

1. A client registers through `POST /api/auth/register` or authenticates through `POST /api/auth/login`.
2. The server validates the request and authenticates against MongoDB-backed user data.
3. Passwords are checked using BCrypt.
4. The server returns a JWT in `data.token` with the `Bearer` token type and normalized authorities such as `ROLE_PATIENT`.
5. The client sends `Authorization: Bearer <token>` with protected requests.
6. The JWT filter validates the signature, issuer, expiration, and claims, then creates the Spring Security authentication context.

Example:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Roles

The application normalizes persisted roles to Spring authorities. Common roles are:

| Role | Typical responsibility |
|---|---|
| `PATIENT` | Book consultations and appointments, place medicine orders, manage reminders, payments, and personal consents |
| `DOCTOR` | Access doctor workflows, submit verification, create clinical records and prescriptions, and manage assigned consultations or appointments |
| `PHARMACY_PARTNER` | Manage pharmacy inventory and access pharmacy-related operations |
| `HEALTH_MANAGER` | Register doctors and pharmacies, manage programs, campaigns, verification decisions, and revenue operations |
| `ADMIN` | Administrative role management, audit counts, payment administration, and elevated platform management |

Public registration creates a patient role. The client-provided role value must not be used to self-assign an administrator or another privileged role. Role assignment is restricted to `PUT /api/auth/users/{username}/roles`, which requires `ADMIN`.

### Security response behavior

Unauthenticated requests receive HTTP `401`. Authenticated users without the required role receive HTTP `403`. Both responses use the standard API response envelope. Validation failures are handled by `GlobalExceptionHandler` and return a client-error status with a readable message and timestamp.

## 8. API conventions

The application uses the following conventions:

| Convention | Behavior |
|---|---|
| Base URL | `http://localhost:9008` by default |
| Content type | `application/json` for JSON requests and responses |
| Authentication | `Authorization: Bearer <JWT>` unless the endpoint is public |
| Success envelope | `{"success": true, "data": ..., "timestamp": ...}` |
| Error envelope | `{"success": false, "message": ..., "timestamp": ...}` |
| Identifiers | MongoDB identifiers represented as strings in URL paths and DTOs |
| Dates | ISO-8601 date or date-time strings, depending on the DTO |
| Correlation | The server creates or propagates a correlation ID for request tracing |

## 9. Endpoint reference

The security column describes the role enforced by the security filter chain. Service-level ownership checks may apply additional restrictions.

### 9.1 Authentication and operational endpoints

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a patient account and receive a JWT |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |
| `GET` | `/api/auth/me` | Authenticated | Return the current username |
| `PUT` | `/api/auth/users/{username}/roles` | `ADMIN` | Replace the persisted roles for a user |
| `GET` | `/api/hello` | Authenticated | Protected smoke-test endpoint |
| `GET` | `/actuator/health` | Public | Health probe |
| `GET` | `/actuator/info` | Authenticated by fallback policy | Application information |
| `GET` | `/actuator/metrics` | Authenticated by fallback policy | Application metrics |
| `GET` | `/swagger-ui.html` | Public | Swagger UI |
| `GET` | `/v3/api-docs` | Public | OpenAPI document |

### 9.2 Doctor and consultation endpoints

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/doctors/register` | `HEALTH_MANAGER`, `ADMIN` | Register a doctor profile |
| `GET` | `/api/doctors/available` | Authenticated healthcare roles | List available doctors |
| `POST` | `/api/consultations/book` | `PATIENT` | Book a consultation |
| `GET` | `/api/consultations/my` | `PATIENT`, `DOCTOR` | List consultations visible to the current actor |
| `PATCH` | `/api/consultations/{id}/status` | `PATIENT`, `DOCTOR` | Update consultation status subject to service rules |

### 9.3 Pharmacy and reminders

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/pharmacies/add` | `HEALTH_MANAGER`, `ADMIN` | Register a pharmacy |
| `GET` | `/api/pharmacies/available` | Authenticated healthcare roles | List available pharmacies |
| `POST` | `/api/pharmacies/medicine-orders` | `PATIENT` | Place a medicine order |
| `GET` | `/api/pharmacies/medicine-orders` | `PATIENT` | List the patient’s orders |
| `POST` | `/api/reminders/create` | `PATIENT` | Create a preventive reminder |
| `GET` | `/api/reminders/list` | `PATIENT` | List the patient’s reminders |
| `PATCH` | `/api/reminders/{id}/complete` | `PATIENT` | Complete a reminder |

### 9.4 Consolidated healthcare endpoints

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/healthcare/doctors` | `HEALTH_MANAGER`, `ADMIN` | Register a doctor |
| `GET` | `/api/healthcare/doctors` | Authenticated healthcare roles | List doctors |
| `POST` | `/api/healthcare/consultations` | `PATIENT` | Create a consultation |
| `GET` | `/api/healthcare/consultations` | `PATIENT`, `DOCTOR` | List consultations |
| `PATCH` | `/api/healthcare/consultations/{id}/status` | `PATIENT`, `DOCTOR` | Update consultation status |
| `POST` | `/api/healthcare/pharmacies` | `HEALTH_MANAGER`, `ADMIN` | Register a pharmacy |
| `GET` | `/api/healthcare/pharmacies` | Authenticated healthcare roles | List pharmacies |
| `POST` | `/api/healthcare/medicine-orders` | `PATIENT` | Place a medicine order |
| `GET` | `/api/healthcare/medicine-orders` | `PATIENT` | List medicine orders |
| `POST` | `/api/healthcare/health-programs` | `HEALTH_MANAGER`, `ADMIN` | Publish a health program |
| `GET` | `/api/healthcare/health-programs` | Public | List health programs |
| `POST` | `/api/healthcare/reminders` | `PATIENT` | Create a reminder |
| `GET` | `/api/healthcare/reminders` | `PATIENT` | List reminders |
| `PATCH` | `/api/healthcare/reminders/{id}/complete` | `PATIENT` | Complete a reminder |
| `GET` | `/api/healthcare/revenue/summary` | `HEALTH_MANAGER`, `ADMIN` | Return revenue summary |

### 9.5 Platform endpoints

| Method | Path | Access | Description |
|---|---|---|---|
| `POST` | `/api/platform/doctor-verifications` | `DOCTOR` | Submit a doctor verification request |
| `GET` | `/api/platform/doctor-verifications/pending` | `HEALTH_MANAGER`, `ADMIN` | List pending verification requests |
| `PATCH` | `/api/platform/doctor-verifications/{id}` | `HEALTH_MANAGER`, `ADMIN` | Approve or reject verification |
| `POST` | `/api/platform/appointments` | `PATIENT` | Create an appointment |
| `GET` | `/api/platform/appointments/mine` | `PATIENT` | List the patient’s appointments |
| `GET` | `/api/platform/appointments/doctor/mine` | `DOCTOR` | List appointments assigned to the current doctor |
| `GET` | `/api/platform/appointments/doctor/{doctorId}` | `HEALTH_MANAGER`, `ADMIN` | List appointments for a doctor |
| `PATCH` | `/api/platform/appointments/{id}/status` | `PATIENT`, `DOCTOR` | Update appointment status; ownership is checked in the service |
| `POST` | `/api/platform/clinical-records` | `DOCTOR` | Create a clinical record |
| `GET` | `/api/platform/clinical-records/mine` | `PATIENT`, `DOCTOR` | List records visible to the current actor |
| `POST` | `/api/platform/prescriptions` | `DOCTOR` | Create a prescription |
| `GET` | `/api/platform/prescriptions/mine` | `PATIENT`, `DOCTOR` | List prescriptions visible to the current actor |
| `POST` | `/api/platform/inventory` | `PHARMACY_PARTNER`, `ADMIN` | Create inventory |
| `PATCH` | `/api/platform/inventory/{id}` | `PHARMACY_PARTNER`, `ADMIN` | Adjust inventory quantity |
| `GET` | `/api/platform/inventory/{pharmacyId}` | Authenticated healthcare roles | List pharmacy inventory |
| `POST` | `/api/platform/payments` | `PATIENT` | Create a payment transaction |
| `GET` | `/api/platform/payments/mine` | Authenticated | List payments belonging to the current actor |
| `PATCH` | `/api/platform/payments/{id}` | `HEALTH_MANAGER`, `ADMIN` | Update payment status |
| `GET` | `/api/platform/notifications` | Authenticated | List notifications for the current actor |
| `PATCH` | `/api/platform/notifications/{id}/read` | Authenticated | Mark a notification as read |
| `POST` | `/api/platform/consents` | `PATIENT` | Grant consent |
| `GET` | `/api/platform/consents` | Authenticated | List consents visible to the current actor |
| `PATCH` | `/api/platform/consents/{id}/revoke` | `PATIENT` | Revoke patient consent |
| `POST` | `/api/platform/campaigns` | `HEALTH_MANAGER`, `ADMIN` | Create a sponsored campaign |
| `GET` | `/api/platform/campaigns/active` | Public | List active campaigns |
| `GET` | `/api/platform/audit/count/{action}` | `ADMIN` | Count audit events for an action |

## 10. Request validation reference

All controller request bodies use Jakarta validation where a body is accepted. The following rules are enforced by the DTO layer.

| DTO | Important validation rules |
|---|---|
| `AuthRequest` | Username and password must be present; password must satisfy the configured length rule |
| `RoleUpdateRequest` | At least one role is required |
| `DoctorRequest` | Username, name, specialization, and license number are required; consultation fee must be non-negative |
| `PharmacyRequest` | Required pharmacy identity/contact fields; commission rate must be non-negative |
| `ConsultationRequest` | Doctor identifier, consultation type, and scheduled time are required |
| `ConsultationStatusRequest` | Status is required and must be a valid `ConsultationStatus` value |
| `AppointmentRequest` | Doctor ID and start/end date-times are required |
| `AppointmentStatusRequest` | Status is required and must be a valid appointment status |
| `MedicineOrderRequest` | Pharmacy ID, delivery address, and a non-empty item list are required |
| `MedicineItemRequest` | Medicine name, quantity, and unit price are validated; quantity must be positive and price non-negative |
| `HealthProgramRequest` | Required title/category/content fields; sponsorship values must satisfy monetary constraints |
| `ReminderRequest` | Reminder type/title and due date are required |
| `VerificationRequest` | Username and license number are required |
| `VerificationDecision` | Decision status is required; rejection reason may be supplied for rejected requests |
| `ClinicalRecordRequest` | Patient username and diagnosis are required; consent and attachments are handled by service rules |
| `PrescriptionRequest` | Patient username and a non-empty list of valid prescription items are required |
| `PrescriptionItemRequest` | Medicine name, dosage, frequency, and duration are required; duration must be at least one day |
| `InventoryRequest` | Pharmacy ID, medicine name, SKU, quantity, and unit price are validated |
| `InventoryAdjustment` | Quantity must be zero or greater |
| `PaymentRequest` | Reference type, reference ID, currency, and amount are required; amount must be at least `0.01` |
| `PaymentStatusRequest` | Payment status is required; provider reference is optional |
| `ConsentRequest` | Grantee and purpose are required |
| `CampaignRequest` | Sponsor, title, description, dates, and budget are required; budget must be non-negative |
| `NotificationRequest` | Notification type, title, and message are required |

Invalid requests are rejected before the business operation executes. Domain failures such as missing records, ownership violations, duplicate usernames, and invalid state transitions are translated by `GlobalExceptionHandler`.

## 11. Transactions and duplicate-submission protection

MongoDB transaction configuration is provided by `MongoTransactionConfiguration`. Multi-document workflows use service-level transactional boundaries so related domain, notification, and audit writes commit together or roll back together.

Payment creation has an additional idempotency mechanism. Clients must send:

```http
Idempotency-Key: payment-attempt-2026-0001
```

The key is scoped to the payer and persisted with the payment transaction. Repeating a payment request using the same payer and key returns the original transaction rather than posting a second payment. A new payment must use a new key.

Transaction support requires MongoDB deployment with replica-set or sharded-cluster transaction capability.

## 12. Rate limiting

`RateLimitingFilter` applies centralized servlet-level request limits. Authentication, payment, and medicine-order routes receive stricter limits than general API routes. When a limit is exceeded, the application returns HTTP `429 Too Many Requests` with a `Retry-After` response header and the standard error envelope.

The current limiter is process-local. In a horizontally scaled deployment, use a shared Redis or API gateway limiter so limits are enforced consistently across instances.

## 13. Logging and observability

The correlation filter assigns or propagates a correlation ID and logs request lifecycle information, including method, URI, status, duration, and correlation ID. Service and controller logs cover authentication attempts, successful logins, role updates, business operations, authorization-sensitive actions, and failures.

Credentials, JWT values, authorization headers, and request bodies must not be logged. Production logging should normally reduce Spring Security and application logging from `DEBUG` to `INFO` or a carefully selected audit level.

Actuator exposes:

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

Only the health endpoint is explicitly public in the security configuration. Review exposure and network restrictions before making operational endpoints accessible outside trusted infrastructure.

## 14. Error handling

`GlobalExceptionHandler` centralizes application exceptions. Typical outcomes are:

| Condition | Expected HTTP status |
|---|---:|
| Missing or malformed request data | `400 Bad Request` |
| Authentication required | `401 Unauthorized` |
| Authenticated but insufficient role | `403 Forbidden` |
| Resource not found | `404 Not Found` |
| Duplicate user or idempotency conflict | `409 Conflict` where applicable |
| Unexpected server failure | `500 Internal Server Error` |
| Rate-limit exceeded | `429 Too Many Requests` |

Clients should inspect the `success` field and the `message` value rather than relying only on an HTTP status.

## 15. Testing

Run the complete suite with:

```bash
./mvnw test
```

The repository includes application-context coverage, deterministic JWT authentication integration coverage, and platform-service regression tests. The JWT test uses a deterministic persistence fixture so it can verify authentication behavior without requiring a local MongoDB process. Production remains MongoDB-backed.

For a deployment-level verification, run the application against a real MongoDB replica set and test registration, login, role authorization, appointment ownership, transaction rollback, payment idempotency, and rate-limit responses.

## 16. API exploration

Swagger UI is available at:

```text
http://localhost:9008/swagger-ui.html
```

The generated OpenAPI document is available at:

```text
http://localhost:9008/v3/api-docs
```

The API documentation and endpoint definitions are generated from the current controller and OpenAPI annotations. Use Swagger for interactive discovery, but use environment-specific credentials and never paste production JWTs into shared documentation or source control.

## 17. Deployment checklist

Before deploying, set a strong secret for `JWT_SECRET`, configure a production MongoDB URI, verify MongoDB transaction support, and disable default-admin creation. Restrict Swagger and actuator endpoints if they are not intended for public access. Configure HTTPS at the edge, rotate credentials, use a shared rate-limit store for multiple instances, and send logs to a centralized system with access controls.

The first administrator should be provisioned through a controlled bootstrap procedure. If the optional default-admin bootstrap is enabled temporarily, supply its credentials through a secret manager, verify that the account exists, disable bootstrap creation, and rotate or remove the bootstrap secret.

## 18. Source references

This documentation is derived from the current repository implementation:

- [`application.yaml`](../src/main/resources/application.yaml)
- [`SecurityConfiguration.java`](../src/main/java/com/health/care/config/SecurityConfiguration.java)
- [`AuthController.java`](../src/main/java/com/health/care/rest/controller/AuthController.java)
- [`HealthcareController.java`](../src/main/java/com/health/care/rest/controller/HealthcareController.java)
- [`PlatformController.java`](../src/main/java/com/health/care/rest/controller/PlatformController.java)
- [`GlobalExceptionHandler.java`](../src/main/java/com/health/care/exceptions/GlobalExceptionHandler.java)
- [`HealthApiResponse.java`](../src/main/java/com/health/care/dtos/HealthApiResponse.java)
- [`MongoUserDetailsService.java`](../src/main/java/com/health/care/security/MongoUserDetailsService.java)
- [`JwtAuthenticationFilter.java`](../src/main/java/com/health/care/security/JwtAuthenticationFilter.java)
- [`pom.xml`](../pom.xml)
