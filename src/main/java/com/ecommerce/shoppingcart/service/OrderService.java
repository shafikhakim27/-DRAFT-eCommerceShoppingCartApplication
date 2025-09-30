package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.model.*;
import com.ecommerce.shoppingcart.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    
    public OrderService(OrderRepository orderRepository, CartService cartService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productService = productService;
    }
    
    @Transactional
    public Order createOrderFromCart(User user, String shippingAddress) {
        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        
        // Validate stock availability
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (!product.isActive() || product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Product " + product.getName() + " is not available or insufficient stock");
            }
        }
        
        // Create order
        BigDecimal totalAmount = cartService.getCartTotal(user);
        Order order = new Order(user, totalAmount);
        order.setShippingAddress(shippingAddress);
        
        // Create order items and update stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), product.getPrice());
            order.getOrderItems().add(orderItem);
            
            // Update stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productService.saveProduct(product);
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart
        cartService.clearCart(user);
        
        return savedOrder;
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        throw new IllegalArgumentException("Order not found");
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}