package org.bigear.api.bigearbackend.reviews;

import org.bigear.api.bigearbackend.dto.ReviewDTO;
import org.bigear.api.bigearbackend.items.SpringBedItem;
import org.bigear.api.bigearbackend.items.SpringBedItemRepository;
import org.bigear.api.bigearbackend.users.User;
import org.bigear.api.bigearbackend.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SpringBedItemRepository springBedItemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         SpringBedItemRepository springBedItemRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.springBedItemRepository = springBedItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDTO> getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsBySpringBedItemId(Long springBedItemId) {
        List<Review> reviews = reviewRepository.findBySpringBedItem_Id(springBedItemId);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Review> reviews = reviewRepository.findByUser_Id(user.getId());
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO createReview(Long itemId, String userEmail, Double rating, String comment) {
        // Validate input
        if (itemId == null || userEmail == null || rating == null ||
                rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid review data");
        }

        // Check if item exists
        SpringBedItem item = springBedItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));

        // Check if user exists
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        // Check if user already reviewed this item
        Optional<Review> existingReview = reviewRepository.findBySpringBedItem_IdAndUser_Id(itemId, user.getId());
        if (existingReview.isPresent()) {
            throw new IllegalArgumentException("User has already reviewed this item. Use update instead.");
        }

        // Create new review
        Review review = new Review();
        review.setSpringBedItem(item);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : "");

        Review savedReview = reviewRepository.save(review);

        // Update item's average rating
        updateItemAverageRating(itemId);

        return convertToDto(savedReview);
    }

    @Transactional
    public ReviewDTO updateReview(Long reviewId, String userEmail, Double rating, String comment) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        // Check if the review belongs to the user
        if (!review.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to update this review");
        }

        // Update fields if provided
        if (rating != null && rating >= 1 && rating <= 5) {
            review.setRating(rating);
        }
        if (comment != null) {
            review.setComment(comment.trim());
        }

        Review savedReview = reviewRepository.save(review);

        // Update item's average rating
        updateItemAverageRating(review.getSpringBedItem().getId());

        return convertToDto(savedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        // Check if the review belongs to the user
        if (!review.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to delete this review");
        }

        Long itemId = review.getSpringBedItem().getId();
        reviewRepository.delete(review);

        // Update item's average rating
        updateItemAverageRating(itemId);
    }

    @Transactional
    public void updateItemAverageRating(Long itemId) {
        List<Review> reviews = reviewRepository.findBySpringBedItem_Id(itemId);

        double averageRating = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        SpringBedItem item = springBedItemRepository.findById(itemId).orElse(null);
        if (item != null) {
            item.setAverageRating(Math.round(averageRating * 10.0) / 10.0);
            springBedItemRepository.save(item);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasUserReviewedItem(String userEmail, Long itemId) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return false;
        }

        return reviewRepository.findBySpringBedItem_IdAndUser_Id(itemId, user.getId()).isPresent();
    }

    @Transactional(readOnly = true)
    public Optional<ReviewDTO> getUserReviewForItem(String userEmail, Long itemId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return reviewRepository.findBySpringBedItem_IdAndUser_Id(itemId, user.getId())
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingForItem(Long itemId) {
        List<Review> reviews = reviewRepository.findBySpringBedItem_Id(itemId);

        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public long getReviewCountForItem(Long itemId) {
        return reviewRepository.countBySpringBedItem_Id(itemId);
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
            dto.setUserEmail(review.getUser().getEmail()); // Add this field to ReviewDTO if needed
        }

        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }
}