package io.stonk.trading.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import io.stonk.trading.entity.TradeType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeCompletedEvent {
    private Long tradeId;
    private Long userId;
    private String symbol;
    private int quantity;
    private BigDecimal price;
    private TradeType type;
}
