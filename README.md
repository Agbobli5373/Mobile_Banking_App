# Mobile Banking Backend

A Spring Boot application implementing a mobile banking system using Domain-Driven Design (DDD) principles.

## Project Structure

The project follows DDD bounded contexts:

### Bounded Contexts

- **User Context** (`com.mobilebanking.user`) - User management and profiles
- **Auth Context** (`com.mobilebanking.auth`) - Authentication and authorization
- **Transaction Context** (`com.mobilebanking.transaction`) - Money transfers and wallet operations

### Layer Architecture

Each bounded context follows a layered architecture:

- **Domain Layer** - Entities, value objects, domain services
- **Application Layer** - Application services, use cases
- **Infrastructure Layer** - Repositories, external integrations
- **Presentation Layer** - REST controllers, DTOs

### Shared Kernel

- **Shared Domain** (`com.mobilebanking.shared.domain`) - Common value objects like Money, UserId, TransactionId

## Build and Run

### Prerequisites

- Java 17 or higher
- Maven (using wrapper)

### Commands

```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test

# Package application
./mvnw package

# Run application
./mvnw spring-boot:run
```

### Development Database

- H2 in-memory database
- Console available at: http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:mobilebanking`
- Username: `sa`
- Password: `password`

## API Base URL

- Development: http://localhost:8080/api

## Configuration

- Main config: `application.yml`
- Test config: `application-test.yml`
