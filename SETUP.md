# Healthcare Telemedicine - Development Setup Guide

Complete guide for setting up the Healthcare Telemedicine development environment.

## 📋 Prerequisites

### System Requirements
- **OS**: Windows, macOS, or Linux
- **Java**: JDK 21 or higher
- **Maven**: 3.6 or higher
- **Git**: Latest version
- **Docker** (optional, for MongoDB): Latest version

### Check Your Environment

**Windows PowerShell**:
```powershell
# Check Java version
java -version

# Check Maven version
mvn --version

# Check Git version
git --version

# Check Docker (if using Docker for MongoDB)
docker --version
```

**macOS/Linux**:
```bash
java -version
mvn --version
git --version
docker --version  # optional
```

## 🔧 Step 1: Install Java 21

### Windows

1. Download Java 21 JDK from [oracle.com](https://www.oracle.com/java/technologies/downloads/)
2. Run the installer
3. Set `JAVA_HOME` environment variable:
   - Right-click "This PC" → Properties → Advanced system settings → Environment Variables
   - New Variable: `JAVA_HOME` = `C:\Program Files\Java\jdk-21`
   - Add `%JAVA_HOME%\bin` to PATH

4. Verify installation:
   ```powershell
   java -version
   ```

### macOS

```bash
# Using Homebrew
brew tap homebrew/cask-versions
brew install java21

# Or download from oracle.com and run the .dmg file

# Verify
java -version
```

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install openjdk-21-jdk

# Verify
java -version
```

## 🏗 Step 2: Install Maven

### Windows

1. Download Maven from [maven.apache.org](https://maven.apache.org/download.cgi)
2. Extract to a folder (e.g., `C:\apache-maven-3.9.0`)
3. Set `MAVEN_HOME` environment variable:
   - `MAVEN_HOME` = `C:\apache-maven-3.9.0`
   - Add `%MAVEN_HOME%\bin` to PATH

4. Verify installation:
   ```powershell
   mvn --version
   ```

### macOS

```bash
# Using Homebrew
brew install maven

# Verify
mvn --version
```

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install maven

# Verify
mvn --version
```

## 💾 Step 3: Set Up MongoDB

### Option A: Using Docker (Recommended)

**Prerequisites**: Docker installed

**Windows PowerShell**:
```powershell
# Pull MongoDB image
docker pull mongo:latest

# Run MongoDB container
docker run -d `
  -p 27017:27017 `
  --name healthcare-mongodb `
  -e MONGO_INITDB_ROOT_USERNAME=admin `
  -e MONGO_INITDB_ROOT_PASSWORD=password `
  mongo:latest

# Check if running
docker ps | findstr healthcare-mongodb

# View logs
docker logs healthcare-mongodb

# Stop container
docker stop healthcare-mongodb

# Remove container
docker rm healthcare-mongodb
```

**macOS/Linux**:
```bash
# Pull MongoDB image
docker pull mongo:latest

# Run MongoDB container
docker run -d \
  -p 27017:27017 \
  --name healthcare-mongodb \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password \
  mongo:latest

# Check if running
docker ps | grep healthcare-mongodb

# View logs
docker logs healthcare-mongodb
```

### Option B: Local MongoDB Installation

**Windows**:
1. Download from [mongodb.com/try/download/community](https://www.mongodb.com/try/download/community)
2. Run MSI installer
3. MongoDB runs as Windows Service by default
4. Default connection: `mongodb://localhost:27017`

**macOS**:
```bash
# Using Homebrew
brew tap mongodb/brew
brew install mongodb-community

# Start MongoDB
brew services start mongodb-community

# Check status
brew services list
```

**Linux (Ubuntu)**:
```bash
curl -fsSL https://www.mongodb.org/static/pgp/server-7.0.asc | sudo gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg

echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] http://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list

sudo apt-get update
sudo apt-get install -y mongodb-org

# Start MongoDB
sudo systemctl start mongod
sudo systemctl status mongod
```

### Verify MongoDB Connection

**Using MongoDB Shell**:
```bash
# Install mongosh (MongoDB shell)
# Windows: chocolatey or direct download
# macOS: brew install mongosh
# Linux: sudo apt install mongosh

mongosh "mongodb://localhost:27017"
```

## 🚀 Step 4: Clone & Setup Project

### Clone Repository

```powershell
# Windows PowerShell
git clone <repository-url>
cd Healthcare-Telemedicine
```

```bash
# macOS/Linux
git clone <repository-url>
cd Healthcare-Telemedicine
```

### Create Environment Configuration

**Option A: Environment Variables (Recommended)**

**Windows PowerShell**:
```powershell
# Set environment variables for this session
$env:MONGO_URI = "mongodb://localhost:27017/Healthcare-Telemedicine"
$env:JWT_SECRET = "your-super-secret-key-minimum-32-characters-long-secret-key"
$env:JWT_EXPIRATION_MS = "86400000"
$env:JWT_HEADER = "Authorization"
$env:JWT_PREFIX = "Bearer"
$env:JWT_ISSUER = "healthcare-telemedicine"
$env:APP_DEFAULT_ADMIN_CREATE = "false"

# To persist these variables, use:
# [Environment]::SetEnvironmentVariable("MONGO_URI", "mongodb://localhost:27017/...", "User")
```

**macOS/Linux**:
```bash
# Add to ~/.bash_profile or ~/.bashrc
export MONGO_URI="mongodb://localhost:27017/Healthcare-Telemedicine"
export JWT_SECRET="your-super-secret-key-minimum-32-characters-long-secret-key"
export JWT_EXPIRATION_MS="86400000"
export JWT_HEADER="Authorization"
export JWT_PREFIX="Bearer"
export JWT_ISSUER="healthcare-telemedicine"
export APP_DEFAULT_ADMIN_CREATE="false"

# Reload shell
source ~/.bash_profile
```

**Option B: Create .env File**

Create `.env.local` file in project root (not committed to git):
```
MONGO_URI=mongodb://localhost:27017/Healthcare-Telemedicine
JWT_SECRET=your-super-secret-key-minimum-32-characters-long-secret-key
JWT_EXPIRATION_MS=86400000
JWT_HEADER=Authorization
JWT_PREFIX=Bearer
JWT_ISSUER=healthcare-telemedicine
APP_DEFAULT_ADMIN_CREATE=false
```

## 🔨 Step 5: Build Project

```bash
# Clean and build
mvn clean install

# Build without running tests (faster)
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl healthcare-telemedicine

# View build details
mvn clean install -X
```

### Troubleshooting Build Issues

**Issue**: "JAVA_HOME is not defined"
```bash
# Check if JAVA_HOME is set
echo $JAVA_HOME

# Set manually (temporary)
export JAVA_HOME="/path/to/jdk-21"
```

**Issue**: "Cannot connect to MongoDB"
- Verify MongoDB is running: `docker ps` or service status
- Check connection string in environment variables
- Check firewall isn't blocking port 27017

**Issue**: "Maven cache corrupted"
```bash
# Remove Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install
```

## ▶️ Step 6: Run Application

### Option A: Using Maven

**Development Mode (with auto-reload)**:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Standard Run**:
```bash
mvn spring-boot:run
```

### Option B: Using Pre-built JAR

```bash
# Run with default profile
java -jar target/Healthcare-Telemedicine-0.0.1-SNAPSHOT.jar

# Run with dev profile
java -jar target/Healthcare-Telemedicine-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Run with environment variables
java -Djava.env.MONGO_URI=mongodb://localhost:27017/... \
     -jar target/Healthcare-Telemedicine-0.0.1-SNAPSHOT.jar
```

### Verify Application Started

Look for this in the console output:
```
...
Started Application in X.XXX seconds (process running for X.XXX)
```

## 🌐 Access Application

Once running, access via:

| Resource | URL |
|----------|-----|
| API Base | `http://localhost:9008/api` |
| Swagger UI | `http://localhost:9008/swagger-ui.html` |
| API Docs (JSON) | `http://localhost:9008/v3/api-docs` |
| Actuator Health | `http://localhost:9008/actuator/health` |

## 🧪 Running Tests

### Run All Tests

```bash
# Run all tests
mvn test

# Run with coverage
mvn jacoco:report

# View coverage report
# Windows: start target/site/jacoco/index.html
# macOS: open target/site/jacoco/index.html
# Linux: xdg-open target/site/jacoco/index.html
```

### Run Specific Tests

```bash
# Run single test class
mvn test -Dtest=AuthControllerTest

# Run tests matching pattern
mvn test -Dtest=*Integration*

# Run single test method
mvn test -Dtest=AuthControllerTest#testLogin
```

## 📝 IDE Setup

### IntelliJ IDEA

1. **Open Project**:
   - File → Open → Select `Healthcare-Telemedicine` folder
   - IntelliJ auto-detects Maven project

2. **Configure JDK**:
   - File → Project Structure → Project → SDK
   - Select or download JDK 21

3. **Run Configuration**:
   - Run → Edit Configurations → Add "Maven"
   - Command line: `spring-boot:run`

4. **Debug**:
   - Set breakpoint (click on line number)
   - Run → Debug → Select configuration
   - Step through code

### VS Code

1. **Install Extensions**:
   - Extension Pack for Java (Microsoft)
   - Spring Boot Extension Pack (Pivotal)
   - REST Client (for testing APIs)

2. **Configure Launch**:
   - Create `.vscode/launch.json`:
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Spring Boot App",
         "request": "launch",
         "mainClass": "com.health.care.Application",
         "projectName": "Healthcare-Telemedicine",
         "preLaunchTask": "build"
       }
     ]
   }
   ```

### Eclipse

1. Import as Maven Project:
   - File → Import → Maven → Existing Maven Projects
   - Select project root

2. Configure JDK:
   - Window → Preferences → Java → Installed JREs
   - Add JDK 21

3. Run Spring Boot Application:
   - Right-click project → Run As → Spring Boot App

## 🔗 Testing API Endpoints

### Using cURL

**Register User**:
```bash
curl -X POST http://localhost:9008/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@1234"
  }'
