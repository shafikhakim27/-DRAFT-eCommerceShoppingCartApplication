package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }
    
    public List<Product> getProductsByCategory(Product.Category category) {
        return productRepository.findByActiveTrueAndCategory(category);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            p.setActive(false);
            productRepository.save(p);
        }
    }
    
    public List<Product> getInStockProducts() {
        return productRepository.findByStockQuantityGreaterThan(0);
    }
}