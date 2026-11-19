# E-Commerce Bookstore Backend

A production-ready **Java 17 & Spring Boot 3.2.x** backend application for an eCommerce bookstore. Designed with a clean layered architecture (**Controller, Service, Repository, Entity, DTO**), robust database transactions, comprehensive input validation, and centralized exception handling.

---

## 🛠️ Technologies Used

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.5
- **ORM & Data Access**: Spring Data JPA, Hibernate 6.x
- **Databases**: 
  - **H2 Database** (Default in-memory development profile with MySQL dialect emulation)
  - **MySQL 8.x** (Production database profile)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Connection Pool**: HikariCP
- **Testing**: JUnit 5, Mockito, Spring Boot MockMvc Test
- **Build Tool**: Apache Maven (Includes Maven Wrapper `mvnw.cmd` / `mvnw`)

---

## 🏛️ Layered Architecture & Project Structure

```
src/main/java/com/bookstore/
├── EcommerceBookstoreApplication.java       # Application Main Entry Point
├── common/                                  # Cross-cutting concerns
│   ├── ApiResponse.java                     # Unified generic API response envelope
│   ├── PagedResponse.java                   # Standard pagination metadata wrapper
│   ├── HomeController.java                  # Root directory endpoint (GET /)
│   └── exception/
│       ├── GlobalExceptionHandler.java      # Centralized @RestControllerAdvice
│       ├── ErrorResponse.java               # Standardized error payload
│       ├── ResourceNotFoundException.java   # 404 Not Found exception
│       ├── BadRequestException.java         # 400 Bad Request exception
│       ├── DuplicateResourceException.java  # 409 Conflict exception
│       └── InsufficientStockException.java  # Stock validation exception
├── book/                                    # Book Management Module
│   ├── entity/Book.java                     # JPA Entity with ISBN & Category indexes
│   ├── repository/BookRepository.java       # Custom JPQL search & pagination queries
│   ├── dto/                                 # BookRequestDto, BookResponseDto, UpdateStockDto
│   ├── service/                             # BookService & BookServiceImpl
│   └── controller/BookController.java       # REST Controller (/api/v1/books)
├── customer/                                # Customer Management Module
│   ├── entity/Customer.java                 # JPA Entity with Unique Email constraint
│   ├── repository/CustomerRepository.java   # Customer queries
│   ├── dto/                                 # CustomerRequestDto, CustomerResponseDto
│   ├── service/                             # CustomerService & CustomerServiceImpl
│   └── controller/CustomerController.java   # REST Controller (/api/v1/customers)
├── cart/                                    # Shopping Cart Module
│   ├── entity/Cart.java & CartItem.java     # Relational cart mapping with live totals
│   ├── repository/                          # CartRepository & CartItemRepository
│   ├── dto/                                 # CartItemRequestDto, CartResponseDto, UpdateCartItemDto
│   ├── service/                             # CartService & CartServiceImpl
│   └── controller/CartController.java       # REST Controller (/api/v1/carts)
└── order/                                   # Order Management Module
    ├── entity/                              # Order, OrderItem, OrderStatus, PaymentStatus
    ├── repository/                          # OrderRepository & OrderItemRepository
    ├── dto/                                 # CheckoutRequestDto, OrderResponseDto, UpdateOrderStatusDto
    ├── service/                             # OrderService & OrderServiceImpl (ACID checkout & cancel)
    └── controller/OrderController.java      # REST Controller (/api/v1/orders)
```

---

## 🔒 Security & Secrets Management

In compliance with enterprise security standards:
- **No Hardcoded Credentials**: Database passwords and credentials are never hardcoded. They are dynamically resolved via environment variables (`${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`) with non-production local defaults.
- **Localhost Binding**: The application binds strictly to `127.0.0.1` (`server.address=127.0.0.1`) to prevent unauthorized exposure on external interfaces.
- **SQL Injection Prevention**: All queries use Spring Data JPA parameterized methods and typed JPQL parameters.
- **Sanitized Error Outputs**: Internal server exceptions and stack traces are logged server-side only and never leaked to API clients.

---

## 🚀 Quickstart & Setup Instructions

### Prerequisites
- **JDK 17** installed and configured (`JAVA_HOME` pointing to JDK 17).
- No global Maven installation required (the included Maven Wrapper is used).

### 1. Run in Development Mode (Zero-Configuration H2)
By default, the application runs on **Port 8082** using an in-memory database pre-seeded with sample books and customers:

```powershell
# Windows (PowerShell / Command Prompt)
.\mvnw.cmd spring-boot:run

# Linux / macOS / Git Bash
./mvnw spring-boot:run
```

Once started, open your browser or Postman at: **`http://127.0.0.1:8082/`**

---

### 2. Run with External MySQL Database
To run with an external MySQL server:

1. Create a MySQL database:
   ```sql
   CREATE DATABASE bookstore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Set your environment variables:
   ```powershell
   $env:DB_URL="jdbc:mysql://localhost:3306/bookstore_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_secure_password"
   ```
3. Launch with the MySQL profile:
   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
   ```

---

### 3. Run Automated Tests
Execute all 42 JUnit 5 and Mockito unit/controller tests:

```powershell
.\mvnw.cmd test
```

### 4. Package Executable JAR
```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target/ecommerce-bookstore-1.0.0.jar
```

---

## 📖 REST API Reference & Examples

### Base URL: `http://127.0.0.1:8082`

