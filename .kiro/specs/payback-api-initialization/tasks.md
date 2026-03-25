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

- [ ] 10. Implement Additional Merchant Data (Requirement 10)
  - [ ] 10.1 Create Merchant entity and repository
    - Create Merchant.java entity in entity package
    - Add fields: id (Long), name (String, unique), category (String), cashbackPercentage (BigDecimal)
    - Add @Entity and @Table annotations
    - Create MerchantRepository.java interface extending JpaRepository
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_

  - [ ] 10.2 Create SQL script for merchant data insertion
    - Create data.sql file in src/main/resources/
    - Add INSERT statements for Zomato (food delivery, 5%)
    - Add INSERT statements for Swiggy (food delivery, 4%)
    - Add INSERT statements for MakeMyTrip (travel, 6%)
    - Add INSERT statements for boAt (electronics, 8%)
    - Add INSERT statements for Meesho (fashion, 10%)
    - Add INSERT statements for Tata CLiQ (electronics/fashion, 7%)
    - Use INSERT ... ON CONFLICT DO NOTHING or equivalent for idempotency
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_

  - [ ] 10.3 Configure Spring Boot to execute data initialization
    - Verify spring.jpa.hibernate.ddl-auto is set to update in application.properties
    - Ensure Spring Boot will execute data.sql on startup
    - _Requirements: 10.8_

  - [ ]* 10.4 Write unit tests for merchant data
    - Test Zomato merchant exists with correct category and cashback percentage
    - Test Swiggy merchant exists with correct category and cashback percentage
    - Test MakeMyTrip merchant exists with correct category and cashback percentage
    - Test boAt merchant exists with correct category and cashback percentage
    - Test Meesho merchant exists with correct category and cashback percentage
    - Test Tata CLiQ merchant exists with correct category and cashback percentage
    - Test data.sql script is syntactically valid
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6_

- [ ] 11. Implement Referral System Backend (Requirement 11)
  - [ ] 11.1 Create Referral entity and update User entity
    - Create Referral.java entity in entity package
    - Add fields: id (Long), referrer (User), referred (User), bonusCashback (BigDecimal), createdAt (LocalDateTime)
    - Add @Entity, @ManyToOne, and @Column annotations
    - Update User.java entity to add referralCode field (String, unique, length 8)
    - Update User.java entity to add cashbackBalance field (BigDecimal)
    - Update User.java entity to add referrals relationship (OneToMany)
    - Create ReferralRepository.java interface extending JpaRepository
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [ ] 11.2 Implement ReferralService for referral logic
    - Create ReferralService.java in service package
    - Add @Service annotation
    - Implement generateReferralCode() method to create unique 8-character alphanumeric code
    - Implement createReferral(String referralCode, User newUser) method
    - Validate referral code exists in database
    - Create Referral relationship linking newUser to referrer
    - Award bonus cashback to both referrer and referred user
    - Implement getReferralStats(Long userId) method to return referral statistics
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.7, 11.9_

  - [ ] 11.3 Create DTOs for referral system
    - Create ReferralStatsDTO.java with fields: referralCode, totalReferrals, totalBonusCashback
    - Add getters and setters
    - _Requirements: 11.7_

  - [ ] 11.4 Create ReferralController with referral stats endpoint
    - Create ReferralController.java in controller package
    - Add @RestController and @RequestMapping("/api/v1") annotations
    - Implement GET /referrals/stats endpoint
    - Extract user ID from JWT token
    - Call ReferralService to get referral statistics
    - Return ReferralStatsDTO with HTTP 200
    - _Requirements: 11.6, 11.7_

  - [ ] 11.5 Add referral code generation to user registration
    - Update user registration logic to call ReferralService.generateReferralCode()
    - Store generated referral code in User entity
    - _Requirements: 11.1, 11.2, 11.3_

  - [ ] 11.6 Add referral code validation to signup flow
    - Update signup logic to accept optional referral code parameter
    - If referral code provided, validate it exists
    - If valid, call ReferralService.createReferral()
    - If invalid, return 400 with error message
    - _Requirements: 11.4, 11.5, 11.9, 11.10_

  - [ ] 11.7 Add error handling for referral system
    - Handle invalid referral code → return 400
    - Handle missing JWT token → return 401
    - Handle self-referral attempt → return 400
    - Handle duplicate referral → return 400
    - Add appropriate error messages for each case
    - _Requirements: 11.8, 11.9, 11.10_

  - [ ] 11.8 Configure referral bonus amount in application.properties
    - Add referral.bonus.amount property with default value
    - Update ReferralService to read bonus amount from configuration
    - _Requirements: 11.5_

  - [ ]* 11.9 Write unit tests for referral system
    - Test generateReferralCode creates 8-character alphanumeric code
    - Test createReferral creates relationship between users
    - Test createReferral awards bonus cashback to both users
    - Test getReferralStats returns correct statistics
    - Test GET /api/v1/referrals/stats endpoint requires authentication
    - Test invalid referral code returns 400
    - Test self-referral returns 400
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8, 11.9, 11.10_

  - [ ]* 11.10 Write property-based tests for referral system
    - **Property 6: Referral Code Generation and Storage**
    - **Validates: Requirements 11.1, 11.2, 11.3**
    - **Property 7: Referral Relationship Creation**
    - **Validates: Requirements 11.4**
    - **Property 8: Referral Bonus Cashback Award**
    - **Validates: Requirements 11.5**
    - **Property 9: Referral Stats Accuracy**
    - **Validates: Requirements 11.7**
    - **Property 10: Referral Endpoint Authentication**
    - **Validates: Requirements 11.8**
    - **Property 11: Invalid Referral Code Rejection**
    - **Validates: Requirements 11.9, 11.10**
    - Use jqwik or QuickTheories for property-based testing
    - Run minimum 100 iterations per property

