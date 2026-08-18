# Healthcare Telemedicine - API Reference

Complete API documentation for Healthcare Telemedicine platform.

## 🔐 Authentication

All protected endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

### Token Format

```
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsImlzcyI6ImhlYWx0aGNhcmUtdGVsZW1lZGljaW5lIiwiaWF0IjoxNjIyNTQ4ODAwLCJleHAiOjE2MjI2MzUyMDB9.sig
```

Token expires after 24 hours (configurable via `JWT_EXPIRATION_MS`).

## 📋 Endpoints

### 1. User Registration

**Endpoint**: `POST /api/auth/register`

**Description**: Register a new user account with username and password.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "SecurePassword123"
}
```

**Request Body Schema**:
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| username | string | Yes | Must not be blank, must be unique |
| password | string | Yes | Must not be blank, min 6 characters (recommended) |

**Success Response** (HTTP 200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**Response Field Descriptions**:
| Field | Type | Description |
|-------|------|-------------|
| token | string | JWT token for authentication |
| tokenType | string | Token type (always "Bearer") |
| expiresIn | number | Token expiration time in milliseconds |

**Error Responses**:

```json
// 400 Bad Request - Invalid input
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register"
}
```

```json
// 409 Conflict - User already exists
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "User already exists: john_doe",
  "path": "/api/auth/register"
}
```

**Example with cURL**:
```bash
curl -X POST http://localhost:9008/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePassword123"
  }'
```

**Example with JavaScript**:
```javascript
const response = await fetch('http://localhost:9008/api/auth/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'john_doe',
    password: 'SecurePassword123'
  })
});

const data = await response.json();
const token = data.token; // Use this token for future requests
```

**Example with Python**:
```python
import requests

response = requests.post(
    'http://localhost:9008/api/auth/register',
    json={
        'username': 'john_doe',
        'password': 'SecurePassword123'
    }
)

if response.status_code == 200:
    token = response.json()['token']
    print(f"Token: {token}")
else:
    print(f"Error: {response.status_code} - {response.text}")
```

---

### 2. User Login

**Endpoint**: `POST /api/auth/login`

**Description**: Authenticate user with credentials and obtain JWT token.

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "john_doe",
  "password": "SecurePassword123"
}
```

**Request Body Schema**:
| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| username | string | Yes | Must not be blank |
| password | string | Yes | Must not be blank |

**Success Response** (HTTP 200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**Error Responses**:

```json
// 400 Bad Request - Missing fields
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/login"
}
```

```json
// 401 Unauthorized - Invalid credentials
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

**Example with cURL**:
```bash
curl -X POST http://localhost:9008/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePassword123"
  }' | jq -r '.token'
```

**Example with JavaScript**:
```javascript
async function login(username, password) {
  const response = await fetch('http://localhost:9008/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });

  if (!response.ok) throw new Error('Login failed');
  
  const data = await response.json();
  // Store token in localStorage for subsequent requests
  localStorage.setItem('authToken', data.token);
  return data.token;
}

const token = await login('john_doe', 'SecurePassword123');
```

---

### 3. Get Current User

**Endpoint**: `GET /api/auth/me`

**Description**: Get information about the currently authenticated user.

**Authentication**: Required (Bearer token)

**Request Headers**:
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

**Success Response** (HTTP 200):
```json
"john_doe"
```

**Error Responses**:

```json
// 401 Unauthorized - No token or invalid token
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/auth/me"
}
```

**Example with cURL**:
```bash
curl -X GET http://localhost:9008/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example with JavaScript**:
```javascript
async function getCurrentUser(token) {
  const response = await fetch('http://localhost:9008/api/auth/me', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });

  if (!response.ok) throw new Error('Failed to get user info');
  
  const username = await response.text();
  return username;
}

const token = localStorage.getItem('authToken');
const username = await getCurrentUser(token);
console.log(`Logged in as: ${username}`);
```

---

### 4. Hello Endpoint

**Endpoint**: `GET /api/hello`

**Description**: Health check endpoint that returns a greeting for authenticated user.

**Authentication**: Required (Bearer token)

**Request Headers**:
```
Authorization: Bearer <jwt-token>
```

