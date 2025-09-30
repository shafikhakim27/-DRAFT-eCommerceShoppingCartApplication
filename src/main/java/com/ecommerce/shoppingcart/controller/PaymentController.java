package com.ecommerce.shoppingcart.controller;

import com.ecommerce.shoppingcart.model.Order;
import com.ecommerce.shoppingcart.model.Payment;
import com.ecommerce.shoppingcart.service.OrderService;
import com.ecommerce.shoppingcart.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    
    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }
    
    @GetMapping("/process/{orderId}")
    public String showPaymentPage(@PathVariable Long orderId, Model model, Authentication authentication) {
        try {
            Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Verify order belongs to current user
            if (!order.getUser().getUsername().equals(authentication.getName())) {
                return "redirect:/orders?error=unauthorized";
            }
            
            // Check if order is in pending status
            if (order.getStatus() != Order.OrderStatus.PENDING) {
                return "redirect:/orders?error=invalid_status";
            }
            
            model.addAttribute("order", order);
            model.addAttribute("paymentMethods", Payment.PaymentMethod.values());
            
            return "payment";
        } catch (Exception e) {
            return "redirect:/orders?error=order_not_found";
        }
    }
    
    @PostMapping("/process/{orderId}")
    public String processPayment(@PathVariable Long orderId,
                               @RequestParam("paymentMethod") Payment.PaymentMethod paymentMethod,
                               @RequestParam("paymentDetails") String paymentDetails,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Verify order belongs to current user
            if (!order.getUser().getUsername().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access to order");
                return "redirect:/orders";
            }
            
            // Process payment
            Payment payment = paymentService.processPayment(order, paymentMethod, paymentDetails);
            
            // Update order status based on payment result
            if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
                orderService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);
                redirectAttributes.addFlashAttribute("success", 
                    "Payment successful! Transaction ID: " + payment.getTransactionId());
                return "redirect:/orders/" + orderId;
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Payment failed: " + payment.getGatewayResponse());
                return "redirect:/payment/process/" + orderId;
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment processing failed: " + e.getMessage());
            return "redirect:/payment/process/" + orderId;
        }
    }
    
    @GetMapping("/status/{transactionId}")
    public String checkPaymentStatus(@PathVariable String transactionId, Model model) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            model.addAttribute("payment", payment);
            return "payment-status";
        } catch (Exception e) {
            model.addAttribute("error", "Payment not found");
            return "payment-status";
        }
    }
}