package io.stonk.market.service;

import io.stonk.market.dto.ws.MarketEventMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Bounded in-memory ring buffer for recent market events.
 *
 * <p>Stores the last {@value MAX_SIZE} events so they can be retrieved
 * via REST ({@code GET /market/events}) even after the WebSocket push.
 */
@Component
public class MarketEventBuffer {

    private static final int MAX_SIZE = 100;

    private final ConcurrentLinkedDeque<MarketEventMessage> buffer = new ConcurrentLinkedDeque<>();

    /**
     * Adds an event to the buffer, evicting the oldest if at capacity.
     */
    public void add(MarketEventMessage event) {
        buffer.addFirst(event);
        while (buffer.size() > MAX_SIZE) {
            buffer.removeLast();
        }
    }

    /**
     * Returns all buffered events, most-recent first.
     */
    public List<MarketEventMessage> getAll() {
        return List.copyOf(buffer);
    }

    /**
     * Returns the last {@code n} events, most-recent first.
     */
    public List<MarketEventMessage> getRecent(int n) {
        return buffer.stream().limit(n).toList();
    }
}
