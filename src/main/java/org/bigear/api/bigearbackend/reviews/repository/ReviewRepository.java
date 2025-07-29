package org.bigear.api.bigearbackend.reviews.repository;

import org.bigear.api.bigearbackend.reviews.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * Finds a list of reviews associated with a specific SpringBedItem ID.
     * The method name 'findBySpringBedItem_Id' follows Spring Data JPA conventions:
     * 'findBy' + 'SpringBedItem' (the field name in the Review entity) + '_Id' (to access its ID).
     *
     * @param springBedItemId The ID of the SpringBedItem to find reviews for.
     * @return A list of Review objects associated with the given SpringBedItem ID.
     */
    List<Review> findBySpringBedItem_Id(Long springBedItemId);
}