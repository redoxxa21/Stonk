package io.stonk.audit.config;

import io.stonk.audit.kafka.AuditSubscribedTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Confirms at startup which topics the audit consumer is bound to (auth + trading + saga).
 */
@Slf4j
@Component
@Order(0)
public class AuditStartupLogger implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info(
                "audit-log-service will persist Kafka events including auth topic {} and trading topic {} (plus saga topics).",
                AuditSubscribedTopics.USER_REGISTRATION,
                AuditSubscribedTopics.TRADE_INITIATED);
    }
}
