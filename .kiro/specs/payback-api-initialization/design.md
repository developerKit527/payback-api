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
   - UserController: Handles user profile management

2. **Service Layer**: Business logic and data processing
   - UserService: User profile update operations

3. **Repository Layer**: Data access and persistence
   - UserRepository: User entity database operations

4. **Configuration Layer**: Application properties and environment configuration
   - Database connection settings
   - Server configuration
   - JWT token settings

5. **Infrastructure Layer**: External dependencies
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
│   │   │               ├── controller/
│   │   │               │   ├── HealthController.java
│   │   │               │   └── UserController.java
│   │   │               ├── service/
│   │   │               │   └── UserService.java
│   │   │               ├── repository/
│   │   │               │   └── UserRepository.java
│   │   │               ├── entity/
│   │   │               │   └── User.java
│   │   │               └── dto/
│   │   │                   ├── UpdateUserRequestDTO.java
│   │   │                   └── UserDTO.java
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

### 7. User Profile Management Endpoint

**Purpose**: Allow authenticated users to update their display name through a REST API endpoint

**Endpoint**: `PUT /api/v1/users/me`

**Authentication**: Requires valid JWT token in Authorization header

**Request**:
```json
{
  "name": "John Doe"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Error Responses**:
- 401 Unauthorized: Missing or invalid JWT token
- 400 Bad Request: Invalid name (empty or exceeds 100 characters)

**Implementation Approach**:

**Controller Layer** (`UserController.java`):
- Class location: `com.payback.api.controller.UserController`
- Annotation: `@RestController`
- Base mapping: `/api/v1`
- New endpoint: `@PutMapping("/users/me")`
- Extract user ID from JWT token using `@AuthenticationPrincipal` or custom JWT filter
- Validate request body using `@Valid` annotation
- Call service layer to update user
- Return updated user DTO

**Service Layer** (`UserService.java`):
- Class location: `com.payback.api.service.UserService`
- Annotation: `@Service`
- Method: `updateUserProfile(Long userId, String name)`
- Fetch user from repository by ID
- Update user's name field
- Save updated user to database
- Return updated user entity

**Repository Layer** (`UserRepository.java`):
- Interface location: `com.payback.api.repository.UserRepository`
- Extends: `JpaRepository<User, Long>`
- Provides standard CRUD operations for User entity

**DTO Layer**:

`UpdateUserRequestDTO.java`:
```java
public class UpdateUserRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    // Getters and setters
}
```

`UserDTO.java`:
```java
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    
    // Getters and setters
}
```

**Entity Layer** (`User.java`):
- Class location: `com.payback.api.entity.User`
- Annotation: `@Entity`, `@Table(name = "users")`
- Fields:
  - `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id`
  - `@Column(nullable = false, length = 100) private String name`
  - `@Column(nullable = false, unique = true) private String email`

**Security Considerations**:
- JWT token validation ensures only authenticated users can update their profile
- User can only update their own profile (user ID extracted from JWT)
- Email cannot be updated through this endpoint (security constraint)
- Name validation prevents empty strings and excessively long names
- Authorization header format: `Bearer <jwt_token>`

**Database Impact**:
- Updates the `name` column in the `users` table
- No schema changes required (name field already exists)
- Transaction management handled by Spring's `@Transactional` annotation

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

### User Entity Model

**Database Table**: `users`

**Structure**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
}
```

**Field Specifications**:
- `id`: Primary key, auto-generated
- `name`: User's display name, max 100 characters, not null
- `email`: User's email address, unique, not null

### Update User Request Model

**Structure**:
```json
{
  "name": "string"
}
```

**Field Specifications**:
- `name`: New display name (required, 1-100 characters)

**Validation Rules**:
- Name cannot be empty or whitespace only
- Name cannot exceed 100 characters

**Usage**: Request body for PUT /api/v1/users/me endpoint

### User Profile Response Model

**Structure**:
```json
{
  "id": "number",
  "name": "string",
  "email": "string"
}
```

**Field Specifications**:
- `id`: User's unique identifier
- `name`: User's display name
- `email`: User's email address

**Usage**: Response body for PUT /api/v1/users/me endpoint

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: JWT Token Expiration Time

*For any* successful authentication request, the issued JWT token SHALL have an expiration time set to exactly 7 days (604800000 milliseconds) from the time of issuance.

**Validates: Requirements 8.3**

### Property 2: Invalid Token Rejection

*For any* invalid or malformed JWT token, the API SHALL reject the request and return HTTP status 401 (Unauthorized).

**Validates: Requirements 8.6**

### Property 3: User Profile Update Round-Trip

*For any* valid display name (non-empty, 1-100 characters) and authenticated user, when the user updates their profile with that name, the API SHALL return HTTP status 200 with a response containing the user's id, the updated name, and email, and the database SHALL contain the updated name.

