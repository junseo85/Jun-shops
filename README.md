# Jun-shops (MySQL)

A Spring Boot e-commerce backend built with **Spring MVC**, **Spring Data JPA**, and **JWT authentication**.  
Provides REST APIs for users, products, categories, carts, orders, and image management.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [MySQL Setup](#mysql-setup)
- [Configuration](#configuration)
  - [`application.properties` (MySQL + JPA + JWT)](#applicationproperties-mysql--jpa--jwt)
- [Run the Application](#run-the-application)
- [Authentication (JWT)](#authentication-jwt)
- [API Overview](#api-overview)
- [Database Notes](#database-notes)
- [Common Issues & Fixes](#common-issues--fixes)
- [Development Tips](#development-tips)
- [License](#license)

---

## Features

- User CRUD (create, update, delete, fetch)
- Login endpoint that returns a JWT
- Product & category endpoints
- Cart management (add/remove/update items, totals)
- Order placement from cart (creates order + order items)
- Inventory reduction when placing orders
- Image upload/download/update/delete APIs

---

## Tech Stack

- Java 17
- Spring Boot
- Spring MVC
- Spring Security + JWT
- Spring Data JPA (Hibernate)
- Lombok
- ModelMapper
- Maven
- MySQL

---

## MySQL Setup

### 1) Create database
sql CREATE DATABASE junshops CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

### 2) Create a database user (recommended)> Use strong credentials and do not commit secrets to version control.

---sql CREATE USER 'junshops_user'@'localhost' IDENTIFIED BY 'YOUR_PASSWORD'; GRANT ALL PRIVILEGES ON junshops.* TO 'junshops_user'@'localhost'; FLUSH PRIVILEGES;

## Configuration

### `application.properties` (MySQL + JPA + JWT)

Edit: `src/main/resources/application.properties`

Example configuration:
#### properties
# ----------------------------
# App
# ----------------------------
api.prefix=/api/v1 spring.application.name=jun-shops
# ----------------------------
# MySQL Datasource
# ----------------------------
spring.datasource.url=jdbc:mysql://localhost:3306/junshops?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC spring.datasource.username=junshops_user spring.datasource.password=YOUR_PASSWORD spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# ----------------------------
# JPA / Hibernate
# ----------------------------
spring.jpa.hibernate.ddl-auto=update spring.jpa.show-sql=true spring.jpa.properties.hibernate.format_sql=true spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
# Recommended: avoids lazy-loading surprises in controllers
spring.jpa.open-in-view=false
# ----------------------------
# JWT
# ----------------------------
# Must be Base64 encoded and long enough for HS256 (your JwtUtils decodes it).
auth.token.jwtSecret=YOUR_BASE64_SECRET auth.token.expirationInMils=3600000
#### Notes
- `spring.jpa.hibernate.ddl-auto=update` is convenient for development. For production, prefer migrations (Flyway/Liquibase) + `validate`.
- If JWT validation fails, the most common causes are:
  - `jwtSecret` is not Base64
  - `jwtSecret` is too short for HS256
  - expiration time is too small/incorrect

---

## Run the Application

### With Maven Wrapper

macOS/Linux:
bash ./mvnw spring-boot:run
Windows:
bat mvnw.cmd spring-boot:run
### Build and run the jar
bash ./mvnw clean package java -jar target/<YOUR_JAR_NAME>.jar
---

## Authentication (JWT)

### Login
`POST ${api.prefix}/auth/login`

On success, the server returns a JWT token.

### Using the token
Include the token on secured endpoints:
Authorization: Bearer <JWT_TOKEN>
---

## API Overview

Base path: `${api.prefix}` (example: `/api/v1`)

### Auth
- `POST /api/v1/auth/login`

### Users
- `GET    /api/v1/users/{userId}/user`
- `POST   /api/v1/users/add`
- `PUT    /api/v1/users/{userId}/update`
- `DELETE /api/v1/users/{userId}/delete`

### Orders
- `POST /api/v1/orders/order?userId=<id>`
- `GET  /api/v1/orders/{orderId}/order`
- `GET  /api/v1/orders/{userId}/order`

### Cart / Cart Items
Cart endpoints typically allow:
- add item to cart
- remove item from cart
- update item quantity
- read total price

Some cart endpoints are secured and require JWT.

### Images
- upload images for a product
- download image by id
- update / delete image

---

## Database Notes

- Tables are created/updated via Hibernate based on the JPA entities.
- Key relationships:
  - `User` ↔ `Cart`
  - `Cart` ↔ `CartItem`
  - `Order` ↔ `OrderItem`
  - `Order` ↔ `User`
- Be cautious with cascade and orphan removal so you don’t accidentally delete data.

---

## Common Issues & Fixes

### 1) `Detached entity passed to persist (Role)`
Cause: seeding users before roles exist or using unmanaged role instances.  
Fix: create roles first and seed inside a transaction.

### 2) `jwtUtils is null` in JWT filter
Cause: filter instantiated with `new ...()` so Spring doesn’t inject dependencies.  
Fix: constructor injection + create the filter bean with dependencies.

### 3) MySQL timezone / connection warnings
Fix: keep `serverTimezone=UTC` in the JDBC URL.

### 4) Cart total not updated after ordering
Fix: when clearing cart items, reset/recalculate cart `totalAmount` and persist.

---

## Development Tips

- Return DTOs from controllers (avoid returning JPA entities directly).
- Keep controllers thin; place business logic in services.
- Use transactions for workflows like “place order” and inventory updates.
- Prefer a GlobalExceptionHandler for consistent API error responses.

---

## License
Jun - MIT License