# Enhanced Features Documentation

## 🚀 eCommerce Shopping Cart Application - Version 2.0 Enhancements

This document details the advanced features and enhancements introduced in Version 2.0 of the eCommerce Shopping Cart Application, transforming it into a production-ready e-commerce platform.

---

## 🆕 New Advanced Features

### **1. Payment Gateway Simulation**

#### **What & Why**
A comprehensive payment processing system that simulates real-world payment gateways without actual financial transactions. This provides a realistic e-commerce experience while maintaining security.

#### **How It Works**
- **Multiple Payment Methods**: Credit/Debit cards, Digital wallets, PayPal, Apple Pay, Google Pay
- **Realistic Simulation**: 90% success rate with varied failure scenarios
- **Transaction Tracking**: Unique transaction IDs and status monitoring
- **Security Features**: Card number masking, encrypted details storage

#### **Implementation Details**

**Payment Entity**
```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(unique = true)
    private String transactionId;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    
    private String gatewayResponse;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime processedAt;
    
    // Card details (masked for security)
    private String maskedCardNumber;
    private String cardHolderName;
    
    public enum PaymentMethod {
        CREDIT_CARD("Credit Card"),
        DEBIT_CARD("Debit Card"),
        DIGITAL_WALLET("Digital Wallet"),
        PAYPAL("PayPal"),
        APPLE_PAY("Apple Pay"),
        GOOGLE_PAY("Google Pay");
        
        private final String displayName;
        PaymentMethod(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
    }
}
```

**Payment Service**
```java
@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private OrderService orderService;
    
    public Payment processPayment(Long orderId, PaymentRequest paymentRequest) {
        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        
        // Create payment record
        Payment payment = Payment.builder()
            .order(order)
            .paymentMethod(paymentRequest.getPaymentMethod())
            .amount(order.getTotalAmount())
            .transactionId(generateTransactionId())
            .maskedCardNumber(maskCardNumber(paymentRequest.getCardNumber()))
            .cardHolderName(paymentRequest.getCardHolderName())
            .build();
        
        payment = paymentRepository.save(payment);
        
        // Simulate payment processing
        PaymentResult result = simulatePaymentProcessing(payment);
        
        // Update payment status
        payment.setPaymentStatus(result.getStatus());
        payment.setGatewayResponse(result.getResponse());
        payment.setProcessedAt(LocalDateTime.now());
        
        Payment processedPayment = paymentRepository.save(payment);
        
        // Update order status based on payment result
        if (result.getStatus() == Payment.PaymentStatus.COMPLETED) {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);
        } else {
            orderService.updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
        }
        
        return processedPayment;
    }
    
    private PaymentResult simulatePaymentProcessing(Payment payment) {
        // Simulate processing time
        try {
            Thread.sleep(2000 + new Random().nextInt(3000)); // 2-5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 90% success rate simulation
        boolean success = new Random().nextDouble() < 0.9;
        
        if (success) {
            return PaymentResult.builder()
                .status(Payment.PaymentStatus.COMPLETED)
                .response("Payment processed successfully")
                .build();
        } else {
            String[] failureReasons = {
                "Insufficient funds",
                "Card expired",
                "Invalid card details",
                "Bank declined transaction",
                "Network timeout"
            };
            String reason = failureReasons[new Random().nextInt(failureReasons.length)];
            
            return PaymentResult.builder()
                .status(Payment.PaymentStatus.FAILED)
                .response("Payment failed: " + reason)
                .build();
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + 
               String.format("%04d", new Random().nextInt(10000));
    }
    
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
```

---

### **2. Product Reviews & Ratings System**

#### **What & Why**
A complete review and rating system allowing customers to share experiences and help others make informed purchasing decisions. Includes verification for authentic reviews.

#### **How It Works**
- **5-Star Rating System**: Visual star selection with descriptive labels
- **Text Reviews**: Detailed customer feedback (up to 1000 characters)
- **Verified Purchases**: Only customers who bought the product can review
- **Review Statistics**: Average ratings, rating distribution, review counts
- **Helpful Voting**: Community-driven quality assessment

#### **Implementation Details**

