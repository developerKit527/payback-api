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
│   │   │               │   ├── UserController.java
│   │   │               │   ├── ReferralController.java
│   │   │               │   └── WithdrawalController.java
│   │   │               ├── service/
│   │   │               │   ├── UserService.java
│   │   │               │   ├── ReferralService.java
│   │   │               │   ├── WithdrawalService.java
│   │   │               │   └── EmailService.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── MerchantRepository.java
│   │   │               │   ├── ReferralRepository.java
│   │   │               │   └── WithdrawalRepository.java
│   │   │               ├── entity/
│   │   │               │   ├── User.java
│   │   │               │   ├── Merchant.java
│   │   │               │   ├── Referral.java
│   │   │               │   └── Withdrawal.java
│   │   │               ├── dto/
│   │   │               │   ├── UpdateUserRequestDTO.java
│   │   │               │   ├── UserDTO.java
│   │   │               │   ├── ReferralStatsDTO.java
│   │   │               │   ├── WithdrawalRequestDTO.java
│   │   │               │   └── WithdrawalDTO.java
│   │   │               └── enums/
│   │   │                   └── WithdrawalStatus.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── data.sql
│   │       └── templates/
│   │           └── email/
│   │               ├── welcome.html
│   │               └── cashback-confirmation.html
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
- `spring-boot-starter-mail` or Resend Java SDK: Email sending capabilities
- `spring-boot-starter-thymeleaf`: Email template rendering

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

**Resend Email Configuration**:
```properties
resend.api.key=${RESEND_API_KEY}
resend.from.email=noreply@payback.com
resend.from.name=Payback
```

**Referral Configuration**:
```properties
referral.bonus.amount=50.00
```

**Withdrawal Configuration**:
```properties
withdrawal.minimum.amount=100.00
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
  - `@Column(unique = true, length = 8) private String referralCode`
  - `@Column(nullable = false) private BigDecimal cashbackBalance`
  - `@OneToMany(mappedBy = "referrer") private List<Referral> referrals`

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
    
    @Column(unique = true, length = 8)
    private String referralCode;
    
    @Column(nullable = false)
    private BigDecimal cashbackBalance;
    
    @OneToMany(mappedBy = "referrer")
    private List<Referral> referrals;
}
```

**Field Specifications**:
- `id`: Primary key, auto-generated
- `name`: User's display name, max 100 characters, not null
- `email`: User's email address, unique, not null
- `referralCode`: User's unique 8-character referral code
- `cashbackBalance`: User's available cashback balance in rupees
- `referrals`: List of referrals made by this user

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

### Merchant Entity Model

**Database Table**: `merchants`

**Structure**:
```java
@Entity
@Table(name = "merchants")
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private BigDecimal cashbackPercentage;
}
```

**Field Specifications**:
- `id`: Primary key, auto-generated
- `name`: Merchant name (e.g., "Zomato"), unique
- `category`: Merchant category (e.g., "food delivery")
- `cashbackPercentage`: Cashback percentage (e.g., 5.0 for 5%)

**Initial Data**:
- Zomato: food delivery, 5%
- Swiggy: food delivery, 4%
- MakeMyTrip: travel, 6%
- boAt: electronics, 8%
- Meesho: fashion, 10%
- Tata CLiQ: electronics/fashion, 7%

### Referral Entity Model

**Database Table**: `referrals`

**Structure**:
```java
@Entity
@Table(name = "referrals")
public class Referral {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "referrer_id", nullable = false)
    private User referrer;
    
    @ManyToOne
    @JoinColumn(name = "referred_id", nullable = false)
    private User referred;
    
    @Column(nullable = false)
    private BigDecimal bonusCashback;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
```

**Field Specifications**:
- `id`: Primary key, auto-generated
- `referrer`: User who made the referral
- `referred`: User who was referred
- `bonusCashback`: Bonus cashback amount awarded
- `createdAt`: Timestamp of referral creation

### Referral Stats Response Model

**Structure**:
```json
{
  "referralCode": "string",
  "totalReferrals": "number",
  "totalBonusCashback": "number"
}
```

**Field Specifications**:
- `referralCode`: User's unique 8-character referral code
- `totalReferrals`: Count of successful referrals
- `totalBonusCashback`: Total bonus cashback earned from referrals

