package io.stonk.market.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeExecutedEvent {

    private String tradeId;
    private String symbol;
    private BigDecimal price;
    private int quantity;
    private String aggressorSide;
    private Instant executedAt;
}
