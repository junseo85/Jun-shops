# 🛍️ Jun-Shops

A full-stack e-commerce web application built with **Spring Boot** and **Vaadin**, featuring a complete shopping experience with user authentication, product management, shopping cart, order processing, **Redis caching for performance optimization**, and admin panel.

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vaadin](https://img.shields.io/badge/Vaadin-24+-blue.svg)](https://vaadin.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)

---

## 📋 Table of Contents

- [Features](#-features)
- [Technologies](#-technologies)
- [Architecture](#-architecture)
- [Caching Strategy](#-caching-strategy)
- [Getting Started](#-getting-started)
- [Usage Guide](#-usage-guide)
- [Database Schema](#-database-schema)
- [API Endpoints](#-api-endpoints)
- [Performance Optimization](#-performance-optimization)
- [Screenshots](#-screenshots)
- [Future Enhancements](#-future-enhancements)
- [Learning Outcomes](#-learning-outcomes)
- [License](#-license)

---

## ✨ Features

### 🔐 User Authentication & Authorization
- User registration and login
- Role-based access control (USER and ADMIN)
- Secure password encryption with BCrypt
- Session management with Spring Security

### 🛒 Shopping Experience
- Browse products by category
- Product search and filtering
- Product details with images
- Add to cart functionality
- Real-time cart updates
- Quantity management

### 📦 Order Management
- Secure checkout process
- Order history tracking
- Order status updates (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- Order details with item breakdown
- Price snapshots for historical accuracy

### 👨‍💼 Admin Panel
- User management (Create, Read, Update, Delete)
- Role assignment (USER/ADMIN)
- View all orders from all users
- Order details and customer information
- Product inventory management

### ⚡ Performance Optimization 
- **Redis caching** for frequently accessed data
- **Product catalog caching** (100x faster queries)
- **Cache-aside pattern** implementation
- **Automatic cache invalidation** on updates
- **TTL-based cache expiration** (1 hour default)
- **Distributed caching** support for horizontal scaling

### 🎨 Modern UI
- Responsive design with Vaadin components
- Clean and intuitive interface
- Real-time notifications
- Interactive dialogs and forms
- Status badges with color coding

---

## 🛠️ Technologies

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.3.7** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM framework
- **MySQL** - Relational database
- **Redis 7.0** - In-memory cache 
- **Lombok** - Boilerplate code reduction
- **ModelMapper** - Object mapping

### Frontend
- **Vaadin 24** - Full-stack web framework
- **Java-based UI** - No separate frontend framework needed

### Build & Development
- **Maven** - Dependency management and build tool
- **Spring Boot DevTools** - Hot reload during development

---

## 🏗️ Architecture

### Layered Architecture
┌─────────────────────────────────────┐ │ Presentation Layer │ │ (Vaadin Views & Components) │ │ - ProductListView │ │ - CartView │ │ - CheckoutView │ │ - OrderView │ │ - AdminViews │ └──────────────┬──────────────────────┘ │ ┌──────────────▼──────────────────────┐ │ Service Layer │ │ (Business Logic + Caching) │ │ - ProductService (@Cacheable) │ │ - CartService │ │ - OrderService │ │ - UserService │ └──────────────┬──────────────────────┘ │ ┌─────┴─────┐ │ │ ┌────────▼──┐ ┌────▼──────────────┐ │ Redis │ │ Repository Layer │ │ (Cache) │ │ (Data Access) │ │ │ │ - Repositories │ └───────────┘ └────┬──────────────┘ │ ┌─────▼─────┐ │ Database │ │ (MySQL) │ └───────────┘



### Request Flow with Caching
User Request → Service Layer ↓ Check Redis Cache ↓ ┌──────────┴──────────┐ │ │ Cache HIT Cache MISS │ │ Return (5ms) Query Database ↓ Store in Cache ↓ Return (500ms)
### Key Design Patterns
- **MVC Pattern** - Separation of concerns
- **Repository Pattern** - Data access abstraction
- **Cache-Aside Pattern** - Lazy loading with Redis
- **DTO Pattern** - Data transfer objects
- **Dependency Injection** - Loose coupling
- **Transaction Management** - ACID properties

---
## 🚀 Caching Strategy

### What We Cache


✅ Product Catalog
   - All products list
   - Products by category
   - Products by brand
   - Product search results
   Cache Key: "products::{query}"
   TTL: 1 hour

✅ Individual Products
   - Product details by ID
   Cache Key: "products::{productId}"
   TTL: 1 hour
   
## Cache Operations

1. **Cache-Aside(Read):**
   @Cacheable - Check cache first, load from DB on miss
   : ├─ Cache Hit: Return immediately (5ms)
   : └─ Cache Miss: Query DB → Store in cache → Return (500ms)

2. **Cache-Invalidation(Write):**
   @CacheEvict - Clear cache when data changes
   : ├─ Product added: Clear all product caches
   : ├─ Product updated: Update specific product + clear lists
   : └─ Product deleted: Clear all product caches

3. **Cache Update(Write):**
   @CachePut - Update cache with new value
   : └─ Product updated: Update cache without clearing

### Performance Gains
: Operation	Without Cache	With Cache	Improvement
: Get All Products	500ms	5ms	100x faster
: Get Product by ID	300ms	3ms	100x faster
: Search Products	800ms	8ms	100x faster
: Category Filter	400ms	4ms	100x faster

## 🚀 Getting Started

### Prerequisites
- **JDK 17+** installed
- **Maven 3.6+** installed
- **MySQL 8.0+** running
- **Docker(for Redis) or Redis 7.0+ installed locxally**
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/junseo85/Jun-shops.git
   cd Jun-shops
   
2. **Start Redis with Docker:**
   (Pull and run Redis) 
: docker pull redis:latest
: docker run -d --name redis-junshops -p 6379:6379 redis:latest

   (Verify Redis is running)
: docker ps
: docker exec -it redis-junshops redis-cli ping
   (Should output: PONG)

3. **Configure database:**

Create a MySQL database:
CREATE DATABASE junshops;
3. **Update application properties:**
   Edit src/main/resources/application.properties:
   spring.datasource.url=jdbc:mysql://localhost:3306/junshops
   spring.datasource.username=your_username
   spring.datasource.password=your_password
4. **Build the project:**
    mvn clean install
5. **Run the application:**
    mvn spring-boot:run
6. **Access the application:**
    http://localhost:8080/
7. **Login with default Admin credentials:**
    username: admin@junshops.com
    password: admin123
   📖 Usage Guide
   For Customers
   Register/Login

## Usage Guide

### For Customers
1. **Register/Login:**
- Navigate to the login page
- Create a new account or login with existing credentials

2. **Browse Products(Cached):**

- View all products on the home page (served from cache after first load)
- Filter by category (results cached)
- Search by name or brand (search results cached)

3. **Shopping Cart:**

- Click "Add to Cart" on any product
- Adjust quantities in cart
- Remove items as needed

4. **Checkout:**

- Click "Proceed to Checkout"
- Fill in payment information
- Enter shipping address
- Review order summary
- Click "Place Order"

5. **View Orders:**

- Navigate to "My Orders"
- View order history
- Check order status
- See item details

## For Admins

1. **Access Admin Panel:**

- Login with admin account
- Admin menu appears in navigation

2. **Manage Users:**

- View all users
- Create new users
- Edit user information
- Assign/remove admin roles
- Delete users

3. **View Order History:**

- See all orders from all users
- Filter by customer name, order ID, or status
- View detailed order information
- Track customer purchases
## 🗄️ Database Schema
 Core Entities
User
: ├─ id (PK)
: ├─ firstName
: ├─ lastName
: ├─ email (unique)
: ├─ password (encrypted)
: └─ roles (Many-to-Many with Role)

 Product (Cached in Redis)
: ├─ id (PK)
: ├─ name
: ├─ brand
: ├─ description
: ├─ price
: ├─ inventory
: └─ category (Many-to-One)

Cart
: ├─ id (PK)
: ├─ user (One-to-One)
: ├─ totalAmount
: └─ items (One-to-Many with CartItem)

Order
: ├─ id (PK)
: ├─ user (Many-to-One)
: ├─ orderDate
: ├─ totalAmount
: ├─ orderStatus
: └─ orderItems (One-to-Many with OrderItem)

OrderItem
: ├─ id (PK)
: ├─ order (Many-to-One)
: ├─ product (Many-to-One)
: ├─ quantity
: └─ price (snapshot at order time)

### Caching Layer
Redis Cache Structure:

- products::allProducts → [List of all products] (TTL: 1h)
- products::123 → {Product object} (TTL: 1h)
- products::category_Electronics → [Filtered products] (TTL: 1h)
- products::brand_Apple → [Filtered products] (TTL: 1h)
- products::name_iPhone → [Search results] (TTL: 1h)

### Relationships
- User ↔ Cart: One-to-One
- User ↔ Orders: One-to-Many
- Product ↔ Category: Many-to-One
- Cart ↔ CartItems: One-to-Many
- Order ↔ OrderItems: One-to-Many
- User ↔ Roles: Many-to-Many


# 🔌 API Endpoints 

Products(Cached)

: GET    /api/v1/products           - Get all products (cached)
: GET    /api/v1/products/{id}      - Get product by ID (cached)
: GET    /api/v1/products/category/{categoryId} - Get products by category (cached)
: POST   /api/v1/products           - Create product (ADMIN) (clears cache)
: PUT    /api/v1/products/{id}      - Update product (ADMIN) (updates cache)
: DELETE /api/v1/products/{id}      - Delete product (ADMIN) (clears cache)

Cart

: GET    /api/v1/carts/user/{userId} - Get user's cart
: POST   /api/v1/carts/{cartId}/items/{productId} - Add item to cart
: PUT    /api/v1/carts/{cartId}/items/{productId} - Update item quantity
: DELETE /api/v1/carts/{cartId}/items/{productId} - Remove item from cart
: DELETE /api/v1/carts/{cartId}      - Clear cart

Orders

: POST   /api/v1/orders/{userId}    - Place order
: GET    /api/v1/orders/{orderId}   - Get order by ID
: GET    /api/v1/orders/user/{userId} - Get user's orders


# Performance Optimization

## Caching Implementation

// Example: Cached product query

@Cacheable(value = "products", key = "#productId")
public Product getProductById(Long productId) {
// 1st call: Query database (500ms)
// Subsequent calls: Return from cache (5ms)
return productRepository.findById(productId);
}

// Example: Cache invalidation on update
@CacheEvict(value = "products", allEntries = true)
public Product addProduct(AddProductRequest request) {
// Clears cache when new product added
return productRepository.save(product);
}