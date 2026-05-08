package io.stonk.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Inbound command from bots or API — never mutates prices; only adds liquidity/matching. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequestMessage {

    private String requestId;
    private String clientId;
    private String symbol;
    private Side side;
    private OrderType orderType;
    private int quantity;
    /** Required for LIMIT; ignored for MARKET. */
    private BigDecimal limitPrice;
}
