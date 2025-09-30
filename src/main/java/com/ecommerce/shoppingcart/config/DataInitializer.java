package com.ecommerce.shoppingcart.config;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.ProductService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ProductService productService;
    private final UserService userService;
    
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
        laptop.setImageUrl("https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=400&h=300&fit=crop");
        productService.saveProduct(laptop);
        
        Product smartphone = new Product("Smartphone Pro", "Latest smartphone with advanced camera", 
                new BigDecimal("899.99"), 25, Product.Category.ELECTRONICS);
        smartphone.setImageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=300&fit=crop");
        productService.saveProduct(smartphone);
        
        Product headphones = new Product("Wireless Headphones", "Noise-cancelling wireless headphones", 
                new BigDecimal("199.99"), 50, Product.Category.ELECTRONICS);
        headphones.setImageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop");
        productService.saveProduct(headphones);
        
        // Clothing
        Product tshirt = new Product("Cotton T-Shirt", "Comfortable cotton t-shirt available in multiple colors", 
                new BigDecimal("29.99"), 100, Product.Category.CLOTHING);
        tshirt.setImageUrl("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=300&fit=crop");
        productService.saveProduct(tshirt);
        
        Product jeans = new Product("Classic Jeans", "Durable denim jeans with modern fit", 
                new BigDecimal("79.99"), 75, Product.Category.CLOTHING);
        jeans.setImageUrl("https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=300&fit=crop");
        productService.saveProduct(jeans);
        
        // Books
        Product book1 = new Product("Java Programming Guide", "Comprehensive guide to Java programming", 
                new BigDecimal("49.99"), 30, Product.Category.BOOKS);
        book1.setImageUrl("https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&h=300&fit=crop");
        productService.saveProduct(book1);
        
        Product book2 = new Product("Web Development Handbook", "Modern web development techniques and best practices", 
                new BigDecimal("39.99"), 40, Product.Category.BOOKS);
        book2.setImageUrl("https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400&h=300&fit=crop");
        productService.saveProduct(book2);
        
        // Home
        Product coffeemaker = new Product("Coffee Maker", "Automatic coffee maker with programmable timer", 
                new BigDecimal("89.99"), 20, Product.Category.HOME);
        coffeemaker.setImageUrl("https://images.unsplash.com/photo-1559056199-641a0ac8b55e?w=400&h=300&fit=crop");
        productService.saveProduct(coffeemaker);
        
        Product lamp = new Product("LED Desk Lamp", "Adjustable LED desk lamp with touch controls", 
                new BigDecimal("45.99"), 35, Product.Category.HOME);
        lamp.setImageUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&h=300&fit=crop");
        productService.saveProduct(lamp);
        
        // Sports
        Product basketball = new Product("Professional Basketball", "Official size basketball for indoor/outdoor use", 
                new BigDecimal("24.99"), 60, Product.Category.SPORTS);
        basketball.setImageUrl("https://images.unsplash.com/photo-1546519638-68e109498ffc?w=400&h=300&fit=crop");
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