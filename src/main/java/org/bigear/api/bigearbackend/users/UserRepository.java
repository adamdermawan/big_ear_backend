package org.bigear.api.bigearbackend.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * This is commonly used for authentication and user lookup.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     * Useful for registration validation.
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their name.
     * Optional method for user search functionality.
     */
    Optional<User> findByName(String name);
}