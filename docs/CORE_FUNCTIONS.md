# Core Functions Documentation

## 🏛️ eCommerce Shopping Cart Application - Core Architecture

This document details the original core functions of the eCommerce Shopping Cart Application and their fundamental implementation.

---

## 📊 Core Functions Overview

The eCommerce Shopping Cart Application is built around five essential core functions that form the foundation of any e-commerce platform:

1. **Product Browsing & Catalog Management**
2. **User Authentication & Session Management**
3. **Shopping Cart Operations**
4. **Order Processing & Management**
5. **Purchase History & Tracking**

---

## 🛒 1. Product Browsing & Catalog Management

### **Purpose**
Provide customers with an intuitive interface to discover, search, and view products available for purchase.

### **Core Components**

#### **Product Entity**
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    @NotBlank(message = "Description is required")
    @Column(length = 1000)
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    @Enumerated(EnumType.STRING)
    private Category category;
    
    private String imageUrl;
    
    @Builder.Default
    private Boolean active = true;
    
    public enum Category {
        ELECTRONICS, CLOTHING, BOOKS, HOME_GARDEN, SPORTS
    }
}
```

#### **Product Repository**
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByActiveTrueAndCategory(Product.Category category);
    List<Product> findByActiveTrueAndNameContainingIgnoreCase(String name);
    Optional<Product> findByIdAndActiveTrue(Long id);
}
```

#### **Product Service**
```java
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> findAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }
    
    public List<Product> findProductsByCategory(Product.Category category) {
        return productRepository.findByActiveTrueAndCategory(category);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByActiveTrueAndNameContainingIgnoreCase(keyword);
    }
    
    public Optional<Product> findById(Long id) {
        return productRepository.findByIdAndActiveTrue(id);
    }
}
```

#### **Product Controller**
```java
@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public String listProducts(Model model, 
                             @RequestParam(required = false) Product.Category category,
                             @RequestParam(required = false) String search) {
        List<Product> products;
        
        if (category != null) {
            products = productService.findProductsByCategory(category);
        } else if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search);
        } else {
            products = productService.findAllActiveProducts();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", Product.Category.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchTerm", search);
        
        return "products";
    }
    
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "product-detail";
        }
        return "redirect:/products";
    }
}
```

### **Key Features**
- **Category Filtering**: Organize products by predefined categories
- **Product Search**: Search functionality by product name
- **Product Details**: Detailed view with images, descriptions, and pricing
- **Stock Management**: Track product availability
- **Active/Inactive Status**: Control product visibility

---

## 🔐 2. User Authentication & Session Management

### **Purpose**
Secure user access control with registration, login, and session management capabilities.

### **Core Components**

#### **User Entity**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    @NotBlank(message = "Username is required")
    private String username;
    
    @Column(unique = true)
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    
    @Builder.Default
    private Boolean enabled = true;
    
    public enum Role {
        USER, ADMIN
    }
}
```

#### **User Repository**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### **User Service**
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public boolean validateUser(String username, String password) {
        Optional<User> user = findByUsername(username);
        return user.isPresent() && 
               passwordEncoder.matches(password, user.get().getPassword());
    }
}
```

#### **Authentication Controller**
```java
@Controller
public class AuthController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
    }
}
```

#### **Security Configuration**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/products/**", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );
        return http.build();
    }
}
```

### **Key Features**
- **User Registration**: Account creation with validation
- **Secure Login**: BCrypt password hashing
- **Session Management**: Spring Security session handling
- **Role-Based Access**: USER and ADMIN role distinction
- **Remember Me**: Persistent login sessions

---

## 🛍️ 3. Shopping Cart Operations

### **Purpose**
Allow customers to collect products for purchase, manage quantities, and maintain cart state across sessions.

### **Core Components**

#### **Cart Item Entity**
```java
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

#### **Cart Repository**
```java
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
    void deleteByUserAndProduct(User user, Product product);
}
```