---

### 1. Root & Health Check
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` or `/api` | API directory and service health overview |

---

### 2. Book Management (`/api/v1/books`)
| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `POST` | `/api/v1/books` | Create a new book | `201 Created` |
| `GET` | `/api/v1/books` | Get paginated books (params: `page`, `size`, `category`, `keyword`, `sortBy`, `sortDir`) | `200 OK` |
| `GET` | `/api/v1/books/{id}` | Get book by ID | `200 OK` |
| `GET` | `/api/v1/books/isbn/{isbn}` | Get book by ISBN | `200 OK` |
| `PUT` | `/api/v1/books/{id}` | Update book details | `200 OK` |
| `PATCH` | `/api/v1/books/{id}/stock` | Update stock quantity | `200 OK` |
| `DELETE` | `/api/v1/books/{id}` | Delete book | `204 No Content` |

#### Sample Create Book Request (`POST /api/v1/books`):
```json
{
  "isbn": "9780134685991",
  "title": "Effective Java (3rd Edition)",
  "author": "Joshua Bloch",
  "category": "Technology",
  "price": 45.00,
  "stockQuantity": 20,
  "description": "The definitive guide to Java platform best practices.",
  "coverImageUrl": "https://example.com/effective-java.jpg"
}
```

---

### 3. Customer Management (`/api/v1/customers`)
| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `POST` | `/api/v1/customers` | Register a new customer | `201 Created` |
| `GET` | `/api/v1/customers` | Get paginated customers list | `200 OK` |
| `GET` | `/api/v1/customers/{id}` | Get customer profile by ID | `200 OK` |
| `GET` | `/api/v1/customers/email/{email}` | Find customer by email | `200 OK` |
| `PUT` | `/api/v1/customers/{id}` | Update customer profile | `200 OK` |
| `DELETE` | `/api/v1/customers/{id}` | Delete customer | `204 No Content` |

#### Sample Customer Registration Request (`POST /api/v1/customers`):
```json
{
  "email": "alice.smith@example.com",
  "firstName": "Alice",
  "lastName": "Smith",
  "phone": "+1-555-0199",
  "addressLine1": "789 Broadway",
  "city": "New York",
  "postalCode": "10003",
  "country": "USA"
}
```

---

### 4. Shopping Cart (`/api/v1/carts`)
| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `GET` | `/api/v1/carts/customer/{customerId}` | Retrieve customer's active cart | `200 OK` |
| `POST` | `/api/v1/carts/customer/{customerId}/items` | Add book item to cart | `201 Created` |
| `PUT` | `/api/v1/carts/customer/{customerId}/items/{itemId}` | Update cart item quantity | `200 OK` |
| `DELETE` | `/api/v1/carts/customer/{customerId}/items/{itemId}` | Remove item from cart | `204 No Content` |
| `DELETE` | `/api/v1/carts/customer/{customerId}/clear` | Clear all items from cart | `204 No Content` |

#### Sample Add to Cart Request (`POST /api/v1/carts/customer/1/items`):
```json
{
  "bookId": 1,
  "quantity": 2
}
```

---

### 5. Order Management (`/api/v1/orders`)
| Method | Endpoint | Description | Status Code |
|---|---|---|---|
| `POST` | `/api/v1/orders/checkout` | Checkout cart and place order | `201 Created` |
| `GET` | `/api/v1/orders/{orderId}` | Get order by ID | `200 OK` |
| `GET` | `/api/v1/orders/number/{orderNumber}` | Get order by order tracking number | `200 OK` |
| `GET` | `/api/v1/orders/customer/{customerId}` | Get customer order history | `200 OK` |
| `PATCH` | `/api/v1/orders/{orderId}/status` | Update order status | `200 OK` |
| `POST` | `/api/v1/orders/{orderId}/cancel` | Cancel order & restore inventory | `200 OK` |

#### Sample Checkout Request (`POST /api/v1/orders/checkout`):
```json
{
  "customerId": 1,
  "shippingAddress": "789 Broadway, Apt 2B, New York, NY 10003"
}
```

#### Sample Successful Order Response:
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 1,
    "orderNumber": "ORD-20261118152517-A9AD13",
    "customerId": 1,
    "customerEmail": "alice.smith@example.com",
    "customerName": "Alice Smith",
    "orderStatus": "PENDING",
    "paymentStatus": "PENDING",
    "totalAmount": 90.00,
    "shippingAddress": "789 Broadway, Apt 2B, New York, NY 10003",
    "items": [
      {
        "id": 1,
        "bookId": 1,
        "bookTitle": "Effective Java (3rd Edition)",
        "bookIsbn": "9780134685991",
        "quantity": 2,
        "pricePerUnit": 45.00,
        "subtotal": 90.00
      }
    ],
    "createdAt": "2026-11-18T15:25:17",
    "updatedAt": "2026-11-18T15:25:17"
  },
  "timestamp": "2026-11-18T15:25:17"
}
```

---

## ⚠️ Standard Error Response Format

When validation fails or an exception occurs, the API returns a structured JSON payload:

```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "Input validation failed. Please check the validationErrors field.",
  "path": "/api/v1/books",
  "timestamp": "2026-11-18T15:20:00",
  "validationErrors": {
    "isbn": "Invalid ISBN format (must be 10 or 13 digits)",
    "price": "Price must be greater than zero",
    "title": "Title cannot be blank"
  }
}
```
