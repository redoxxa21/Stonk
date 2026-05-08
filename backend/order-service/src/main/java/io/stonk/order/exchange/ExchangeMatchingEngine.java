package io.stonk.order.exchange;

import io.stonk.order.dto.OrderRequestMessage;
import io.stonk.order.dto.TradeExecutedEvent;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeMatchingEngine {

    @Getter
    private final OrderBook.Registry registry = new OrderBook.Registry();

    public List<TradeExecutedEvent> submit(OrderRequestMessage req) {
        String sym = req.getSymbol().toUpperCase();
        OrderBook book = registry.book(sym);
        return book.handle(req);
    }
}
