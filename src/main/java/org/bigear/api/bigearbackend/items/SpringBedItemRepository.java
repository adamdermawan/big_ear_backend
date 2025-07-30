package org.bigear.api.bigearbackend.items;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringBedItemRepository extends JpaRepository<SpringBedItem, Long> {
}
