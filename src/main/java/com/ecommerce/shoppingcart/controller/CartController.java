package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.model.CartItem;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.CartService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    private final CartService cartService;
    private final UserService userService;
    
    @Autowired
    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }
    
    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        List<CartItem> cartItems = cartService.getCartItems(user);
        BigDecimal total = cartService.getCartTotal(user);
        int cartCount = cartService.getCartItemCount(user);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartCount);
        
        return "cart";
    }
    
    @PostMapping("/update")
    public String updateCartItem(
            @RequestParam Long cartItemId,
            @RequestParam Integer quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            cartService.updateCartItem(userOpt.get(), cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update cart");
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam Long cartItemId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            cartService.removeFromCart(userOpt.get(), cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove item");
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        try {
            cartService.clearCart(userOpt.get());
            redirectAttributes.addFlashAttribute("success", "Cart cleared successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to clear cart");
        }
        
        return "redirect:/cart";
    }
}