package com.ecommerce.shoppingcart.repository;

import com.ecommerce.shoppingcart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByActiveTrue();
    
    List<Product> findByCategory(Product.Category category);
    
    List<Product> findByActiveTrueAndCategory(Product.Category category);
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
}