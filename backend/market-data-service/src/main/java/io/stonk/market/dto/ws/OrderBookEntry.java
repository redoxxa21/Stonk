package io.stonk.market.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A single price level in the order book.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookEntry {

    private BigDecimal price;
    private int quantity;
}