**Validates: Requirements 9.3, 9.4, 9.5, 9.9**

### Property 4: Invalid Display Name Rejection

*For any* invalid display name (empty, whitespace-only, or exceeding 100 characters), when an authenticated user attempts to update their profile, the API SHALL return HTTP status 400 with an error message.

**Validates: Requirements 9.8**

### Property 5: Unauthenticated Profile Update Rejection

*For any* profile update request without a valid JWT token (missing, expired, or malformed), the API SHALL return HTTP status 401 (Unauthorized).

**Validates: Requirements 9.7**

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

**User Profile Update Errors**:
- **Missing JWT Token**: Return 401 Unauthorized with message "Authentication required"
- **Invalid JWT Token**: Return 401 Unauthorized with message "Invalid or expired token"
- **Empty Display Name**: Return 400 Bad Request with message "Name cannot be empty"
- **Display Name Too Long**: Return 400 Bad Request with message "Name cannot exceed 100 characters"
- **Whitespace-Only Name**: Return 400 Bad Request with message "Name cannot be empty"
- **User Not Found**: Return 404 Not Found with message "User not found" (edge case if JWT references non-existent user)
- **Database Error**: Return 500 Internal Server Error with generic message, log detailed error

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

**User Controller Tests**:
- Test PUT /api/v1/users/me endpoint exists and accepts PUT requests
- Test endpoint accepts JSON request body with "name" field
- Test endpoint requires Authorization header with JWT token
- Test specific example: updating name to "John Doe" returns updated profile
- Test edge case: empty string name returns 400
- Test edge case: 100-character name is accepted
- Test edge case: 101-character name returns 400

**User Service Tests**:
- Test updateUserProfile method updates user name in database
- Test updateUserProfile method returns updated user entity
- Test updateUserProfile throws exception when user not found

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

**End-to-End User Profile Update**:
- Test authenticated user can update their profile
- Test profile update persists to database
- Test profile update returns correct response structure
- Test unauthenticated request is rejected
- Test invalid name validation works end-to-end

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

**Property Test 3: User Profile Update Round-Trip**
```java
// Feature: payback-api-initialization, Property 3: For any valid display name and authenticated user,
// when the user updates their profile, the API SHALL return 200 with updated profile and database SHALL contain the updated name
@Property
void userProfileUpdateRoundTrip(@ForAll("validDisplayNames") String name, @ForAll User user) {
    // Generate random valid display name (1-100 chars, non-empty)
    // Generate random authenticated user with valid JWT
    // Update user profile with name
    // Assert: response status == 200
    // Assert: response contains id, updated name, and email
    // Assert: database query returns user with updated name
}

@Provide
Arbitrary<String> validDisplayNames() {
    return Arbitraries.strings()
        .alpha().numeric().whitespace()
        .ofMinLength(1)
        .ofMaxLength(100)
        .filter(s -> !s.trim().isEmpty());
}
```

**Property Test 4: Invalid Display Name Rejection**
```java
// Feature: payback-api-initialization, Property 4: For any invalid display name,
// the API SHALL return HTTP status 400 with an error message
@Property
void invalidDisplayNamesAreRejected(@ForAll("invalidDisplayNames") String name, @ForAll User user) {
    // Generate random invalid display name (empty, whitespace-only, or >100 chars)
    // Generate random authenticated user with valid JWT
    // Attempt to update user profile with invalid name
    // Assert: response status == 400
    // Assert: response contains error message
}

@Provide
Arbitrary<String> invalidDisplayNames() {
    return Arbitraries.oneOf(
        Arbitraries.just(""),  // empty string
        Arbitraries.strings().whitespace().ofMinLength(1).ofMaxLength(10),  // whitespace only
        Arbitraries.strings().ofMinLength(101).ofMaxLength(200)  // too long
    );
}
```

**Property Test 5: Unauthenticated Profile Update Rejection**
```java
// Feature: payback-api-initialization, Property 5: For any profile update request without valid JWT token,
// the API SHALL return HTTP status 401
@Property
void unauthenticatedRequestsAreRejected(@ForAll("invalidTokens") String token, @ForAll String name) {
    // Generate random invalid/missing/expired token
    // Generate random display name
    // Attempt to update profile with invalid token
    // Assert: response status == 401
}

@Provide
Arbitrary<String> invalidTokens() {
    return Arbitraries.oneOf(
        Arbitraries.just(""),  // missing token
        Arbitraries.just("invalid.token.format"),  // malformed
        Arbitraries.strings().alpha().numeric().ofLength(50),  // random string
        // expired tokens would be generated by creating tokens with past expiration
    );
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
