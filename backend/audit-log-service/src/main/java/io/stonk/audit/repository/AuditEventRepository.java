package io.stonk.audit.repository;

import io.stonk.audit.entity.AuditEventRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventRecord, Long> {

    Page<AuditEventRecord> findByTopicOrderByReceivedAtDesc(String topic, Pageable pageable);

    Page<AuditEventRecord> findAllByOrderByReceivedAtDesc(Pageable pageable);
}
