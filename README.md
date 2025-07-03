# 🎾 Tennis Club Management System
A Spring Boot application to manage tennis courts reservations, users, and surface types.

## 🛠️ Tech Stack
- Java 21
- Spring Boot 3
- JPA (Hibernate)
- Liquibase
- Postgres
- Docker
- H2 (dev/test)
- Maven
- Junit

## ✨ Features
- JWT-based authentication with refresh tokens
- Court reservation with various filtering
- Admin and user roles
- RESTful API with DTO-based communication
- Postgres database with Liquibase migrations
- Unit and integration tests

## 📘 API Documentation
Access the full Postman docs here:  
🔗 [View API Docs](https://documenter.getpostman.com/view/38876428/2sB34bL4B9)

## 🚀 Quick Start (with Docker)
git clone https://github.com/Metafyzik/tennis-club.git

cd tennisclub

docker-compose up --build

## 🧩 Architecture

### Class Diagram
![Class Diagram](docs/uml/tennis-club-class-diagram.svg)

### Sequence Diagram for court creation
![Sequence Diagram](docs/uml/sequence-diagram-court-creation.svg)
