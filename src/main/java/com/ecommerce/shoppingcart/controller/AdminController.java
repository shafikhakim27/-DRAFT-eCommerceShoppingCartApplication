package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.model.Order;
import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.OrderService;
import com.ecommerce.shoppingcart.service.ProductService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    
    public AdminController(ProductService productService, UserService userService, 
                          OrderService orderService) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
    }
    
    // Check admin access
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        return userOpt.isPresent() && userOpt.get().getRole() == User.Role.ADMIN;
    }
    
    @GetMapping
    public String adminDashboard(Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        // Dashboard statistics
        model.addAttribute("totalProducts", productService.getAllActiveProducts().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        model.addAttribute("pendingOrders", orderService.getOrdersByStatus(Order.OrderStatus.PENDING).size());
        
        // Recent orders
        List<Order> recentOrders = orderService.getAllOrders().stream()
            .limit(5)
            .toList();
        model.addAttribute("recentOrders", recentOrders);
        
        return "admin/dashboard";
    }
    
    // Product Management
    @GetMapping("/products")
    public String manageProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            Model model, Authentication authentication) {
        
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage;
        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchProducts(search.trim(), pageable);
            model.addAttribute("searchKeyword", search);
        } else {
            productPage = productService.getAllProductsIncludingInactive(pageable);
        }
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "admin/products";
    }
    
    @GetMapping("/products/add")
    public String showAddProductForm(Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Product.Category.values());
        return "admin/add-product";
    }
    
    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute Product product, 
                           BindingResult result, 
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fix the errors in the form");
            return "redirect:/admin/products/add";
        }
        
        try {
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/admin/products?error=product_not_found";
        }
        
        model.addAttribute("product", productOpt.get());
        model.addAttribute("categories", Product.Category.values());
        return "admin/edit-product";
    }
    
    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id,
                              @Valid @ModelAttribute Product product,
                              BindingResult result,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fix the errors in the form");
            return "redirect:/admin/products/edit/" + id;
        }
        
        try {
            product.setId(id);
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/toggle-status/{id}")
    public String toggleProductStatus(@PathVariable Long id,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setActive(!product.isActive());
                productService.saveProduct(product);
                
                String status = product.isActive() ? "activated" : "deactivated";
                redirectAttributes.addFlashAttribute("success", "Product " + status + " successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Product not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating product status: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/update-stock/{id}")
    public String updateProductStock(@PathVariable Long id,
                                   @RequestParam Integer stockQuantity,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setStockQuantity(stockQuantity);
                productService.saveProduct(product);
                redirectAttributes.addFlashAttribute("success", "Stock updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Product not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating stock: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }
    
    // Order Management
    @GetMapping("/orders")
    public String manageOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Order.OrderStatus status,
            Model model, Authentication authentication) {
        
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        
        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderService.getOrdersByStatus(status, pageable);
            model.addAttribute("selectedStatus", status);
        } else {
            orderPage = orderService.getAllOrders(pageable);
        }
        
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("size", size);
        
        return "admin/orders";
    }
    
    @PostMapping("/orders/update-status/{id}")
    public String updateOrderStatus(@PathVariable Long id,
                                  @RequestParam Order.OrderStatus status,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        
        if (!isAdmin(authentication)) {
            return "redirect:/products?error=access_denied";
        }
        
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating order status: " + e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
}