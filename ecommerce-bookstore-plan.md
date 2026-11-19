# eCommerce Bookstore Backend Implementation Plan

## Top-Level Overview
Design and implement a production-ready Java 17 Spring Boot backend application for an eCommerce bookstore using Spring Data JPA, Hibernate, and MySQL. The backend is architected in distinct layers (Controller, Service, Repository, Entity, DTO) and encapsulates four core modules: Book Management, Customer Management, Shopping Cart, and Order Management.

---

## Architecture and Design Overview

### Tech Stack & Configuration
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.x (or 3.x latest stable)
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: MySQL 8.x
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **JSON Processing**: Jackson (default in Spring Boot Web)
- **Security & Network Binding**: Configured to bind on `127.0.0.1:8080`, parameterized queries via JPA to eliminate SQL injection, secure exception formatting without sensitive stack trace leaks.

---

## Database Design & Entity Relationship

```
+--------------------+        +---------------------+
|     customers      | 1    1 |        carts        |
+--------------------+--------+---------------------+
| id (PK, BIGINT)    |        | id (PK, BIGINT)     |
| email (UNIQUE)     |        | customer_id (FK)    |
| first_name         |        | created_at          |
| last_name          |        | updated_at          |
| phone              |        +----------+----------+
| address_line1      |                   | 1
| address_line2      |                   |
| city               |                   | *
| postal_code        |        +----------v----------+
| country            |        |     cart_items      |
| created_at         |        +---------------------+
| updated_at         |        | id (PK, BIGINT)     |
+---------+----------+        | cart_id (FK)        |
          | 1                 | book_id (FK)        |
          |                   | quantity            |
          | *                 | unit_price          |
+---------v----------+        +---------------------+
|       orders       |                   ^
+--------------------+                   |
| id (PK, BIGINT)    |                   |
| order_number (UNQ) |        +----------+----------+
| customer_id (FK)   |        |        books        |
| order_status       |        +---------------------+
| total_amount       |        | id (PK, BIGINT)     |
| shipping_address   |        | isbn (UNIQUE)       |
| payment_status     |        | title               |
| created_at         |        | author              |
| updated_at         |        | category            |
+---------+----------+        | price               |
          | 1                 | stock_quantity      |
          |                   | description         |
          | *                 | cover_image_url     |
+---------v----------+        | created_at          |
|    order_items     |        | updated_at          |
+--------------------+        +---------------------+
| id (PK, BIGINT)    |                   ^
| order_id (FK)      |                   |
| book_id (FK)       +-------------------+
| book_title         |
| quantity           |
| price_per_unit     |
| subtotal           |
+--------------------+
```

---

## Proposed Project Directory Structure

```
ecommerce-bookstore/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/bookstore/
│   │   │   ├── EcommerceBookstoreApplication.java
│   │   │   ├── common/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── PagedResponse.java
│   │   │   │   └── exception/
│   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │       ├── ResourceNotFoundException.java
│   │   │   │       ├── BadRequestException.java
│   │   │   │       ├── InsufficientStockException.java
│   │   │   │       └── ErrorResponse.java
│   │   │   ├── book/
│   │   │   │   ├── controller/BookController.java
│   │   │   │   ├── service/BookService.java
│   │   │   │   ├── service/impl/BookServiceImpl.java
│   │   │   │   ├── repository/BookRepository.java
│   │   │   │   ├── entity/Book.java
│   │   │   │   └── dto/
│   │   │   │       ├── BookRequestDto.java
│   │   │   │       └── BookResponseDto.java
│   │   │   ├── customer/
│   │   │   │   ├── controller/CustomerController.java
│   │   │   │   ├── service/CustomerService.java
│   │   │   │   ├── service/impl/CustomerServiceImpl.java
│   │   │   │   ├── repository/CustomerRepository.java
│   │   │   │   ├── entity/Customer.java
│   │   │   │   └── dto/
│   │   │   │       ├── CustomerRequestDto.java
│   │   │   │       └── CustomerResponseDto.java
│   │   │   ├── cart/
│   │   │   │   ├── controller/CartController.java
│   │   │   │   ├── service/CartService.java
│   │   │   │   ├── service/impl/CartServiceImpl.java
│   │   │   │   ├── repository/CartRepository.java
│   │   │   │   ├── repository/CartItemRepository.java
│   │   │   │   ├── entity/Cart.java
│   │   │   │   ├── entity/CartItem.java
│   │   │   │   └── dto/
│   │   │   │       ├── CartResponseDto.java
│   │   │   │       ├── CartItemRequestDto.java
│   │   │   │       ├── CartItemResponseDto.java
│   │   │   │       └── UpdateCartItemDto.java
│   │   │   └── order/
│   │   │       ├── controller/OrderController.java
│   │   │       ├── service/OrderService.java
│   │   │       ├── service/impl/OrderServiceImpl.java
│   │   │       ├── repository/OrderRepository.java
│   │   │       ├── repository/OrderItemRepository.java
│   │   │       ├── entity/Order.java
│   │   │       ├── entity/OrderItem.java
│   │   │       ├── entity/OrderStatus.java
│   │   │       ├── entity/PaymentStatus.java
│   │   │       └── dto/
│   │   │           ├── CheckoutRequestDto.java
│   │   │           ├── OrderResponseDto.java
│   │   │           ├── OrderItemResponseDto.java
│   │   │           └── UpdateOrderStatusDto.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── schema.sql
│   └── test/
│       └── java/com/bookstore/
│           ├── BookServiceTest.java
│           ├── CustomerServiceTest.java
│           ├── CartServiceTest.java
│           └── OrderServiceTest.java
```

