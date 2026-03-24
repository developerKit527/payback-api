# Implementation Plan: Payback API Initialization

## Overview

This plan implements a Spring Boot 3.2 REST API with PostgreSQL database integration, health monitoring, and JWT authentication with 7-day token expiration. The implementation follows a standard Spring Boot architecture with Maven dependency management and Docker Compose for local database provisioning.

## Tasks

- [ ] 1. Set up Maven project structure and dependencies
  - Create pom.xml with Spring Boot 3.2 and Java 21 configuration
  - Add spring-boot-starter-web dependency
  - Add spring-boot-starter-data-jpa dependency
  - Add postgresql driver dependency
  - Add lombok dependency
  - Create standard Maven directory structure (src/main/java, src/main/resources)
  - Create package structure: com/payback/api and com/payback/api/controller
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 2.1, 2.2, 2.3, 2.4_

- [ ] 2. Create application bootstrap and configuration
  - [ ] 2.1 Implement PaybackApplication main class
    - Create PaybackApplication.java in com.payback.api package
    - Add @SpringBootApplication annotation
    - Implement main method with SpringApplication.run()
    - _Requirements: 3.1, 3.2, 3.3_

  - [ ] 2.2 Configure application.properties for database and server
    - Set spring.datasource.url to jdbc:postgresql://localhost:5432/payback_db
    - Set spring.datasource.username to admin
    - Set spring.datasource.password to password123
    - Set spring.datasource.driver-class-name to org.postgresql.Driver
    - Set spring.jpa.hibernate.ddl-auto to update
    - Set spring.jpa.show-sql to true
    - Set spring.jpa.properties.hibernate.format_sql to true
    - Set spring.jpa.properties.hibernate.dialect to org.hibernate.dialect.PostgreSQLDialect
    - Set server.port to 8080
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9_

- [ ] 3. Implement health monitoring endpoint
  - [ ] 3.1 Create HealthController class
    - Create HealthController.java in com.payback.api.controller package
    - Add @RestController annotation
    - Implement GET /api/v1/health endpoint
    - Return JSON response with "status": "UP" and "database": "CONNECTED"
    - Ensure HTTP 200 status code
    - Ensure application/json content type
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 6.1, 6.2, 6.3_

  - [ ]* 3.2 Write unit tests for HealthController
    - Test health endpoint returns 200 status code
    - Test health endpoint returns correct JSON structure
    - Test health endpoint returns "UP" status value
    - Test health endpoint returns "CONNECTED" database value
    - Test health endpoint returns application/json content type
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [ ] 4. Set up Docker database environment
  - [ ] 4.1 Create docker-compose.yml configuration
    - Define PostgreSQL service using postgres:15 image
    - Set container name to payback_postgres
    - Map host port 5433 to container port 5432
    - Set POSTGRES_DB environment variable to payback_db
    - Set POSTGRES_USER environment variable to admin
    - Set POSTGRES_PASSWORD environment variable to password123
    - Define named volume postgres_data for data persistence
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

- [ ] 5. Checkpoint - Verify basic application startup
  - Start PostgreSQL container with docker-compose up -d
  - Run application with mvn spring-boot:run
  - Verify health endpoint responds at http://localhost:8080/api/v1/health
  - Ensure all tests pass, ask the user if questions arise

- [ ] 6. Implement JWT token expiry configuration
  - [ ] 6.1 Update application.properties with JWT expiration configuration
    - Add jwt.expiration property with value ${JWT_EXPIRATION:604800000}
    - Verify the property is correctly formatted (7 days = 604800000ms)
    - _Requirements: 8.1, 8.2, 8.4_

  - [ ] 6.2 Verify JwtService reads the expiration property
    - Check that JwtService class uses @Value("${jwt.expiration}") to read the property
    - Ensure token generation uses the configured expiration value
    - If JwtService doesn't exist yet, document that this will be implemented in authentication feature
    - _Requirements: 8.3, 8.4_

  - [ ]* 6.3 Write unit tests for JWT expiration configuration
    - Test that jwt.expiration property loads correctly from application.properties
    - Test that default value of 604800000ms is used
    - Test that environment variable override works correctly
    - _Requirements: 8.1, 8.2_

  - [ ]* 6.4 Write property-based tests for JWT token expiration
    - **Property 1: JWT Token Expiration Time**
    - **Validates: Requirements 8.3**
    - Test that all issued tokens have exactly 7-day expiration (604800000ms from issuance)
    - Use jqwik or QuickTheories for property-based testing
    - Run minimum 100 iterations
    - _Requirements: 8.3_

  - [ ]* 6.5 Write property-based tests for invalid token rejection
    - **Property 2: Invalid Token Rejection**
    - **Validates: Requirements 8.6**
    - Test that all invalid/malformed tokens are rejected with 401 status
    - Generate random invalid token variations
    - Use jqwik or QuickTheories for property-based testing
    - Run minimum 100 iterations
    - _Requirements: 8.5, 8.6_

