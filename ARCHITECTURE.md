# Healthcare Telemedicine - Architecture Documentation

## System Architecture Overview

Healthcare Telemedicine is built following a layered architecture pattern with clear separation of concerns. This document describes the system design, components, and data flow.

## 🏗 Architecture Layers

```
┌─────────────────────────────────────────────┐
│           REST API Controllers              │
│    (AuthController, HelloController)        │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│      Security & Authentication Layer        │
│  (JwtAuthenticationFilter, SecurityConfig)  │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│        Business Logic / Service Layer       │
│  (InMemoryUserStore, JwtService)            │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│          Data Access / Persistence          │
│    (UserRepository, MongoDB)                │
└─────────────────────────────────────────────┘
```

## 🔐 Security Architecture

### Authentication Flow

```
┌─────────────────────────────────────────────────────────┐
│  Client Request (Credentials or Protected Resource)    │
└────────────────┬────────────────────────────────────────┘
                 │
        ┌────────▼─────────┐
        │ Login/Register?  │
        └────────┬────┬────┘
                 │    │
           YES  │    │  NO (Protected Resource)
                │    │
    ┌───────────▼─┐  └──────────────────────┐
    │ Authenticate│                         │
    │ Credentials │          ┌──────────────▼────────┐
    └───────┬─────┘          │ Extract JWT from      │
            │                │ Authorization Header  │
            │                └──────────────┬────────┘
            │                               │
            │       ┌───────────────────────▼───────┐
            │       │  Validate JWT Signature       │
            │       │  & Expiration                 │
            │       └───────┬───────────────────────┘
            │               │
    ┌───────▼──────────────▼────────────┐
    │ Create Security Context           │
    │ (Set Principal/Authorities)       │
    └───────┬──────────────────────────┘
            │
    ┌───────▼──────────────────────────┐
    │ Process Request / Generate Token  │
    └───────┬──────────────────────────┘
            │
    ┌───────▼──────────────────────────┐
    │ Return Response with JWT Token   │
    │ (for login/register)             │
    └───────────────────────────────────┘
```

### JWT Token Structure

```
Header.Payload.Signature

Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "username",
  "iss": "healthcare-telemedicine",
  "iat": 1622548800,
  "exp": 1622635200
}

Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

## 📦 Component Architecture

### 1. REST API Layer

**Controllers**:
- `AuthController`: Handles authentication endpoints (login, register, current user)
- `HelloController`: Health check and API availability endpoint

**Responsibilities**:
- Accept HTTP requests
- Validate input using annotations
- Delegate to business logic
- Return appropriate HTTP responses
- Log request/response details

### 2. Security Layer

**Components**:
- `JwtAuthenticationFilter`: Intercepts requests to validate JWT tokens
- `SecurityConfiguration`: Spring Security configuration
- `PasswordEncoderConfiguration`: BCrypt password encoding setup
- `JwtProperties`: JWT configuration properties

**Responsibilities**:
- Request filtering and token validation
- Security context establishment
- Password encoding and verification
- CORS and CSRF protection

### 3. Service Layer

**Components**:
- `JwtService`: JWT token generation and validation
- `InMemoryUserStore`: User authentication and registration
- `UserRepository`: User data access

**Responsibilities**:
- Business logic implementation
- User management
- Token lifecycle management
- Data validation and transformation

### 4. Data Access Layer

**Components**:
- `UserDocument`: MongoDB entity representing a user
- `UserRepository`: Spring Data MongoDB repository interface

**Responsibilities**:
- Database operations (CRUD)
- Query generation
- Entity persistence

**Database Schema**:
```
User Collection
├── _id: ObjectId
├── username: String (unique)
├── password: String (encoded)
└── roles: Array<String>
```

## 🔄 Request Processing Flow

### Login/Register Flow

```
1. Client Request
   POST /api/auth/login
   Content: {"username": "user", "password": "pass"}
           ↓
2. AuthController.login()
   - Validates input (NotBlank)
   - Logs login attempt
           ↓
3. AuthenticationManager.authenticate()
   - Loads user from InMemoryUserStore
   - Compares passwords
   - Creates Authentication object
           ↓
4. InMemoryUserStore.loadUserByUsername()
   - Queries UserRepository
   - Returns UserDetails with roles
           ↓
5. JwtService.generateToken()
   - Creates JWT payload
   - Signs with secret key
   - Returns encoded token
           ↓
6. Response
   {"token": "...", "tokenType": "Bearer", "expiresIn": 86400000}
```

### Protected Resource Access Flow

```
1. Client Request
   GET /api/hello
   Authorization: Bearer <token>
           ↓
2. JwtAuthenticationFilter.doFilterInternal()
   - Extracts token from header
           ↓
3. JwtService.isTokenValid()
   - Verifies signature
   - Checks expiration
   - Returns true/false
           ↓
4. JwtService.extractUsername()
   - Parses JWT claims
   - Returns username
           ↓