**Usage**: Response body for GET /api/v1/referrals/stats endpoint

### Withdrawal Entity Model

**Database Table**: `withdrawals`

**Structure**:
```java
@Entity
@Table(name = "withdrawals")
public class Withdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String upiId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus status;
    
    @Column(nullable = false)
    private LocalDateTime requestedAt;
    
    @Column
    private LocalDateTime processedAt;
}
```

**Field Specifications**:
- `id`: Primary key, auto-generated
- `user`: User requesting withdrawal
- `upiId`: UPI ID for payment (format: username@bankname)
- `amount`: Withdrawal amount in rupees
- `status`: Withdrawal status (PENDING, APPROVED, PAID, REJECTED)
- `requestedAt`: Timestamp of request creation
- `processedAt`: Timestamp of status change to PAID

### Withdrawal Request Model

**Structure**:
```json
{
  "upiId": "string",
  "amount": "number"
}
```

**Field Specifications**:
- `upiId`: UPI ID in format username@bankname
- `amount`: Withdrawal amount (minimum 100 rupees)

**Validation Rules**:
- UPI ID must match pattern: `^[a-zA-Z0-9._-]+@[a-zA-Z]+$`
- Amount must be >= 100
- Amount must be <= user's cashback balance

**Usage**: Request body for POST /api/v1/withdrawals endpoint

### Withdrawal Response Model

**Structure**:
```json
{
  "id": "number",
  "upiId": "string",
  "amount": "number",
  "status": "string",
  "requestedAt": "string",
  "processedAt": "string"
}
```

**Field Specifications**:
- `id`: Withdrawal request ID
- `upiId`: UPI ID for payment
- `amount`: Withdrawal amount
- `status`: Current status (PENDING, APPROVED, PAID, REJECTED)
- `requestedAt`: ISO 8601 timestamp
- `processedAt`: ISO 8601 timestamp (null if not processed)

**Usage**: Response body for withdrawal endpoints

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

### Property 6: Referral Code Generation and Storage

*For any* user registration, the API SHALL generate a unique alphanumeric referral code exactly 8 characters long and store it in the database associated with that user.

**Validates: Requirements 11.1, 11.2, 11.3**

### Property 7: Referral Relationship Creation

*For any* user signup with a valid referral code, the API SHALL create a referral relationship linking the new user to the referring user.

**Validates: Requirements 11.4**

### Property 8: Referral Bonus Cashback Award

*For any* referral relationship created, the API SHALL award bonus cashback to both the referring user and the referred user.

**Validates: Requirements 11.5**

### Property 9: Referral Stats Accuracy

*For any* authenticated user, when the referral stats endpoint is called, the API SHALL return the user's referral code, the correct count of total referrals, and the correct sum of total bonus cashback earned.

**Validates: Requirements 11.7**

### Property 10: Referral Endpoint Authentication

*For any* request to referral endpoints without a valid JWT token, the API SHALL return HTTP status 401 (Unauthorized).

**Validates: Requirements 11.8**

### Property 11: Invalid Referral Code Rejection

*For any* signup attempt with a non-existent referral code, the API SHALL return HTTP status 400 with an error message.

**Validates: Requirements 11.9, 11.10**

### Property 12: UPI ID Format Validation

*For any* withdrawal request with an invalid UPI ID format (not matching username@bankname pattern), the API SHALL return HTTP status 400 with an error message.

**Validates: Requirements 12.3, 12.14**

### Property 13: Withdrawal Amount Balance Validation

*For any* withdrawal request where the amount exceeds the user's available cashback balance, the API SHALL return HTTP status 400 with an error message.

**Validates: Requirements 12.4, 12.15**

### Property 14: Withdrawal Minimum Amount Validation

*For any* withdrawal request with an amount less than 100 rupees, the API SHALL return HTTP status 400 with an error message.

**Validates: Requirements 12.5**

### Property 15: Withdrawal Request Storage

*For any* valid withdrawal request, the API SHALL store the withdrawal in the database with status "PENDING" and the request SHALL be retrievable via the withdrawal history endpoint.

**Validates: Requirements 12.6**

### Property 16: Withdrawal Approval Status Update

