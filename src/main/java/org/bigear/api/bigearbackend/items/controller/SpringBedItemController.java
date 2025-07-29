// src/main/java/org/bigear/api/bigearbackend/items/controller/SpringBedItemController.java
package org.bigear.api.bigearbackend.items.controller;

import org.bigear.api.bigearbackend.dto.SpringBedItemDTO; // IMPORT SpringBedItemDTO
import org.bigear.api.bigearbackend.items.service.SpringBedItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class SpringBedItemController {

    private final SpringBedItemService springBedItemService;

    @Autowired
    public SpringBedItemController(SpringBedItemService springBedItemService) {
        this.springBedItemService = springBedItemService;
    }

    @GetMapping
    public List<SpringBedItemDTO> getAllItems() { // Change return type
        return springBedItemService.getAllSpringBedItemsWithReviews();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpringBedItemDTO> getItemById(@PathVariable Long id) { // Change return type
        return springBedItemService.getSpringBedItemByIdWithReviews(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add other CRUD operations as needed
}