5. InMemoryUserStore.loadUserByUsername()
   - Loads user details
   - Returns authorities
           ↓
6. SecurityContextHolder.setAuthentication()
   - Sets UsernamePasswordAuthenticationToken
   - Makes user available to controller
           ↓
7. HelloController.hello()
   - Accesses authentication from context
   - Returns response
           ↓
8. Response
   {"message": "Hello, username"}
```

## 🗂 Configuration Management

### Configuration Hierarchy

```
1. Default Values (hardcoded in code)
   ↓
2. application.yaml (property file)
   ↓
3. application-{profile}.yaml (profile-specific)
   ↓
4. Environment Variables (highest priority)
```

### Configuration Properties

| Property | Type | Default | Environment Variable |
|----------|------|---------|----------------------|
| `app.jwt.secret` | String | "change-me..." | `JWT_SECRET` |
| `app.jwt.expiration-ms` | Long | 86400000 | `JWT_EXPIRATION_MS` |
| `app.jwt.header` | String | "Authorization" | `JWT_HEADER` |
| `app.jwt.prefix` | String | "Bearer" | `JWT_PREFIX` |
| `app.jwt.issuer` | String | "healthcare-telemedicine" | `JWT_ISSUER` |
| `app.default-admin.create` | Boolean | false | `APP_DEFAULT_ADMIN_CREATE` |
| `app.default-admin.username` | String | "" | `APP_DEFAULT_ADMIN_USERNAME` |
| `app.default-admin.password` | String | "" | `APP_DEFAULT_ADMIN_PASSWORD` |
| `spring.data.mongodb.uri` | String | "mongodb://localhost:27017/..." | `MONGO_URI` |

## 📊 Data Model

### User Entity

```java
UserDocument {
    ObjectId _id;           // MongoDB auto-generated ID
    String username;        // Unique username
    String password;        // BCrypt encoded password
    List<String> roles;     // User roles ["ROLE_USER", "ROLE_ADMIN"]
}
```

## 🔌 Integration Points

### External Services

1. **MongoDB Database**
   - Connection: Spring Data MongoDB
   - Protocol: MongoDB wire protocol
   - Authentication: Optional (uri-based)

2. **Spring Security**
   - Integration: SecurityConfiguration bean
   - Provides: Authentication, Authorization, Password encoding

3. **JJWT Library**
   - Integration: JwtService
   - Provides: JWT token creation and validation

## 📈 Scalability Considerations

### Current Limitations
- Single node application
- In-memory user store (uses MongoDB as backend)
- No caching mechanism
- JWT validation on every request

### Future Improvements
- Add Redis for token blacklisting
- Implement API rate limiting
- Add distributed caching (Redis/Memcached)
- Implement microservices architecture
- Add database replication and sharding
- Load balancing with multiple instances

## 🔍 Error Handling Strategy

### Exception Handling

```
1. Validation Exception (Input validation failed)
   └─> HTTP 400 (Bad Request)

2. Authentication Exception (Bad credentials)
   └─> HTTP 401 (Unauthorized)

3. Authorization Exception (Insufficient permissions)
   └─> HTTP 403 (Forbidden)

4. Resource Not Found
   └─> HTTP 404 (Not Found)

5. Duplicate Resource (User already exists)
   └─> HTTP 409 (Conflict)

6. Internal Server Error
   └─> HTTP 500 (Internal Server Error)
```

### Logging Strategy

Each layer logs relevant information:

**REST Layer**: Request details, parameters, timing
**Security Layer**: Authentication attempts, token validation
**Service Layer**: Business logic execution, state changes
**Data Layer**: Database operations, queries

## 🔐 Security Considerations

### Key Security Features

1. **No Hardcoded Credentials**
   - All secrets come from environment variables
   - Configuration files contain no sensitive data

2. **Password Security**
   - Passwords hashed with BCrypt
   - Never stored in plain text
   - Never logged or exposed

3. **JWT Security**
   - Signed with HS256 algorithm
   - 32-character minimum secret key
   - Configurable expiration

4. **Default Admin Protection**
   - Disabled by default
   - Requires explicit environment variable
   - Logged when created
   - Recommended to rotate after initial setup

5. **Request Validation**
   - All inputs validated with constraints
   - Prevents injection attacks
   - Type and format checking

## 🚀 Deployment Architecture

### Development Environment
```
Client Application
    ↓
Spring Boot (dev profile)
    ↓
MongoDB (local/Docker)
```

### Production Environment
```
Load Balancer
    ↓
Spring Boot Instance 1
Spring Boot Instance 2  ──→ MongoDB Cluster (with replication)
Spring Boot Instance N
    ↓
CDN/Cache (Redis)
```

## 📚 Related Documentation

- [README.md](README.md): Getting started and API usage
- [SETUP.md](SETUP.md): Development environment setup
- See inline code comments for implementation details
- API Docs: [Swagger UI](http://localhost:9008/swagger-ui.html)

---

**Last Updated**: August 2, 2026
**Version**: 1.0.0

