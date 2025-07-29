// src/main/java/org/bigear/api/bigearbackend/reviews/service/ReviewService.java
package org.bigear.api.bigearbackend.reviews.service;

import org.bigear.api.bigearbackend.reviews.model.Review;
import org.bigear.api.bigearbackend.reviews.repository.ReviewRepository;
import org.bigear.api.bigearbackend.dto.ReviewDTO; // IMPORT THE NEW DTO PACKAGE
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewDTO> getAllReviews() { // Change return type
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<ReviewDTO> getReviewById(Long id) { // Change return type
        return reviewRepository.findById(id)
                .map(this::convertToDto);
    }

    public List<ReviewDTO> getReviewsBySpringBedItemId(Long springBedItemId) { // Change return type
        List<Review> reviews = reviewRepository.findBySpringBedItem_Id(springBedItemId); // Corrected method name as per repository
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert Review entity to ReviewDTO
    private ReviewDTO convertToDto(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        // Handle lazy loaded fields - ensure you access them within the transaction context
        if (review.getSpringBedItem() != null) {
            dto.setItemId(review.getSpringBedItem().getId());
            dto.setItemName(review.getSpringBedItem().getName());
        }
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getName());
        }
        return dto;
    }
}