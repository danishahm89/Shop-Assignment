# Shop E-Commerce API

A Spring Boot REST API for managing products and orders with JWT authentication.

## Setup

**Requirements:**
- Java 17+
- Maven 3.6+

**Run Locally:**
```bash
./mvnw spring-boot:run
```

Application starts at `http://localhost:8080`

**Run Tests:**
```bash
./mvnw test
```

## Test Credentials

| Username | Password    | Role          |
|----------|-------------|---------------|
| admin    | admin123    | ADMIN         |
| premium  | premium123  | PREMIUM_USER  |
| user     | user123     | USER          |

## Design Decisions

**Architecture:**
- Layered architecture (Controller → Service → Repository)
- DTOs for API contracts separate from entities
- JPA Specifications for dynamic filtering

**Security:**
- JWT tokens for stateless authentication
- Role-based authorization using Spring Security
- BCrypt for password hashing

**Discount System:**
- Strategy pattern for flexible discount rules
- Premium users get 10% discount
- Orders over $500 get additional 5%
- Discounts stack together

**Database:**
- H2 for local development
- PostgreSQL for production
- Flyway for version-controlled migrations
- Soft deletes for products (status-based)

**Validation:**
- Bean Validation (JSR-380) on all DTOs
- Comprehensive field-level constraints
- Nested validation for complex objects
- Custom validation messages
- Global exception handling with detailed field errors

**Performance:**
- Database indexes on frequently queried columns (status, name, user_id)
- Pagination for large datasets
- Connection pooling configured

## API Documentation

### Authentication

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "eyJhbGc...",
  "username": "admin",
  "role": "ADMIN"
}
```

Use token in subsequent requests:
```http
Authorization: Bearer <token>
```

### Products

**List Products**
```http
GET /api/products?name=laptop&minPrice=1000&maxPrice=3000&page=0&size=10&sort=price,asc
```

Query parameters (all optional):
- `name` - search by name
- `minPrice`, `maxPrice` - price range (both required if using)
- `available` - filter in-stock items
- `page`, `size` - pagination
- `sort` - sorting (default: name,asc)

**Get Product**
```http
GET /api/products/{id}
```

**Create Product** (Admin only)
```http
POST /api/products
Content-Type: application/json

{
  "name": "MacBook Pro",
  "description": "16-inch laptop",
  "price": 2499.99,
  "quantity": 15
}
```

**Validation Rules:**
- `name`: Required, 3-100 characters
- `description`: Optional, max 500 characters
- `price`: Required, must be > 0, max 10 digits + 2 decimals
- `quantity`: Required, must be >= 0

**Validation Error Example:**
```json
{
  "timestamp": "2025-12-14T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "fieldErrors": {
    "name": "Product name is required",
    "price": "Price must be greater than 0",
    "quantity": "Quantity cannot be negative"
  }
}
```

**Update Product** (Admin only)
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "name": "MacBook Pro",
  "description": "Updated description",
  "price": 2299.99,
  "quantity": 20
}
```

**Delete Product** (Admin only)
```http
DELETE /api/products/{id}
```

### Orders

**Place Order**
```http
POST /api/v1/orders
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**Validation Rules:**
- `items`: Required, must contain at least 1 item, max 100 items
- `items[].productId`: Required, must be positive number
- `items[].quantity`: Required, must be between 1 and 1000

Response includes:
- Order details
- Applied discounts
- Final total

**Invalid Order Example:**
```http
POST /api/v1/orders
{
  "items": []
}
```

Response (400 Bad Request):
```json
{
  "timestamp": "2025-12-14T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "fieldErrors": {
    "items": "Order must contain at least one item"
  }
}
```

## Database Schema

**users**
- id, username (unique), email, password, role, created_at, updated_at

**products**
- id, name, description, price, quantity, status, created_at, updated_at
- Indexes: status, name

**orders**
- id, user_id (FK), order_total, status, created_at
- Indexes: user_id, status, created_at

**order_items**
- id, order_id (FK), product_id (FK), quantity, unit_price, discount_applied, total_price
- Indexes: order_id, product_id

## Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-12-14T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found"
}
```

### Validation Error Format
```json
{
  "timestamp": "2025-12-14T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "fieldErrors": {
    "name": "Product name is required",
    "price": "Price must be greater than 0",
    "quantity": "Quantity cannot be negative",
    "email": "Email must be valid"
  }
}
```

Common status codes:
- 200 - Success
- 201 - Created
- 400 - Bad request / Validation failed
- 401 - Unauthorized
- 403 - Forbidden
- 404 - Not found
- 409 - Conflict (duplicate resource)
- 500 - Server error

## Input Validation

All DTOs are validated using Bean Validation (JSR-380). See [VALIDATION_GUIDE.md](VALIDATION_GUIDE.md) for complete details.

### Common Validation Constraints

**ProductDto:**
- Name: 3-100 characters, required
- Description: max 500 characters, optional
- Price: > 0, max 10 digits + 2 decimals, required
- Quantity: >= 0, required

**OrderDto:**
- Items: 1-100 items, required
- Each item validated individually

**OrderItemDto:**
- Product ID: positive number, required
- Quantity: 1-1000, required

**UserDto:**
- Username: 3-50 characters, alphanumeric + underscore only, required
- Password: 6-100 characters, required
- Email: valid email format, required
- Role: required

**LoginRequest:**
- Username: required, not blank
- Password: required, not blank

## Project Structure

```
src/main/java/com/assignment/shop/
├── config/          # Configuration classes
├── security/        # JWT authentication
├── products/        # Product management
├── orders/          # Order processing
├── users/           # User management
└── discounts/       # Discount engine
```

## Configuration

Different profiles for environments:
- `local` - H2 database
- `dev`, `qa`, `prod` - PostgreSQL

Set active profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Postman Collection

Import `Shop_API.postman_collection.json` for ready-to-use requests.

Setup:
1. Import collection
2. Create environment variable: `base_url = http://localhost:8080`
3. Login to get token (automatically saved)
4. Use other endpoints

## Build

```bash
# Package
./mvnw clean package

# Run JAR
java -jar target/shop-0.0.1-SNAPSHOT.jar
```

