# Design Document: Payback API Initialization

## Overview

This design document specifies the implementation of a Spring Boot REST API initialization for the Payback system. The API will be built using Spring Boot 3.2 with Java 21, providing a foundational structure for a production-ready service with PostgreSQL database integration, health monitoring capabilities, and JWT authentication with extended token lifetime.

The system follows a standard Spring Boot architecture with Maven for dependency management, Docker Compose for local database provisioning, and RESTful endpoints for health monitoring. The JWT authentication system is configured with a 7-day token expiration to improve user experience while maintaining security.

## Architecture

### Technology Stack

- **Framework**: Spring Boot 3.2
- **Language**: Java 21
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **Container Orchestration**: Docker Compose
- **Authentication**: JWT (JSON Web Tokens)

### Architectural Layers

1. **Controller Layer**: REST endpoints for API access
   - HealthController: Provides health check functionality

2. **Configuration Layer**: Application properties and environment configuration
   - Database connection settings
   - Server configuration
   - JWT token settings

3. **Infrastructure Layer**: External dependencies
   - PostgreSQL database via Docker
   - Spring Boot embedded server

### Project Structure

```
payback-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── payback/
│   │   │           └── api/
│   │   │               ├── PaybackApplication.java
│   │   │               └── controller/
│   │   │                   └── HealthController.java
│   │   └── resources/
│   │       └── application.properties
├── docker-compose.yml
└── pom.xml
```

## Components and Interfaces

### 1. PaybackApplication

**Purpose**: Main entry point for the Spring Boot application

**Implementation**:
- Class location: `com.payback.api.PaybackApplication`
- Annotation: `@SpringBootApplication`
- Method: `public static void main(String[] args)` - calls `SpringApplication.run()`

**Responsibilities**:
- Initialize Spring application context
- Bootstrap all Spring components
- Start embedded Tomcat server on port 8080

### 2. HealthController

**Purpose**: Provides health check endpoint for monitoring

**Implementation**:
- Class location: `com.payback.api.controller.HealthController`
- Annotation: `@RestController`
- Base mapping: `/api/v1`

**Endpoint**:
```
GET /api/v1/health
Response: 200 OK
Content-Type: application/json
Body: {
  "status": "UP",
  "database": "CONNECTED"
}
```

**Responsibilities**:
- Return API operational status
- Indicate database connectivity
- Provide monitoring integration point

### 3. Maven Configuration (pom.xml)

**Dependencies**:
- `spring-boot-starter-web`: REST API capabilities
- `spring-boot-starter-data-jpa`: Database ORM
- `postgresql`: PostgreSQL JDBC driver
- `lombok`: Boilerplate code reduction

**Build Configuration**:
- Spring Boot version: 3.2
- Java version: 21
- Maven compiler plugin configuration

### 4. Application Configuration

**File**: `src/main/resources/application.properties`

**Database Configuration**:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/payback_db
spring.datasource.username=admin
spring.datasource.password=password123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Server Configuration**:
```properties
server.port=8080
```

**JWT Configuration**:
```properties
jwt.expiration=${JWT_EXPIRATION:604800000}
```

**Configuration Details**:
- Database URL points to localhost:5432 (Docker container internal port)
- Hibernate DDL auto-update enables automatic schema updates
- SQL logging enabled for development visibility
- JWT expiration set to 604800000ms (7 days) with environment variable override support

### 5. Docker Compose Configuration

**File**: `docker-compose.yml`

