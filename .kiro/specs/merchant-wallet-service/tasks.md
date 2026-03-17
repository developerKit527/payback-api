# Implementation Plan: Merchant & Wallet Service

## Overview

This implementation plan breaks down the Merchant & Wallet Service into discrete coding tasks. The service provides merchant management, click tracking, and wallet/transaction functionality using Java 25, Spring Boot 3.2, Spring Data JPA, and the existing PostgreSQL database `payback_db`.

The implementation follows a bottom-up approach: entities first, then repositories, services, controllers, and finally seed data initialization. Testing tasks are included as optional sub-tasks to enable faster MVP delivery while maintaining quality standards.

## Tasks

- [x] 1. Set up project structure and dependencies
  - Create package structure: `com.payback.api.{entity, repository, service, dto, controller, config, exception}`
  - Add required dependencies to `pom.xml`: Spring Data JPA, PostgreSQL driver, Lombok, Bean Validation
  - Verify existing database configuration in `application.properties` (payback_db connection)
  - _Requirements: 8.1, 8.2_

- [x] 2. Create core entities
  - [x] 2.1 Create Merchant entity
    - Create `com.payback.api.entity.Merchant` class with JPA annotations
    - Define fields: id (Long, auto-generated), name (String, required), logoUrl (String), cashbackRate (Double, required), manualTrackingUrl (String), clickCount (Long, default 0)
    - Add Lombok annotations: @Data, @NoArgsConstructor, @AllArgsConstructor
    - Add JPA annotations: @Entity, @Table(name = "merchants"), @Id, @GeneratedValue, @Column
    - _Requirements: 1.1, 1.4_
  
  - [ ]* 2.2 Write property test for Merchant persistence
    - **Property 1: Merchant Persistence Round-Trip**
    - **Validates: Requirements 1.1, 8.1**
  
  - [x] 2.3 Create Wallet entity
    - Create `com.payback.api.entity.Wallet` class with JPA annotations
    - Define fields: id (Long, auto-generated), userId (Long, unique, required), totalBalance (BigDecimal, default 0.00, precision 10 scale 2), upiId (String)
    - Add @OneToMany relationship to Transaction entity (mappedBy = "wallet", cascade = ALL, fetch = LAZY)
    - Add Lombok and JPA annotations
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [ ]* 2.4 Write property test for Wallet persistence
    - **Property 9: Wallet Persistence Round-Trip**
    - **Validates: Requirements 3.2, 8.2**
  
  - [x] 2.5 Create Transaction entity
    - Create `com.payback.api.entity.Transaction` class with JPA annotations
    - Define fields: id (Long, auto-generated), wallet (Wallet, foreign key, required), merchantName (String, required), orderAmount (BigDecimal, precision 10 scale 2), cashbackAmount (BigDecimal, precision 10 scale 2), status (TransactionStatus enum), createdAt (LocalDateTime)
    - Add @ManyToOne relationship to Wallet (fetch = LAZY, optional = false)
    - Add @PrePersist method to auto-set createdAt and default status to PENDING
    - Add Lombok and JPA annotations
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [ ]* 2.6 Write property test for Transaction persistence
    - **Property 11: Transaction Persistence Round-Trip**
    - **Validates: Requirements 4.1, 8.3**
  
  - [x] 2.7 Create TransactionStatus enum
    - Create `com.payback.api.entity.TransactionStatus` enum with values: PENDING, CONFIRMED, REJECTED
    - _Requirements: 4.3, 5.1_

- [x] 3. Create repository interfaces
  - [x] 3.1 Create MerchantRepository
    - Create `com.payback.api.repository.MerchantRepository` interface extending JpaRepository<Merchant, Long>
    - Add custom query method: `List<Merchant> findAllByOrderByClickCountDesc()`
    - _Requirements: 1.2_
  
  - [ ]* 3.2 Write property test for merchant listing sort order
    - **Property 2: Merchant Listing Sort Order**
    - **Validates: Requirements 1.2**
  
  - [x] 3.3 Create WalletRepository
    - Create `com.payback.api.repository.WalletRepository` interface extending JpaRepository<Wallet, Long>
    - Add custom query method: `Optional<Wallet> findByUserId(Long userId)`
    - _Requirements: 3.1, 3.4_
  
  - [ ]* 3.4 Write property test for wallet uniqueness per user
    - **Property 8: Wallet Uniqueness Per User**
    - **Validates: Requirements 3.1, 3.4**
  
  - [x] 3.5 Create TransactionRepository
    - Create `com.payback.api.repository.TransactionRepository` interface extending JpaRepository<Transaction, Long>
    - Add custom query methods: `List<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet)` and `List<Transaction> findByWalletAndStatus(Wallet wallet, TransactionStatus status)`
    - _Requirements: 6.2, 7.2, 7.3_

