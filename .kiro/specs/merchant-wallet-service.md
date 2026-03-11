# Technical Spec: Merchant & Wallet Service

## 1. Overview
Implement the core backend engine for PayBack India as defined in the root `PAYBACK_PRD.md`. This includes merchant listing, click tracking, and a transaction-based wallet system.

## 2. Tech Stack & Constraints
* **Framework:** Spring Boot 3.2 (Java 25)
* **Database:** PostgreSQL (via Docker)
* **API Style:** RESTful JSON
* **Auth:** Mock User ID `1` for now (Google Auth integration in next spec)

## 3. Data Models (Entities)
### Merchant
* `Long id` (Primary Key)
* `String name` (e.g., Flipkart)
* `String logoUrl`
* `Double cashbackRate`
* `String manualTrackingUrl` (Manual affiliate link)
* `Long clickCount` (Default 0, used for smart sorting)

### Wallet
* `Long id`
* `Long userId` (Unique)
* `BigDecimal totalBalance`
* `String upiId` (Primary withdrawal method)

### Transaction
* `Long id`
* `Wallet wallet` (Many-to-One)
* `String merchantName`
* `BigDecimal orderAmount`
* `BigDecimal cashbackAmount`
* `String status` (Enum: PENDING, CONFIRMED, REJECTED)
* `LocalDateTime createdAt`

## 4. API Endpoints
* `GET /api/v1/merchants`: Returns list of merchants ordered by `clickCount` DESC.
* `POST /api/v1/merchants/{id}/click`: Increments `clickCount` and returns 200 OK.
* `GET /api/v1/wallet/{userId}`: Returns wallet details and a list of transactions.

## 5. Seed Data Requirements
On startup, verify/insert:
1. **Merchants:** Flipkart (10%), Myntra (8.5%), Ajio (7%) with placeholder URLs.
2. **Wallet:** Initial wallet for `userId: 1`.
3. **Transactions:** Two `PENDING` transactions for User 1 to verify the UI list works.