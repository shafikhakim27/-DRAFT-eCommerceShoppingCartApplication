package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.Review;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.ProductService;
import com.ecommerce.shoppingcart.service.ReviewService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    
    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserService userService;
    
    public ReviewController(ReviewService reviewService, ProductService productService, UserService userService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.userService = userService;
    }
    
    @GetMapping("/add/{productId}")
    public String showAddReviewForm(@PathVariable Long productId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Product product = productOpt.get();
        User user = userOpt.get();
        
        // Check if user already reviewed this product
        if (reviewService.hasUserReviewedProduct(product, user)) {
            return "redirect:/products/" + productId + "?error=already_reviewed";
        }
        
        model.addAttribute("product", product);
        model.addAttribute("review", new Review());
        model.addAttribute("canReview", reviewService.canUserReviewProduct(product, user));
        
        return "add-review";
    }
    
    @PostMapping("/add/{productId}")
    public String addReview(@PathVariable Long productId,
                           @Valid @ModelAttribute Review review,
                           BindingResult result,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product not found");
            return "redirect:/products";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fix the errors in your review");
            return "redirect:/reviews/add/" + productId;
        }
        
        try {
            Product product = productOpt.get();
            User user = userOpt.get();
            
            reviewService.addReview(product, user, review.getRating(), review.getReviewText());
            
            redirectAttributes.addFlashAttribute("success", "Review added successfully!");
            return "redirect:/products/" + productId;
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviews/add/" + productId;
        }
    }
    
    @GetMapping("/edit/{reviewId}")
    public String showEditReviewForm(@PathVariable Long reviewId, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            Review review = reviewService.getUserReviews(userOpt.get()).stream()
                .filter(r -> r.getId().equals(reviewId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Review not found or access denied"));
            
            model.addAttribute("review", review);
            model.addAttribute("product", review.getProduct());
            
            return "edit-review";
        } catch (Exception e) {
            return "redirect:/reviews/my-reviews?error=review_not_found";
        }
    }
    
    @PostMapping("/edit/{reviewId}")
    public String updateReview(@PathVariable Long reviewId,
                              @RequestParam Integer rating,
                              @RequestParam String reviewText,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            Review updatedReview = reviewService.updateReview(reviewId, rating, reviewText, userOpt.get());
            redirectAttributes.addFlashAttribute("success", "Review updated successfully!");
            return "redirect:/products/" + updatedReview.getProduct().getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviews/edit/" + reviewId;
        }
    }
    
    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            reviewService.deleteReview(reviewId, userOpt.get());
            redirectAttributes.addFlashAttribute("success", "Review deleted successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/reviews/my-reviews";
    }
    
    @GetMapping("/my-reviews")
    public String myReviews(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        model.addAttribute("reviews", reviewService.getUserReviews(userOpt.get()));
        return "my-reviews";
    }
    
    @PostMapping("/helpful/{reviewId}")
    @ResponseBody
    public String markHelpful(@PathVariable Long reviewId) {
        try {
            Review review = reviewService.markReviewHelpful(reviewId);
            return String.valueOf(review.getHelpfulCount());
        } catch (Exception e) {
            return "error";
        }
    }
}