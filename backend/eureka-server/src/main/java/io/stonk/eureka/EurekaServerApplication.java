package io.stonk.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Discovery Server — the central registry for all Stonk microservices.
 *
 * <p>Every service registers here on startup. The API Gateway and inter-service
 * clients resolve service locations via this registry rather than hardcoded URLs.
 *
 * <p>Start this service FIRST before any other service in the platform.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
