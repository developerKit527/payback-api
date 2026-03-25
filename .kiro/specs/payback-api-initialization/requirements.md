# Requirements Document

## Introduction

This document defines the requirements for initializing a Spring Boot REST API project for the Payback system. The project will establish the foundational structure, dependencies, and basic health monitoring capabilities needed for a production-ready API service.

## Glossary

- **Payback_API**: The Spring Boot REST API application being initialized
- **Maven**: The build automation and dependency management tool
- **Health_Endpoint**: A REST endpoint that reports the operational status of the API
- **PostgreSQL_Database**: The relational database system used for data persistence
- **Application_Properties**: The configuration file containing runtime settings
- **POM**: Project Object Model file that defines Maven project configuration
- **Docker_Compose**: The tool for defining and running multi-container Docker applications
- **Database_Container**: The Docker container running the PostgreSQL_Database instance
- **JWT_Token**: JSON Web Token used for authenticating API requests with configurable expiration time
- **Display_Name**: The user's customizable name stored in the database name field
- **User_Profile**: The user's account information including id, name, and email
- **Merchant**: A business partner that offers cashback rewards to users
- **Cashback_Percentage**: The percentage of transaction amount returned as cashback
- **Referral_Code**: A unique identifier assigned to each user for tracking referrals
- **Referral_Relationship**: The connection between a referring user and a referred user
- **Bonus_Cashback**: Additional cashback awarded for successful referrals
- **Withdrawal_Request**: A user's request to transfer earned cashback to their UPI account
- **UPI_ID**: Unified Payments Interface identifier for receiving payments
- **Admin_Approval**: The process by which administrators authorize withdrawal requests
- **Email_Notification**: Automated email messages sent to users for important events
- **Resend_Service**: The email delivery service used for sending notifications
- **Email_Template**: Pre-formatted email content for specific notification types

## Requirements

### Requirement 1: Maven Project Configuration

**User Story:** As a developer, I want a properly configured Maven project, so that I can build and manage dependencies for the Spring Boot application.

#### Acceptance Criteria

1. THE Payback_API SHALL use Spring Boot version 3.2
2. THE Payback_API SHALL use Java version 21
3. THE POM SHALL include spring-boot-starter-web dependency
4. THE POM SHALL include spring-boot-starter-data-jpa dependency
5. THE POM SHALL include postgresql driver dependency
6. THE POM SHALL include lombok dependency

### Requirement 2: Project Structure

**User Story:** As a developer, I want a standard Maven directory structure, so that the project follows Java conventions and integrates with build tools.

#### Acceptance Criteria

1. THE Payback_API SHALL have a src/main/java directory for source code
2. THE Payback_API SHALL have a src/main/resources directory for configuration files
3. THE Payback_API SHALL have a com/payback/api package structure under src/main/java
4. THE Payback_API SHALL have a controller package under com/payback/api

### Requirement 3: Application Bootstrap

**User Story:** As a developer, I want a main application class, so that the Spring Boot application can start and run.

#### Acceptance Criteria

1. THE Payback_API SHALL have a PaybackApplication class in the com.payback.api package
2. THE PaybackApplication class SHALL serve as the Spring Boot application entry point
3. WHEN the PaybackApplication is executed, THE Payback_API SHALL initialize the Spring context

### Requirement 4: Database Configuration

**User Story:** As a developer, I want database connection settings configured, so that the application can connect to PostgreSQL.

#### Acceptance Criteria

1. THE Application_Properties SHALL specify the PostgreSQL_Database connection URL as jdbc:postgresql://localhost:5432/payback_db
2. THE Application_Properties SHALL specify the database username as admin
3. THE Application_Properties SHALL specify the database password as password123
4. THE Application_Properties SHALL configure Hibernate DDL auto-update mode
5. THE Application_Properties SHALL configure the PostgreSQL driver class name
6. THE Application_Properties SHALL enable SQL logging for development visibility
7. THE Application_Properties SHALL configure SQL formatting for readability
8. THE Application_Properties SHALL specify the PostgreSQL dialect for Hibernate
9. THE Application_Properties SHALL configure the server port as 8080

### Requirement 5: Health Monitoring

**User Story:** As a system operator, I want a health check endpoint, so that I can verify the API and database are operational.

#### Acceptance Criteria

