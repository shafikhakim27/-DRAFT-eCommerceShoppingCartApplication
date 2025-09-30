# eCommerce Shopping Cart Application

A comprehensive eCommerce Shopping Cart web application built with Spring Boot, Thymeleaf, React.js, and MySQL. This application demonstrates modern web development practices with a complete shopping experience.

## 🚀 Features

### Core Functionalities
- **Browse Products** - View products with category filtering and search functionality
- **Login/Logout** - Secure user authentication and session management
- **Shopping Cart** - Add, update, and remove items from cart
- **Checkout Process** - Complete order placement with shipping information
- **Purchase History** - View past orders and order details

### Technology Stack
- **Backend**: Spring Boot 3.2.0, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf templates, Bootstrap 5, React.js components
- **Database**: H2 (development), MySQL (production ready)
- **Build Tool**: Maven
- **Authentication**: Spring Security with BCrypt password encoding

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0+ (for production)

## 🛠️ Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/shafikhakim27/eCommerce-Shopping-Cart-Application-.git
cd eCommerce-Shopping-Cart-Application-
```

### 2. Build the Application
```bash
mvn clean compile
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Database Configuration

**Development (H2 - Default)**
- In-memory database
- Console available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`, Password: (empty)

**Production (MySQL)**
Uncomment and configure the MySQL settings in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

## 👤 Demo Accounts

| Username | Password | Role |
|----------|----------|------|
| testuser | password | USER |
| admin    | password | ADMIN |

## 🌐 API Endpoints

### Products API (REST)
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products?category={category}` - Filter by category
- `GET /api/products?search={keyword}` - Search products
- `GET /api/products/categories` - Get all categories

### Web Routes
- `/` or `/products` - Product listing (homepage)
- `/login` - User login
- `/register` - User registration
- `/cart` - Shopping cart
- `/orders/checkout` - Checkout process
- `/orders` - Order history
- `/orders/{id}` - Order details

## 🎯 Key Features Demonstrated

### 1. Spring Boot MVC
- RESTful web services
- Thymeleaf template engine
- Spring Security integration
- JPA/Hibernate data persistence

### 2. React.js Integration
- Product browser component consuming REST API
- Dynamic search and filtering
- Real-time data updates

### 3. Database Design
- User management with roles
- Product catalog with categories
- Shopping cart persistence
- Order management and history

### 4. Security
- Password encryption with BCrypt
- Session-based authentication
- Role-based access control
- CSRF protection

## 📁 Project Structure

```
src/main/java/com/ecommerce/shoppingcart/
├── config/          # Configuration classes
├── controller/      # Web and REST controllers
├── model/          # JPA entities
├── repository/     # Data access layer
├── service/        # Business logic layer
└── ShoppingCartApplication.java

src/main/resources/
├── static/         # CSS, JS, images
├── templates/      # Thymeleaf templates
└── application.properties

src/test/java/      # Test classes
```

## 🧪 Testing the Application

### 1. Test REST API
```bash
# Get all products
curl http://localhost:8080/api/products

# Search products
curl "http://localhost:8080/api/products?search=laptop"

# Filter by category
curl "http://localhost:8080/api/products?category=ELECTRONICS"
```

### 2. Test Web Interface
1. Navigate to `http://localhost:8080`
2. Browse products and categories
3. Login with demo account: `testuser/password`
4. Add products to cart
5. Complete checkout process
6. View order history

## 📱 React Component

The application includes a React.js component that demonstrates frontend-backend separation:
- Located in `/static/js/product-browser.jsx`
- Consumes REST API endpoints
- Provides interactive product browsing
- Real-time search and filtering

## 🔧 Development

### Running in Development Mode
```bash
mvn spring-boot:run
```

### Building for Production
```bash
mvn clean package
java -jar target/shopping-cart-1.0.0.jar
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is developed for educational purposes as part of J2EE coursework.

## 📧 Contact

For questions or support, please contact the development team.
