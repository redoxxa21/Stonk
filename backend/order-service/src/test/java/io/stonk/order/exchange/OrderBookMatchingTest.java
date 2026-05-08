package io.stonk.order.exchange;

import io.stonk.order.dto.OrderRequestMessage;
import io.stonk.order.dto.OrderType;
import io.stonk.order.dto.Side;
import io.stonk.order.dto.TradeExecutedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookMatchingTest {

    @Test
    void orderBookSortsPriceTime_BuyMatchesBestAskFirst() {
        OrderBook book = new OrderBook("AAPL");
        book.handle(limitSell("c1", "s1", 50, new BigDecimal("100.00")));
        book.handle(limitSell("c1", "s2", 50, new BigDecimal("100.00")));
        List<TradeExecutedEvent> trades = book.handle(limitBuy("c2", "b1", 70, new BigDecimal("100.00")));
        assertThat(trades).hasSize(2);
        assertThat(trades.get(0).getQuantity()).isEqualTo(50);
        assertThat(trades.get(1).getQuantity()).isEqualTo(20);
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("100.00"), trades.get(0).getPrice());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("100.00"), trades.get(1).getPrice());
    }

    @Test
    void partialFillLeavesRestingLiquidity() {
        OrderBook book = new OrderBook("NVDA");
        book.handle(limitSell("x", "o1", 100, new BigDecimal("50")));
        List<TradeExecutedEvent> t = book.handle(limitBuy("y", "o2", 40, new BigDecimal("50")));
        assertThat(t).singleElement().satisfies(tr -> assertThat(tr.getQuantity()).isEqualTo(40));
        assertThat(book.bestAskSize()).isEqualTo(60);
    }

    @Test
    void marketBuyConsumesAsksIocStyle() {
        OrderBook book = new OrderBook("TSLA");
        book.handle(limitSell("x", "a", 25, new BigDecimal("10")));
        List<TradeExecutedEvent> t = book.handle(marketBuy("y", "m", 25));
        assertThat(t).singleElement().satisfies(tr -> assertThat(tr.getQuantity()).isEqualTo(25));
    }

    @Test
    void whaleSizedTradeCreatesMultiplePartialFillsAgainstBook() {
        OrderBook book = new OrderBook("AMD");
        for (int i = 0; i < 5; i++) {
            book.handle(limitSell("mm", "id" + i, 20, new BigDecimal("100")));
        }
        List<TradeExecutedEvent> legs = book.handle(marketBuy("whale", "W", 100));
        assertThat(legs).hasSize(5);
        assertThat(legs.stream().mapToInt(TradeExecutedEvent::getQuantity).sum()).isEqualTo(100);
    }

    private static OrderRequestMessage limitSell(String client, String id, int qty, BigDecimal px) {
        return OrderRequestMessage.builder()
                .requestId(id)
                .clientId(client)
                .symbol("dummy")
                .side(Side.SELL)
                .orderType(OrderType.LIMIT)
                .quantity(qty)
                .limitPrice(px)
                .build();
    }

    private static OrderRequestMessage limitBuy(String client, String id, int qty, BigDecimal px) {
        return OrderRequestMessage.builder()
                .requestId(id)
                .clientId(client)
                .symbol("dummy")
                .side(Side.BUY)
                .orderType(OrderType.LIMIT)
                .quantity(qty)
                .limitPrice(px)
                .build();
    }

    private static OrderRequestMessage marketBuy(String client, String id, int qty) {
        return OrderRequestMessage.builder()
                .requestId(id)
                .clientId(client)
                .symbol("dummy")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .quantity(qty)
                .build();
    }
}
