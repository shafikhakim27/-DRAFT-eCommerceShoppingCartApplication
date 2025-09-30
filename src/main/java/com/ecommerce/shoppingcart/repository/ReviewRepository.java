package com.ecommerce.shoppingcart.repository;

import com.ecommerce.shoppingcart.model.Review;
import com.ecommerce.shoppingcart.model.Product;
import com.ecommerce.shoppingcart.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    
    Page<Review> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);
    
    List<Review> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<Review> findByProductAndUser(Product product, User user);
    
    boolean existsByProductAndUser(Product product, User user);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(@Param("product") Product product);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product")
    Long countByProduct(@Param("product") Product product);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product AND r.rating = :rating")
    Long countByProductAndRating(@Param("product") Product product, @Param("rating") Integer rating);
    
    List<Review> findByRatingOrderByCreatedAtDesc(Integer rating);
    
    List<Review> findByVerifiedPurchaseOrderByCreatedAtDesc(Boolean verifiedPurchase);
}