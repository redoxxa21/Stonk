package io.stonk.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant receivedAt;

    @Column(nullable = false, length = 256)
    private String topic;

    @Column(length = 256)
    private String messageKey;

    private Integer partitionIndex;

    private Long offsetValue;

    @Column(columnDefinition = "TEXT")
    private String payload;
}
