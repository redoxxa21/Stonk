package io.stonk.portfolio.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class HoldingResponse {
    private Long id;
    private String username;
    private String symbol;
    private Integer quantity;
    private BigDecimal averagePrice;
    private BigDecimal totalInvested;
}
