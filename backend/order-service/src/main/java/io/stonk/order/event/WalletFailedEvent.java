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
public class WalletFailedEvent {
    private Long tradeId;
    private Long userId;
    private BigDecimal amount;
    private String reason;
}

