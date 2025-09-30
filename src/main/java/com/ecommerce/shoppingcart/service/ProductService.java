package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }
    
    public Page<Product> getAllActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }
    
    public List<Product> getProductsByCategory(Product.Category category) {
        return productRepository.findByActiveTrueAndCategory(category);
    }
    
    public Page<Product> getProductsByCategory(Product.Category category, Pageable pageable) {
        return productRepository.findByActiveTrueAndCategory(category, pageable);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }
    
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable);
    }
    
    public Page<Product> getAllProductsIncludingInactive(Pageable pageable) {
        return productRepository.findAll(pageable);
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