- [ ] 7. Final checkpoint - Ensure all tests pass
  - Run mvn test to execute all unit and property tests
  - Verify application starts successfully
  - Verify health endpoint is accessible
  - Verify JWT configuration is loaded correctly
  - Ensure all tests pass, ask the user if questions arise

- [ ] 8. Implement User Profile Management (Requirement 9)
  - [ ] 8.1 Create DTOs for user profile management
    - Create UpdateUserRequestDTO.java with name field and validation annotations
    - Ensure UserDTO.java exists with id, name, and email fields
    - Add validation: @NotBlank for name, @Size(max = 100) for name length
    - _Requirements: 9.2, 9.3_

  - [ ] 8.2 Create UserController with profile update endpoint
    - Create UserController.java in controller package
    - Add @RestController and @RequestMapping("/api/v1") annotations
    - Implement PUT /users/me endpoint
    - Extract user ID from JWT token using @AuthenticationPrincipal or JWT filter
    - Validate request body using @Valid annotation
    - Call UserService to update profile
    - Return updated UserDTO with HTTP 200
    - _Requirements: 9.1, 9.5, 9.6, 9.9_

  - [ ] 8.3 Implement UserService for profile updates
    - Create UserService.java in service package (if not exists)
    - Add @Service annotation
    - Implement updateUserProfile(Long userId, String name) method
    - Fetch user from UserRepository by ID
    - Update user's name field
    - Save updated user to database
    - Return updated user entity
    - _Requirements: 9.4_

  - [ ] 8.4 Add error handling for profile updates
    - Handle missing/invalid JWT token → return 401
    - Handle empty or whitespace-only name → return 400
    - Handle name exceeding 100 characters → return 400
    - Handle user not found → return 404
    - Add appropriate error messages for each case
    - _Requirements: 9.7, 9.8_

  - [ ]* 8.5 Write unit tests for user profile management
    - Test PUT /users/me endpoint accepts valid requests
    - Test endpoint requires Authorization header
    - Test valid name update returns 200 with updated profile
    - Test empty name returns 400
    - Test name exceeding 100 characters returns 400
    - Test missing JWT token returns 401
    - Test UserService updates user in database
    - _Requirements: 9.1, 9.2, 9.3, 9.5, 9.6, 9.7, 9.8, 9.9_

  - [ ]* 8.6 Write property-based tests for user profile management
    - **Property 3: User Profile Update Round-Trip**
    - **Validates: Requirements 9.3, 9.4, 9.5, 9.9**
    - Test that any valid display name (1-100 chars) updates successfully
    - **Property 4: Invalid Display Name Rejection**
    - **Validates: Requirements 9.8**
    - Test that any invalid display name (empty, whitespace, >100 chars) returns 400
    - **Property 5: Unauthenticated Profile Update Rejection**
    - **Validates: Requirements 9.7**
    - Test that any request without valid JWT returns 401
    - Use jqwik or QuickTheories for property-based testing
    - Run minimum 100 iterations per property

- [ ] 9. Final checkpoint — Requirement 9
  - Run mvn test and confirm all tests pass
  - Verify PUT /api/v1/users/me endpoint works with valid JWT
  - Verify name validation works correctly
  - Verify unauthenticated requests are rejected
  - Ask the user if questions arise before writing any code

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (Properties 1-5)
- Unit tests validate specific examples and edge cases
- JwtService implementation may be part of a separate authentication feature - task 6.2 is primarily verification
- Docker container uses host port 5433 to avoid conflicts with local PostgreSQL installations
- User profile management (Task 8) requires JWT authentication to be implemented first