- [ ] 12. Checkpoint - Verify referral system
  - Run mvn test and confirm all tests pass
  - Verify referral code is generated on user registration
  - Verify GET /api/v1/referrals/stats endpoint works with valid JWT
  - Verify referral relationships are created correctly
  - Verify bonus cashback is awarded
  - Ask the user if questions arise

- [ ] 13. Implement Withdrawal Flow Backend (Requirement 12)
  - [ ] 13.1 Create Withdrawal entity and enum
    - Create WithdrawalStatus.java enum in enums package
    - Add values: PENDING, APPROVED, PAID, REJECTED
    - Create Withdrawal.java entity in entity package
    - Add fields: id (Long), user (User), upiId (String), amount (BigDecimal), status (WithdrawalStatus), requestedAt (LocalDateTime), processedAt (LocalDateTime)
    - Add @Entity, @ManyToOne, @Enumerated, and @Column annotations
    - Create WithdrawalRepository.java interface extending JpaRepository
    - _Requirements: 12.6, 12.8, 12.10_

  - [ ] 13.2 Implement WithdrawalService for withdrawal logic
    - Create WithdrawalService.java in service package
    - Add @Service annotation
    - Implement createWithdrawalRequest(Long userId, String upiId, BigDecimal amount) method
    - Validate UPI ID format (regex: ^[a-zA-Z0-9._-]+@[a-zA-Z]+$)
    - Validate amount >= 100 rupees
    - Validate amount <= user's cashback balance
    - Create Withdrawal entity with status PENDING
    - Save to database
    - Implement approveWithdrawal(Long withdrawalId) method to update status to APPROVED
    - Implement markAsPaid(Long withdrawalId) method to update status to PAID and deduct balance
    - Implement getWithdrawalHistory(Long userId) method to return user's withdrawals
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8, 12.9, 12.10_

  - [ ] 13.3 Create DTOs for withdrawal system
    - Create WithdrawalRequestDTO.java with fields: upiId, amount
    - Add validation annotations: @NotBlank for upiId, @Min(100) for amount
    - Create WithdrawalDTO.java with fields: id, upiId, amount, status, requestedAt, processedAt
    - Add getters and setters
    - _Requirements: 12.2_

  - [ ] 13.4 Create WithdrawalController with user endpoints
    - Create WithdrawalController.java in controller package
    - Add @RestController and @RequestMapping("/api/v1") annotations
    - Implement POST /withdrawals endpoint
    - Extract user ID from JWT token
    - Validate request body using @Valid annotation
    - Call WithdrawalService to create withdrawal request
    - Return WithdrawalDTO with HTTP 201
    - Implement GET /withdrawals/history endpoint
    - Extract user ID from JWT token
    - Call WithdrawalService to get withdrawal history
    - Return list of WithdrawalDTO with HTTP 200
    - _Requirements: 12.1, 12.2, 12.11, 12.12_

  - [ ] 13.5 Create admin endpoints for withdrawal approval
    - Add @RequestMapping("/api/v1/admin") to WithdrawalController or create separate AdminController
    - Implement PUT /admin/withdrawals/{id}/approve endpoint
    - Verify user has admin role
    - Call WithdrawalService to approve withdrawal
    - Return WithdrawalDTO with HTTP 200
    - Implement PUT /admin/withdrawals/{id}/mark-paid endpoint
    - Verify user has admin role
    - Call WithdrawalService to mark as paid
    - Return WithdrawalDTO with HTTP 200
    - _Requirements: 12.7, 12.9, 12.13_

  - [ ] 13.6 Add error handling for withdrawal system
    - Handle invalid UPI ID format → return 400
    - Handle amount exceeding balance → return 400
    - Handle amount below minimum → return 400
    - Handle withdrawal not found → return 404
    - Handle invalid status transition → return 400
    - Handle missing JWT token → return 401
    - Handle non-admin access to admin endpoints → return 403
    - Add appropriate error messages for each case
    - _Requirements: 12.14, 12.15, 12.5, 12.12, 12.13_

  - [ ] 13.7 Configure withdrawal settings in application.properties
    - Add withdrawal.minimum.amount property with value 100.00
    - Update WithdrawalService to read minimum amount from configuration
    - _Requirements: 12.5_

  - [ ]* 13.8 Write unit tests for withdrawal system
    - Test createWithdrawalRequest validates UPI ID format
    - Test createWithdrawalRequest validates minimum amount
    - Test createWithdrawalRequest validates balance sufficiency
    - Test approveWithdrawal changes status to APPROVED
    - Test markAsPaid changes status to PAID and deducts balance
    - Test POST /api/v1/withdrawals endpoint requires authentication
    - Test admin endpoints require admin role
    - Test invalid UPI ID returns 400
    - Test insufficient balance returns 400
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8, 12.9, 12.10, 12.11, 12.12, 12.13, 12.14, 12.15_

  - [ ]* 13.9 Write property-based tests for withdrawal system
    - **Property 12: UPI ID Format Validation**
    - **Validates: Requirements 12.3, 12.14**
    - **Property 13: Withdrawal Amount Balance Validation**
    - **Validates: Requirements 12.4, 12.15**
    - **Property 14: Withdrawal Minimum Amount Validation**
    - **Validates: Requirements 12.5**
    - **Property 15: Withdrawal Request Storage**
    - **Validates: Requirements 12.6**
    - **Property 16: Withdrawal Approval Status Update**
    - **Validates: Requirements 12.8**
    - **Property 17: Withdrawal Payment Processing**
    - **Validates: Requirements 12.10**
    - **Property 18: Withdrawal Endpoint Authentication**
    - **Validates: Requirements 12.12**
    - **Property 19: Withdrawal Admin Authorization**
    - **Validates: Requirements 12.13**
    - Use jqwik or QuickTheories for property-based testing
    - Run minimum 100 iterations per property

