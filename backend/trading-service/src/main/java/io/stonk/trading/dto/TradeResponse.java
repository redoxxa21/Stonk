package io.stonk.trading.dto;

import io.stonk.trading.entity.TradeStatus;
import io.stonk.trading.entity.TradeType;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class TradeResponse {
    private Long id;
    private Long userId;
    private String symbol;
    private TradeType type;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private TradeStatus status;
    private Long orderId;
    private LocalDateTime createdAt;
}