- [x] 4. Create DTOs for API requests and responses
  - [x] 4.1 Create MerchantDTO
    - Create `com.payback.api.dto.MerchantDTO` class with fields: id, name, logoUrl, cashbackRate, manualTrackingUrl, clickCount
    - Add Lombok annotations: @Data, @AllArgsConstructor, @NoArgsConstructor
    - _Requirements: 1.5_
  
  - [x] 4.2 Create TransactionDTO
    - Create `com.payback.api.dto.TransactionDTO` class with fields: id, merchantName, orderAmount, cashbackAmount, status (String), createdAt
    - Add Lombok annotations
    - _Requirements: 6.3, 9.3, 9.4_
  
  - [x] 4.3 Create WalletResponseDTO
    - Create `com.payback.api.dto.WalletResponseDTO` class with fields: userId, totalBalance, upiId, totalEarned, pendingAmount, availableBalance, transactions (List<TransactionDTO>)
    - Add Lombok annotations
    - _Requirements: 6.1, 7.4_
  
  - [x] 4.4 Create ErrorResponse DTO
    - Create `com.payback.api.dto.ErrorResponse` class with fields: error, message
    - Add Lombok annotations
    - _Requirements: 9.2_

- [x] 5. Implement service layer
  - [x] 5.1 Create MerchantService interface and implementation
    - Create `com.payback.api.service.MerchantService` interface with methods: getAllMerchants(), incrementClickCount(Long merchantId), initializeSeedData()
    - Create `com.payback.api.service.impl.MerchantServiceImpl` implementation
    - Implement getAllMerchants(): fetch from repository, convert to DTOs, return sorted by click count
    - Implement incrementClickCount(): fetch merchant, increment count, save, throw EntityNotFoundException if not found
    - _Requirements: 1.2, 2.1, 2.4_
  
  - [ ]* 5.2 Write property tests for merchant service
    - **Property 3: Merchant Click Count Default**
    - **Property 4: Click Increment Atomicity**
    - **Validates: Requirements 1.4, 2.1, 2.4**
  
  - [ ]* 5.3 Write unit tests for MerchantService
    - Test getAllMerchants returns correct order
    - Test incrementClickCount for existing merchant
    - Test incrementClickCount throws exception for non-existent merchant
    - _Requirements: 1.2, 2.1_
  
  - [x] 5.4 Create TransactionService interface and implementation
    - Create `com.payback.api.service.TransactionService` interface with methods: createTransaction(), updateTransactionStatus(), getTransactionsByWallet(), initializeSeedData()
    - Create `com.payback.api.service.impl.TransactionServiceImpl` implementation
    - Implement createTransaction(): create transaction with PENDING status, auto-set timestamp, save
    - Implement updateTransactionStatus(): fetch transaction, update status, if CONFIRMED update wallet balance, save
    - Implement getTransactionsByWallet(): fetch transactions ordered by createdAt desc, convert to DTOs
    - _Requirements: 4.2, 4.3, 5.2, 5.3, 6.2_
  
  - [ ]* 5.5 Write property tests for transaction service
    - **Property 12: Transaction Default Status Pending**
    - **Property 13: Transaction Timestamp Auto-Generation**
    - **Property 15: Cashback Calculation Correctness**
    - **Property 17: Confirmed Status Increases Balance**
    - **Property 18: Rejected Status Preserves Balance**
    - **Validates: Requirements 4.2, 4.3, 4.5, 5.2, 5.3**
  
  - [ ]* 5.6 Write unit tests for TransactionService
    - Test createTransaction sets PENDING status and timestamp
    - Test updateTransactionStatus to CONFIRMED increases wallet balance
    - Test updateTransactionStatus to REJECTED preserves wallet balance
    - Test getTransactionsByWallet returns correct order
    - _Requirements: 4.2, 5.2, 5.3, 6.2_
  
  - [x] 5.7 Create WalletService interface and implementation
    - Create `com.payback.api.service.WalletService` interface with methods: getWalletByUserId(Long userId), createWallet(Long userId, String upiId), initializeSeedData()
    - Create `com.payback.api.service.impl.WalletServiceImpl` implementation
    - Implement getWalletByUserId(): fetch wallet, fetch transactions, calculate balances (totalEarned, pendingAmount, availableBalance), convert to DTO, throw EntityNotFoundException if not found
    - Implement createWallet(): create wallet with zero balance, save
    - Inject TransactionService for transaction retrieval
    - _Requirements: 3.3, 6.1, 6.2, 7.1, 7.2, 7.3, 7.4_
  
  - [ ]* 5.8 Write property tests for wallet service
    - **Property 10: Wallet Initial Balance Zero**
    - **Property 19: Pending Transactions Excluded From Available Balance**
    - **Property 24: Total Earned Calculation**
    - **Property 25: Pending Amount Calculation**
    - **Property 26: Available Balance Calculation**
    - **Property 28: Decimal Precision Preservation**
    - **Validates: Requirements 3.3, 5.4, 7.1, 7.2, 7.3, 7.5**
  
  - [ ]* 5.9 Write unit tests for WalletService
    - Test createWallet initializes balance to zero
    - Test getWalletByUserId calculates correct balances with known transaction sets
    - Test getWalletByUserId throws exception for non-existent user
    - _Requirements: 3.3, 6.1, 7.1, 7.2, 7.3_

