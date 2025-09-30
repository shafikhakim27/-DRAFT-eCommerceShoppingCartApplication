package com.ecommerce.shoppingcart.repository;

import com.ecommerce.shoppingcart.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByOrderId(Long orderId);
    
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByPaymentMethod(Payment.PaymentMethod method);
}