// src/main/java/org/bigear/api/bigearbackend/items/service/SpringBedItemService.java
package org.bigear.api.bigearbackend.items;

import org.bigear.api.bigearbackend.reviews.Review; // Needed for the 'Review' entity
import org.bigear.api.bigearbackend.dto.SpringBedItemDTO; // IMPORT SpringBedItemDTO
import org.bigear.api.bigearbackend.dto.ReviewDTO; // IMPORT ReviewDTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpringBedItemService {
    private final SpringBedItemRepository springBedItemRepository;

    @Autowired
    public SpringBedItemService(SpringBedItemRepository springBedItemRepository) {
        this.springBedItemRepository = springBedItemRepository;
    }

    public List<SpringBedItemDTO> getAllSpringBedItemsWithReviews() { // Change return type
        List<SpringBedItem> items = springBedItemRepository.findAll();
        return items.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<SpringBedItemDTO> getSpringBedItemByIdWithReviews(Long id) { // Change return type
        return springBedItemRepository.findById(id)
                .map(this::convertToDto);
    }

    // Helper method to convert SpringBedItem entity to SpringBedItemDTO
    private SpringBedItemDTO convertToDto(SpringBedItem item) {
        SpringBedItemDTO dto = new SpringBedItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setImageAsset(item.getImageAsset());

        // Calculate average rating
        if (item.getReviews() != null && !item.getReviews().isEmpty()) {
            double averageRating = item.getReviews().stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
            dto.setRate(Math.round(averageRating * 10.0) / 10.0); // Round to one decimal place
        } else {
            dto.setRate(0.0);
        }

        // Convert List<Review> to List<ReviewDTO>
        if (item.getReviews() != null) {
            List<ReviewDTO> reviewDTOs = item.getReviews().stream()
                    .map(review -> {
                        ReviewDTO reviewDto = new ReviewDTO();
                        reviewDto.setId(review.getId());
                        reviewDto.setRating(review.getRating());
                        reviewDto.setComment(review.getComment());
                        // Populate user and item details in review DTO
                        if (review.getUser() != null) {
                            reviewDto.setUserId(review.getUser().getId());
                            reviewDto.setUserName(review.getUser().getName());
                        }
                        if (review.getSpringBedItem() != null) {
                            reviewDto.setItemId(review.getSpringBedItem().getId());
                            reviewDto.setItemName(review.getSpringBedItem().getName());
                        }
                        return reviewDto;
                    })
                    .collect(Collectors.toList());
            dto.setReviews(reviewDTOs);
        }

        return dto;
    }
}