---

## Detailed REST API Endpoints Specification

### 1. Book Management Module (`/api/v1/books`)
- `POST /api/v1/books` - Create a new book (`201 CREATED`)
- `GET /api/v1/books` - Retrieve paginated & filtered list of books (`200 OK`) (query params: `page`, `size`, `category`, `keyword`, `sort`)
- `GET /api/v1/books/{id}` - Retrieve book details by ID (`200 OK`)
- `GET /api/v1/books/isbn/{isbn}` - Retrieve book by ISBN (`200 OK`)
- `PUT /api/v1/books/{id}` - Update book information (`200 OK`)
- `PATCH /api/v1/books/{id}/stock` - Update book inventory stock (`200 OK`)
- `DELETE /api/v1/books/{id}` - Delete book (`204 NO CONTENT`)

### 2. Customer Management Module (`/api/v1/customers`)
- `POST /api/v1/customers` - Register a new customer (`201 CREATED`)
- `GET /api/v1/customers` - Get all customers with pagination (`200 OK`)
- `GET /api/v1/customers/{id}` - Get customer profile by ID (`200 OK`)
- `GET /api/v1/customers/email/{email}` - Find customer by email (`200 OK`)
- `PUT /api/v1/customers/{id}` - Update customer profile / address (`200 OK`)
- `DELETE /api/v1/customers/{id}` - Delete/deactivate customer (`204 NO CONTENT`)

### 3. Shopping Cart Module (`/api/v1/carts`)
- `GET /api/v1/carts/customer/{customerId}` - Get active cart for a customer (`200 OK`)
- `POST /api/v1/carts/customer/{customerId}/items` - Add item to cart (`201 CREATED`)
- `PUT /api/v1/carts/customer/{customerId}/items/{itemId}` - Update item quantity (`200 OK`)
- `DELETE /api/v1/carts/customer/{customerId}/items/{itemId}` - Remove item from cart (`204 NO CONTENT`)
- `DELETE /api/v1/carts/customer/{customerId}/clear` - Clear all items from cart (`204 NO CONTENT`)

### 4. Order Management Module (`/api/v1/orders`)
- `POST /api/v1/orders/checkout` - Checkout shopping cart and create order (`201 CREATED`)
- `GET /api/v1/orders/{orderId}` - Get order by ID (`200 OK`)
- `GET /api/v1/orders/number/{orderNumber}` - Get order by tracking order number (`200 OK`)
- `GET /api/v1/orders/customer/{customerId}` - Get customer order history (`200 OK`)
- `PATCH /api/v1/orders/{orderId}/status` - Update order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED) (`200 OK`)
- `POST /api/v1/orders/{orderId}/cancel` - Cancel order and restore stock (`200 OK`)

---

## Sub-Tasks

- [ ] **Sub-Task 1: Project Initialization & Build Setup**
  - **Intent**: Set up Maven project structure, Java 17 configuration, `pom.xml` dependencies, and application properties with secure host binding (`127.0.0.1`).
  - **Expected Outcomes**: Valid `pom.xml`, `application.properties` with MySQL and JPA configurations, and root `EcommerceBookstoreApplication.java`.
  - **Todo List**:
    1. Create `pom.xml` with Spring Boot 3.2.x, Spring Data JPA, Spring Web, Validation, MySQL Driver, and Lombok (or standard POJOs).
    2. Configure `application.properties` with database connection string, Hibernate dialect, DDL auto, and server port/host (`server.address=127.0.0.1`).
    3. Create main application entry point `EcommerceBookstoreApplication.java`.
  - **Relevant Context**: Root directory, `src/main/resources/application.properties`.
  - **Status**: `[ ] pending`

