# 🎾 Tennis Club Management System
A Spring Boot application to manage tennis courts reservations, users, and surface types.

## Potential improvments of current functionality
With implementation authentication and authorization users with **MEMBER** role should be able to:
- Create reservations only for their own accounts — the system should infer the user automatically.
- Update and delete their **upcoming** reservations.
- See only necessary data in reservations — no sensitive data (usernames or phone numbers) of other users.
- Access an additional endpoint to view **only their own** reservations.

## 🛠️ Tech Stack
- Java 21
- Spring Boot 3
- JPA (Hibernate)
- Liquibase
- H2 (dev/test)
- Maven
- Junit

## ✨ Features
- JWT-based authentication with refresh tokens
- Court reservation with various filtering
- Admin and user roles
- RESTful API with DTO-based communication
- In-memory H2 database with Liquibase migrations  

## 🧩 Architecture

### Class Diagram
![Class Diagram](docs/uml/tennis-court-class-diagram.svg)

### Sequence Diagram for court creation
![Sequence Diagram](docs/uml/sequence-diagram-court-creation.svg)
