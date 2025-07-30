package org.bigear.api.bigearbackend.items;

import org.bigear.api.bigearbackend.reviews.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Table(name = "spring_bed_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpringBedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_asset")
    private String imageAsset;

    @Column(name = "average_rating")
    private Double averageRating;

    @OneToMany(mappedBy = "springBedItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews; // This will be populated when fetching a product with its reviews
}
