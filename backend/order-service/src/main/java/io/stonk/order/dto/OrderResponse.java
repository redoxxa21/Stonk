package io.stonk.order.dto;

import io.stonk.order.entity.OrderStatus;
import io.stonk.order.entity.OrderType;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String symbol;
    private OrderType type;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
