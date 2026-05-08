package io.stonk.market.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Logs WebSocket STOMP session lifecycle events and tracks
 * the number of active connections.
 */
@Slf4j
@Component
public class WebSocketEventListener {

    private final AtomicInteger activeConnections = new AtomicInteger(0);

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        int count = activeConnections.incrementAndGet();
        log.info("WebSocket CONNECT — session={}, active={}", sessionId, count);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        int count = activeConnections.decrementAndGet();
        log.info("WebSocket DISCONNECT — session={}, active={}", sessionId, count);
    }

    /** Returns the current number of active WebSocket connections. */
    public int getActiveConnectionCount() {
        return activeConnections.get();
    }
}
