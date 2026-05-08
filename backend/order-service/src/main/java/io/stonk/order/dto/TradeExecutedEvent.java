package io.stonk.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** Emitted only when the matching engine crosses resting liquidity. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeExecutedEvent {

    private String tradeId;
    private String symbol;
    private BigDecimal price;
    private int quantity;
    private Side aggressorSide;
    private String buyOrderId;
    private String sellOrderId;
    private String buyClientId;
    private String sellClientId;
    private Instant executedAt;
}
