package io.stonk.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Auth Service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>User registration — stores hashed credentials, assigns roles</li>
 *   <li>User login — validates credentials, issues JWT tokens</li>
 *   <li>JWT issuance — tokens consumed by the API Gateway and other services</li>
 *   <li>Registers with Eureka Discovery Server</li>
 * </ul>
 *
 * <p>This service does NOT manage user profile data after registration.
 * Profile management is delegated to the User Service.
 */
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
