// src/main/java/org/bigear/api/bigearbackend/dto/SpringBedItemDTO.java
package org.bigear.api.bigearbackend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpringBedItemDTO {
    private Long id;
    private String name;
    private String description;
    private String imageAsset;
    private Double rate; // This will hold the calculated average rating
    private List<ReviewDTO> reviews; // List of ReviewDTOs
}