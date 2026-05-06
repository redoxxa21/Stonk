package io.stonk.user.repository;

import io.stonk.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUsername(String username);
}