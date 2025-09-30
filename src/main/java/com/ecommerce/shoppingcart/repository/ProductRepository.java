package com.ecommerce.shoppingcart.repository;

import com.ecommerce.shoppingcart.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByActiveTrue();
    
    Page<Product> findByActiveTrue(Pageable pageable);
    
    List<Product> findByCategory(Product.Category category);
    
    Page<Product> findByCategory(Product.Category category, Pageable pageable);
    
    List<Product> findByActiveTrueAndCategory(Product.Category category);
    
    Page<Product> findByActiveTrueAndCategory(Product.Category category, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
}