1. THE Health_Endpoint SHALL be accessible at GET /api/v1/health
2. WHEN the Health_Endpoint is called, THE Payback_API SHALL return HTTP status 200
3. WHEN the Health_Endpoint is called, THE Payback_API SHALL return JSON content type
4. WHEN the Health_Endpoint is called, THE Payback_API SHALL return a JSON object with exactly two fields: "status" and "database"
5. THE Health_Endpoint response SHALL include a status field with string value "UP"
6. THE Health_Endpoint response SHALL include a database field with string value "CONNECTED"

### Requirement 6: Controller Implementation

**User Story:** As a developer, I want a health controller class, so that health check logic is properly organized.

#### Acceptance Criteria

1. THE Payback_API SHALL have a HealthController class in the com.payback.api.controller package
2. THE HealthController SHALL handle requests to the Health_Endpoint
3. THE HealthController SHALL be annotated as a REST controller

### Requirement 7: Docker Database Environment

**User Story:** As a developer, I want a Docker Compose configuration for PostgreSQL, so that I can run the database locally without manual installation.

#### Acceptance Criteria

1. THE Payback_API SHALL include a docker-compose.yml file in the project root
2. THE Docker_Compose configuration SHALL define a PostgreSQL service using postgres:15 image
3. THE Database_Container SHALL be named payback_postgres
4. THE Database_Container SHALL expose PostgreSQL on host port 5433 mapped to container port 5432
5. THE Database_Container SHALL configure POSTGRES_DB environment variable as payback_db
6. THE Database_Container SHALL configure POSTGRES_USER environment variable as admin
7. THE Database_Container SHALL configure POSTGRES_PASSWORD environment variable as password123
8. THE Docker_Compose configuration SHALL define a named volume postgres_data for data persistence

### Requirement 8: Extend JWT Token Lifetime

**User Story:** As a user, I want my session to last for 7 days instead of 24 hours, so that I don't have to log in repeatedly during normal usage.

#### Acceptance Criteria

1. THE Payback_API SHALL set JWT_Token expiration to 604800000 milliseconds (7 days) in Application_Properties
2. THE Payback_API SHALL use the configuration property jwt.expiration with default value ${JWT_EXPIRATION:604800000}
3. WHEN a user authenticates successfully, THE Payback_API SHALL issue a JWT_Token with 7-day expiration
4. THE Payback_API SHALL read the expiration value from Application_Properties configuration
5. THE Payback_API SHALL continue to validate JWT_Token signatures and claims as before
6. THE Payback_API SHALL continue to reject invalid or malformed JWT_Tokens with 401 errors

### Requirement 9: User Profile Management

**User Story:** As a user, I want to update my display name through the API, so that I can personalize how my name appears in the application.

#### Acceptance Criteria

1. THE Payback_API SHALL provide a PUT endpoint at /api/v1/users/me for updating the authenticated user's profile
2. THE Payback_API SHALL accept a JSON request body containing the updated Display_Name
3. THE Payback_API SHALL validate that the Display_Name is not empty and does not exceed 100 characters
4. WHEN a valid Display_Name update request is received, THE Payback_API SHALL update the user's name in the PostgreSQL_Database
5. WHEN the update succeeds, THE Payback_API SHALL return HTTP status 200 with the updated User_Profile
6. THE Payback_API SHALL require a valid JWT_Token for authentication
7. WHEN an unauthenticated request is made, THE Payback_API SHALL return HTTP status 401
8. WHEN an invalid Display_Name is provided, THE Payback_API SHALL return HTTP status 400 with an error message
9. THE Payback_API SHALL return the updated User_Profile including id, name, and email

### Requirement 10: Additional Merchant Data

**User Story:** As a system administrator, I want to populate the database with additional merchant partners, so that users have more cashback opportunities across different categories.

#### Acceptance Criteria

1. THE PostgreSQL_Database SHALL contain a Merchant record for Zomato with category "food delivery" and Cashback_Percentage of 5%
2. THE PostgreSQL_Database SHALL contain a Merchant record for Swiggy with category "food delivery" and Cashback_Percentage of 4%
3. THE PostgreSQL_Database SHALL contain a Merchant record for MakeMyTrip with category "travel" and Cashback_Percentage of 6%
4. THE PostgreSQL_Database SHALL contain a Merchant record for boAt with category "electronics" and Cashback_Percentage of 8%
5. THE PostgreSQL_Database SHALL contain a Merchant record for Meesho with category "fashion" and Cashback_Percentage of 10%
6. THE PostgreSQL_Database SHALL contain a Merchant record for Tata CLiQ with category "electronics/fashion" and Cashback_Percentage of 7%
7. THE Payback_API SHALL provide SQL scripts for inserting these Merchant records
8. WHEN the database is initialized, THE Payback_API SHALL execute the merchant data insertion scripts

