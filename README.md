# Online Banking Application

A Spring Boot web application for managing bank accounts and transactions, built with Spring MVC, Spring Security, and Thymeleaf.

## Tech Stack

- **Java 21** / Spring Boot 3.5
- **Spring Security** — form-based login, BCrypt password encoding
- **Spring Data JPA** + H2 in-memory database
- **Thymeleaf** — server-side templating
- **Lombok** — boilerplate reduction

## Getting Started

**Run the application:**
```bash
./mvnw spring-boot:run
```

The app starts at [http://localhost:8080](http://localhost:8080).
H2 console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:bankingdb`)

> The database is in-memory — all data is lost on restart.

## Features

- User registration and authentication (email + password)
- Four account types: **Current**, **Saving**, **Fixed Deposit**, **Credit**
- Deposit, withdrawal, and inter-account transfer
- Transaction history on the dashboard (last 10 transactions)

## Running Tests

```bash
# All tests
./mvnw test

# Single test class
./mvnw test -Dtest=BankAccountServiceTest
```

46 unit tests covering the service and controller layers.

## Project Structure

```
src/main/java/com/online/banking/
├── config/        # Security and application configuration
├── entity/        # JPA entities (User, BankAccount subtypes, Transaction)
├── repositories/  # Spring Data JPA repositories
├── service/       # Business logic and custom exceptions
└── controller/    # MVC controllers and global exception handler

src/main/resources/
├── templates/     # Thymeleaf HTML templates
└── static/css/    # Stylesheets
```
