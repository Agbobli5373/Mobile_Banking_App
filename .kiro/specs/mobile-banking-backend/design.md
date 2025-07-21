# Design Document

## Overview

This design document outlines the architecture for a mobile banking backend application using Domain Driven Design (DDD) principles with Java and Spring Boot. The system is organized around three main bounded contexts: User Management, Authentication, and Transaction Processing. Each bounded context encapsulates its own domain logic, ensuring clear separation of concerns and maintainability.

The application follows a layered architecture with clear boundaries between the presentation layer (REST controllers), application layer (services), domain layer (entities and domain services), and infrastructure layer (repositories and external integrations).

## Architecture

### Bounded Contexts

The system is divided into three bounded contexts based on domain expertise and business capabilities:

1. **User Management Context**: Handles user registration, profile management, and user-related operations
2. **Authentication Context**: Manages user authentication, JWT token generation, and security concerns
3. **Transaction Context**: Processes financial transactions, wallet operations, and transaction history

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│              (REST Controllers, DTOs)                       │
├─────────────────────────────────────────────────────────────┤
│                   Application Layer                         │
│           (Application Services, Use Cases)                 │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                            │
│        (Entities, Value Objects, Domain Services)          │
├─────────────────────────────────────────────────────────────┤
│                 Infrastructure Layer                        │
│         (Repositories, Database, External APIs)            │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security with JWT
- **Database**: JPA/Hibernate with H2 (development) / PostgreSQL (production)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test

## Components and Interfaces

### User Management Context

#### Domain Entities

**User Aggregate Root**

```java
@Entity
public class User {
    private UserId id;
    private UserName name;
    private PhoneNumber phone;
    private HashedPin pin;
    private Money balance;

    // Domain methods
    public void debitBalance(Money amount);
    public void creditBalance(Money amount);
    public boolean hasValidPin(String rawPin);
    public boolean hasSufficientBalance(Money amount);
}
```

**Value Objects**

- `UserId`: Wraps UUID for type safety
- `UserName`: Validates and encapsulates user name
- `PhoneNumber`: Validates phone number format
- `HashedPin`: Handles PIN hashing and validation
- `Money`: Handles monetary amounts with proper precision

#### Application Services

- `UserRegistrationService`: Handles user registration workflow
- `UserQueryService`: Provides user lookup and profile operations

#### Repository Interfaces

- `UserRepository`: Persistence operations for User aggregate

### Authentication Context

#### Domain Services

- `AuthenticationService`: Validates credentials and manages authentication state
- `JwtTokenService`: Generates and validates JWT tokens

#### Application Services

- `LoginService`: Orchestrates login workflow
- `TokenValidationService`: Handles token validation for API requests

### Transaction Context

#### Domain Entities

**Transaction Aggregate Root**

```java
@Entity
public class Transaction {
    private TransactionId id;
    private UserId senderId;
    private UserId receiverId;
    private Money amount;
    private TransactionTimestamp timestamp;
    private TransactionType type;

    // Factory methods
    public static Transaction createTransfer(UserId sender, UserId receiver, Money amount);
    public static Transaction createDeposit(UserId user, Money amount);
}
```

**Value Objects**

- `TransactionId`: Wraps UUID for transactions
- `TransactionTimestamp`: Handles transaction timing
- `TransactionType`: Enum for different transaction types (TRANSFER, DEPOSIT)

#### Domain Services

- `MoneyTransferService`: Handles the business logic for money transfers
- `TransactionHistoryService`: Manages transaction history queries

#### Application Services

- `WalletService`: Orchestrates wallet operations (balance, transfers, deposits)
- `TransactionQueryService`: Handles transaction history retrieval

#### Repository Interfaces

- `TransactionRepository`: Persistence operations for Transaction aggregate

### Cross-Cutting Concerns

#### Security Configuration

- JWT token configuration and validation
- Method-level security annotations
- CORS configuration for mobile clients

#### Exception Handling