- [ ] 14. Checkpoint - Verify withdrawal system
  - Run mvn test and confirm all tests pass
  - Verify POST /api/v1/withdrawals endpoint works with valid JWT
  - Verify withdrawal validation works correctly
  - Verify admin approval and mark-paid endpoints work
  - Verify balance is deducted after payment
  - Ask the user if questions arise

- [ ] 15. Implement Email Notification System (Requirement 13)
  - [ ] 15.1 Add email dependencies to pom.xml
    - Add Resend Java SDK dependency or spring-boot-starter-mail
    - Add spring-boot-starter-thymeleaf dependency for template rendering
    - _Requirements: 13.1_

  - [ ] 15.2 Configure Resend email service in application.properties
    - Add resend.api.key property with environment variable ${RESEND_API_KEY}
    - Add resend.from.email property with value noreply@payback.com
    - Add resend.from.name property with value Payback
    - _Requirements: 13.1, 13.2, 13.7_

  - [ ] 15.3 Create email templates
    - Create templates/email/ directory in src/main/resources/
    - Create welcome.html template with user name placeholder and getting started information
    - Create cashback-confirmation.html template with transaction amount, cashback amount, and merchant name placeholders
    - Use HTML formatting with inline CSS for styling
    - Include Payback branding and styling
    - _Requirements: 13.3, 13.4, 13.5, 13.6, 13.8, 13.10, 13.11_

  - [ ] 15.4 Implement EmailService for sending notifications
    - Create EmailService.java in service package
    - Add @Service annotation
    - Implement sendWelcomeEmail(User user) method
    - Render welcome.html template with user data
    - Send email via Resend API
    - Implement sendCashbackConfirmation(User user, Transaction transaction) method
    - Render cashback-confirmation.html template with transaction data
    - Send email via Resend API
    - Add error handling: catch exceptions, log errors, don't throw
    - Use @Async annotation for non-blocking email sending
    - _Requirements: 13.1, 13.3, 13.4, 13.5, 13.6, 13.7, 13.9, 13.10_

  - [ ] 15.5 Integrate email notifications into user registration
    - Update user registration logic to call EmailService.sendWelcomeEmail()
    - Ensure registration succeeds even if email fails
    - _Requirements: 13.3, 13.4, 13.9_

  - [ ] 15.6 Integrate email notifications into cashback crediting
    - Update cashback crediting logic to call EmailService.sendCashbackConfirmation()
    - Ensure cashback credit succeeds even if email fails
    - _Requirements: 13.5, 13.6, 13.9_

  - [ ]* 15.7 Write unit tests for email notification system
    - Test sendWelcomeEmail sends email with user name
    - Test sendCashbackConfirmation includes transaction details
    - Test email sending failure is logged but doesn't throw exception
    - Test email templates exist in resources/templates/email/
    - Test Resend API configuration is loaded from application.properties
    - Test email content is HTML formatted
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 13.10, 13.11_

  - [ ]* 15.8 Write property-based tests for email notification system
    - **Property 20: Welcome Email Delivery**
    - **Validates: Requirements 13.3, 13.4**
    - **Property 21: Cashback Confirmation Email Delivery**
    - **Validates: Requirements 13.5, 13.6**
    - **Property 22: Email Failure Resilience**
    - **Validates: Requirements 13.9**
    - **Property 23: Email HTML Formatting**
    - **Validates: Requirements 13.10**
    - Use jqwik or QuickTheories for property-based testing
    - Run minimum 100 iterations per property

- [ ] 16. Final checkpoint - Verify all new features
  - Run mvn test and confirm all tests pass
  - Verify merchant data is loaded in database
  - Verify referral system works end-to-end
  - Verify withdrawal flow works with admin approval
  - Verify email notifications are sent correctly
  - Ensure all tests pass, ask the user if questions arise

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (Properties 1-23)
- Unit tests validate specific examples and edge cases
- JwtService implementation may be part of a separate authentication feature - task 6.2 is primarily verification
- Docker container uses host port 5433 to avoid conflicts with local PostgreSQL installations
- User profile management (Task 8) requires JWT authentication to be implemented first
- Referral system (Task 11) requires User entity updates and cashback balance tracking
- Withdrawal system (Task 13) requires admin role implementation for approval endpoints
- Email notification system (Task 15) requires Resend API key configuration via environment variable
