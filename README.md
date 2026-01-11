# Banking System API

![Java CI](https://github.com/NatePombi/banking-system-api/actions/workflows/test.yml/badge.svg)
[![codecov](https://codecov.io/gh/NatePombi/banking-system-api/branch/master/graph/badge.svg?token=YOUR_TOKEN)](https://codecov.io/gh/NatePombi/banking-system-api)


A secure and modular banking backend built with Spring Boot, following professional fintech architecture principles.
Supports account management, money transfers, ledger entries, and audit logging with data integrity guaranteed by optimistic locking and transactional boundaries.

---

#### Features (So Far)
* User registration and authentication
* Create and manage bank accounts
* Transfer funds between accounts (fully atomic operations)
* Double-entry ledger system for accounting integrity
* Full audit trail for every transactions
* RESTful API design following industry conventions
* Layered architecture (Controller → Service → Repository → Domain)
* Comprehensive error handling and validation
* Uses Flyway for database migrations
* Concurrency-safe transactions processing using pessimistic locking
* Deadlock prevention via deterministic account lock ordering
* Configurable transactions isolation levels for data consistency
* Automatic retry mechanism for transient lock/contention failures
* Optimistic locking support using versioning for additional safety
* Idempotent transactions handling to prevent double processing
* JUnit-based test coverage including concurrency and edge cases

---

### Tech Stack

* ![Java](https://img.shields.io/badge/Java-17-blue)

* ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-green)

* ![Spring Security](https://img.shields.io/badge/Security-JWT-yellow)

* ![Flyway](https://img.shields.io/badge/Database%20Migrations-Flyway-red?logo=flyway)

* ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-3-brightgreen?logo=spring&logoColor=white)

* ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-blue)

* ![JUnit](https://img.shields.io/badge/Testing-JUnit%20%26%20Mockito-orange)

* ![Swagger](https://img.shields.io/badge/Docs-Swagger-brightgreen)

* ![H2 Database](https://img.shields.io/badge/H2-Database-blue?logo=h2&logoColor=white)

* ![Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)




---

### Future Plans

* Docker & Docker Compose (with Postgres)

* Analytics dashboard (product stock levels, sales trends, etc.)

--- 
### Environment Variable

This project use a '.env' file to store configuration values the database, there is a template named "envtemplate.env.example" showing you exactly how it should look like.

Create a '.env' file in the **project root**:

```env

DB_URL=jdbc:postgresql://localhost:5432/bankingapi

DB_USERNAME=yourusername 

DB_PASSWORD=yourpassword
```
---


### Database Configuration

- This project uses PostgreSQl.

      1. Go to your .env file in your project root. 
      2. Configure the database details to your custom details.
      3. Go to Run -> Edit Connfiguration -> Environment Variables
      4. Enable your .env file ( use the EnvFile plugin)

- Steps to set up the database:

    - Make sure PostgreSQL is installed and running.

    - Create the database:

        - CREATE DATABASE bankingSystemAPI;
        - Update the username and password in application.properties to your own.
        - The database schema is managed by Flyway migrations located in src/main/resources/db/migration.
          * On startup, Flyway automatically applies any new migrations to keep the database in sync with the application.



---
### Admin Seeding

- Admin User (Seeded on Runtime)
    - For testing and management purposes, the application automatically creates and admin user when applications starts. This ensures that theres always at least one admin present.

    - Credentials:
      ```json
      {
        "username": "admin",
        "password": "admin123"
      }


### How to Use (For Now)

1) Clone the repo

- git clone https://github.com/NatePombi/banking-system-api.git


2) Navigate into the project

- cd banking-system-api


3) Run the project

- ./mvnw spring-boot:run


4) Open Swagger docs in browser:

- http://localhost:8080/swagger-ui/index.html



---

### Status

- This project is actively maintained and open for improvements and contributions.

---

### API Usage 

This section explains how to interact with the Banking System API, including available endpoints, request examples, and how to view live API documentation via Swagger UI.

#### Base URl
- http://localhost:8080

### Endpoints Overview

| Method | Endpoints                    | Description                                                        |
|--------|------------------------------|--------------------------------------------------------------------|
| Post   | /auth/register               | Register new User                                                  |
| Post   | /auth/login                  | Logs in User                                                       |
|Post    | /account                     | Creates new account                                                |
 |Get    | /account/{id}                | Gets account by id                                                 |
|Get    | /account/accountNum/{accNum} | Gets account by account number                                     |
 | Get   | /account                     | Retrieves a paginated list of accounts (for the Authenticated user |
  | Post | /transactions/transfer       | Transfers funds between accounts                                   |
 | Post  | /transactions/deposit        | Deposits funds in account                                          |
 | Post  | /transactions/withdraw       | Withdraw funds from account                                        

### Sample API calls

Register User
**POST** `/auth/register`
```json
{
  "fullName": "John Doe",
  "username": "john123",
  "email": "eail@gmail.com",
  "password": "john123"
}
```

Login User
**POST** `/auth/login`
```json
{
  "username": "john123",
  "password": "john123"
}

```

Create Account
**POST** `/account`
```json
{
  "balance": 0,
  "currency": "USD"
}
```

Response:
```json
{
  "id": 1,
  "accountNum": 1306192354,
  "balance": 0,
  "currency": "USD",
  "user": 2,
  "integer": 0
}
```

Transfer Funds
**POST** `/transactions/transfer`
```json
{
  "fromAccount": 8986793488,
  "toAccount": 1306192354,
  "amount": 100,
  "requestID": "UUID232"
}
```

Response:
```json
{
  "id": 3,
  "amount": 100,
  "fromAccount": 8986793488,
  "toAccount": 1306192354,
  "status": "SUCCESS"
}
```

---

### Postman Collection

You can import the full Postman collection to test the API:

[📥 Download Postman Collection](./.docs/postman_collection.json)