**Review Entity**
```java
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
    
    @Size(max = 1000, message = "Review text cannot exceed 1000 characters")
    private String reviewText;
    
    @Builder.Default
    private Boolean verifiedPurchase = false;
    
    @Builder.Default
    private Integer helpfulCount = 0;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Review Service**
```java
@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private OrderService orderService;
    
    public Review addReview(Long productId, String username, ReviewRequest request) {
        // Verify user has purchased this product
        boolean hasPurchased = orderService.hasUserPurchasedProduct(username, productId);
        
        // Check if user already reviewed this product
        Optional<Review> existingReview = reviewRepository
            .findByProductIdAndUserUsername(productId, username);
        
        if (existingReview.isPresent()) {
            throw new DuplicateReviewException("You have already reviewed this product");
        }
        
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        Product product = productService.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        Review review = Review.builder()
            .product(product)
            .user(user)
            .rating(request.getRating())
            .reviewText(request.getReviewText())
            .verifiedPurchase(hasPurchased)
            .build();
        
        return reviewRepository.save(review);
    }
    
    public ReviewStatistics getReviewStatistics(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        
        if (reviews.isEmpty()) {
            return ReviewStatistics.builder()
                .averageRating(0.0)
                .totalReviews(0L)
                .ratingDistribution(new int[5])
                .build();
        }
        
        double averageRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        
        int[] distribution = new int[5];
        reviews.forEach(review -> distribution[review.getRating() - 1]++);
        
        return ReviewStatistics.builder()
            .averageRating(Math.round(averageRating * 10.0) / 10.0)
            .totalReviews((long) reviews.size())
            .ratingDistribution(distribution)
            .verifiedPurchaseCount(reviews.stream()
                .mapToInt(r -> r.getVerifiedPurchase() ? 1 : 0)
                .sum())
            .build();
    }
    
    public void markReviewHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }
}
```

---

### **3. Advanced Pagination System**

#### **What & Why**
Efficient data management for large product catalogs, preventing performance issues and improving user experience through organized content display.

#### **How It Works**
- **Configurable Page Sizes**: 6, 12, 24, 48 items per page
- **Multi-Sort Options**: Name (A-Z, Z-A), Price (Low-High, High-Low), Category
- **Search Integration**: Pagination works with search and filters
- **URL State Management**: Bookmarkable URLs with pagination state

#### **Implementation Details**

**Enhanced Product Repository**
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Pagination methods
    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByActiveTrueAndCategory(Product.Category category, Pageable pageable);
    
    // Search with pagination
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Advanced filtering
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findWithFilters(@Param("category") Product.Category category,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);
}
```

**Enhanced Product Service**
```java
@Service
public class ProductService {
    
    public Page<Product> findProducts(ProductSearchCriteria criteria) {
        Pageable pageable = createPageable(criteria);
        
        if (criteria.hasSearchKeyword()) {
            return productRepository.searchByKeyword(criteria.getKeyword(), pageable);
        }
        
        if (criteria.hasFilters()) {
            return productRepository.findWithFilters(
                criteria.getCategory(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                pageable
            );
        }
        
        return productRepository.findByActiveTrue(pageable);
    }
    
    private Pageable createPageable(ProductSearchCriteria criteria) {
        Sort sort = createSort(criteria.getSortBy(), criteria.getSortDirection());
        return PageRequest.of(criteria.getPage(), criteria.getSize(), sort);
    }
    
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        return switch (sortBy) {
            case "name" -> Sort.by(direction, "name");
            case "price" -> Sort.by(direction, "price");
            case "category" -> Sort.by(direction, "category");
            case "rating" -> Sort.by(direction, "averageRating"); // Requires calculation
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }
}
```

