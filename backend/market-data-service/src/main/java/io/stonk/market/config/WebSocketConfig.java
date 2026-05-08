package io.stonk.market.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures STOMP-over-WebSocket with SockJS fallback.
 *
 * <p>Clients connect at {@code /ws-market} and subscribe to
 * {@code /topic/**} destinations for live market data.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10_000, 10_000})
                .setTaskScheduler(brokerTaskScheduler());
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Primary WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws-market")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Raw WebSocket endpoint (no SockJS) for native WS clients
        registry.addEndpoint("/ws-market")
                .setAllowedOriginPatterns("*");
    }

    /**
     * Task scheduler required for STOMP heartbeat support.
     */
    @org.springframework.context.annotation.Bean
    public org.springframework.scheduling.TaskScheduler brokerTaskScheduler() {
        var scheduler = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}