*For any* pending withdrawal, when an administrator approves it, the API SHALL update the withdrawal status to "APPROVED".

**Validates: Requirements 12.8**

### Property 17: Withdrawal Payment Processing

*For any* approved withdrawal, when marked as paid, the API SHALL update the withdrawal status to "PAID" and deduct the withdrawal amount from the user's cashback balance.

**Validates: Requirements 12.10**

### Property 18: Withdrawal Endpoint Authentication

*For any* request to withdrawal endpoints without a valid JWT token, the API SHALL return HTTP status 401 (Unauthorized).

**Validates: Requirements 12.12**

### Property 19: Withdrawal Admin Authorization

*For any* request to admin withdrawal endpoints (approve, mark-paid) from a non-admin user, the API SHALL return HTTP status 403 (Forbidden).

**Validates: Requirements 12.13**

### Property 20: Welcome Email Delivery

*For any* user registration, the API SHALL send a welcome email to the user's email address containing the user's name and getting started information.

**Validates: Requirements 13.3, 13.4**

### Property 21: Cashback Confirmation Email Delivery

*For any* confirmed transaction with cashback credit, the API SHALL send a cashback confirmation email containing the transaction amount, cashback amount, and merchant name.

**Validates: Requirements 13.5, 13.6**

### Property 22: Email Failure Resilience

*For any* operation that triggers an email notification, if the email fails to send, the API SHALL log the error and continue with the primary operation without blocking or failing.

**Validates: Requirements 13.9**

### Property 23: Email HTML Formatting

*For any* email sent by the API, the email content SHALL be formatted as HTML.

**Validates: Requirements 13.10**

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

**Referral System Errors**:
- **Invalid Referral Code**: Return 400 Bad Request with message "Invalid referral code"
- **Referral Code Generation Collision**: Retry generation with new random code (max 5 attempts)
- **Self-Referral Attempt**: Return 400 Bad Request with message "Cannot use your own referral code"
- **Duplicate Referral**: Return 400 Bad Request with message "User already registered with a referral code"
- **Missing JWT Token**: Return 401 Unauthorized with message "Authentication required"

**Withdrawal Flow Errors**:
- **Invalid UPI ID Format**: Return 400 Bad Request with message "Invalid UPI ID format. Expected: username@bankname"
- **Insufficient Balance**: Return 400 Bad Request with message "Withdrawal amount exceeds available balance"
- **Below Minimum Amount**: Return 400 Bad Request with message "Minimum withdrawal amount is 100 rupees"
- **Withdrawal Not Found**: Return 404 Not Found with message "Withdrawal request not found"
- **Invalid Status Transition**: Return 400 Bad Request with message "Cannot approve/pay withdrawal in current status"
- **Missing JWT Token**: Return 401 Unauthorized with message "Authentication required"
- **Non-Admin Access**: Return 403 Forbidden with message "Admin privileges required"
- **Database Error**: Return 500 Internal Server Error with generic message, log detailed error

**Email Notification Errors**:
- **Email Send Failure**: Log error with full context (user ID, email type, error message), continue with primary operation
- **Invalid Email Address**: Log warning, skip email sending, continue with primary operation
- **Template Rendering Error**: Log error with template name and variables, continue with primary operation
- **Resend API Error**: Log error with API response, consider retry for transient failures
- **Missing Configuration**: Log error at startup, fail fast if email credentials not configured

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

**Merchant Data Tests**:
- Test Zomato merchant exists with category "food delivery" and 5% cashback
- Test Swiggy merchant exists with category "food delivery" and 4% cashback
- Test MakeMyTrip merchant exists with category "travel" and 6% cashback
- Test boAt merchant exists with category "electronics" and 8% cashback
- Test Meesho merchant exists with category "fashion" and 10% cashback
- Test Tata CLiQ merchant exists with category "electronics/fashion" and 7% cashback
- Test data.sql script exists and is syntactically valid

**Referral Service Tests**:
- Test generateReferralCode creates 8-character alphanumeric code
- Test createReferral creates relationship between users
- Test createReferral awards bonus cashback to both users
- Test getReferralStats returns correct statistics
- Test specific example: user with 3 referrals returns count of 3

