package io.stonk.auth.config;

import io.stonk.auth.entity.Role;
import io.stonk.auth.entity.User;
import io.stonk.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Ensures the platform always has one bootstrap admin account.
 *
 * <p>The seeded account is created only if the configured username does not
 * already exist. This keeps startup idempotent across repeated runs.
 */
@Slf4j
@Configuration
public class AdminAccountSeeder {

    @Bean
    ApplicationRunner seedAdminAccount(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.seed.enabled:true}") boolean enabled,
            @Value("${app.admin.seed.username:admin}") String username,
            @Value("${app.admin.seed.email:admin@stonk.local}") String email,
            @Value("${app.admin.seed.password:Admin@123}") String password) {
        return args -> {
            if (!enabled) {
                log.info("Admin account seeding is disabled.");
                return;
            }

            if (userRepository.existsByUsername(username)) {
                log.info("Seed admin account already exists for username '{}'.", username);
                return;
            }

            if (userRepository.existsByEmail(email)) {
                log.warn("Seed admin email '{}' already exists under another account; admin bootstrap skipped.", email);
                return;
            }

            User admin = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Seeded default admin account with username '{}'.", username);
        };
    }
}
