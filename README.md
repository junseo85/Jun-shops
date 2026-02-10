# Jun-shops (MySQL)

A full-stack Spring Boot e-commerce application featuring both a **Vaadin web UI** and **REST API**.  
Built with **Spring MVC**, **Spring Data JPA**, **Vaadin 24.3.5**, and dual authentication (**session-based** for UI, **JWT** for API).  
Provides a modern web interface for product management, shopping cart, and orders, plus REST APIs for programmatic access and integration.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [MySQL Setup](#mysql-setup)
- [Configuration](#configuration)
  - [`application.properties` (MySQL + JPA + JWT)](#applicationproperties-mysql--jpa--jwt)
- [Run the Application](#run-the-application)
- [Accessing the Application](#accessing-the-application)
- [Authentication](#authentication)
- [Vaadin Configuration](#vaadin-configuration)
- [Security Configuration](#security-configuration)
- [User Interface](#user-interface)
- [API Overview](#api-overview)
- [Database Notes](#database-notes)
- [Common Issues & Fixes](#common-issues--fixes)
- [Development Tips](#development-tips)
- [License](#license)

---

## Features

- **Vaadin Web UI** for managing products, cart, and orders
- **Dual Interface**: Modern web UI + REST API for flexibility
- User CRUD (create, update, delete, fetch)
- **Session-based authentication** for web UI
- **JWT authentication** for REST API
- Login endpoint that returns a JWT
- Product & category endpoints
- Interactive product management (CRUD operations)
- Shopping cart interface with real-time totals
- Cart management (add/remove/update items, totals)
- Order placement from cart (creates order + order items)
- Inventory reduction when placing orders
- Image upload/download/update/delete APIs
- User-friendly navigation with drawer menu
- Responsive design for mobile and desktop

---

## Tech Stack

- Java 17
- Spring Boot
- Spring MVC
- Spring Security (Session-based for UI + JWT for API)
- Spring Data JPA (Hibernate)
- Vaadin 24.3.5
- Vaadin Spring Boot Integration
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

## Accessing the Application

After starting the application, you can access it in two ways:

### 1. Vaadin Web UI (Recommended for Users)
- **URL**: `http://localhost:8080/`
- **Authentication**: Session-based login
- **Default Admin Credentials**:
  - Email: `admin1@email.com`
  - Password: `123456`

#### Available Views:
- **Products**: View, add, edit, and delete products with a searchable grid
- **Cart**: Manage shopping cart items and view totals
- **Categories**: Category management (coming soon)
- **Orders**: Order history and management (coming soon)
- **Users**: User management (Admin only)

### 2. REST API (For Developers/Integration)
- **Base URL**: `http://localhost:8080/api/v1`
- **Authentication**: JWT token-based
- **Login**: `POST /api/v1/auth/login`
- **API Documentation**: See [API Overview](#api-overview) section below

---

## Authentication

### Web UI Authentication (Vaadin)
1. Navigate to `http://localhost:8080/`
2. You'll be redirected to `/login`
3. Enter credentials (use default admin account for testing)
4. After login, you'll have access to all views based on your role

### API Authentication (JWT)
1. Send POST request to `/api/v1/auth/login` with email and password
2. Receive JWT token in response
3. Include token in subsequent API requests:
   ```
   Authorization: Bearer <JWT_TOKEN>
   ```
4. Token expires based on configuration (default: 1 hour)

---

## Vaadin Configuration

The application includes Vaadin-specific settings in `application.properties`:

```properties
# Vaadin
vaadin.launch-browser=true
vaadin.whitelisted-packages=com.dailyproject.Junshops
```

- `vaadin.launch-browser`: Automatically opens browser when running
- `vaadin.whitelisted-packages`: Allows Vaadin to scan for UI components

---

## Security Configuration

The application uses **dual authentication**:

1. **Session-based (for Vaadin UI)**:
   - Uses Spring Security's form login
   - Sessions managed by Spring
   - Logout available in UI header

2. **JWT-based (for REST API)**:
   - Stateless authentication
   - Token expiration configurable
   - Secured endpoints: `/api/v1/cart/**`, `/api/v1/cartItems/**`
   - All other API endpoints permit all (for demo purposes)

Both authentication methods coexist and work independently.

---

## User Interface

### Main Features

**Navigation**: Side drawer menu with quick access to all sections

**Products View**:
- Searchable product grid
- Add/Edit/Delete products with inline form
- Real-time filtering by product name
- Displays: ID, Name, Brand, Price, Stock, Category

**Shopping Cart**:
- View all cart items
- Remove individual items
- Clear entire cart
- See running total
- Checkout button (integration pending)

**Login**:
- Clean, centered login form
- Error messages on failed authentication
- Automatic redirect after successful login

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

### General
- Return DTOs from controllers (avoid returning JPA entities directly)
- Keep controllers thin; place business logic in services
- Use transactions for workflows like "place order" and inventory updates
- Prefer a GlobalExceptionHandler for consistent API error responses

### Vaadin UI
- All views are in `src/main/java/com/dailyproject/Junshops/views/`
- Use `@Route` annotation to define navigation paths
- Use `@PermitAll` or `@RolesAllowed` for security
- Vaadin components are injected with Spring dependencies
- Use `Notification.show()` for user feedback
- Extend `MainLayout` for consistent navigation across views

### REST API
- Base path configured via `api.prefix` property
- Use JWT for stateless authentication
- Include proper error handling in responses

---

## License
Jun - MIT License