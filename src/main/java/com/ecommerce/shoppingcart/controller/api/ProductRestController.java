package com.ecommerce.shoppingcart.controller.api;

import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductRestController {
    
    private final ProductService productService;
    
    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) Product.Category category,
            @RequestParam(required = false) String search) {
        
        List<Product> products;
        
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search.trim());
        } else if (category != null) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllActiveProducts();
        }
        
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/categories")
    public ResponseEntity<Product.Category[]> getCategories() {
        return ResponseEntity.ok(Product.Category.values());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Product.Category category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
}