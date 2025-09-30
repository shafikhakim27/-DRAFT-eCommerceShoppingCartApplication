package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.CartService;
import com.ecommerce.shoppingcart.service.ProductService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {
    
    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;
    
    @Autowired
    public ProductController(ProductService productService, CartService cartService, UserService userService) {
        this.productService = productService;
        this.cartService = cartService;
        this.userService = userService;
    }
    
    @GetMapping({"/", "/products"})
    public String listProducts(
            @RequestParam(required = false) Product.Category category,
            @RequestParam(required = false) String search,
            Model model,
            Authentication authentication) {
        
        List<Product> products;
        
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search.trim());
            model.addAttribute("searchKeyword", search);
        } else if (category != null) {
            products = productService.getProductsByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            products = productService.getAllActiveProducts();
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", Product.Category.values());
        
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
        
        // Add cart count for authenticated users
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userService.findByUsername(authentication.getName());
            if (userOpt.isPresent()) {
                int cartCount = cartService.getCartItemCount(userOpt.get());
                model.addAttribute("cartCount", cartCount);
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