- [ ] **Sub-Task 2: Common Infrastructure & Global Exception Handling**
  - **Intent**: Provide unified API response formats, standard pagination wrappers, and centralized exception handling with HTTP status mapping.
  - **Expected Outcomes**: Custom exceptions, standardized JSON error responses, and `@RestControllerAdvice` handler.
  - **Todo List**:
    1. Create `ApiResponse<T>`, `PagedResponse<T>`, and `ErrorResponse`.
    2. Implement custom exceptions (`ResourceNotFoundException`, `BadRequestException`, `InsufficientStockException`).
    3. Implement `GlobalExceptionHandler` handling validation errors (`MethodArgumentNotValidException`), entity not found, business rule violations, and generic exceptions.
  - **Relevant Context**: `com.bookstore.common` package.
  - **Status**: `[ ] pending`

- [ ] **Sub-Task 3: Book Management Module**
  - **Intent**: Implement Book entity, repository, DTOs, service layer with business logic, and REST controllers with validation.
  - **Expected Outcomes**: Full CRUD, stock inventory management, category/keyword filtering, and pagination for books.
  - **Todo List**:
    1. Create `Book` JPA entity with JPA annotations and table indexing on ISBN and category.
    2. Create `BookRepository` with pagination and custom query methods (keyword/category filter).
    3. Implement `BookRequestDto` and `BookResponseDto` with validation constraints.
    4. Implement `BookService` and `BookServiceImpl` with transactional logic.
    5. Implement `BookController` exposing REST APIs.
  - **Relevant Context**: `com.bookstore.book` package.
  - **Status**: `[ ] pending`

- [ ] **Sub-Task 4: Customer Management Module**
  - **Intent**: Implement Customer entity, repository, DTOs, service layer, and REST controllers.
  - **Expected Outcomes**: Customer registration, profile management, email uniqueness validation, and address management.
  - **Todo List**:
    1. Create `Customer` JPA entity.
    2. Create `CustomerRepository` with unique email lookup.
    3. Implement `CustomerRequestDto` and `CustomerResponseDto` with email and phone validation.
    4. Implement `CustomerService` and `CustomerServiceImpl`.
    5. Implement `CustomerController` exposing REST endpoints.
  - **Relevant Context**: `com.bookstore.customer` package.
  - **Status**: `[ ] pending`

- [ ] **Sub-Task 5: Shopping Cart Module**
  - **Intent**: Implement Shopping Cart & Cart Item entities, calculation of totals, adding/updating/removing items, and syncing with book stock availability.
  - **Expected Outcomes**: Cart persistence per customer, live total calculation, stock verification before adding items.
  - **Todo List**:
    1. Create `Cart` and `CartItem` JPA entities with bidirectional/cascading mapping.
    2. Create `CartRepository` and `CartItemRepository`.
    3. Implement `CartResponseDto`, `CartItemRequestDto`, `CartItemResponseDto`.
    4. Implement `CartService` and `CartServiceImpl` (add item, update quantity, remove item, clear cart, total calculations).
    5. Implement `CartController` exposing REST endpoints.
  - **Relevant Context**: `com.bookstore.cart` package.
  - **Status**: `[ ] pending`

- [ ] **Sub-Task 6: Order Management Module**
  - **Intent**: Implement checkout process, converting cart to order, stock deduction in database transaction, order status transitions, and order history retrieval.
  - **Expected Outcomes**: ACID-compliant checkout, stock decrement, order number generation (UUID/timestamp), order cancellation with stock restoration.
  - **Todo List**:
    1. Create `Order`, `OrderItem`, `OrderStatus` (enum), and `PaymentStatus` (enum).
    2. Create `OrderRepository` and `OrderItemRepository`.
    3. Implement `CheckoutRequestDto`, `OrderResponseDto`, `OrderItemResponseDto`, `UpdateOrderStatusDto`.
    4. Implement `OrderService` and `OrderServiceImpl` with `@Transactional` checkout logic, inventory deduction, and cart clearing.
    5. Implement `OrderController` exposing REST endpoints.
  - **Relevant Context**: `com.bookstore.order` package.
  - **Status**: `[ ] pending`

- [ ] **Sub-Task 7: Sample Data, Schema SQL & Integration Verification**
  - **Intent**: Provide `schema.sql` / `data.sql` sample seed data and automated tests to verify the layered architecture end-to-end.
  - **Expected Outcomes**: Working seed data, automated unit/integration tests verifying Book, Customer, Cart, and Order flows.
  - **Todo List**:
    1. Create `schema.sql` and `data.sql` for MySQL or H2 testing.
    2. Write unit and service tests for Book, Customer, Cart, and Order services.
    3. Verify build, tests, and API contract compliance.
  - **Relevant Context**: `src/main/resources/`, `src/test/java/`.
  - **Status**: `[ ] pending`
