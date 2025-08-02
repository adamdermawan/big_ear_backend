package org.bigear.api.bigearbackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long itemId; // ID of the associated SpringBedItem
    private String itemName; // Name of the associated SpringBedItem
    private Long userId; // ID of the associated User
    private String userName; // Name of the associated User
    private String userEmail; // Email of the associated User (added for authentication)
    private Double rating;
    private String comment;
    private LocalDateTime createdAt; // Optional: if you want to track creation time
    private LocalDateTime updatedAt; // Optional: if you want to track last update time
}