- [ ] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement controller layer
  - [x] 7.1 Create MerchantController
    - Create `com.payback.api.controller.MerchantController` class with @RestController and @RequestMapping("/api/v1/merchants")
    - Implement GET /api/v1/merchants endpoint: call merchantService.getAllMerchants(), return ResponseEntity with 200 status
    - Implement POST /api/v1/merchants/{id}/click endpoint: call merchantService.incrementClickCount(id), return 200 OK or 404 if not found
    - Inject MerchantService
    - _Requirements: 1.2, 1.5, 2.1, 2.2, 2.3_
  
  - [ ]* 7.2 Write property tests for merchant controller
    - **Property 6: Valid Merchant Click Returns Success**
    - **Property 7: Invalid Merchant Click Returns Not Found**
    - **Validates: Requirements 2.2, 2.3**
  
  - [ ]* 7.3 Write integration tests for MerchantController
    - Test GET /api/v1/merchants returns 200 and JSON array
    - Test POST /api/v1/merchants/{id}/click returns 200 for valid ID
    - Test POST /api/v1/merchants/{id}/click returns 404 for invalid ID
    - Test response format matches MerchantDTO structure
    - _Requirements: 1.5, 2.2, 2.3, 9.1_
  
  - [x] 7.4 Create WalletController
    - Create `com.payback.api.controller.WalletController` class with @RestController and @RequestMapping("/api/v1/wallet")
    - Implement GET /api/v1/wallet/{userId} endpoint: call walletService.getWalletByUserId(userId), return ResponseEntity with 200 status or 404 if not found
    - Inject WalletService
    - _Requirements: 6.1, 6.2, 6.4, 6.5_
  
  - [ ]* 7.5 Write property tests for wallet controller
    - **Property 20: Wallet Retrieval Completeness**
    - **Property 21: Transaction List Sort Order**
    - **Property 22: Transaction DTO Completeness**
    - **Property 23: Invalid User Returns Not Found**
    - **Property 27: Balance Summary Completeness**
    - **Validates: Requirements 6.1, 6.2, 6.3, 6.4, 7.4**
  
  - [ ]* 7.6 Write integration tests for WalletController
    - Test GET /api/v1/wallet/{userId} returns 200 and complete wallet data
    - Test GET /api/v1/wallet/{userId} returns 404 for non-existent user
    - Test response includes all balance calculations
    - Test transactions are ordered by createdAt desc
    - Test response format matches WalletResponseDTO structure
    - _Requirements: 6.1, 6.2, 6.4, 7.4, 9.1_