**Withdrawal Service Tests**:
- Test createWithdrawalRequest validates UPI ID format
- Test createWithdrawalRequest validates minimum amount (100 rupees)
- Test createWithdrawalRequest validates balance sufficiency
- Test approveWithdrawal changes status to APPROVED
- Test markAsPaid changes status to PAID and deducts balance
- Test specific example: withdrawal of 500 rupees with balance of 1000 succeeds

**Email Service Tests**:
- Test sendWelcomeEmail sends email with user name
- Test sendCashbackConfirmation includes transaction details
- Test email sending failure is logged but doesn't throw exception
- Test email templates exist in resources/templates/email/
- Test Resend API configuration is loaded from application.properties

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

**End-to-End Referral Flow**:
- Test user registration generates referral code
- Test signup with referral code creates relationship
- Test referral stats endpoint returns correct data
- Test invalid referral code is rejected

**End-to-End Withdrawal Flow**:
- Test user can create withdrawal request
- Test admin can approve withdrawal
- Test admin can mark withdrawal as paid
- Test balance is deducted after payment
- Test withdrawal history shows all user withdrawals

**End-to-End Email Notifications**:
- Test welcome email is sent on registration
- Test cashback confirmation email is sent on transaction
- Test email failure doesn't block registration or transaction

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

**Property Test 6: Referral Code Generation and Storage**
```java
// Feature: payback-api-initialization, Property 6: For any user registration,
// the API SHALL generate a unique alphanumeric referral code exactly 8 characters long and store it
@Property
void referralCodeGenerationAndStorage(@ForAll User user) {
    // Generate random user registration data
    // Register user
    // Query database for user's referral code
    // Assert: referral code exists
    // Assert: referral code is exactly 8 characters
    // Assert: referral code is alphanumeric (matches [A-Za-z0-9]{8})
    // Assert: referral code is unique (not used by other users)
}
```

**Property Test 7: Referral Relationship Creation**
```java
// Feature: payback-api-initialization, Property 7: For any user signup with valid referral code,
// the API SHALL create a referral relationship linking the new user to the referring user
@Property
void referralRelationshipCreation(@ForAll User referrer, @ForAll User newUser) {
    // Generate random referring user with referral code
    // Generate random new user
    // Sign up new user with referrer's code
    // Query database for referral relationship
    // Assert: relationship exists linking newUser to referrer
}
```

**Property Test 8: Referral Bonus Cashback Award**
```java
// Feature: payback-api-initialization, Property 8: For any referral relationship created,
// the API SHALL award bonus cashback to both users
@Property
void referralBonusCashbackAward(@ForAll User referrer, @ForAll User newUser) {
    // Generate random users
    // Record initial cashback balances
    // Create referral relationship
    // Query updated cashback balances
    // Assert: referrer's balance increased by bonus amount
    // Assert: newUser's balance increased by bonus amount
}
```

**Property Test 9: Referral Stats Accuracy**
```java
// Feature: payback-api-initialization, Property 9: For any authenticated user,
// referral stats SHALL return correct code, count, and total bonus cashback
@Property
void referralStatsAccuracy(@ForAll User user, @ForAll("referralList") List<Referral> referrals) {
    // Generate random user with random referrals
    // Call GET /api/v1/referrals/stats
    // Assert: response contains user's referral code
    // Assert: totalReferrals == referrals.size()
    // Assert: totalBonusCashback == sum of all referral bonuses
}
```

**Property Test 10: Referral Endpoint Authentication**
```java
// Feature: payback-api-initialization, Property 10: For any request to referral endpoints without valid JWT,
// the API SHALL return HTTP status 401
@Property
void referralEndpointAuthentication(@ForAll("invalidTokens") String token) {
    // Generate random invalid token
    // Make request to /api/v1/referrals/stats with invalid token
    // Assert: response status == 401
}
```

**Property Test 11: Invalid Referral Code Rejection**
```java
// Feature: payback-api-initialization, Property 11: For any signup with non-existent referral code,
// the API SHALL return HTTP status 400 with error message
@Property
void invalidReferralCodeRejection(@ForAll("nonExistentCodes") String code, @ForAll User newUser) {
    // Generate random non-existent referral code
    // Generate random new user
    // Attempt signup with invalid code
    // Assert: response status == 400
    // Assert: response contains error message
}
```