```

**Login**:
```bash
curl -X POST http://localhost:9008/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test@1234"
  }'
```

**Access Protected Endpoint**:
```bash
curl -X GET http://localhost:9008/api/hello \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Using Postman

1. Download [Postman](https://www.postman.com/downloads/)
2. Create new requests
3. Set endpoint URL, method, headers, body
4. Send and view response
5. Save requests in collections for reuse

### Using REST Client (VS Code)

Create `requests.rest` file:
```rest
### Register
POST http://localhost:9008/api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "Test@1234"
}

### Login
POST http://localhost:9008/api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "Test@1234"
}

### Hello (protected)
GET http://localhost:9008/api/hello
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

## 🛑 Stopping Application

**In Terminal** (where app is running):
- Press `Ctrl+C` to stop

**Docker MongoDB**:
```bash
docker stop healthcare-mongodb
```

**MongoDB Service**:
```bash
# Windows
net stop MongoDB

# macOS
brew services stop mongodb-community

# Linux
sudo systemctl stop mongod
```

## 🐛 Debugging

### View Application Logs

**Console Output**: Logs appear in running terminal

**Log File**: Check `logs/healthcare-telemedicine.log`

**View Log File** (macOS/Linux):
```bash
tail -f logs/healthcare-telemedicine.log
```

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Connection refused" | MongoDB not running, check port 27017 |
| "401 Unauthorized" | JWT token missing or expired |
| "400 Bad Request" | Check JSON format, required fields |
| "409 Conflict" | User already exists, use different username |
| "JAVA_HOME not set" | Set JAVA_HOME environment variable |
| "Port 9008 in use" | Change `server.port` in application.yaml |

## 📚 Useful Commands Reference

```bash
# Project
mvn clean install              # Build project
mvn clean install -DskipTests  # Build without tests
mvn test                       # Run tests
mvn spring-boot:run           # Run application

# Database
docker pull mongo:latest                        # Pull latest MongoDB
docker run -d -p 27017:27017 mongo              # Run MongoDB
docker ps                                       # List containers
docker logs <container-name>                    # View logs
docker stop <container-name>                    # Stop container

# Git
git clone <url>               # Clone repository
git status                    # Check status
git pull origin main          # Pull latest changes
git checkout -b feature/name  # Create feature branch
git add .                     # Stage changes
git commit -m "message"       # Commit changes
git push origin feature/name  # Push to remote
```

## 🔗 Useful Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [MongoDB Documentation](https://docs.mongodb.com/)
- [JWT Introduction](https://jwt.io/introduction)
- [Maven Getting Started](https://maven.apache.org/guides/getting-started/)
- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)

## ❓ Getting Help

1. Check logs for error messages
2. Review ARCHITECTURE.md for system design
3. Check README.md for API usage
4. Search in project documentation
5. Create an issue on GitHub with:
   - Error message and stack trace
   - Steps to reproduce
   - Environment details (OS, Java version, etc.)

---

**Last Updated**: August 2, 2026
**Tested On**: Java 21, Maven 3.9+, MongoDB 7.0+, Docker 24.0+

