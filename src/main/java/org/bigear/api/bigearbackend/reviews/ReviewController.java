package org.bigear.api.bigearbackend.reviews;

import org.bigear.api.bigearbackend.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewDTO> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/springbeditem/{springBedItemId}")
    public List<ReviewDTO> getReviewsBySpringBedItemId(@PathVariable Long springBedItemId) {
        return reviewService.getReviewsBySpringBedItemId(springBedItemId);
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody CreateReviewRequest request) {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body("Authentication required");
            }

            String userEmail = authentication.getName();

            // Validate request
            if (request.getItemId() == null || request.getRating() == null ||
                    request.getRating() < 1 || request.getRating() > 5) {
                return ResponseEntity.badRequest().body("Invalid review data");
            }

            ReviewDTO createdReview = reviewService.createReview(
                    request.getItemId(),
                    userEmail,
                    request.getRating(),
                    request.getComment()
            );

            return ResponseEntity.status(201).body(createdReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long id,
            @RequestBody CreateReviewRequest request) {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body("Authentication required");
            }

            String userEmail = authentication.getName();

            ReviewDTO updatedReview = reviewService.updateReview(
                    id,
                    userEmail,
                    request.getRating(),
                    request.getComment()
            );

            return ResponseEntity.ok(updatedReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(e.getMessage());
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.status(500).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).body("Authentication required");
            }

            String userEmail = authentication.getName();

            reviewService.deleteReview(id, userEmail);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(e.getMessage());
            } else if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(403).body(e.getMessage());
            }
            return ResponseEntity.status(500).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/user/my-reviews")
    public ResponseEntity<List<ReviewDTO>> getCurrentUserReviews() {
        try {
            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).build();
            }

            String userEmail = authentication.getName();
            List<ReviewDTO> userReviews = reviewService.getReviewsByUserEmail(userEmail);
            return ResponseEntity.ok(userReviews);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // DTO class for creating/updating reviews
    public static class CreateReviewRequest {
        private Long itemId;
        private Double rating;
        private String comment;

        public CreateReviewRequest() {}

        public CreateReviewRequest(Long itemId, Double rating, String comment) {
            this.itemId = itemId;
            this.rating = rating;
            this.comment = comment;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public Double getRating() {
            return rating;
        }

        public void setRating(Double rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}