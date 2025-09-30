package com.ecommerce.shoppingcart.config;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.ProductService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ProductService productService;
    private final UserService userService;
    
    @Autowired
    public DataInitializer(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }
    
    @Override
    public void run(String... args) throws Exception {
        initializeProducts();
        initializeUsers();
    }
    
    private void initializeProducts() {
        // Electronics
        Product laptop = new Product("Gaming Laptop", "High-performance gaming laptop with RTX 4060", 
                new BigDecimal("1299.99"), 10, Product.Category.ELECTRONICS);
        laptop.setImageUrl("/images/laptop.jpg");
        productService.saveProduct(laptop);
        
        Product smartphone = new Product("Smartphone Pro", "Latest smartphone with advanced camera", 
                new BigDecimal("899.99"), 25, Product.Category.ELECTRONICS);
        smartphone.setImageUrl("/images/smartphone.jpg");
        productService.saveProduct(smartphone);
        
        Product headphones = new Product("Wireless Headphones", "Noise-cancelling wireless headphones", 
                new BigDecimal("199.99"), 50, Product.Category.ELECTRONICS);
        headphones.setImageUrl("/images/headphones.jpg");
        productService.saveProduct(headphones);
        
        // Clothing
        Product tshirt = new Product("Cotton T-Shirt", "Comfortable cotton t-shirt available in multiple colors", 
                new BigDecimal("29.99"), 100, Product.Category.CLOTHING);
        tshirt.setImageUrl("/images/tshirt.jpg");
        productService.saveProduct(tshirt);
        
        Product jeans = new Product("Classic Jeans", "Durable denim jeans with modern fit", 
                new BigDecimal("79.99"), 75, Product.Category.CLOTHING);
        jeans.setImageUrl("/images/jeans.jpg");
        productService.saveProduct(jeans);
        
        // Books
        Product book1 = new Product("Java Programming Guide", "Comprehensive guide to Java programming", 
                new BigDecimal("49.99"), 30, Product.Category.BOOKS);
        book1.setImageUrl("/images/java-book.jpg");
        productService.saveProduct(book1);
        
        Product book2 = new Product("Web Development Handbook", "Modern web development techniques and best practices", 
                new BigDecimal("39.99"), 40, Product.Category.BOOKS);
        book2.setImageUrl("/images/web-book.jpg");
        productService.saveProduct(book2);
        
        // Home
        Product coffeemaker = new Product("Coffee Maker", "Automatic coffee maker with programmable timer", 
                new BigDecimal("89.99"), 20, Product.Category.HOME);
        coffeemaker.setImageUrl("/images/coffee-maker.jpg");
        productService.saveProduct(coffeemaker);
        
        Product lamp = new Product("LED Desk Lamp", "Adjustable LED desk lamp with touch controls", 
                new BigDecimal("45.99"), 35, Product.Category.HOME);
        lamp.setImageUrl("/images/desk-lamp.jpg");
        productService.saveProduct(lamp);
        
        // Sports
        Product basketball = new Product("Professional Basketball", "Official size basketball for indoor/outdoor use", 
                new BigDecimal("24.99"), 60, Product.Category.SPORTS);
        basketball.setImageUrl("/images/basketball.jpg");
        productService.saveProduct(basketball);
    }
    
    private void initializeUsers() {
        // Create admin user
        if (!userService.existsByUsername("admin")) {
            User admin = new User("admin", "admin@example.com", "password", "Admin", "User");
            admin.setRole(User.Role.ADMIN);
            userService.registerUser(admin);
        }
        
        // Create test user
        if (!userService.existsByUsername("testuser")) {
            User testUser = new User("testuser", "test@example.com", "password", "Test", "User");
            userService.registerUser(testUser);
        }
    }
}