**Property Test 12: UPI ID Format Validation**
```java
// Feature: payback-api-initialization, Property 12: For any withdrawal with invalid UPI ID format,
// the API SHALL return HTTP status 400 with error message
@Property
void upiIdFormatValidation(@ForAll("invalidUpiIds") String upiId, @ForAll BigDecimal amount) {
    // Generate random invalid UPI ID (not matching username@bankname)
    // Generate random valid amount
    // Attempt withdrawal request
    // Assert: response status == 400
    // Assert: response contains error message about UPI format
}

@Provide
Arbitrary<String> invalidUpiIds() {
    return Arbitraries.oneOf(
        Arbitraries.just(""),  // empty
        Arbitraries.just("username"),  // missing @bankname
        Arbitraries.just("@bankname"),  // missing username
        Arbitraries.strings().withChars('@').ofLength(20),  // multiple @
        Arbitraries.just("user name@bank")  // spaces
    );
}
```

**Property Test 13: Withdrawal Amount Balance Validation**
```java
// Feature: payback-api-initialization, Property 13: For any withdrawal exceeding balance,
// the API SHALL return HTTP status 400 with error message
@Property
void withdrawalAmountBalanceValidation(@ForAll User user, @ForAll("excessiveAmounts") BigDecimal amount) {
    // Generate random user with known balance
    // Generate random amount exceeding balance
    // Attempt withdrawal request
    // Assert: response status == 400
    // Assert: response contains error message about insufficient balance
}
```

**Property Test 14: Withdrawal Minimum Amount Validation**
```java
// Feature: payback-api-initialization, Property 14: For any withdrawal below 100 rupees,
// the API SHALL return HTTP status 400 with error message
@Property
void withdrawalMinimumAmountValidation(@ForAll("belowMinimum") BigDecimal amount) {
    // Generate random amount below 100
    // Generate valid UPI ID
    // Attempt withdrawal request
    // Assert: response status == 400
    // Assert: response contains error message about minimum amount
}

@Provide
Arbitrary<BigDecimal> belowMinimum() {
    return Arbitraries.bigDecimals()
        .between(BigDecimal.ZERO, new BigDecimal("99.99"))
        .ofScale(2);
}
```

**Property Test 15: Withdrawal Request Storage**
```java
// Feature: payback-api-initialization, Property 15: For any valid withdrawal request,
// the API SHALL store it with status PENDING and be retrievable
@Property
void withdrawalRequestStorage(@ForAll("validUpiIds") String upiId, @ForAll("validAmounts") BigDecimal amount) {
    // Generate random valid UPI ID and amount
    // Create withdrawal request
    // Assert: response status == 201
    // Query withdrawal history
    // Assert: withdrawal exists with status PENDING
    // Assert: withdrawal has correct upiId and amount
}
```

**Property Test 16: Withdrawal Approval Status Update**
```java
// Feature: payback-api-initialization, Property 16: For any pending withdrawal,
// when approved, status SHALL update to APPROVED
@Property
void withdrawalApprovalStatusUpdate(@ForAll Withdrawal pendingWithdrawal) {
    // Generate random pending withdrawal
    // Admin approves withdrawal
    // Query withdrawal from database
    // Assert: status == APPROVED
}
```

**Property Test 17: Withdrawal Payment Processing**
```java
// Feature: payback-api-initialization, Property 17: For any approved withdrawal,
// when marked paid, status SHALL be PAID and balance SHALL be deducted
@Property
void withdrawalPaymentProcessing(@ForAll Withdrawal approvedWithdrawal) {
    // Generate random approved withdrawal
    // Record user's initial balance
    // Admin marks withdrawal as paid
    // Query withdrawal and user balance
    // Assert: withdrawal status == PAID
    // Assert: user balance == initialBalance - withdrawal amount
}
```

**Property Test 18: Withdrawal Endpoint Authentication**
```java
// Feature: payback-api-initialization, Property 18: For any request to withdrawal endpoints without valid JWT,
// the API SHALL return HTTP status 401
@Property
void withdrawalEndpointAuthentication(@ForAll("invalidTokens") String token) {
    // Generate random invalid token
    // Make request to withdrawal endpoint with invalid token
    // Assert: response status == 401
}
```

