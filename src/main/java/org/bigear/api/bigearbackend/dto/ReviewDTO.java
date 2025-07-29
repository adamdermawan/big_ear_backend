// src/main/java/org/bigear/api/bigearbackend/dto/ReviewDTO.java
package org.bigear.api.bigearbackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
    private Double rating;
    private String comment;
}