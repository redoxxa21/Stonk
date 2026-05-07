package io.stonk.trading.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioFailedEvent {
    private Long tradeId;
    private Long userId;
    private String symbol;
    private String reason;
}