**Property Test 19: Withdrawal Admin Authorization**
```java
// Feature: payback-api-initialization, Property 19: For any request to admin withdrawal endpoints from non-admin,
// the API SHALL return HTTP status 403
@Property
void withdrawalAdminAuthorization(@ForAll("nonAdminUsers") User user, @ForAll Withdrawal withdrawal) {
    // Generate random non-admin user with valid JWT
    // Attempt to approve or mark-paid withdrawal
    // Assert: response status == 403
}
```

**Property Test 20: Welcome Email Delivery**
```java
// Feature: payback-api-initialization, Property 20: For any user registration,
// welcome email SHALL be sent with user's name and getting started info
@Property
void welcomeEmailDelivery(@ForAll User user) {
    // Generate random user
    // Register user
    // Verify email was sent (check email service mock/spy)
    // Assert: email recipient == user.email
    // Assert: email content contains user.name
    // Assert: email content contains getting started information
}
```

**Property Test 21: Cashback Confirmation Email Delivery**
```java
// Feature: payback-api-initialization, Property 21: For any cashback transaction,
// confirmation email SHALL include transaction amount, cashback amount, and merchant name
@Property
void cashbackConfirmationEmailDelivery(@ForAll Transaction transaction) {
    // Generate random transaction with cashback
    // Process transaction
    // Verify email was sent
    // Assert: email content contains transaction amount
    // Assert: email content contains cashback amount
    // Assert: email content contains merchant name
}
```

**Property Test 22: Email Failure Resilience**
```java
// Feature: payback-api-initialization, Property 22: For any operation triggering email,
// if email fails, operation SHALL continue and error SHALL be logged
@Property
void emailFailureResilience(@ForAll User user) {
    // Generate random user
    // Configure email service to fail
    // Register user (triggers welcome email)
    // Assert: registration succeeds (returns 201)
    // Assert: user exists in database
    // Assert: error was logged
}
```