#### **Cart Service**
```java
@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    public CartItem addToCart(String username, Long productId, Integer quantity) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productService.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(quantity)
                .build();
            return cartItemRepository.save(newItem);
        }
    }
    
    public List<CartItem> getCartItems(String username) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return cartItemRepository.findByUser(user);
    }
    
    public void updateCartItemQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }
    
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
    
    public void clearCart(String username) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        cartItemRepository.deleteByUser(user);
    }
    
    public BigDecimal calculateCartTotal(String username) {
        List<CartItem> cartItems = getCartItems(username);
        return cartItems.stream()
            .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

#### **Cart Controller**
```java
@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    
    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<CartItem> cartItems = cartService.getCartItems(username);
        BigDecimal total = cartService.calculateCartTotal(username);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "cart";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam Integer quantity,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            cartService.addToCart(username, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products";
    }
    
    @PostMapping("/update")
    public String updateCartItem(@RequestParam Long cartItemId,
                                @RequestParam Integer quantity,
                                RedirectAttributes redirectAttributes) {
        try {
            cartService.updateCartItemQuantity(cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long cartItemId,
                                RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }
}
```

### **Key Features**
- **Add to Cart**: Add products with specified quantities
- **Quantity Management**: Update item quantities in cart
- **Remove Items**: Remove individual items from cart
- **Cart Persistence**: Database-backed cart storage
- **Total Calculation**: Automatic cart total computation
- **User Isolation**: Each user has their own cart

---

## 📦 4. Order Processing & Management

### **Purpose**
Convert shopping cart contents into confirmed orders with proper tracking and management capabilities.

### **Core Components**

#### **Order Entity**
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems = new HashSet<>();
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    private String shippingAddress;
    private String shippingCity;
    private String shippingZip;
    
    @CreationTimestamp
    private LocalDateTime orderDate;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

#### **Order Item Entity**
```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    private Integer quantity;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal price; // Price at time of order
}
```

#### **Order Repository**
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByUserAndStatusOrderByOrderDateDesc(User user, Order.OrderStatus status);
    Optional<Order> findByIdAndUser(Long id, User user);
}
```

#### **Order Service**
```java
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    @Transactional
    public Order createOrder(String username, String shippingAddress, 
                           String shippingCity, String shippingZip) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<CartItem> cartItems = cartService.getCartItems(username);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot create order with empty cart");
        }
        
        Order order = Order.builder()
            .user(user)
            .shippingAddress(shippingAddress)
            .shippingCity(shippingCity)
            .shippingZip(shippingZip)
            .build();
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<OrderItem> orderItems = new HashSet<>();
        
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getProduct().getPrice())
                .build();
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(
                cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(username);
        
        return savedOrder;
    }
    
    public List<Order> findOrdersByUser(String username) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    public Optional<Order> findOrderByIdAndUser(Long orderId, String username) {
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByIdAndUser(orderId, user);
    }
    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
```

#### **Order Controller**
```java
@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;
    
    @GetMapping("/checkout")
    public String checkoutPage(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<CartItem> cartItems = cartService.getCartItems(username);
        
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        BigDecimal total = cartService.calculateCartTotal(username);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        
        return "checkout";
    }
    
    @PostMapping("/checkout")
    public String processCheckout(@RequestParam String shippingAddress,
                                 @RequestParam String shippingCity,
                                 @RequestParam String shippingZip,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            Order order = orderService.createOrder(username, shippingAddress, 
                                                 shippingCity, shippingZip);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Order placed successfully! Order ID: " + order.getId());
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }
    
    @GetMapping
    public String orderHistory(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Order> orders = orderService.findOrdersByUser(username);
        model.addAttribute("orders", orders);
        return "order-history";
    }
    
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, 
                             Model model, 
                             Authentication authentication) {
        String username = authentication.getName();
        Optional<Order> order = orderService.findOrderByIdAndUser(id, username);
        
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            return "order-detail";
        }
        
        return "redirect:/orders";
    }
}
```

### **Key Features**
- **Order Creation**: Convert cart to order with shipping details
- **Order Status Tracking**: Track order progress through workflow
- **Order History**: View all past orders
- **Order Details**: Detailed view of individual orders
- **Transaction Integrity**: Atomic order processing
- **Inventory Management**: Stock tracking during order placement