**PostgreSQL Service**:
```yaml
services:
  postgres:
    image: postgres:15
    container_name: payback_postgres
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: payback_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password123
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

**Configuration Details**:
- Host port 5433 maps to container port 5432 (avoids conflicts with local PostgreSQL)
- Named volume ensures data persistence across container restarts
- Environment variables match application.properties configuration

### 6. JWT Token Expiry Configuration

**Purpose**: Extend JWT token lifetime from 24 hours to 7 days for improved user experience

**Implementation Approach**:

The JWT token expiration is configured through the `application.properties` file using a configurable property that can be overridden via environment variables.

**Configuration Property**:
- Property name: `jwt.expiration`
- Default value: `604800000` (milliseconds)
- Calculation: 7 days × 24 hours × 60 minutes × 60 seconds × 1000 milliseconds = 604,800,000ms
- Environment variable override: `JWT_EXPIRATION`

**JwtService Integration**:
The JwtService class (to be implemented in authentication feature) should read this property using Spring's `@Value` annotation:

```java
@Value("${jwt.expiration}")
private Long jwtExpirationMs;
```

**Token Generation**:
When generating JWT tokens, the expiration time is calculated as:
```
expirationDate = currentTime + jwtExpirationMs
```

**Backward Compatibility**:
- Existing tokens with 24-hour expiry will continue to work until they naturally expire
- New tokens issued after configuration change will have 7-day expiry
- Token validation logic remains unchanged - only the expiration claim value differs
- No database migrations or token revocation needed

**Security Considerations**:
- **Increased Exposure Window**: Longer token lifetime means compromised tokens remain valid for 7 days instead of 24 hours
- **Trade-off**: Improved user experience (fewer login prompts) vs slightly increased security risk
- **Mitigations**:
  - Ensure HTTPS is enforced for all API communication
  - Implement secure token storage in frontend (httpOnly cookies or secure storage)
  - Consider implementing token refresh mechanism in future iterations
  - Monitor for suspicious token usage patterns
  - Maintain ability to revoke tokens if needed (future enhancement)

**Environment Variable Override**:
The configuration supports environment-based override for different deployment environments:
- Development: Use default 7-day expiration
- Production: Can override via `JWT_EXPIRATION` environment variable if different policy needed
- Testing: Can set shorter expiration for faster test cycles

## Data Models

### Health Response Model

**Structure**:
```json
{
  "status": "string",
  "database": "string"
}
```

**Field Specifications**:
- `status`: Indicates API operational status (value: "UP")
- `database`: Indicates database connectivity (value: "CONNECTED")

**Usage**: Returned by GET /api/v1/health endpoint

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: JWT Token Expiration Time

*For any* successful authentication request, the issued JWT token SHALL have an expiration time set to exactly 7 days (604800000 milliseconds) from the time of issuance.

**Validates: Requirements 8.3**

### Property 2: Invalid Token Rejection

*For any* invalid or malformed JWT token, the API SHALL reject the request and return HTTP status 401 (Unauthorized).

**Validates: Requirements 8.6**

## Error Handling

### Application Startup Errors

**Database Connection Failure**:
- Symptom: Application fails to start with connection exception
- Cause: PostgreSQL container not running or incorrect connection settings
- Resolution: Verify Docker container is running (`docker-compose ps`), check application.properties credentials

**Port Conflict**:
- Symptom: Application fails to start with "Port already in use" error
- Cause: Another process using port 8080
- Resolution: Stop conflicting process or change `server.port` in application.properties

### Runtime Errors

**Health Endpoint Errors**:
- If database connection is lost, health endpoint should still respond with 200 but may indicate database status differently
- Controller should handle exceptions gracefully

**JWT Token Errors**:
- Expired tokens: Return 401 with appropriate error message
- Invalid signature: Return 401 with appropriate error message
- Malformed tokens: Return 401 with appropriate error message

## Testing Strategy

### Unit Testing

Unit tests will focus on specific examples and edge cases:

**Health Controller Tests**:
- Test health endpoint returns 200 status code
- Test health endpoint returns correct JSON structure with "status" and "database" fields
- Test health endpoint returns "UP" status value
- Test health endpoint returns "CONNECTED" database value
- Test health endpoint returns application/json content type

**JWT Configuration Tests**:
- Test jwt.expiration property is correctly loaded from application.properties
- Test default value of 604800000ms is used when environment variable not set
- Test environment variable override works correctly

### Integration Testing

Integration tests will verify component interactions:

**Application Context Tests**:
- Test Spring application context loads successfully
- Test all required beans are created
- Test database connection is established

**End-to-End Health Check**:
- Test health endpoint is accessible at /api/v1/health
- Test health endpoint with database running
- Test health endpoint response format

### Property-Based Testing

Property-based tests will verify universal properties across all inputs using a Java property-based testing library such as jqwik or QuickTheories.

**Configuration**:
- Minimum 100 iterations per property test
- Each test tagged with reference to design document property

**Property Test 1: JWT Token Expiration Time**
```java
// Feature: payback-api-initialization, Property 1: For any successful authentication request, 
// the issued JWT token SHALL have an expiration time set to exactly 7 days from issuance
@Property
void jwtTokenHasSevenDayExpiration(@ForAll /* authentication request parameters */) {
    // Generate random valid authentication request
    // Issue JWT token
    // Extract expiration claim from token
    // Assert: expirationTime - issuanceTime == 604800000ms (7 days)
}
```

**Property Test 2: Invalid Token Rejection**
```java
// Feature: payback-api-initialization, Property 2: For any invalid or malformed JWT token,
// the API SHALL reject the request and return HTTP status 401
@Property
void invalidTokensAreRejected(@ForAll /* invalid token variations */) {
    // Generate random invalid/malformed token
    // Make API request with invalid token
    // Assert: response status == 401
}
```

### Testing Approach

This feature uses a dual testing approach:
- **Unit tests** verify specific examples (health endpoint structure, configuration loading)
- **Property tests** verify universal properties (all tokens have 7-day expiration, all invalid tokens rejected)

Together, these provide comprehensive coverage where unit tests catch concrete bugs and property tests verify general correctness across all possible inputs.

### Test Execution

- Unit tests: Run with `mvn test`
- Integration tests: Require Docker container running (`docker-compose up -d`)
- Property tests: Run with unit tests, minimum 100 iterations per property
- All tests should pass before deployment

## Implementation Notes

### Development Workflow

1. Start PostgreSQL container: `docker-compose up -d`
2. Run application: `mvn spring-boot:run`
3. Verify health endpoint: `curl http://localhost:8080/api/v1/health`
4. Stop container: `docker-compose down`

### Configuration Management

- Development: Use application.properties defaults
- Production: Override sensitive values via environment variables
- Docker: Database accessible on host port 5433, application connects to internal port 5432

### Future Enhancements

- Add actuator endpoints for detailed health metrics
- Implement token refresh mechanism
- Add token revocation capability
- Implement comprehensive logging and monitoring
- Add API documentation with Swagger/OpenAPI