**Success Response** (HTTP 200):
```json
"Hello, john_doe"
```

**Error Responses**:

```json
// 401 Unauthorized - No token or invalid token
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Unauthorized",
  "path": "/api/hello"
}
```

**Example with cURL**:
```bash
curl -X GET http://localhost:9008/api/hello \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Example with JavaScript**:
```javascript
async function getGreeting(token) {
  const response = await fetch('http://localhost:9008/api/hello', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) throw new Error('Request failed');
  
  const greeting = await response.text();
  return greeting;
}

const token = localStorage.getItem('authToken');
const greeting = await getGreeting(token);
console.log(greeting); // Output: "Hello, john_doe"
```

---

## 📊 HTTP Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 200 | OK | Request successful |
| 400 | Bad Request | Invalid request parameters or validation failed |
| 401 | Unauthorized | Authentication required or failed |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., duplicate user) |
| 500 | Internal Server Error | Unexpected server error |

## 🔄 Common Use Cases

### Complete Authentication Flow

**Step 1: Register**
```bash
curl -X POST http://localhost:9008/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "pass123"}'

# Response: {"token": "...", "tokenType": "Bearer", "expiresIn": 86400000}
```

**Step 2: Use Token**
```bash
TOKEN="<token-from-step-1>"

# Access protected endpoint
curl -X GET http://localhost:9008/api/hello \
  -H "Authorization: Bearer $TOKEN"

# Response: "Hello, user1"
```

### Re-authentication Flow

**Get New Token**
```bash
curl -X POST http://localhost:9008/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "pass123"}'

# Response: {"token": "...", "tokenType": "Bearer", "expiresIn": 86400000}
```

## 🛡️ Rate Limiting & Throttling

Currently, no rate limiting is implemented. For production, consider:
- Implement rate limiting middleware
- Use API gateway with rate limiting
- Add request throttling per user/IP

## 🔒 Security Headers

Recommended headers for production:

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

## 📦 Response Content Types

All responses are in JSON format:

```
Content-Type: application/json; charset=utf-8
```

## 🔍 Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descriptive error message",
  "path": "/api/endpoint"
}
```

## 📝 Request/Response Examples by Language

### Java
```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

var client = HttpClient.newHttpClient();
var request = HttpRequest.newBuilder()
  .uri(URI.create("http://localhost:9008/api/hello"))
  .header("Authorization", "Bearer " + token)
  .build();

var response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.body());
```

### Python
```python
import requests

headers = {
    'Authorization': f'Bearer {token}',
    'Content-Type': 'application/json'
}

response = requests.get('http://localhost:9008/api/hello', headers=headers)
print(response.json())
```

### JavaScript/TypeScript
```typescript
interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
}

async function register(username: string, password: string): Promise<string> {
  const response = await fetch('http://localhost:9008/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });

  const data: AuthResponse = await response.json();
  return data.token;
}
```

## 🧪 Testing with Postman

### Import Collection

1. Download [Postman](https://www.postman.com/)
2. Create new collection "Healthcare API"
3. Add requests:
   - POST /api/auth/register
   - POST /api/auth/login
   - GET /api/auth/me
   - GET /api/hello

### Environment Variables in Postman

Create environment with variables:
```
base_url: http://localhost:9008
token: (set after login)
```

Use in requests:
```
{{base_url}}/api/hello
Authorization: Bearer {{token}}
```

## 🔗 API Gateway Integration

For production with API Gateway:

```yaml
# Example API Gateway config
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: healthcare-api
spec:
  hosts:
  - "api.healthcare-telemedicine.com"
  http:
  - match:
    - uri:
        prefix: "/api/"
    route:
    - destination:
        host: healthcare-telemedicine
        port:
          number: 9008
```

## 📚 Related Documentation

- [README.md](README.md) - Getting started guide
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture
- [SETUP.md](SETUP.md) - Development setup guide
- [Swagger UI](http://localhost:9008/swagger-ui.html) - Interactive API documentation
- [OpenAPI Spec](http://localhost:9008/v3/api-docs) - Machine-readable API definition

---

**Last Updated**: August 2, 2026
**API Version**: 1.0.0
**Base URL**: `http://localhost:9008/api`

