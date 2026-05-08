package io.stonk.market.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Live order book snapshot pushed to {@code /topic/orderbook/{symbol}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveOrderBookMessage {

    private String symbol;
    private List<OrderBookEntry> bids;
    private List<OrderBookEntry> asks;
    private long timestamp;
}
