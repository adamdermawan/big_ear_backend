package org.bigear.api.bigearbackend.users.controller;

import org.bigear.api.bigearbackend.users.model.User;
import org.bigear.api.bigearbackend.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users") // Base URL for user-related endpoints
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a list of all users.
     * Accessible via GET request to /api/users
     * @return A list of User objects.
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Retrieves a specific user by their ID.
     * Accessible via GET request to /api/users/{id}
     * @param id The ID of the user to retrieve.
     * @return ResponseEntity containing the User object if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok) // If user is found, return 200 OK with the user
                .orElse(ResponseEntity.notFound().build()); // If not found, return 404 Not Found
    }

    // You can add more CRUD (Create, Update, Delete) methods here if your Flutter app needs them:

    /*
    @PostMapping // For creating a new user
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @PutMapping("/{id}") // For updating an existing user
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userService.getUserById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    // ... update other fields as necessary
                    User updatedUser = userService.saveUser(user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}") // For deleting a user
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.getUserById(id).isPresent()) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found if user doesn't exist
        }
    }
    */
}
