# Requirements Document

## Introduction

This document defines the requirements for the Merchant & Wallet Service, the core backend engine for PayBack India - a cashback aggregator platform for the Indian market. The system enables users to discover merchants, track affiliate clicks, earn cashback on purchases, and manage their wallet transactions with detailed history and withdrawal capabilities.

## Glossary

- **Merchant_Service**: The component responsible for managing merchant data and tracking user interactions
- **Wallet_Service**: The component responsible for managing user wallets and transaction records
- **Transaction_Engine**: The component that processes and records cashback transactions
- **Transaction_Entity**: The database entity that links to Wallet and tracks individual cashback history with merchant name, order amount, cashback amount, status, and timestamp
- **Click_Tracker**: The component that increments merchant click counts
- **API_Gateway**: The REST API layer that exposes service endpoints
- **Database**: The EXISTING PostgreSQL database named payback_db (configured at jdbc:postgresql://localhost:5432/payback_db with username admin and password password123)
- **User**: A registered platform user who shops through affiliate links
- **Merchant**: An e-commerce partner offering cashback (e.g., Flipkart, Myntra, Ajio)
- **Transaction**: A record of a purchase and its associated cashback
- **Wallet**: A user's account containing balance and transaction history
- **Click_Count**: The number of times users have clicked on a merchant's affiliate link
- **Cashback_Rate**: The percentage of order amount returned as cashback
- **Transaction_Status**: The state of a transaction (PENDING, CONFIRMED, or REJECTED)
- **UPI_ID**: Unified Payments Interface identifier for withdrawals

## Requirements

### Requirement 1: Merchant Listing

**User Story:** As a user, I want to view available merchants sorted by popularity, so that I can discover trusted stores first.

#### Acceptance Criteria

1. THE Merchant_Service SHALL store merchant records with name, logo URL, cashback rate, tracking URL, and click count
2. WHEN a merchant listing request is received, THE API_Gateway SHALL return all merchants ordered by click count in descending order
3. THE Merchant_Service SHALL initialize three seed merchants on first startup: Flipkart with 10% cashback rate, Myntra with 8.5% cashback rate, and Ajio with 7% cashback rate
4. FOR ALL merchant records, THE Database SHALL enforce that click count defaults to zero when not specified
5. THE API_Gateway SHALL return merchant data in JSON format with all merchant fields included

### Requirement 2: Click Tracking

**User Story:** As a platform operator, I want to track merchant link clicks, so that popular merchants appear first in listings.

#### Acceptance Criteria

1. WHEN a click tracking request is received for a valid merchant, THE Click_Tracker SHALL increment the merchant's click count by one
2. WHEN a click tracking request is received for a valid merchant, THE API_Gateway SHALL return HTTP status 200
3. IF a click tracking request is received for a non-existent merchant, THEN THE API_Gateway SHALL return HTTP status 404
4. THE Click_Tracker SHALL persist click count changes to the Database immediately
5. FOR ALL click tracking operations, THE Click_Tracker SHALL ensure atomicity to prevent race conditions

### Requirement 3: User Wallet Management

**User Story:** As a user, I want a personal wallet to track my cashback earnings, so that I can see my available balance.

#### Acceptance Criteria

1. THE Wallet_Service SHALL create exactly one wallet per user identifier
2. THE Wallet_Service SHALL store wallet records with user identifier, total balance, and UPI identifier
3. WHEN a wallet creation request is received, THE Wallet_Service SHALL initialize total balance to zero
4. THE Database SHALL enforce uniqueness constraint on user identifier in wallet records
5. THE Wallet_Service SHALL initialize one seed wallet for user identifier 1 on first startup

### Requirement 4: Transaction Entity and Recording (CRITICAL - "Individual Record" Rule)

**User Story:** As a user, I want to see detailed history of every purchase, so that I can track my cashback earnings.

**Context:** This implements the "Individual Record" Rule from PAYBACK_PRD.md - users must be able to see a detailed history of every purchase with full transaction lifecycle tracking.

#### Acceptance Criteria

1. THE Transaction_Entity SHALL be stored in the Database with the following fields: transaction identifier (primary key), wallet reference (foreign key), merchant name, order amount, cashback amount, status, and creation timestamp
2. THE Transaction_Entity SHALL link to exactly one Wallet via foreign key constraint to maintain referential integrity
3. THE Transaction_Entity SHALL support exactly three status values: PENDING (tracked but not payable), CONFIRMED (cashback received and ready for withdrawal), and REJECTED (order cancelled or returned)
4. WHEN a transaction is created, THE Transaction_Engine SHALL set status to PENDING
5. WHEN a transaction is created, THE Transaction_Engine SHALL record the current timestamp
6. THE Transaction_Engine SHALL calculate cashback amount based on order amount and merchant cashback rate
7. THE Transaction_Engine SHALL initialize two seed transactions with PENDING status for user identifier 1 on first startup
8. FOR ALL transactions, THE Database SHALL enforce NOT NULL constraints on wallet reference, merchant name, order amount, cashback amount, status, and creation timestamp

### Requirement 5: Transaction Status Management

**User Story:** As a user, I want to see the status of my transactions, so that I know which cashback is confirmed or pending.

#### Acceptance Criteria

1. THE Transaction_Engine SHALL support exactly three transaction statuses: PENDING, CONFIRMED, and REJECTED
2. WHEN a transaction status changes to CONFIRMED, THE Wallet_Service SHALL add the cashback amount to the wallet's total balance
3. WHEN a transaction status changes to REJECTED, THE Wallet_Service SHALL not modify the wallet's total balance
4. WHILE a transaction status is PENDING, THE Wallet_Service SHALL not include the cashback amount in available withdrawal balance
5. THE Transaction_Engine SHALL record status transitions with timestamp information

### Requirement 6: Wallet History Retrieval

**User Story:** As a user, I want to retrieve my wallet details and transaction history, so that I can review my earnings.

#### Acceptance Criteria

1. WHEN a wallet retrieval request is received for a valid user, THE API_Gateway SHALL return wallet details including total balance and UPI identifier
2. WHEN a wallet retrieval request is received for a valid user, THE API_Gateway SHALL return all associated transactions ordered by creation timestamp in descending order
3. FOR ALL transactions returned, THE API_Gateway SHALL include merchant name, order amount, cashback amount, status, and creation timestamp
4. IF a wallet retrieval request is received for a non-existent user, THEN THE API_Gateway SHALL return HTTP status 404
5. THE API_Gateway SHALL return wallet and transaction data in JSON format

### Requirement 7: Wallet Balance Calculation

**User Story:** As a user, I want to see my total earned, pending amount, and available balance, so that I know how much I can withdraw.

#### Acceptance Criteria

1. THE Wallet_Service SHALL calculate total earned as the sum of all transaction cashback amounts regardless of status
2. THE Wallet_Service SHALL calculate pending amount as the sum of cashback amounts for transactions with PENDING status
3. THE Wallet_Service SHALL calculate available balance as the sum of cashback amounts for transactions with CONFIRMED status
4. WHEN wallet summary is requested, THE Wallet_Service SHALL return all three calculated values: total earned, pending amount, and available balance
5. FOR ALL balance calculations, THE Wallet_Service SHALL use decimal precision to prevent rounding errors

### Requirement 8: Database Configuration and Persistence

**User Story:** As a platform operator, I want all data persisted reliably in the existing database, so that user earnings are never lost.

**Context:** This service uses the EXISTING payback_db PostgreSQL database already configured in payback-api/src/main/resources/application.properties (Connection: jdbc:postgresql://localhost:5432/payback_db, Username: admin, Password: password123).

#### Acceptance Criteria

1. THE Database SHALL use the existing payback_db PostgreSQL database configured at jdbc:postgresql://localhost:5432/payback_db
2. THE Database SHALL authenticate using username admin and password password123 as configured in application.properties
3. THE Database SHALL persist all merchant records with referential integrity
4. THE Database SHALL persist all wallet records with referential integrity
5. THE Database SHALL persist all Transaction_Entity records with foreign key constraints to wallet records
6. WHEN the application restarts, THE Database SHALL retain all previously stored data
7. THE Database SHALL enforce data type constraints for all numeric fields to prevent data corruption
8. THE Database SHALL use Hibernate DDL auto-update mode to manage schema evolution without data loss

### Requirement 9: API Response Format

**User Story:** As a frontend developer, I want consistent JSON responses, so that I can reliably parse API data.

#### Acceptance Criteria

1. THE API_Gateway SHALL return all successful responses with HTTP status 200 and JSON content type
2. WHEN an error occurs, THE API_Gateway SHALL return appropriate HTTP status codes: 404 for not found, 400 for bad request, 500 for server error
3. THE API_Gateway SHALL format all monetary values as decimal numbers with two decimal places
4. THE API_Gateway SHALL format all timestamps in ISO 8601 format
5. FOR ALL API responses, THE API_Gateway SHALL include appropriate CORS headers for cross-origin requests

### Requirement 10: Seed Data Initialization

**User Story:** As a developer, I want seed data loaded on startup, so that I can test the system immediately.

#### Acceptance Criteria

1. WHEN the application starts for the first time, THE Merchant_Service SHALL verify and insert three merchant records if they do not exist
2. WHEN the application starts for the first time, THE Wallet_Service SHALL verify and insert one wallet for user identifier 1 if it does not exist
3. WHEN the application starts for the first time, THE Transaction_Engine SHALL verify and insert two PENDING transactions for user identifier 1 if they do not exist
4. THE Database SHALL support idempotent seed data initialization to prevent duplicate records on restart
5. FOR ALL seed data operations, THE Database SHALL complete initialization before accepting API requests

### Requirement 11: User Registration

**User Story:** As a new user, I want to create an account, so that I can track my personal cashback.

#### Acceptance Criteria

1. THE API_Gateway SHALL expose a POST /api/v1/auth/register endpoint accepting name, email, and password fields
2. WHEN a registration request is received with a unique email, THE API_Gateway SHALL return HTTP status 201 with a JWT token and user info (id, name, email)
3. IF a registration request is received with an already-registered email, THEN THE API_Gateway SHALL return HTTP status 400
4. WHEN a new user registers successfully, THE Wallet_Service SHALL automatically create a wallet for that user
5. THE Auth_Service SHALL store passwords as bcrypt hashes and SHALL never store or return plaintext passwords

### Requirement 12: User Login

**User Story:** As a returning user, I want to log in, so that I can access my wallet.

#### Acceptance Criteria

1. THE API_Gateway SHALL expose a POST /api/v1/auth/login endpoint accepting email and password fields
2. WHEN a login request is received with valid credentials, THE API_Gateway SHALL return HTTP status 200 with a JWT token and user info (id, name, email)
3. IF a login request is received with an email that does not exist or a wrong password, THEN THE API_Gateway SHALL return HTTP status 400
4. THE Auth_Service SHALL never reveal which specific field (email or password) caused the authentication failure, to prevent user enumeration attacks

### Requirement 13: JWT Token Security

**User Story:** As a platform operator, I want stateless JWT auth, so that the API scales without sessions.

#### Acceptance Criteria

1. THE Auth_Service SHALL sign all JWT tokens using HS256 algorithm with the value of the JWT_SECRET environment variable
2. THE Auth_Service SHALL set token expiry to 24 hours (86400000 ms) from the time of issuance
3. THE Auth_Service SHALL embed userId, email, and name as claims within the JWT token
4. WHEN a request is received with an invalid or expired JWT token on a protected endpoint, THE API_Gateway SHALL return HTTP status 401

### Requirement 14: Protected Wallet Endpoint

**User Story:** As a user, I want my wallet to be private, so that only I can see my balance.

#### Acceptance Criteria

1. THE API_Gateway SHALL expose a GET /api/v1/wallet/me endpoint that requires a valid JWT in the Authorization header (Bearer scheme)
2. WHEN a valid JWT is provided, THE Wallet_Service SHALL return the wallet and transaction history for the authenticated user only
3. THE existing GET /api/v1/wallet/{userId} endpoint SHALL remain publicly accessible for backward compatibility

### Requirement 15: Public Endpoints

**User Story:** As a visitor, I want to browse merchants without logging in, so that I can explore before registering.

#### Acceptance Criteria

1. GET /api/v1/merchants SHALL be publicly accessible without a JWT token
2. POST /api/v1/merchants/{id}/click SHALL be publicly accessible without a JWT token
3. GET /api/v1/health SHALL be publicly accessible without a JWT token
4. POST /api/v1/auth/register SHALL be publicly accessible without a JWT token
5. POST /api/v1/auth/login SHALL be publicly accessible without a JWT token

### Requirement 16: Transaction Creation on Merchant Click

**User Story:** As a logged-in user, I want a transaction record created when I click "Shop Now", so that my cashback earnings are tracked from the moment I visit a merchant.

#### Acceptance Criteria

1. THE API_Gateway SHALL expose a POST /api/v1/transactions endpoint that requires a valid JWT in the Authorization header
2. THE request body SHALL accept merchantId (Long) and orderAmount (BigDecimal)
3. WHEN a valid request is received, THE Transaction_Engine SHALL resolve the authenticated user's wallet using the userId from the JWT claims
4. THE Transaction_Engine SHALL look up the merchant by merchantId and calculate cashbackAmount as orderAmount × (cashbackRate / 100)
5. THE Transaction_Engine SHALL create a new Transaction with status PENDING, linked to the authenticated user's wallet, and return HTTP status 201 with the created TransactionDTO
6. IF the merchantId does not exist, THE API_Gateway SHALL return HTTP status 404
7. IF the authenticated user has no wallet, THE API_Gateway SHALL return HTTP status 404
8. IF the request is made without a valid JWT, THE API_Gateway SHALL return HTTP status 401
9. THE POST /api/v1/transactions endpoint SHALL be added to the authenticated routes in the security configuration
