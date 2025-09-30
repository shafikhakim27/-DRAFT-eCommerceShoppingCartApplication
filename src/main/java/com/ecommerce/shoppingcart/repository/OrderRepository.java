package com.ecommerce.shoppingcart.repository;

import com.ecommerce.shoppingcart.model.Order;
import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o " +
           "JOIN o.orderItems oi " +
           "WHERE o.user = :user AND oi.product = :product AND o.status = :status")
    boolean existsByUserAndOrderItemsProductAndStatus(@Param("user") User user, 
                                                     @Param("product") Product product, 
                                                     @Param("status") Order.OrderStatus status);
}