**Property Test 23: Email HTML Formatting**
```java
// Feature: payback-api-initialization, Property 23: For any email sent,
// content SHALL be formatted as HTML
@Property
void emailHtmlFormatting(@ForAll("emailTypes") String emailType, @ForAll User user) {
    // Generate random email type (welcome, cashback confirmation)
    // Trigger email sending
    // Capture email content
    // Assert: content contains HTML tags (e.g., <html>, <body>, <p>)
    // Assert: content is valid HTML
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

### 8. Merchant Data Management

**Purpose**: Populate the database with merchant partner information for cashback opportunities

**Implementation**:
- SQL script location: `src/main/resources/db/migration/` or `src/main/resources/data.sql`
- Merchant entity with fields: name, category, cashback_percentage

**Merchant Records**:
- Zomato: food delivery, 5%
- Swiggy: food delivery, 4%
- MakeMyTrip: travel, 6%
- boAt: electronics, 8%
- Meesho: fashion, 10%
- Tata CLiQ: electronics/fashion, 7%

**Database Initialization**:
- Use Spring Boot's data.sql or Flyway/Liquibase migrations
- Execute on application startup
- Idempotent inserts (use INSERT ... ON CONFLICT DO NOTHING or similar)

### 9. Referral System

**Purpose**: Enable users to refer friends and earn bonus cashback

**Components**:

**Entity Layer** (`Referral.java`):
- Class location: `com.payback.api.entity.Referral`
- Fields:
  - `@Id @GeneratedValue private Long id`
  - `@ManyToOne private User referrer` (the user who referred)
  - `@ManyToOne private User referred` (the new user)
  - `@Column private BigDecimal bonusCashback`
  - `@Column private LocalDateTime createdAt`

**User Entity Updates**:
- Add field: `@Column(unique = true, length = 8) private String referralCode`
- Add field: `@OneToMany private List<Referral> referrals`

**Service Layer** (`ReferralService.java`):
- `generateReferralCode()`: Generate unique 8-character alphanumeric code
- `createReferral(String referralCode, User newUser)`: Create referral relationship and award bonus
- `getReferralStats(Long userId)`: Get user's referral statistics

**Controller Layer** (`ReferralController.java`):
- `GET /api/v1/referrals/stats`: Return referral code, count, and total bonus cashback
- Requires JWT authentication

**Referral Code Generation**:
- Algorithm: Random alphanumeric string (A-Z, a-z, 0-9)
- Length: Exactly 8 characters
- Uniqueness: Check database before assigning
- Generated during user registration

**Bonus Cashback Logic**:
- Award bonus to both referrer and referred user
- Amount configurable via application.properties
- Credited immediately upon successful referral

### 10. Withdrawal Flow

**Purpose**: Allow users to withdraw earned cashback to UPI accounts with admin approval

**Components**:

**Entity Layer** (`Withdrawal.java`):
- Class location: `com.payback.api.entity.Withdrawal`
- Fields:
  - `@Id @GeneratedValue private Long id`
  - `@ManyToOne private User user`
  - `@Column private String upiId`
  - `@Column private BigDecimal amount`
  - `@Enumerated private WithdrawalStatus status` (PENDING, APPROVED, PAID, REJECTED)
  - `@Column private LocalDateTime requestedAt`
  - `@Column private LocalDateTime processedAt`

**User Entity Updates**:
- Add field: `@Column private BigDecimal cashbackBalance`

**Service Layer** (`WithdrawalService.java`):
- `createWithdrawalRequest(Long userId, String upiId, BigDecimal amount)`: Validate and create request
- `approveWithdrawal(Long withdrawalId)`: Admin approval
- `markAsPaid(Long withdrawalId)`: Mark paid and deduct balance
- `getWithdrawalHistory(Long userId)`: Get user's withdrawal history

**Controller Layer** (`WithdrawalController.java`):
- `POST /api/v1/withdrawals`: Create withdrawal request (user endpoint)
- `GET /api/v1/withdrawals/history`: Get withdrawal history (user endpoint)
- `PUT /api/v1/admin/withdrawals/{id}/approve`: Approve withdrawal (admin endpoint)
- `PUT /api/v1/admin/withdrawals/{id}/mark-paid`: Mark as paid (admin endpoint)

**Validation Rules**:
- UPI ID format: `username@bankname` (regex: `^[a-zA-Z0-9._-]+@[a-zA-Z]+$`)
- Minimum withdrawal: 100 rupees
- Maximum withdrawal: User's available cashback balance
- Status transitions: PENDING → APPROVED → PAID

**Authorization**:
- User endpoints: Require valid JWT token
- Admin endpoints: Require JWT token with admin role

### 11. Email Notification System

**Purpose**: Send automated email notifications for important user events

**Components**:

**Email Service Integration**:
- Service: Resend (https://resend.com)
- Configuration in `application.properties`:
  ```properties
  resend.api.key=${RESEND_API_KEY}
  resend.from.email=noreply@payback.com
  resend.from.name=Payback
  ```

**Service Layer** (`EmailService.java`):
- Class location: `com.payback.api.service.EmailService`
- `sendWelcomeEmail(User user)`: Send welcome email to new users
- `sendCashbackConfirmation(User user, Transaction transaction)`: Send cashback confirmation
- Error handling: Log failures but don't block primary operations

**Email Templates**:
- Location: `src/main/resources/templates/email/`
- Format: HTML with inline CSS
- Templates:
  - `welcome.html`: Welcome email with user name and getting started info
  - `cashback-confirmation.html`: Transaction amount, cashback amount, merchant name

**Template Engine**:
- Use Thymeleaf or similar for template rendering
- Variables: user name, transaction details, cashback amounts

**Email Content Requirements**:
- HTML formatted for better presentation
- Include Payback branding (logo, colors, styling)
- Responsive design for mobile devices
- Plain text fallback

**Integration Points**:
- User registration: Trigger welcome email
- Cashback credit: Trigger confirmation email
- Async processing: Use Spring's `@Async` to avoid blocking

**Error Handling**:
- Catch email sending exceptions
- Log errors with full context
- Continue with primary operation
- Consider retry mechanism for transient failures

### Future Enhancements

- Add actuator endpoints for detailed health metrics
- Implement token refresh mechanism
- Add token revocation capability
- Implement comprehensive logging and monitoring
- Add API documentation with Swagger/OpenAPI
- Implement withdrawal rejection flow
- Add email notification for withdrawal status changes
- Implement referral code sharing functionality
- Add merchant management API endpoints
