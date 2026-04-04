# Inventory Management System

A Spring Boot 3 backend application for inventory management with Admin authentication.

## Tech Stack

- **Java**: 17
- **Framework**: Spring Boot 3.2.3
- **Database**: PostgreSQL
- **Build Tool**: Maven
- **Dependencies**:
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Lombok
  - Validation

## Project Structure

```
src/main/java/com/tuktak/inventory/
├── InventoryApplication.java    # Main application entry point
├── config/                      # Configuration classes
│   ├── SecurityConfig.java      # Spring Security configuration
│   ├── DataInitializer.java     # Initial data setup
│   └── GlobalExceptionHandler.java
├── controller/                  # REST API controllers
│   ├── AuthController.java      # Authentication endpoints
│   ├── AdminController.java     # Admin management endpoints
│   ├── ProductController.java   # Product CRUD endpoints
│   └── CategoryController.java  # Category CRUD endpoints
├── dto/                         # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── AdminDto.java
│   ├── ProductDto.java
│   ├── CategoryDto.java
│   └── ApiResponse.java
├── entity/                      # JPA Entities
│   ├── BaseEntity.java
│   ├── Admin.java
│   ├── Product.java
│   └── Category.java
├── repository/                  # Spring Data JPA Repositories
│   ├── AdminRepository.java
│   ├── ProductRepository.java
│   └── CategoryRepository.java
└── service/                     # Business Logic Layer
    ├── AdminService.java
    ├── ProductService.java
    ├── CategoryService.java
    └── CustomUserDetailsService.java
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

## Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE inventory_db;
```

2. Update `src/main/resources/application.yml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_db
    username: your_username
    password: your_password
```

## Running the Application

```bash
# Navigate to project directory
cd inventory

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Default Admin Credentials

On first startup, a default super admin is created:
- **Username**: `admin`
- **Password**: `admin123`

> **Important**: Change these credentials in production!

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Admin login |
| POST | `/api/auth/logout` | Admin logout |
| GET | `/api/auth/me` | Get current user |

### Admin Management (SUPER_ADMIN only)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin` | Create admin |
| GET | `/api/admin` | Get all admins |
| GET | `/api/admin/{id}` | Get admin by ID |
| PUT | `/api/admin/{id}` | Update admin |
| DELETE | `/api/admin/{id}` | Delete admin |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create product |
| GET | `/api/products` | Get all products |
| GET | `/api/products/active` | Get active products |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/sku/{sku}` | Get product by SKU |
| GET | `/api/products/category/{id}` | Get products by category |
| GET | `/api/products/search?name=` | Search products |
| GET | `/api/products/low-stock` | Get low stock products |
| PUT | `/api/products/{id}` | Update product |
| PATCH | `/api/products/{id}/stock?quantity=` | Update stock |
| PATCH | `/api/products/{id}/deactivate` | Deactivate product |
| DELETE | `/api/products/{id}` | Delete product |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/categories` | Create category |
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/{id}` | Get category by ID |
| GET | `/api/categories/name/{name}` | Get category by name |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |

## Example API Requests

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Create Category (with Basic Auth)
```bash
curl -X POST http://localhost:8080/api/categories \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"name": "Electronics", "description": "Electronic products"}'
```

### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "sku": "LAP-001",
    "price": 999.99,
    "quantity": 50,
    "categoryId": 1
  }'
```

## Security

- Basic Authentication is used for API endpoints
- All `/api/**` endpoints require authentication (except `/api/auth/**`)
- Admin management endpoints (`/api/admin/**`) require `SUPER_ADMIN` role
- Passwords are encrypted using BCrypt

## Configuration Profiles

- `default`: Production configuration (PostgreSQL)
- `dev`: Development configuration with `create-drop` DDL
- `test`: Test configuration with H2 in-memory database
