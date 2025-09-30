package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.model.Order;
import com.ecommerce.shoppingcart.model.Payment;
import com.ecommerce.shoppingcart.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();
    
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }
    
    /**
     * Process payment for an order
     */
    public Payment processPayment(Order order, Payment.PaymentMethod paymentMethod, 
                                 String paymentDetails) {
        
        Payment payment = new Payment(order, order.getTotalAmount(), paymentMethod);
        payment.setTransactionId(generateTransactionId());
        
        // Simulate payment processing based on method
        switch (paymentMethod) {
            case DIGITAL_WALLET:
                processDigitalWalletPayment(payment, paymentDetails);
                break;
            case PAYPAL:
                processPayPalPayment(payment, paymentDetails);
                break;
            case APPLE_PAY:
                processApplePayPayment(payment, paymentDetails);
                break;
            case GOOGLE_PAY:
                processGooglePayPayment(payment, paymentDetails);
                break;
            case CREDIT_CARD:
            case DEBIT_CARD:
                processCardPayment(payment, paymentDetails);
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Simulate digital wallet payment processing
     */
    private void processDigitalWalletPayment(Payment payment, String walletDetails) {
        // Parse wallet details (format: "walletType:accountId")
        String[] parts = walletDetails.split(":");
        if (parts.length == 2) {
            payment.setWalletType(parts[0]);
            payment.setWalletAccount(maskAccount(parts[1]));
        }
        
        // Simulate processing delay and random success/failure
        simulateProcessing(payment);
        
        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
            payment.setGatewayResponse("Digital wallet payment successful via " + payment.getWalletType());
        } else {
            payment.setGatewayResponse("Digital wallet payment failed - insufficient funds");
        }
    }
    
    /**
     * Simulate PayPal payment processing
     */
    private void processPayPalPayment(Payment payment, String paypalEmail) {
        payment.setWalletType("PayPal");
        payment.setWalletAccount(maskEmail(paypalEmail));
        
        simulateProcessing(payment);
        
        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
            payment.setGatewayResponse("PayPal payment successful");
        } else {
            payment.setGatewayResponse("PayPal payment failed - account verification required");
        }
    }
    
    /**
     * Simulate Apple Pay payment processing
     */
    private void processApplePayPayment(Payment payment, String deviceId) {
        payment.setWalletType("Apple Pay");
        payment.setWalletAccount(maskAccount(deviceId));
        
        simulateProcessing(payment);
        
        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
            payment.setGatewayResponse("Apple Pay payment successful - Touch ID verified");
        } else {
            payment.setGatewayResponse("Apple Pay payment failed - authentication failed");
        }
    }
    
    /**
     * Simulate Google Pay payment processing
     */
    private void processGooglePayPayment(Payment payment, String deviceId) {
        payment.setWalletType("Google Pay");
        payment.setWalletAccount(maskAccount(deviceId));
        
        simulateProcessing(payment);
        
        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
            payment.setGatewayResponse("Google Pay payment successful - Fingerprint verified");
        } else {
            payment.setGatewayResponse("Google Pay payment failed - network error");
        }
    }
    
    /**
     * Simulate card payment processing
     */
    private void processCardPayment(Payment payment, String cardDetails) {
        // Parse card details (format: "cardNumber:expiryMonth:expiryYear:cvv")
        String[] parts = cardDetails.split(":");
        if (parts.length >= 1) {
            String cardNumber = parts[0];
            payment.setCardLastFour(getLastFour(cardNumber));
            payment.setCardType(detectCardType(cardNumber));
        }
        
        simulateProcessing(payment);
        
        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
            payment.setGatewayResponse("Card payment successful - Authorization: " + generateAuthCode());
        } else {
            payment.setGatewayResponse("Card payment failed - declined by issuer");
        }
    }
    
    /**
     * Simulate payment processing with random success/failure
     */
    private void simulateProcessing(Payment payment) {
        payment.setPaymentStatus(Payment.PaymentStatus.PROCESSING);
        
        // Simulate 90% success rate
        boolean success = random.nextDouble() < 0.9;
        
        if (success) {
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        } else {
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        }
        
        payment.setProcessedAt(LocalDateTime.now());
    }
    
    /**
     * Get payment by order ID
     */
    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    /**
     * Get payment by transaction ID
     */
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    /**
     * Get all payments by status
     */
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }
    
    /**
     * Refund a payment
     */
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getPaymentStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Can only refund completed payments");
        }
        
        payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
        payment.setGatewayResponse("Payment refunded successfully");
        
        return paymentRepository.save(payment);
    }
    
    // Utility methods
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateAuthCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    private String maskAccount(String account) {
        if (account == null || account.length() <= 4) {
            return account;
        }
        return "*".repeat(account.length() - 4) + account.substring(account.length() - 4);
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        if (username.length() <= 2) {
            return email;
        }
        return username.substring(0, 2) + "*".repeat(username.length() - 2) + "@" + parts[1];
    }
    
    private String getLastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
    
    private String detectCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "Unknown";
        }
        
        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        
        if (cleaned.startsWith("4")) {
            return "Visa";
        } else if (cleaned.startsWith("5") || cleaned.startsWith("2")) {
            return "Mastercard";
        } else if (cleaned.startsWith("3")) {
            return "American Express";
        } else if (cleaned.startsWith("6")) {
            return "Discover";
        } else {
            return "Unknown";
        }
    }
}