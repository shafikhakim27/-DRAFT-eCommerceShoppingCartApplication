package com.ecommerce.shoppingcart.service;

import com.ecommerce.shoppingcart.model.*;
import com.ecommerce.shoppingcart.repository.ReviewRepository;
import com.ecommerce.shoppingcart.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    
    public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }
    
    /**
     * Add a new review for a product
     */
    public Review addReview(Product product, User user, Integer rating, String reviewText) {
        // Check if user already reviewed this product
        if (reviewRepository.existsByProductAndUser(product, user)) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }
        
        Review review = new Review(product, user, rating, reviewText);
        
        // Check if this is a verified purchase
        boolean hasOrderedProduct = orderRepository.existsByUserAndOrderItemsProductAndStatus(
            user, product, Order.OrderStatus.CONFIRMED);
        review.setVerifiedPurchase(hasOrderedProduct);
        
        return reviewRepository.save(review);
    }
    
    /**
     * Update an existing review
     */
    public Review updateReview(Long reviewId, Integer rating, String reviewText, User user) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Check if the review belongs to the user
        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own reviews");
        }
        
        review.setRating(rating);
        review.setReviewText(reviewText);
        
        return reviewRepository.save(review);
    }
    
    /**
     * Delete a review
     */
    public void deleteReview(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Check if the review belongs to the user or user is admin
        if (!review.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("You can only delete your own reviews");
        }
        
        reviewRepository.delete(review);
    }
    
    /**
     * Get all reviews for a product
     */
    public List<Review> getProductReviews(Product product) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }
    
    /**
     * Get paginated reviews for a product
     */
    public Page<Review> getProductReviews(Product product, Pageable pageable) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product, pageable);
    }
    
    /**
     * Get reviews by a user
     */
    public List<Review> getUserReviews(User user) {
        return reviewRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get user's review for a specific product
     */
    public Optional<Review> getUserReviewForProduct(Product product, User user) {
        return reviewRepository.findByProductAndUser(product, user);
    }
    
    /**
     * Check if user has reviewed a product
     */
    public boolean hasUserReviewedProduct(Product product, User user) {
        return reviewRepository.existsByProductAndUser(product, user);
    }
    
    /**
     * Get average rating for a product
     */
    public Double getAverageRating(Product product) {
        Double average = reviewRepository.findAverageRatingByProduct(product);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }
    
    /**
     * Get review count for a product
     */
    public Long getReviewCount(Product product) {
        return reviewRepository.countByProduct(product);
    }
    
    /**
     * Get rating distribution for a product
     */
    public ReviewStats getReviewStats(Product product) {
        Long totalReviews = getReviewCount(product);
        Double averageRating = getAverageRating(product);
        
        ReviewStats stats = new ReviewStats();
        stats.setTotalReviews(totalReviews);
        stats.setAverageRating(averageRating);
        
        // Get count for each rating
        for (int i = 1; i <= 5; i++) {
            Long count = reviewRepository.countByProductAndRating(product, i);
            switch (i) {
                case 1: stats.setOneStarCount(count); break;
                case 2: stats.setTwoStarCount(count); break;
                case 3: stats.setThreeStarCount(count); break;
                case 4: stats.setFourStarCount(count); break;
                case 5: stats.setFiveStarCount(count); break;
            }
        }
        
        return stats;
    }
    
    /**
     * Mark a review as helpful
     */
    public Review markReviewHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        return reviewRepository.save(review);
    }
    
    /**
     * Check if user can review a product (has purchased it)
     */
    public boolean canUserReviewProduct(Product product, User user) {
        return orderRepository.existsByUserAndOrderItemsProductAndStatus(
            user, product, Order.OrderStatus.CONFIRMED);
    }
    
    // Inner class for review statistics
    public static class ReviewStats {
        private Long totalReviews = 0L;
        private Double averageRating = 0.0;
        private Long fiveStarCount = 0L;
        private Long fourStarCount = 0L;
        private Long threeStarCount = 0L;
        private Long twoStarCount = 0L;
        private Long oneStarCount = 0L;
        
        // Getters and setters
        public Long getTotalReviews() { return totalReviews; }
        public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
        
        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
        
        public Long getFiveStarCount() { return fiveStarCount; }
        public void setFiveStarCount(Long fiveStarCount) { this.fiveStarCount = fiveStarCount; }
        
        public Long getFourStarCount() { return fourStarCount; }
        public void setFourStarCount(Long fourStarCount) { this.fourStarCount = fourStarCount; }
        
        public Long getThreeStarCount() { return threeStarCount; }
        public void setThreeStarCount(Long threeStarCount) { this.threeStarCount = threeStarCount; }
        
        public Long getTwoStarCount() { return twoStarCount; }
        public void setTwoStarCount(Long twoStarCount) { this.twoStarCount = twoStarCount; }
        
        public Long getOneStarCount() { return oneStarCount; }
        public void setOneStarCount(Long oneStarCount) { this.oneStarCount = oneStarCount; }
        
        public Double getPercentage(Long count) {
            return totalReviews > 0 ? (count.doubleValue() / totalReviews) * 100 : 0.0;
        }
    }
}