# Healthcare Telemedicine Platform

A secure, scalable REST API for a healthcare telemedicine platform built with Spring Boot, featuring JWT-based authentication, MongoDB integration, and comprehensive API documentation.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Security](#security)
- [Logging](#logging)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Contributing](#contributing)

## 🎯 Overview

Healthcare Telemedicine is a modern API-first application designed to facilitate secure medical consultations and patient management. The platform prioritizes security with JWT-based authentication and comprehensive logging for audit trails.

## ✨ Features

- **JWT Authentication**: Secure token-based authentication with configurable expiration
- **User Management**: Registration and login endpoints with secure password encoding
- **API Documentation**: Interactive Swagger/OpenAPI UI for easy API exploration
- **Comprehensive Logging**: Structured logging throughout the application for debugging and auditing
- **MongoDB Integration**: Flexible NoSQL database for user and medical data storage
- **Security Best Practices**: 
  - No hardcoded credentials
  - Configurable default admin creation
  - Password encryption with BCrypt
  - CORS and CSRF protection
- **Health Monitoring**: Endpoint status and system health checks

## 🛠 Tech Stack

- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 4.0.7**: Rapid application development framework
- **Spring Security**: Comprehensive security framework
- **JWT (JJWT)**: JSON Web Token implementation
- **MongoDB**: NoSQL database
- **SpringDoc OpenAPI 3.0.2**: API documentation generation
- **Maven**: Build and dependency management
- **SLF4J/Logback**: Logging framework

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- MongoDB 4.0 or higher (local or Docker)
- Git

### Installation & Running

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Healthcare-Telemedicine
   ```

2. **Set up MongoDB** (using Docker)
   ```bash
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   ```

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or using the pre-built JAR:
   ```bash
   java -jar target/Healthcare-Telemedicine-0.0.1-SNAPSHOT.jar
   ```

5. **Access the application**
   - API Base URL: `http://localhost:9008/api`
   - Swagger UI: `http://localhost:9008/swagger-ui.html`
   - API Docs: `http://localhost:9008/v3/api-docs`

## 📚 API Documentation

### Interactive API Documentation

The application provides an interactive Swagger UI interface for exploring and testing all API endpoints:

**URL**: `http://localhost:9008/swagger-ui.html`

Features:
- Visual representation of all endpoints
- Request/response examples
- Built-in API testing tool
- Schema documentation

### API Endpoints

#### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Authenticate user and obtain JWT token |
| POST | `/api/auth/register` | Register a new user account |
| GET | `/api/auth/me` | Get current authenticated user info |

#### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hello` | Hello endpoint for authenticated users |

### Example Requests

**Register a new user:**
```bash
curl -X POST http://localhost:9008/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**Login:**
```bash
curl -X POST http://localhost:9008/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

**Access protected endpoint:**
```bash
curl -X GET http://localhost:9008/api/hello \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## ⚙️ Configuration

### Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database
MONGO_URI=mongodb://localhost:27017/Healthcare-Telemedicine

# JWT Configuration
JWT_SECRET=your-super-secret-key-minimum-32-characters
JWT_EXPIRATION_MS=86400000  # 24 hours
JWT_HEADER=Authorization
JWT_PREFIX=Bearer
JWT_ISSUER=healthcare-telemedicine

# Default Admin (only for local development)
APP_DEFAULT_ADMIN_CREATE=false
APP_DEFAULT_ADMIN_USERNAME=admin
APP_DEFAULT_ADMIN_PASSWORD=admin123
```

### application.yaml

```yaml
spring:
  application:
    name: Healthcare-Telemedicine
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017/Healthcare-Telemedicine}

server:
  port: 9008

app:
  jwt:
    secret: ${JWT_SECRET:change-me-change-me-change-me-change-me}
    expiration-ms: ${JWT_EXPIRATION_MS:86400000}
    header: ${JWT_HEADER:Authorization}
    prefix: ${JWT_PREFIX:Bearer}
    issuer: ${JWT_ISSUER:healthcare-telemedicine}
  default-admin:
    create: ${APP_DEFAULT_ADMIN_CREATE:false}
    username: ${APP_DEFAULT_ADMIN_USERNAME:}
    password: ${APP_DEFAULT_ADMIN_PASSWORD:}

logging:
  level:
    root: INFO
    com.health.care: DEBUG
```

### Profile-Specific Configuration

Create profile-specific configuration files:

**application-dev.yaml** (for local development):
```yaml
app:
  default-admin:
    create: true
    username: admin
    password: admin123

logging:
  level:
    com.health.care: DEBUG
    org.springframework.security: DEBUG
```

**application-prod.yaml** (for production):
```yaml
app:
  default-admin:
    create: false

logging:
  level:
    root: WARN
    com.health.care: INFO
```

Run with profile:
```bash
java -jar target/Healthcare-Telemedicine-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## 🔐 Security

### JWT Authentication Flow

1. User submits credentials to `/api/auth/login` or `/api/auth/register`
2. Server validates credentials and generates JWT token
3. Client includes token in `Authorization: Bearer <token>` header for protected requests
4. Server validates token signature and expiration
5. Request is processed if valid, rejected if invalid/expired

### Security Features

- **Password Encryption**: All passwords are hashed using BCrypt (Spring Security)
- **JWT Tokens**: Signed using HS256 algorithm with configurable secret key
- **Token Expiration**: Configurable TTL (default: 24 hours)
- **No Hardcoded Credentials**: All sensitive data must be provided via environment variables
- **Default Admin**: Optional, disabled by default, only for development
- **Input Validation**: Request validation using Jakarta Validation annotations
- **Error Handling**: Secure error messages that don't expose internal details

### HTTPS/TLS

In production, always use HTTPS:

```yaml
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

## 📊 Logging

### Logging Levels

- **DEBUG**: Detailed information for development and debugging
- **INFO**: General application flow and important events
- **WARN**: Potential issues and important alerts
- **ERROR**: Application errors and exceptions

### Logging Configuration

Logs are configured in `application.yaml`:

```yaml
logging:
  level:
    root: INFO
    com.health.care: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/healthcare-telemedicine.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 10
```

### Log Output

- **Console**: Real-time log output to console
- **File**: Rolling log files in `logs/` directory (max 10 files of 10MB each)

### Key Log Points

- **Authentication**: Login attempts, registration, token generation
- **Security**: Token validation, authorization failures
- **Business Logic**: User operations, API calls
- **Errors**: Exceptions with full stack traces

## 📁 Project Structure

```
Healthcare-Telemedicine/
├── src/
│   ├── main/
│   │   ├── java/com/health/care/
│   │   │   ├── Application.java                      # Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   ├── JwtProperties.java               # JWT configuration properties
│   │   │   │   ├── PasswordEncoderConfiguration.java # Password encryption setup
│   │   │   │   ├── SecurityConfiguration.java       # Spring Security configuration
│   │   │   │   └── OpenApiConfiguration.java        # Swagger/OpenAPI configuration
│   │   │   ├── dtos/
│   │   │   │   ├── AuthRequest.java                 # Login/register request DTO
│   │   │   │   └── AuthResponse.java                # Authentication response DTO
│   │   │   ├── security/
│   │   │   │   ├── InMemoryUserStore.java          # User store implementation
│   │   │   │   ├── JwtService.java                 # JWT token service
│   │   │   │   ├── JwtAuthenticationFilter.java    # JWT authentication filter
│   │   │   │   ├── UserDocument.java               # MongoDB user entity
│   │   │   │   └── UserRepository.java             # User data repository
│   │   │   └── rest/controller/
│   │   │       ├── AuthController.java             # Authentication endpoints
│   │   │       └── HelloController.java            # Health check endpoint
│   │   └── resources/
│   │       └── application.yaml                     # Spring Boot configuration
│   └── test/
│       ├── java/com/health/care/
│       │   ├── ApplicationTests.java                # Application context tests
│       │   └── JwtAuthenticationIntegrationTest.java # JWT authentication tests
│       └── resources/
├── pom.xml                                          # Maven configuration
├── README.md                                        # This file
├── ARCHITECTURE.md                                  # System architecture documentation
├── SETUP.md                                         # Development setup guide
└── logs/                                            # Application logs (created at runtime)
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthControllerTest

# Run with coverage report
mvn jacoco:report
```

### Test Classes

- **ApplicationTests.java**: Context loading and basic application tests
- **JwtAuthenticationIntegrationTest.java**: JWT authentication flow integration tests

### Writing Tests

Follow these conventions:
1. Test names should be descriptive: `testLoginWithValidCredentials_ShouldReturnToken`
2. Use AAA pattern: Arrange, Act, Assert
3. Mock external dependencies
4. Test both happy path and error scenarios

Example:
```java
@Test
void testLoginWithInvalidCredentials_ShouldThrowException() {
    // Arrange
    AuthRequest request = new AuthRequest("user", "wrongpassword");
    
    // Act & Assert
    assertThrows(BadCredentialsException.class, () -> {
        authController.login(request);
    });
}
```

## 🤝 Contributing

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass: `mvn test`
6. Commit your changes: `git commit -m 'Add amazing feature'`
7. Push to the branch: `git push origin feature/amazing-feature`
8. Open a Pull Request

### Code Style

- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add Javadoc for public methods and classes
- Keep methods small and focused (max 20 lines)

### Pre-commit Checklist

- [ ] Code compiles without warnings: `mvn clean compile`
- [ ] All tests pass: `mvn test`
- [ ] No code style violations: `mvn checkstyle:check`
- [ ] New features have documentation
- [ ] Logging is added for important operations

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support, issues, or questions:
- Create an issue on GitHub
- Email: support@healthcare-telemedicine.com
- Documentation: See ARCHITECTURE.md and SETUP.md

## 🔄 Changelog

### Version 1.0.0 (Current)
- Initial release with JWT authentication
- User registration and login
- MongoDB integration
- Comprehensive API documentation with Swagger
- Structured logging throughout application
- Secure credential management
- Configurable default admin user

---

**Last Updated**: August 2, 2026