### Requirement 11: Referral System Backend

**User Story:** As a user, I want to refer friends and earn bonus cashback, so that I can benefit from growing the platform's user base.

#### Acceptance Criteria

1. WHEN a new user registers, THE Payback_API SHALL generate a unique Referral_Code for that user
2. THE Referral_Code SHALL be alphanumeric and exactly 8 characters long
3. THE Payback_API SHALL store the Referral_Code in the PostgreSQL_Database associated with the user
4. WHEN a user signs up with a valid Referral_Code, THE Payback_API SHALL create a Referral_Relationship linking the new user to the referring user
5. WHEN a Referral_Relationship is created, THE Payback_API SHALL award Bonus_Cashback to both the referring user and the referred user
6. THE Payback_API SHALL provide a GET endpoint at /api/v1/referrals/stats for retrieving referral statistics
7. WHEN the referral stats endpoint is called, THE Payback_API SHALL return the user's Referral_Code, total referrals count, and total Bonus_Cashback earned
8. THE Payback_API SHALL require a valid JWT_Token for accessing referral endpoints
9. THE Payback_API SHALL validate that Referral_Codes exist before creating Referral_Relationships
10. WHEN an invalid Referral_Code is provided during signup, THE Payback_API SHALL return HTTP status 400 with an error message

### Requirement 12: Withdrawal Flow Backend

**User Story:** As a user, I want to withdraw my earned cashback to my UPI account, so that I can use the money for real purchases.

#### Acceptance Criteria

1. THE Payback_API SHALL provide a POST endpoint at /api/v1/withdrawals for creating withdrawal requests
2. WHEN a withdrawal request is submitted, THE Payback_API SHALL accept a JSON body containing the UPI_ID and withdrawal amount
3. THE Payback_API SHALL validate that the UPI_ID follows the standard UPI format (username@bankname)
4. THE Payback_API SHALL validate that the withdrawal amount does not exceed the user's available cashback balance
5. THE Payback_API SHALL validate that the withdrawal amount is at least 100 rupees
6. WHEN a valid withdrawal request is received, THE Payback_API SHALL store the Withdrawal_Request in the PostgreSQL_Database with status "PENDING"
7. THE Payback_API SHALL provide a PUT endpoint at /api/v1/admin/withdrawals/{id}/approve for Admin_Approval
8. WHEN an administrator approves a withdrawal, THE Payback_API SHALL update the Withdrawal_Request status to "APPROVED"
9. THE Payback_API SHALL provide a PUT endpoint at /api/v1/admin/withdrawals/{id}/mark-paid for marking withdrawals as paid
10. WHEN a withdrawal is marked as paid, THE Payback_API SHALL update the Withdrawal_Request status to "PAID" and deduct the amount from the user's cashback balance
11. THE Payback_API SHALL provide a GET endpoint at /api/v1/withdrawals/history for retrieving the user's withdrawal history
12. THE Payback_API SHALL require a valid JWT_Token for accessing withdrawal endpoints
13. THE Payback_API SHALL require administrator privileges for approval and mark-paid endpoints
14. WHEN an invalid UPI_ID format is provided, THE Payback_API SHALL return HTTP status 400 with an error message
15. WHEN a withdrawal amount exceeds available balance, THE Payback_API SHALL return HTTP status 400 with an error message

### Requirement 13: Email Notification System

**User Story:** As a user, I want to receive email notifications for important events, so that I stay informed about my account activity and cashback earnings.

#### Acceptance Criteria

1. THE Payback_API SHALL integrate with the Resend_Service for sending Email_Notifications
2. THE Payback_API SHALL configure Resend_Service API credentials in Application_Properties
3. WHEN a new user completes registration, THE Payback_API SHALL send a welcome Email_Notification to the user's email address
4. THE welcome Email_Notification SHALL use a predefined Email_Template containing the user's name and getting started information
5. WHEN a transaction is confirmed and cashback is credited, THE Payback_API SHALL send a cashback confirmation Email_Notification
6. THE cashback confirmation Email_Notification SHALL include the transaction amount, cashback amount, and merchant name
7. THE Payback_API SHALL use the Resend_Service free tier for email delivery
8. THE Payback_API SHALL store Email_Template content in the codebase for maintainability
9. WHEN an email fails to send, THE Payback_API SHALL log the error but not block the primary operation
10. THE Payback_API SHALL format emails with HTML content for better presentation
11. THE Email_Template SHALL include the Payback branding and styling
