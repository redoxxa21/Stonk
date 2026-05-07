package io.stonk.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAddedEvent {
    private Long tradeId;
    private Long userId;
    private String symbol;
    private int quantity;
    private BigDecimal price;
}

