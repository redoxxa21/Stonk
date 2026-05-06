package io.stonk.trading.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TradeType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal price;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TradeStatus status;

    private Long orderId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