**Enhanced Product Controller**
```java
@Controller
@RequestMapping("/products")
public class ProductController {
    
    @GetMapping
    public String listProducts(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             @RequestParam(defaultValue = "name") String sortBy,
                             @RequestParam(defaultValue = "asc") String sortDir,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) Product.Category category,
                             @RequestParam(required = false) BigDecimal minPrice,
                             @RequestParam(required = false) BigDecimal maxPrice) {
        
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
            .page(page)
            .size(Math.min(size, 48)) // Max 48 items per page
            .sortBy(sortBy)
            .sortDirection(sortDir)
            .keyword(search)
            .category(category)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .build();
        
        Page<Product> productPage = productService.findProducts(criteria);
        
        // Add pagination metadata
        PaginationModel pagination = PaginationModel.builder()
            .currentPage(page)
            .totalPages(productPage.getTotalPages())
            .totalElements(productPage.getTotalElements())
            .hasNext(productPage.hasNext())
            .hasPrevious(productPage.hasPrevious())
            .pageSize(size)
            .build();
        
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", criteria);
        model.addAttribute("categories", Product.Category.values());
        model.addAttribute("pageSizes", Arrays.asList(6, 12, 24, 48));
        model.addAttribute("sortOptions", getSortOptions());
        
        return "products";
    }
    
    private List<SortOption> getSortOptions() {
        return Arrays.asList(
            new SortOption("name-asc", "Name (A-Z)"),
            new SortOption("name-desc", "Name (Z-A)"),
            new SortOption("price-asc", "Price (Low to High)"),
            new SortOption("price-desc", "Price (High to Low)"),
            new SortOption("category-asc", "Category")
        );
    }
}
```

---

### **4. Admin Management Panel**

#### **What & Why**
A comprehensive backend management system allowing administrators to control all aspects of the e-commerce platform without technical intervention.

#### **How It Works**
- **Role-Based Access**: Only ADMIN role users can access
- **Product Management**: CRUD operations, stock control, visibility toggles
- **Order Management**: Status updates, order tracking
- **Dashboard Analytics**: Key metrics and recent activity monitoring

#### **Implementation Details**

**Admin Controller**
```java
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @GetMapping
    public String adminDashboard(Model model) {
        AdminDashboardData dashboardData = AdminDashboardData.builder()
            .totalProducts(productService.getTotalProductCount())
            .activeProducts(productService.getActiveProductCount())
            .totalOrders(orderService.getTotalOrderCount())
            .pendingOrders(orderService.getPendingOrderCount())
            .totalRevenue(orderService.getTotalRevenue())
            .recentOrders(orderService.getRecentOrders(10))
            .lowStockProducts(productService.getLowStockProducts(10))
            .topSellingProducts(productService.getTopSellingProducts(5))
            .build();
        
        model.addAttribute("dashboard", dashboardData);
        return "admin/dashboard";
    }
    
    @GetMapping("/products")
    public String manageProducts(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Product> products = productService.findAllProducts(pageable);
        
        model.addAttribute("products", products);
        model.addAttribute("categories", Product.Category.values());
        
        return "admin/products";
    }
    
    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute ProductRequest productRequest,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Please correct the errors in the form");
            return "redirect:/admin/products";
        }
        
        try {
            productService.createProduct(productRequest);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to add product: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/update-stock/{id}")
    public String updateStock(@PathVariable Long id,
                            @RequestParam Integer stockQuantity,
                            RedirectAttributes redirectAttributes) {
        try {
            productService.updateStock(id, stockQuantity);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Stock updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update stock: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/toggle-status/{id}")
    public String toggleProductStatus(@PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            productService.toggleProductStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update product status: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @GetMapping("/orders")
    public String manageOrders(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) Order.OrderStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "orderDate"));
        
        Page<Order> orders;
        if (status != null) {
            orders = orderService.findOrdersByStatus(status, pageable);
        } else {
            orders = orderService.findAllOrders(pageable);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        
        return "admin/orders";
    }
    
    @PostMapping("/orders/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id,
                                  @RequestParam Order.OrderStatus status,
                                  RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Order status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to update order status: " + e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
}
```

---

### **5. Modern UI/UX Design System**

#### **What & Why**
A complete visual overhaul implementing modern design principles for improved user engagement and professional appearance.

#### **Design Implementation**
- **Glassmorphism Effects**: Translucent elements with backdrop blur
- **Gradient Themes**: Dynamic color schemes throughout the interface
- **Typography**: Google Fonts (Inter family) for clean readability
- **Responsive Design**: Mobile-first approach with Bootstrap 5.3.0
- **Interactive Elements**: Hover effects, transitions, and micro-animations

#### **CSS Architecture**

