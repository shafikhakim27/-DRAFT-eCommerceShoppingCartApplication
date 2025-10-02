package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.CartService;
import com.ecommerce.shoppingcart.service.ProductService;
import com.ecommerce.shoppingcart.service.ReviewService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ProductController {
    
    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;
    private final ReviewService reviewService;
    
    public ProductController(ProductService productService, CartService cartService, UserService userService, ReviewService reviewService) {
        this.productService = productService;
        this.cartService = cartService;
        this.userService = userService;
        this.reviewService = reviewService;
    }
    
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) Product.Category category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model,
            Authentication authentication) {
        
        // Create pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> productPage;
        
        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchProducts(search.trim(), pageable);
            model.addAttribute("searchKeyword", search);
        } else if (category != null) {
            productPage = productService.getProductsByCategory(category, pageable);
            model.addAttribute("selectedCategory", category);
        } else {
            productPage = productService.getAllActiveProducts(pageable);
        }
        
        model.addAttribute("productPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", Product.Category.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Calculate page numbers for pagination
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(productPage.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        
        // Add cart count for authenticated users
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                int cartCount = cartService.getCartItemCount(userOpt.get());
                model.addAttribute("cartCount", cartCount);
            }
        }
        
        return "products";
    }
    
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }
        
        model.addAttribute("product", productOpt.get());
        
        // Add review information
        Product product = productOpt.get();
        model.addAttribute("reviews", reviewService.getProductReviews(product));
        model.addAttribute("reviewStats", reviewService.getReviewStats(product));
        
        // Check if user can review this product
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                int cartCount = cartService.getCartItemCount(user);
                model.addAttribute("cartCount", cartCount);
                model.addAttribute("canReview", reviewService.canUserReviewProduct(product, user));
                model.addAttribute("hasReviewed", reviewService.hasUserReviewedProduct(product, user));
                model.addAttribute("userReview", reviewService.getUserReviewForProduct(product, user).orElse(null));
            }
        }
        
        return "product-detail";
    }
    
    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            redirectAttributes.addFlashAttribute("error", "Please log in to add items to cart");
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/login";
        }
        
        try {
            cartService.addToCart(userOpt.get(), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Product added to cart successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/products/" + productId;
    }
}