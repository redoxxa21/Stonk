package io.stonk.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the User Service.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Manages user profile data</li>
 *   <li>Exposes user-related REST APIs to other services</li>
 *   <li>Registers with Eureka Discovery Server</li>
 * </ul>
 *
 * <p>Authentication is handled by the Auth Service — this service
 * trusts JWT tokens already validated upstream by the API Gateway.
 */
@SpringBootApplication
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