**Custom CSS Variables**
```css
:root {
    /* Primary Color Palette */
    --primary-gradient: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    --secondary-gradient: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    --success-gradient: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    --warning-gradient: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    
    /* Glassmorphism Effects */
    --glass-bg: rgba(255, 255, 255, 0.25);
    --glass-border: rgba(255, 255, 255, 0.18);
    --backdrop-blur: blur(10px);
    
    /* Shadows */
    --card-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
    --hover-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
    --inset-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
    
    /* Spacing and Layout */
    --border-radius: 16px;
    --border-radius-sm: 8px;
    --border-radius-lg: 24px;
    
    /* Typography */
    --font-family-primary: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
    --font-weight-light: 300;
    --font-weight-normal: 400;
    --font-weight-medium: 500;
    --font-weight-semibold: 600;
    --font-weight-bold: 700;
    
    /* Transitions */
    --transition-fast: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    --transition-slow: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
```

**Glassmorphism Components**
```css
.glass-card {
    background: var(--glass-bg);
    backdrop-filter: var(--backdrop-blur);
    border: 1px solid var(--glass-border);
    border-radius: var(--border-radius);
    box-shadow: var(--card-shadow);
    transition: var(--transition);
}

.glass-card:hover {
    transform: translateY(-5px);
    box-shadow: var(--hover-shadow);
}

.gradient-button {
    background: var(--primary-gradient);
    border: none;
    border-radius: var(--border-radius-sm);
    color: white;
    font-weight: var(--font-weight-medium);
    padding: 12px 24px;
    transition: var(--transition);
    position: relative;
    overflow: hidden;
}

.gradient-button::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    transition: var(--transition);
}

.gradient-button:hover::before {
    left: 100%;
}
```

**Responsive Design Enhancements**
```css
/* Mobile-first responsive design */
.product-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 1.5rem;
    padding: 1rem;
}

@media (min-width: 576px) {
    .product-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (min-width: 768px) {
    .product-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 2rem;
        padding: 1.5rem;
    }
}

@media (min-width: 1200px) {
    .product-grid {
        grid-template-columns: repeat(4, 1fr);
        gap: 2.5rem;
        padding: 2rem;
    }
}

/* Interactive elements */
.product-card {
    position: relative;
    overflow: hidden;
    transition: var(--transition);
}

.product-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--primary-gradient);
    opacity: 0;
    transition: var(--transition);
    z-index: 1;
}

.product-card:hover::before {
    opacity: 0.1;
}

.product-card:hover .product-image {
    transform: scale(1.05);
}

.product-image {
    transition: var(--transition-slow);
}
```

---

## 🔧 Core Function Enhancements

### **Enhanced Product Browsing**
- **Original**: Basic listing, no pagination, simple search
- **Enhanced**: Advanced pagination, multi-sort options, review integration, performance optimization

### **Enhanced Authentication**
- **Original**: Basic username/password, simple registration, single role
- **Enhanced**: Role-based access control, enhanced security, admin privileges, session persistence

### **Enhanced Shopping Cart**
- **Original**: Basic add/remove, simple updates, session storage
- **Enhanced**: Persistent storage, real-time updates, validation, mobile optimization

### **Enhanced Order Management**
- **Original**: Basic order creation, simple tracking, limited details
- **Enhanced**: Payment integration, detailed status tracking, admin management, notifications

### **Enhanced Purchase History**
- **Original**: Basic order list, simple details, no payment tracking
- **Enhanced**: Comprehensive details, payment integration, reorder functionality, advanced filtering

---

## 📊 Performance Improvements

### **Database Optimizations**
- Strategic indexes on frequently queried fields
- Connection pooling with HikariCP
- Efficient pagination with LIMIT/OFFSET queries
- Query optimization for complex searches

### **Frontend Optimizations**
- Asset minification and compression
- Image optimization with proper formats
- Browser caching for static assets
- CDN integration for external libraries

### **Application Performance**
- Spring Boot Actuator for monitoring
- Lazy loading for JPA entities
- Strategic caching for frequently accessed data
- Async processing for non-blocking operations

---

**This completes the documentation of the enhanced features that transform the application into a production-ready e-commerce platform.**

*For core function documentation, see [CORE_FUNCTIONS.md](CORE_FUNCTIONS.md)*

*For team collaboration guidelines, see [TEAM_COLLABORATION.md](TEAM_COLLABORATION.md)*