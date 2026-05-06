package io.stonk.auth.repository;

import io.stonk.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data-access layer for {@link User} credentials.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository}
 * plus domain-specific lookup methods used during registration and login.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}