---

## 📋 5. Purchase History & Tracking

### **Purpose**
Provide customers with comprehensive access to their order history and detailed tracking information.

### **Core Features**

#### **Order History Display**
```java
// In OrderController
@GetMapping
public String orderHistory(Model model, Authentication authentication) {
    String username = authentication.getName();
    List<Order> orders = orderService.findOrdersByUser(username);
    
    // Add additional order information
    List<OrderHistoryDTO> orderHistory = orders.stream()
        .map(order -> OrderHistoryDTO.builder()
            .order(order)
            .itemCount(order.getOrderItems().size())
            .canCancel(order.getStatus() == Order.OrderStatus.PENDING)
            .estimatedDelivery(calculateEstimatedDelivery(order))
            .build())
        .collect(Collectors.toList());
    
    model.addAttribute("orderHistory", orderHistory);
    return "order-history";
}

private LocalDate calculateEstimatedDelivery(Order order) {
    return order.getOrderDate().toLocalDate().plusDays(7); // 7 days delivery
}
```

#### **Order Detail Tracking**
```java
// Enhanced order detail view
@GetMapping("/{id}")
public String orderDetail(@PathVariable Long id, 
                         Model model, 
                         Authentication authentication) {
    String username = authentication.getName();
    Optional<Order> orderOpt = orderService.findOrderByIdAndUser(id, username);
    
    if (orderOpt.isPresent()) {
        Order order = orderOpt.get();
        
        // Calculate order summary
        OrderSummary summary = OrderSummary.builder()
            .subtotal(calculateSubtotal(order))
            .tax(calculateTax(order))
            .shipping(calculateShipping(order))
            .total(order.getTotalAmount())
            .build();
        
        model.addAttribute("order", order);
        model.addAttribute("summary", summary);
        model.addAttribute("statusHistory", generateStatusHistory(order));
        
        return "order-detail";
    }
    
    return "redirect:/orders";
}
```

### **Key Features**
- **Chronological Order List**: Orders sorted by date (newest first)
- **Order Status Display**: Visual status indicators
- **Quick Order Actions**: Cancel, reorder, track options
- **Detailed Order View**: Complete order breakdown
- **Order Search**: Find orders by date, status, or amount
- **Download Receipts**: PDF receipt generation capability

---

## 🏗️ Data Architecture

### **Entity Relationships**
```
User (1) ──────────── (*) CartItem (*) ──────────── (1) Product
  │                                                      │
  │                                                      │
  └── (1) ──────────── (*) Order (*) ──────────── (1) ──┘
                          │
                          │
                          └── (1) ──────────── (*) OrderItem
```

### **Database Schema**
- **users**: User account information
- **products**: Product catalog
- **cart_items**: Shopping cart contents
- **orders**: Order headers
- **order_items**: Order line items

### **Key Design Principles**
- **Normalization**: Proper entity relationships
- **Data Integrity**: Foreign key constraints
- **Audit Trail**: Creation timestamps
- **Soft Deletes**: Product activation/deactivation
- **Price History**: Order items store price at time of purchase

---

## 🔧 Technology Foundation

### **Spring Boot Features**
- **Spring MVC**: Web application framework
- **Spring Data JPA**: Data access abstraction
- **Spring Security**: Authentication and authorization
- **Thymeleaf**: Server-side template engine
- **Bean Validation**: Input validation framework

### **Database Support**
- **H2**: In-memory development database
- **MySQL**: Production database support
- **JPA/Hibernate**: Object-relational mapping

### **Frontend Technologies**
- **Bootstrap**: Responsive CSS framework
- **Thymeleaf**: Dynamic HTML templates
- **JavaScript**: Client-side interactivity

---

**This completes the documentation of the core functions that form the foundation of the eCommerce Shopping Cart Application.**

*For enhanced features and advanced functionality, see [ENHANCED_FEATURES.md](ENHANCED_FEATURES.md)*

*For team collaboration guidelines, see [TEAM_COLLABORATION.md](TEAM_COLLABORATION.md)*