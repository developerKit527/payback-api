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
