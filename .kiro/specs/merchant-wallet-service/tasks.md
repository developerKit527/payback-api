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

- [ ] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 12. Integration verification and manual testing
  - [ ] 12.1 Verify database schema creation
    - Start application and verify three tables created: merchants, wallets, transactions
    - Verify foreign key constraint from transactions to wallets
    - Verify unique constraint on wallets.user_id
    - _Requirements: 8.3, 8.4, 8.5_
  
  - [ ] 12.2 Verify seed data loaded correctly
    - Query database to confirm 3 merchants exist
    - Query database to confirm 1 wallet exists for userId = 1
    - Query database to confirm 2 PENDING transactions exist
    - _Requirements: 10.1, 10.2, 10.3_
  
  - [ ] 12.3 Test merchant listing endpoint
    - Call GET /api/v1/merchants and verify response contains 3 merchants
    - Verify merchants are sorted by clickCount
    - Verify JSON format matches MerchantDTO structure
    - _Requirements: 1.2, 1.5, 9.1_
  
  - [ ] 12.4 Test click tracking endpoint
    - Call POST /api/v1/merchants/1/click and verify 200 response
    - Call GET /api/v1/merchants and verify clickCount incremented
    - Call POST /api/v1/merchants/999/click and verify 404 response
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
  
  - [ ] 12.5 Test wallet retrieval endpoint
    - Call GET /api/v1/wallet/1 and verify response contains wallet data
    - Verify totalEarned = 327.50 (sum of both transactions)
    - Verify pendingAmount = 327.50 (both transactions are PENDING)
    - Verify availableBalance = 0.00 (no CONFIRMED transactions)
    - Verify transactions array contains 2 items ordered by createdAt desc
    - _Requirements: 6.1, 6.2, 6.3, 7.1, 7.2, 7.3, 7.4_

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