- [x] 8. Implement global exception handling
  - [x] 8.1 Create custom exceptions
    - Create `com.payback.api.exception.EntityNotFoundException` extending RuntimeException
    - _Requirements: 2.3, 6.4_
  
  - [x] 8.2 Create GlobalExceptionHandler
    - Create `com.payback.api.exception.GlobalExceptionHandler` class with @RestControllerAdvice
    - Add @ExceptionHandler for EntityNotFoundException: return 404 with ErrorResponse
    - Add @ExceptionHandler for IllegalArgumentException: return 400 with ErrorResponse
    - Add @ExceptionHandler for Exception: return 500 with ErrorResponse
    - _Requirements: 9.2_
  
  - [ ]* 8.3 Write property tests for error handling
    - **Property 30: Error Response Status Codes**
    - **Validates: Requirements 9.2**
  
  - [ ]* 8.4 Write unit tests for GlobalExceptionHandler
    - Test EntityNotFoundException returns 404
    - Test IllegalArgumentException returns 400
    - Test generic Exception returns 500
    - _Requirements: 9.2_

- [x] 9. Implement seed data initialization
  - [x] 9.1 Create SeedDataLoader component
    - Create `com.payback.api.config.SeedDataLoader` class with @Component and implement CommandLineRunner
    - Inject MerchantService, WalletService, TransactionService
    - In run() method, call initializeSeedData() on all three services
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [x] 9.2 Implement MerchantService.initializeSeedData()
    - Check if merchants exist by name (Flipkart, Myntra, Ajio)
    - If not exist, create and save three merchants with specified cashback rates and zero click counts
    - Flipkart: 10% cashback, Myntra: 8.5% cashback, Ajio: 7% cashback
    - _Requirements: 1.3, 10.1, 10.4_
  
  - [x] 9.3 Implement WalletService.initializeSeedData()
    - Check if wallet exists for userId = 1
    - If not exist, create wallet with userId = 1, totalBalance = 0.00, upiId = "user1@paytm"
    - _Requirements: 3.5, 10.2, 10.4_
  
  - [x] 9.4 Implement TransactionService.initializeSeedData()
    - Check if transactions exist for wallet with userId = 1
    - If not exist, create two PENDING transactions:
      - Transaction 1: Flipkart, orderAmount = 2000.00, cashbackAmount = 200.00
      - Transaction 2: Myntra, orderAmount = 1500.00, cashbackAmount = 127.50
    - _Requirements: 4.7, 10.3, 10.4_
  
  - [ ]* 9.5 Write property test for seed data idempotency
    - **Property 34: Seed Data Idempotency**
    - **Validates: Requirements 10.4**
  
  - [ ]* 9.6 Write unit tests for seed data initialization
    - Test MerchantService.initializeSeedData() creates exactly 3 merchants
    - Test WalletService.initializeSeedData() creates exactly 1 wallet
    - Test TransactionService.initializeSeedData() creates exactly 2 transactions
    - Test running initialization twice does not create duplicates
    - _Requirements: 1.3, 3.5, 4.7, 10.1, 10.2, 10.3, 10.4_

- [x] 10. Configure JSON serialization and CORS
  - [x] 10.1 Configure Jackson for monetary values and timestamps
    - Create `com.payback.api.config.JacksonConfig` class with @Configuration
    - Configure ObjectMapper to format BigDecimal with 2 decimal places
    - Configure ObjectMapper to format LocalDateTime in ISO 8601 format
    - _Requirements: 9.3, 9.4_
  
  - [ ]* 10.2 Write property tests for response formatting
    - **Property 29: Successful Response Format**
    - **Property 31: Monetary Value Formatting**
    - **Property 32: Timestamp ISO 8601 Format**
    - **Validates: Requirements 9.1, 9.3, 9.4**
  
  - [x] 10.3 Configure CORS
    - Create `com.payback.api.config.CorsConfig` class with @Configuration
    - Add @Bean for WebMvcConfigurer with CORS mappings allowing all origins for development
    - _Requirements: 9.5_
  
  - [ ]* 10.4 Write property test for CORS headers
    - **Property 33: CORS Headers Present**
    - **Validates: Requirements 9.5**

