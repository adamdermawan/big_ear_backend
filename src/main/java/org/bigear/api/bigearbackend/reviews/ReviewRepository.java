package org.bigear.api.bigearbackend.reviews;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds a list of reviews associated with a specific SpringBedItem ID.
     */
    List<Review> findBySpringBedItem_Id(Long springBedItemId);

    /**
     * Finds a list of reviews associated with a specific User ID.
     */
    List<Review> findByUser_Id(Long userId);

    /**
     * Finds a specific review by SpringBedItem ID and User ID.
     * Useful for checking if a user has already reviewed an item.
     */
    Optional<Review> findBySpringBedItem_IdAndUser_Id(Long springBedItemId, Long userId);

    /**
     * Counts the number of reviews for a specific SpringBedItem.
     */
    long countBySpringBedItem_Id(Long springBedItemId);

    /**
     * Finds reviews by user email (through the User relationship).
     */
    List<Review> findByUser_Email(String userEmail);

    /**
     * Deletes all reviews for a specific SpringBedItem.
     * Useful when deleting an item.
     */
    void deleteBySpringBedItem_Id(Long springBedItemId);

    /**
     * Deletes all reviews by a specific user.
     * Useful when deleting a user account.
     */
    void deleteByUser_Id(Long userId);
}