package io.stonk.user.repository;

import io.stonk.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data-access layer for {@link User} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD operations.
 * All additional query methods follow Spring Data JPA naming conventions.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a username is already taken.
     *
     * @param username the username to check
     * @return {@code true} if taken
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email is already taken.
     *
     * @param email the email to check
     * @return {@code true} if taken
     */
    boolean existsByEmail(String email);
}