- [-] 11. Implement JWT authentication

  - [x] 11.1 Add auth dependencies to pom.xml
    - Add `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (io.jsonwebtoken 0.12.x) to pom.xml
    - Add `spring-security-test` in test scope
    - _Requirements: 13.1_

  - [x] 11.2 Create User entity
    - Create `com.payback.api.entity.User` with fields: id (Long, PK), name (String, required), email (String, unique, required), passwordHash (String, required)
    - Add JPA annotations: @Entity, @Table(name = "users"), unique constraint on email
    - _Requirements: 11.5_

  - [x] 11.3 Create UserRepository
    - Create `com.payback.api.repository.UserRepository` extending JpaRepository<User, Long>
    - Add `Optional<User> findByEmail(String email)`
    - _Requirements: 11.3, 12.3_

  - [x] 11.4 Create auth DTOs
    - Create `RegisterRequestDTO` (name, email, password), `LoginRequestDTO` (email, password)
    - Create `AuthResponseDTO` (token, UserDTO) and `UserDTO` (id, name, email)
    - _Requirements: 11.1, 12.1_

  - [x] 11.5 Create JwtService
    - Create `com.payback.api.service.JwtService` that reads JWT_SECRET from environment
    - Implement `generateToken(User user)`: sign HS256, embed userId/email/name claims, set 86400000 ms expiry
    - Implement `validateToken(String token)`: return claims or throw on invalid/expired
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

  - [x] 11.6 Create AuthService and AuthController
    - Create `com.payback.api.service.AuthService` interface and `AuthServiceImpl`
    - `register()`: check email uniqueness (400 if taken), BCrypt-hash password, save User, auto-create wallet via WalletService, return JWT + UserDTO with 201
    - `login()`: look up user by email, verify BCrypt hash, return JWT + UserDTO with 200; return generic 400 on any failure
    - Create `com.payback.api.controller.AuthController` with @RequestMapping("/api/v1/auth")
    - Map POST /register → 201, POST /login → 200
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 12.1, 12.2, 12.3, 12.4_

  - [x] 11.7 Create JWT filter and Spring Security config
    - Create `JwtAuthenticationFilter` extending OncePerRequestFilter: extract Bearer token, validate, set SecurityContext
    - Create `SecurityConfig` (@Configuration, @EnableWebSecurity):
      - Permit all: POST /api/v1/auth/**, GET /api/v1/merchants/**, POST /api/v1/merchants/**/click, GET /api/v1/health, GET /api/v1/wallet/{userId}
      - Require authentication: GET /api/v1/wallet/me
      - Add JwtAuthenticationFilter before UsernamePasswordAuthenticationFilter
      - Disable CSRF (stateless API), disable session creation
    - _Requirements: 13.4, 14.1, 14.3, 15.1, 15.2, 15.3, 15.4, 15.5_

  - [x] 11.8 Add GET /api/v1/wallet/me endpoint
    - In WalletController, add GET /me endpoint
    - Extract userId from SecurityContext (JWT claims), call walletService.getWalletByUserId(userId)
    - Return 200 with WalletResponseDTO; 401 handled by security filter
    - _Requirements: 14.1, 14.2_

  - [ ]* 11.9 Write property tests for auth
    - **Property 35: Password Never Stored Plaintext**
    - **Property 36: Duplicate Email Registration Rejected**
    - **Property 37: JWT Claims Completeness**
    - **Property 38: JWT Expiry Enforced**
    - **Property 39: Invalid Credentials Return Generic Error**
    - **Property 40: Wallet Auto-Created On Registration**
    - **Property 41: Protected Endpoint Requires Valid JWT**
    - **Property 42: Authenticated Wallet Returns Own Data Only**
    - _Requirements: 11.3, 11.4, 11.5, 12.3, 12.4, 13.3, 13.4, 14.1, 14.2_

- [-] 14. Implement transaction creation endpoint

  - [x] 14.1 Create CreateTransactionRequestDTO
    - Create `com.payback.api.dto.CreateTransactionRequestDTO` with fields: merchantId (Long), orderAmount (BigDecimal)
    - Add Lombok @Data annotation
    - _Requirements: 16.2_

  - [x] 14.2 Add createTransactionForUser() to TransactionService
    - Add method `TransactionDTO createTransactionForUser(Long userId, Long merchantId, BigDecimal orderAmount)` to TransactionService interface
    - Implement in TransactionServiceImpl:
      - Look up wallet by userId (throw EntityNotFoundException if not found)
      - Look up merchant by merchantId (throw EntityNotFoundException if not found)
      - Calculate cashbackAmount = orderAmount × (merchant.cashbackRate / 100)
      - Create and save Transaction with PENDING status linked to the wallet
      - Return TransactionDTO
    - _Requirements: 16.3, 16.4, 16.5, 16.6, 16.7_

  - [x] 14.3 Create TransactionController
    - Create `com.payback.api.controller.TransactionController` with @RequestMapping("/api/v1/transactions")
    - Implement POST / endpoint: extract userId from Authentication principal, call transactionService.createTransactionForUser(), return 201 with TransactionDTO
    - _Requirements: 16.1, 16.5_

  - [x] 14.4 Update SecurityConfig to protect POST /api/v1/transactions
    - Add `.requestMatchers(HttpMethod.POST, "/api/v1/transactions").authenticated()` to the security filter chain
    - _Requirements: 16.8, 16.9_

  - [ ]* 14.5 Write property tests for transaction creation endpoint
    - **Property 43: Transaction Created on Merchant Click**
    - **Property 44: Transaction Creation Requires Authentication**
    - _Requirements: 16.3, 16.4, 16.5, 16.8_

- [ ] 15. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 16. Integration verification and manual testing
  - [ ] 16.1 Verify database schema creation
    - Start application and verify four tables created: users, merchants, wallets, transactions
    - Verify foreign key constraint from transactions to wallets
    - Verify unique constraint on wallets.user_id and users.email
    - _Requirements: 8.3, 8.4, 8.5_
  
  - [ ] 16.2 Verify seed data loaded correctly
    - Query database to confirm 3 merchants exist
    - Query database to confirm 1 wallet exists for userId = 1
    - Query database to confirm 2 PENDING transactions exist
    - _Requirements: 10.1, 10.2, 10.3_
  
  - [ ] 16.3 Test merchant listing endpoint
    - Call GET /api/v1/merchants and verify response contains 3 merchants
    - Verify merchants are sorted by clickCount
    - Verify JSON format matches MerchantDTO structure
    - _Requirements: 1.2, 1.5, 9.1_
  
  - [ ] 16.4 Test click tracking endpoint
    - Call POST /api/v1/merchants/1/click and verify 200 response
    - Call GET /api/v1/merchants and verify clickCount incremented
    - Call POST /api/v1/merchants/999/click and verify 404 response
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 16.5 Test wallet retrieval endpoint
    - Call GET /api/v1/wallet/1 and verify response contains wallet data
    - Verify totalEarned = 327.50, pendingAmount = 327.50, availableBalance = 0.00
    - Verify transactions array contains 2 items ordered by createdAt desc
    - _Requirements: 6.1, 6.2, 6.3, 7.1, 7.2, 7.3, 7.4_

  - [ ] 16.6 Test auth endpoints
    - POST /api/v1/auth/register with new email → 201 + JWT
    - POST /api/v1/auth/register with duplicate email → 400
    - POST /api/v1/auth/login with valid credentials → 200 + JWT
    - POST /api/v1/auth/login with wrong password → 400 (generic message)
    - GET /api/v1/wallet/me with valid JWT → 200 + wallet data
    - GET /api/v1/wallet/me without JWT → 401
  - [ ] 16.7 Test transaction creation endpoint
    - POST /api/v1/transactions with valid JWT + merchantId + orderAmount → 201 + TransactionDTO
    - POST /api/v1/transactions without JWT → 401
    - POST /api/v1/transactions with invalid merchantId → 404
    - Verify transaction appears in GET /api/v1/wallet/me response
    - _Requirements: 16.1, 16.5, 16.6, 16.7, 16.8_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties across all inputs
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end API behavior
- All code should be placed in the `payback-api` directory
- The existing `payback_db` database connection is already configured in `application.properties`
- Seed data initialization is idempotent and safe to run on every startup
