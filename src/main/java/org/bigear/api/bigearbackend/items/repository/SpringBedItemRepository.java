package org.bigear.api.bigearbackend.items.repository;

import org.bigear.api.bigearbackend.items.model.SpringBedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringBedItemRepository extends JpaRepository<SpringBedItem, Long> {
}
