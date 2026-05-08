package io.stonk.audit.service;

import io.stonk.audit.entity.AuditEventRecord;
import io.stonk.audit.kafka.AuditSubscribedTopics;
import io.stonk.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditIngestService {

    private static final int MAX_PAYLOAD_LEN = 32_000;

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public void record(String topic, String key, Integer partition, Long offset, String payload) {
        String body = payload;
        if (body != null && body.length() > MAX_PAYLOAD_LEN) {
            body = body.substring(0, MAX_PAYLOAD_LEN) + "...(truncated)";
        }
        AuditEventRecord row = AuditEventRecord.builder()
                .receivedAt(Instant.now())
                .topic(topic)
                .messageKey(key)
                .partitionIndex(partition)
                .offsetValue(offset)
                .payload(body)
                .build();
        auditEventRepository.save(row);
        if (AuditSubscribedTopics.USER_REGISTRATION.equals(topic) || AuditSubscribedTopics.TRADE_INITIATED.equals(topic)) {
            log.info("Audit stored id={} topic={} key={}", row.getId(), topic, key);
        } else {
            log.debug("Audit stored topic={} key={} id={}", topic, key, row.getId());
        }
    }
}
