package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.model.Order;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.service.CartService;
import com.ecommerce.shoppingcart.service.OrderService;
import com.ecommerce.shoppingcart.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/orders")
public class OrderController {
    
    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;
    
    public OrderController(OrderService orderService, CartService cartService, UserService userService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userService = userService;
    }
    
    @GetMapping("/checkout")
    public String checkoutPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        User user = userOpt.get();
        
        // Check if cart is empty
        if (cartService.getCartItems(user).isEmpty()) {
            return "redirect:/cart";
        }
        
        model.addAttribute("cartItems", cartService.getCartItems(user));
        model.addAttribute("total", cartService.getCartTotal(user));
        model.addAttribute("user", user);
        
        return "checkout";
    }
    
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String shippingAddress,
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
            Order order = orderService.createOrderFromCart(userOpt.get(), shippingAddress);
            redirectAttributes.addFlashAttribute("success", "Order created successfully! Please complete payment.");
            return "redirect:/payment/process/" + order.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/checkout";
        }
    }
    
    @GetMapping
    public String orderHistory(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        List<Order> orders = orderService.getUserOrders(userOpt.get());
        model.addAttribute("orders", orders);
        
        return "order-history";
    }
    
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty()) {
            return "redirect:/orders";
        }
        
        Order order = orderOpt.get();
        
        // Check if the order belongs to the current user
        if (!order.getUser().getId().equals(userOpt.get().getId())) {
            return "redirect:/orders";
        }
        
        model.addAttribute("order", order);
        
        return "order-detail";
    }
}