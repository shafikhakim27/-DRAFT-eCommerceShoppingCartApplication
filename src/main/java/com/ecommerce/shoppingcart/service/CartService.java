package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.model.CartItem;
import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import com.ecommerce.shoppingcart.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    
    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }
    
    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }
    
    @Transactional
    public CartItem addToCart(User user, Long productId, Integer quantity) {
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found");
        }
        
        Product product = productOpt.get();
        if (!product.isActive() || product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Product not available or insufficient stock");
        }
        
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(user, product, quantity);
            return cartItemRepository.save(newItem);
        }
    }
    
    @Transactional
    public void updateCartItem(User user, Long cartItemId, Integer quantity) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (item.getUser().getId().equals(user.getId())) {
                if (quantity <= 0) {
                    cartItemRepository.delete(item);
                } else {
                    item.setQuantity(quantity);
                    cartItemRepository.save(item);
                }
            }
        }
    }
    
    @Transactional
    public void removeFromCart(User user, Long cartItemId) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            if (item.getUser().getId().equals(user.getId())) {
                cartItemRepository.delete(item);
            }
        }
    }
    
    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
    
    public BigDecimal getCartTotal(User user) {
        List<CartItem> items = getCartItems(user);
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getCartItemCount(User user) {
        List<CartItem> items = getCartItems(user);
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}