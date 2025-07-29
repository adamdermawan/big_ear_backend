// src/main/java/org/bigear/api/bigearbackend/reviews/controller/ReviewController.java
package org.bigear.api.bigearbackend.reviews.controller;

import org.bigear.api.bigearbackend.dto.ReviewDTO; // IMPORT THE NEW DTO PACKAGE
import org.bigear.api.bigearbackend.reviews.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewDTO> getAllReviews() { // Change return type
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) { // Change return type
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/springbeditem/{springBedItemId}")
    public List<ReviewDTO> getReviewsBySpringBedItemId(@PathVariable Long springBedItemId) { // Change return type
        return reviewService.getReviewsBySpringBedItemId(springBedItemId);
    }

    // Add methods for POST, PUT, DELETE if needed by your Flutter app for reviews
}