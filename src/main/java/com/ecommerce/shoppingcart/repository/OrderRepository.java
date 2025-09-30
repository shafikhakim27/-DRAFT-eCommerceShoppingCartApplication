package com.ecommerce.shoppingcart.repository;

import com.ecommerce.shoppingcart.model.Order;
import com.ecommerce.shoppingcart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    List<Order> findByStatus(Order.OrderStatus status);
}