package io.stonk.order.exchange;

import io.stonk.order.dto.Side;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public final class BookOrder {

    private final String orderId;
    private final String clientId;
    private final Side side;
    private final BigDecimal price;
    private int remainingQty;
    private final long sequence;

    public BookOrder(String orderId, String clientId, Side side, BigDecimal price, int remainingQty, long sequence) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.side = side;
        this.price = price;
        this.remainingQty = remainingQty;
        this.sequence = sequence;
    }

    public void reduce(int qty) {
        if (qty > remainingQty) {
            throw new IllegalArgumentException("overfill");
        }
        remainingQty -= qty;
    }

    public boolean isFilled() {
        return remainingQty <= 0;
    }
}
