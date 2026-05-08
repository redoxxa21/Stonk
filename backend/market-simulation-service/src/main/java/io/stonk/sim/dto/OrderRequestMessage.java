package io.stonk.sim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestMessage {
    private String requestId;
    private String clientId;
    private String symbol;
    private String side;
    private String orderType;
    private int quantity;
    private BigDecimal limitPrice;
}
