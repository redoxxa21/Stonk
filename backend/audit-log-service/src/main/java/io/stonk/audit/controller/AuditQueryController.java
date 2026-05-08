package io.stonk.audit.controller;

import io.stonk.audit.entity.AuditEventRecord;
import io.stonk.audit.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditQueryController {

    private final AuditEventRepository auditEventRepository;

    /** Recent audit rows (newest first). Optional filter by Kafka topic. */
    @GetMapping("/events")
    public ResponseEntity<Page<AuditEventRecord>> list(
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pr = PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "receivedAt"));
        Page<AuditEventRecord> result = topic == null || topic.isBlank()
                ? auditEventRepository.findAllByOrderByReceivedAtDesc(pr)
                : auditEventRepository.findByTopicOrderByReceivedAtDesc(topic, pr);
        return ResponseEntity.ok(result);
    }
}
