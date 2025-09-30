# Team Collaboration Guide

## 👥 eCommerce Shopping Cart Application - Team Development

This guide provides comprehensive instructions for team members to collaborate effectively on the eCommerce Shopping Cart Application project.

---

## 🍴 How to Fork & Set Up the Repository

### **Step 1: Fork the Repository**

1. **Navigate to the Main Repository**
   - Go to: [eCommerce Shopping Cart Application](https://github.com/shafikhakim27/eCommerce-Shopping-Cart-Application-)
   - Make sure you're logged into your GitHub account

2. **Create Your Fork**
   - Click the **"Fork"** button in the top-right corner of the repository page
   - Select your GitHub account as the destination
   - Wait for the forking process to complete (usually takes a few seconds)
   - You'll be redirected to your fork at: `https://github.com/YOUR_USERNAME/eCommerce-Shopping-Cart-Application-`

### **Step 2: Clone Your Fork Locally**

```bash
# Replace YOUR_USERNAME with your GitHub username
git clone https://github.com/YOUR_USERNAME/eCommerce-Shopping-Cart-Application-.git

# Navigate into the project directory
cd eCommerce-Shopping-Cart-Application-

# Add the original repository as upstream (for staying in sync)
git remote add upstream https://github.com/shafikhakim27/eCommerce-Shopping-Cart-Application-.git

# Verify your remotes
git remote -v
# Should show:
# origin    https://github.com/YOUR_USERNAME/eCommerce-Shopping-Cart-Application-.git (fetch)
# origin    https://github.com/YOUR_USERNAME/eCommerce-Shopping-Cart-Application-.git (push)
# upstream  https://github.com/shafikhakim27/eCommerce-Shopping-Cart-Application-.git (fetch)
# upstream  https://github.com/shafikhakim27/eCommerce-Shopping-Cart-Application-.git (push)
```

### **Step 3: Development Environment Setup**

**Prerequisites Verification:**
```bash
# Check Java version (must be 21+)
java -version

# Check Maven version (must be 3.9+)
mvn -version

# Check Git version
git --version
```

**Database Setup Options:**

**Option A: Quick Start with H2 (Recommended for Development)**
```bash
# No additional setup needed - H2 runs in memory
# Perfect for testing and development
mvn spring-boot:run
```

**Option B: Production Setup with MySQL**
```bash
# Install MySQL 8.0+ (if not already installed)
# Ubuntu/Debian:
sudo apt update && sudo apt install mysql-server-8.0

# macOS with Homebrew:
brew install mysql

# Start MySQL service
sudo systemctl start mysql  # Linux
brew services start mysql   # macOS

# Create database and user
mysql -u root -p
```

```sql
-- Execute these commands in MySQL console
CREATE DATABASE ecommerce_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ecommerce_dev'@'localhost' IDENTIFIED BY 'dev_password';
GRANT ALL PRIVILEGES ON ecommerce_dev.* TO 'ecommerce_dev'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

**Configure MySQL in application.properties:**
```properties
# Comment out H2 configuration and uncomment MySQL:
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce_dev?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=ecommerce_dev
spring.datasource.password=dev_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### **Step 4: Build and Run the Application**

```bash
# Clean and compile the project
mvn clean compile

# Run tests to ensure everything works
mvn test

# Start the application
mvn spring-boot:run

# Alternative: Build JAR and run
mvn clean package -DskipTests
java -jar target/shopping-cart-1.0.0.jar
```

**Access the Application:**
- **Main Application**: [http://localhost:8080](http://localhost:8080)
- **H2 Console** (if using H2): [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`, Password: (empty)

### **Step 5: Verify Setup with Demo Accounts**

Test the application using these demo accounts:

| Username | Password | Role | Purpose |
|----------|----------|------|---------|
| `admin` | `password` | ADMIN | Test admin features, product management |
| `testuser` | `password` | USER | Test user features, shopping, reviews |
| `johndoe` | `password` | USER | Test additional user scenarios |

**Quick Verification Checklist:**
- [ ] Application starts without errors
- [ ] Can access login page
- [ ] Can log in with demo accounts
- [ ] Product listing loads with pagination
- [ ] Shopping cart functionality works
- [ ] Admin panel accessible (admin account only)

---

## 🔄 Development Workflow

### **Before Starting Work**

```bash
# Always start by syncing with the upstream repository
git checkout main
git fetch upstream
git merge upstream/main
git push origin main
```

### **Feature Development Process**

1. **Create a Feature Branch**
```bash
# Create and switch to a new feature branch
git checkout -b feature/your-feature-name

# Examples:
git checkout -b feature/product-search-enhancement
git checkout -b feature/email-notifications
git checkout -b bugfix/cart-quantity-validation
```

2. **Make Your Changes**
```bash
# Make your code changes
# Add new files or modify existing ones

# Stage your changes
git add .

# Commit with descriptive message
git commit -m "feat: Add advanced product search with filters

- Implement search by name, category, and price range
- Add autocomplete suggestions
- Update UI with search filters panel
- Add unit tests for search functionality"
```

3. **Keep Your Branch Updated**
```bash
# Regularly sync with upstream while working
git fetch upstream
git rebase upstream/main

# Resolve any conflicts if they arise
# Push your updated branch
git push origin feature/your-feature-name
```

4. **Create a Pull Request**
- Go to your fork on GitHub
- Click **"Compare & pull request"** button
- Fill out the pull request template:
  - **Title**: Clear, concise description
  - **Description**: What changed, why, and how to test
  - **Screenshots**: For UI changes
  - **Testing**: How you tested the changes

### **Code Review Process**

**For Authors:**
- Ensure all tests pass before requesting review
- Provide clear description and testing instructions
- Respond to feedback promptly and professionally
- Make requested changes in additional commits

**For Reviewers:**
- Review code for functionality, readability, and best practices
- Test the changes locally if possible
- Provide constructive feedback
- Approve when satisfied with the changes

### **Merging Guidelines**

1. **Before Merging:**
   - [ ] All tests pass
   - [ ] Code review approved
   - [ ] No merge conflicts
   - [ ] Feature branch is up-to-date with main

2. **Merge Process:**
```bash
# Switch to main branch
git checkout main

# Fetch latest changes
git fetch upstream
git merge upstream/main

# Merge your feature branch (use --no-ff to preserve history)
git merge --no-ff feature/your-feature-name

# Push to upstream (if you have permissions)
git push upstream main

# Clean up feature branch
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

---

## 🛠️ IDE Setup Recommendations

### **VS Code Extensions**
```json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "vscjava.vscode-spring-boot",
    "redhat.java",
    "formulahendry.auto-rename-tag",
    "bradlc.vscode-tailwindcss",
    "ms-vscode.vscode-typescript-next",
    "eamodio.gitlens",
    "streetsidesoftware.code-spell-checker"
  ]
}
```

### **IntelliJ IDEA Setup**
1. **Import Project**: File → Open → Select project directory
2. **Enable Plugins**: Spring Boot, Maven, Git
3. **Configure JDK**: File → Project Structure → Project SDK → Java 21
4. **Enable Auto-Import**: Settings → Build → Build Tools → Maven → Auto-import

### **Eclipse Setup**
1. **Import Project**: File → Import → Existing Maven Projects
2. **Install Spring Tools**: Help → Eclipse Marketplace → Search "Spring Tools"
3. **Configure Build Path**: Project Properties → Java Build Path → Libraries

---

## 🚨 Common Issues & Solutions

### **Port 8080 Already in Use**
```bash
# Find process using port 8080
sudo lsof -i :8080

# Kill the process (replace PID with actual process ID)
sudo kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

### **Database Connection Issues**
```bash
# Check MySQL status
sudo systemctl status mysql

# Restart MySQL if needed
sudo systemctl restart mysql

# Verify connection manually
mysql -u ecommerce_dev -p ecommerce_dev
```

### **Maven Build Issues**
```bash
# Clean Maven cache
mvn dependency:purge-local-repository

# Force update dependencies
mvn clean install -U

# Skip tests if they're failing
mvn clean package -DskipTests
```

### **Git Merge Conflicts**
```bash
# Check conflict status
git status

# Edit conflicted files manually or use merge tool
git mergetool

# After resolving conflicts
git add .
git commit -m "resolve: Merge conflicts in [file names]"
```

---

## 📋 Development Standards

### **Code Style Guidelines**
- **Java**: Follow Google Java Style Guide
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Naming**: Use descriptive variable and method names
- **Comments**: Document complex business logic and public APIs

### **Commit Message Format**
```
type(scope): brief description

Detailed explanation of what changed and why.

- List specific changes
- Reference issue numbers if applicable
- Include breaking changes if any

Examples:
feat(payment): Add Apple Pay integration
fix(cart): Resolve quantity validation bug
docs(readme): Update setup instructions
refactor(service): Extract common validation logic
test(integration): Add payment processing tests
```

### **Branch Naming Conventions**
- **Features**: `feature/description-of-feature`
- **Bug Fixes**: `bugfix/description-of-bug`
- **Documentation**: `docs/description-of-update`
- **Refactoring**: `refactor/description-of-refactor`
- **Tests**: `test/description-of-test`

### **Testing Requirements**
- **Unit Tests**: Cover service layer business logic (target: >80% coverage)
- **Integration Tests**: Test controller endpoints and database interactions
- **Manual Testing**: Verify UI functionality across different browsers
- **Performance Testing**: Test with larger datasets for pagination features

---

## 🤝 Communication Guidelines

### **GitHub Issues**
- **Use Templates**: Follow the issue templates for bugs and features
- **Be Specific**: Include steps to reproduce, expected vs actual behavior
- **Add Labels**: Use appropriate labels (bug, enhancement, documentation, etc.)
- **Reference PRs**: Link related pull requests and commits

### **Pull Request Best Practices**
- **Descriptive Titles**: Clearly state what the PR accomplishes
- **Detailed Descriptions**: Explain the changes, testing approach, and impact
- **Small, Focused PRs**: Keep changes focused on a single feature or fix
- **Include Screenshots**: For UI changes, include before/after screenshots
- **Update Documentation**: Update README or inline docs if needed

### **Team Communication**
- **Daily Standups**: Share progress, blockers, and plans
- **Code Reviews**: Provide constructive feedback within 24 hours
- **Documentation**: Keep README and technical docs updated
- **Knowledge Sharing**: Document architectural decisions and patterns

---

## 📊 Project Structure Guidelines

### **Package Organization**
```java
com.ecommerce.shoppingcart/
├── config/          # Configuration classes (@Configuration)
├── controller/      # Web controllers (@Controller, @RestController)
├── model/          # JPA entities (@Entity)
├── repository/     # Data access (@Repository)
├── service/        # Business logic (@Service)
├── dto/            # Data transfer objects (for API responses)
├── exception/      # Custom exceptions
└── util/           # Utility classes
```

### **File Naming Conventions**
- **Controllers**: `[Entity]Controller.java` (e.g., `ProductController.java`)
- **Services**: `[Entity]Service.java` (e.g., `ProductService.java`)
- **Repositories**: `[Entity]Repository.java` (e.g., `ProductRepository.java`)
- **DTOs**: `[Entity]DTO.java` or `[Purpose]DTO.java`
- **Templates**: `kebab-case.html` (e.g., `product-detail.html`)

### **Database Migration Strategy**
- **Development**: Use `spring.jpa.hibernate.ddl-auto=update`
- **Production**: Use `spring.jpa.hibernate.ddl-auto=validate` with Flyway migrations
- **Version Schema**: Include migration scripts in `src/main/resources/db/migration/`

---

## 🎯 Quick Reference

### **Useful Commands**
```bash
# Start application
mvn spring-boot:run

# Run tests
mvn test

# Build without tests
mvn clean package -DskipTests

# Check for updates
git fetch upstream && git status

# Create feature branch
git checkout -b feature/new-feature

# Sync with upstream
git fetch upstream && git rebase upstream/main
```

### **Important URLs**
- **Main Application**: [http://localhost:8080](http://localhost:8080)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Admin Panel**: [http://localhost:8080/admin](http://localhost:8080/admin)
- **API Documentation**: [http://localhost:8080/api/products](http://localhost:8080/api/products)

### **Demo Credentials**
- **Admin**: username=`admin`, password=`password`
- **User**: username=`testuser`, password=`password`

---

**Happy Collaborating! 🚀**

*For more detailed information, see the main [README.md](../README.md) file.*