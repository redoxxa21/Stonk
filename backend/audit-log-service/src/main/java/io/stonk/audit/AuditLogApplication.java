package io.stonk.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class AuditLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditLogApplication.class, args);
    }
}
