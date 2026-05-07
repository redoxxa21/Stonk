package io.stonk.wallet.event;

import io.stonk.wallet.entity.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeInitiatedEvent {
    private Long tradeId;
    private Long userId;
    private String symbol;
    private int quantity;
    private BigDecimal price;
    private TradeType type;
    private BigDecimal totalAmount;
}

