# ecommerce service

Spring Boot backend application for managing products, users, and orders with JWT authentication and role-based access control and discount strategy.

## Features

- User authentication with JWT
- Role-based authorization (USER, PREMIUM_USER, ADMIN)
- Product CRUD operations with search and filtering
- Order processing with stock validation
- Dynamic discount calculation using Strategy pattern
- Soft delete for products
- Redis caching (optional, configurable)
- Database migrations with Flyway
- API documentation with Swagger
- Multiple environment profiles (dev, prod)

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL / H2
- Redis (optional)
- Flyway
- JUnit 5 & Mockito
- Swagger/OpenAPI
- Docker

## Run

mvn spring-boot:run -Dspring-boot.run.profiles=dev

## Configuration

### Environment Profiles

- **dev**: H2 database, simple caching, detailed logging
- **prod**: PostgreSQL, optional Redis caching, minimal logging

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Products
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search` - Search products
- `POST /api/products` - Create product (Admin only)
- `PUT /api/products/{id}` - Update product (Admin only)
- `DELETE /api/products/{id}` - Soft Delete product (Admin only)

### Orders
- `POST /api/orders` - Place order
- `GET /api/orders/{id}` - Get order details
- `GET /api/orders/my-orders` - Get user's orders
- `GET /api/orders/all-orders` - Get all orders (Admin only)
 

Controller tests are included for all endpoints with proper security mocking.
 

## Default Test Accounts


USERNAME  		EMAIL  				ROLE  
----------------------------------------
admin		admin@test.com			ADMIN
premium		premium@test.com		PREMIUM_USER
user		user@test.com			USER
adminuser1	adminuser1@test.com		ADMIN
premiumuser	premiumuser@test.com	PREMIUM_USER
manoj		manoj@test.com			USER

## API Documentation

Access Swagger UI: http://localhost:8081/swagger-ui.html
 