- Global exception handler for consistent error responses
- Domain-specific exceptions (InsufficientFundsException, UserNotFoundException)

## Data Models

### Database Schema

#### Users Table

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Transactions Table

```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    sender_id UUID REFERENCES users(id),
    receiver_id UUID REFERENCES users(id),
    amount DECIMAL(19,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT positive_amount CHECK (amount > 0)
);
```

### Domain Model Relationships

```mermaid
classDiagram
    class User {
        +UserId id
        +UserName name
        +PhoneNumber phone
        +HashedPin pin
        +Money balance
        +debitBalance(Money)
        +creditBalance(Money)
        +hasValidPin(String)
        +hasSufficientBalance(Money)
    }

    class Transaction {
        +TransactionId id
        +UserId senderId
        +UserId receiverId
        +Money amount
        +TransactionTimestamp timestamp
        +TransactionType type
    }

    class MoneyTransferService {
        +transferMoney(UserId, UserId, Money)
        +validateTransfer(UserId, UserId, Money)
    }

    User ||--o{ Transaction : participates_in
    MoneyTransferService ..> User : uses
    MoneyTransferService ..> Transaction : creates
```

## Error Handling

### Domain Exceptions

- `InsufficientFundsException`: Thrown when user lacks sufficient balance
- `UserNotFoundException`: Thrown when referenced user doesn't exist
- `InvalidCredentialsException`: Thrown during authentication failures
- `DuplicatePhoneNumberException`: Thrown during registration with existing phone
- `SelfTransferException`: Thrown when user tries to transfer to themselves

### Error Response Format

```json
{
  "timestamp": "2025-07-21T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient funds for transfer",
  "path": "/api/wallet/send"
}
```

### Global Exception Handler

- Centralized exception handling using `@ControllerAdvice`
- Consistent error response format across all endpoints
- Proper HTTP status codes for different error types
- Security-conscious error messages (no sensitive data exposure)

## Testing Strategy

### Unit Testing

- **Domain Layer**: Test domain entities, value objects, and domain services in isolation
- **Application Layer**: Test application services with mocked dependencies
- **Repository Layer**: Test repository implementations with `@DataJpaTest`

### Integration Testing

- **API Layer**: Test REST endpoints with `@SpringBootTest` and `MockMvc`
- **Database Integration**: Test complete data flow with embedded H2 database
- **Security Integration**: Test JWT authentication and authorization

### Test Data Management

- Use test fixtures and builders for consistent test data creation
- Separate test profiles for different testing scenarios
- Database cleanup strategies for integration tests

### Testing Approach by Layer

#### Domain Layer Tests

```java
@Test
void shouldDebitBalanceWhenSufficientFunds() {
    // Given
    User user = User.create("John", "1234567890", "1234");
    user.creditBalance(Money.of(100.00));

    // When
    user.debitBalance(Money.of(50.00));

    // Then
    assertThat(user.getBalance()).isEqualTo(Money.of(50.00));
}
```

#### Application Layer Tests

```java
@Test
void shouldTransferMoneyBetweenUsers() {
    // Given
    given(userRepository.findById(senderId)).willReturn(sender);
    given(userRepository.findById(receiverId)).willReturn(receiver);

    // When
    walletService.transferMoney(senderId, receiverId, Money.of(100.00));

    // Then
    verify(transactionRepository).save(any(Transaction.class));
    verify(userRepository).save(sender);
    verify(userRepository).save(receiver);
}
```

#### Integration Tests

```java
@SpringBootTest
@AutoConfigureTestDatabase
class WalletControllerIntegrationTest {

    @Test
    void shouldTransferMoneySuccessfully() throws Exception {
        // Given authenticated user with sufficient balance

        // When
        mockMvc.perform(post("/api/wallet/send")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferRequest))

        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
```

### Performance Testing Considerations

- Load testing for concurrent transactions
- Database performance testing with realistic data volumes
- JWT token validation performance under load
