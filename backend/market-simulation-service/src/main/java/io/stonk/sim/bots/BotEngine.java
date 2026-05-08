package io.stonk.sim.bots;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.sim.dto.OrderRequestMessage;
import io.stonk.sim.events.MarketEventType;
import io.stonk.sim.events.SimulationConditions;
import io.stonk.sim.kafka.SimulationKafkaTopics;
import io.stonk.sim.state.MarketTick;
import io.stonk.sim.state.MarketViewRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * All autonomous actors issue {@link OrderRequestMessage} only — never price APIs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BotEngine {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final MarketViewRegistry marketViewRegistry;
    private final SimulationConditions conditions;

    private final AtomicInteger tickCounter = new AtomicInteger();
    private final ConcurrentHashMap<String, Integer> syntheticShort = new ConcurrentHashMap<>();

    @Scheduled(fixedRateString = "${simulation.tick-ms:500}")
    public void tick() {
        conditions.decayIfExpired();
        int t = tickCounter.incrementAndGet();
        for (String sym : marketViewRegistry.symbols()) {
            MarketTick m = marketViewRegistry.get(sym);
            if (m == null || m.getLastPrice() == null) {
                continue;
            }
            try {
                marketMaker(sym, m);
                momentum(sym, m);
                panic(sym, m);
                fomo(sym, m);
                whale(sym, m, t);
                shortSeller(sym, m);
                retail(sym, m);
            } catch (Exception ex) {
                log.debug("bot tick {}: {}", sym, ex.getMessage());
            }
        }
    }

    private void marketMaker(String sym, MarketTick m) {
        BigDecimal p = m.getLastPrice();
        BigDecimal spread = BigDecimal.valueOf(0.02 + conditions.liquidityBias().doubleValue() * 0.01);
        int baseQty = 15 + ThreadLocalRandom.current().nextInt(20);
        emit(OrderRequestMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .clientId("BOT-MM")
                .symbol(sym)
                .side("BUY")
                .orderType("LIMIT")
                .quantity(baseQty)
                .limitPrice(p.subtract(spread).setScale(4, RoundingMode.HALF_UP))
                .build());
        emit(OrderRequestMessage.builder()
                .requestId(UUID.randomUUID().toString())
                .clientId("BOT-MM")
                .symbol(sym)
                .side("SELL")
                .orderType("LIMIT")
                .quantity(baseQty)
                .limitPrice(p.add(spread).setScale(4, RoundingMode.HALF_UP))
                .build());
    }

    private void momentum(String sym, MarketTick m) {
        BigDecimal prev = marketViewRegistry.momentumPrev(sym);
        if (prev.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal delta = m.getLastPrice().subtract(prev);
        double mult = conditions.hypeMultiplier().doubleValue();
        if (delta.signum() > 0) {
            int q = (int) (10 * mult * (1 + delta.abs().divide(prev, 4, RoundingMode.HALF_UP).doubleValue() * 30));
            q = Math.min(Math.max(q, 5), 200);
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-MOM")
                    .symbol(sym).side("BUY").orderType("MARKET").quantity(q).build());
        } else if (delta.signum() < 0) {
            int q = (int) (10 * conditions.fearMultiplier().doubleValue());
            q = Math.min(Math.max(q, 5), 200);
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-MOM")
                    .symbol(sym).side("SELL").orderType("MARKET").quantity(q).build());
        }
    }

    private void panic(String sym, MarketTick m) {
        if (m.getChangePercent() == null) {
            return;
        }
        if (m.getChangePercent().compareTo(BigDecimal.valueOf(-0.8)) < 0
                || (m.getVolatility() != null && m.getVolatility().compareTo(BigDecimal.valueOf(40)) > 0)) {
            int q = (int) (25 * Math.pow(conditions.fearMultiplier().doubleValue(), 1.5));
            q = Math.min(q, 400);
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-PANIC")
                    .symbol(sym).side("SELL").orderType("MARKET").quantity(Math.max(q, 10)).build());
        }
    }

    private void fomo(String sym, MarketTick m) {
        if (m.getChangePercent() != null && m.getChangePercent().compareTo(BigDecimal.valueOf(0.6)) > 0) {
            int q = (int) (20 * conditions.hypeMultiplier().doubleValue());
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-FOMO")
                    .symbol(sym).side("BUY").orderType("MARKET").quantity(Math.min(Math.max(q, 8), 350)).build());
        }
    }

    private void whale(String sym, MarketTick m, int t) {
        if (t % 37 != 0) {
            return;
        }
        int q = 500 + ThreadLocalRandom.current().nextInt(800);
        boolean buy = ThreadLocalRandom.current().nextBoolean();
        emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-WHALE")
                .symbol(sym).side(buy ? "BUY" : "SELL").orderType("MARKET").quantity(q).build());
    }

    private void shortSeller(String sym, MarketTick m) {
        if (m.getChangePercent() != null && m.getChangePercent().compareTo(BigDecimal.valueOf(1.2)) > 0) {
            int add = 15 + ThreadLocalRandom.current().nextInt(40);
            syntheticShort.merge(sym, add, Integer::sum);
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-SHORT")
                    .symbol(sym).side("SELL").orderType("MARKET").quantity(add).build());
        }
        boolean squeeze = conditions.activeEvent() == MarketEventType.SHORT_SQUEEZE_ALERT;
        int exposure = syntheticShort.getOrDefault(sym, 0);
        if ((squeeze || (m.getChangePercent() != null && m.getChangePercent().compareTo(BigDecimal.valueOf(3)) > 0)) && exposure > 0) {
            int cover = Math.min(exposure, 80 + ThreadLocalRandom.current().nextInt(120));
            syntheticShort.put(sym, exposure - cover);
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-SHORT")
                    .symbol(sym).side("BUY").orderType("MARKET").quantity(cover).build());
        }
    }

    private void retail(String sym, MarketTick m) {
        if (ThreadLocalRandom.current().nextDouble() > 0.35) {
            return;
        }
        int q = 1 + ThreadLocalRandom.current().nextInt(12);
        boolean buy = ThreadLocalRandom.current().nextBoolean();
        if (ThreadLocalRandom.current().nextDouble() < 0.08 && m.getChangePercent() != null && m.getChangePercent().signum() < 0) {
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-RETAIL")
                    .symbol(sym).side("SELL").orderType("MARKET").quantity(q).build());
        } else if (ThreadLocalRandom.current().nextDouble() < 0.08 && m.getChangePercent() != null && m.getChangePercent().signum() > 0) {
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-RETAIL")
                    .symbol(sym).side("BUY").orderType("MARKET").quantity(q).build());
        } else if (ThreadLocalRandom.current().nextBoolean()) {
            BigDecimal px = m.getLastPrice().multiply(BigDecimal.valueOf(1 + (buy ? -0.002 : 0.002)));
            emit(OrderRequestMessage.builder().requestId(UUID.randomUUID().toString()).clientId("BOT-RETAIL")
                    .symbol(sym).side(buy ? "BUY" : "SELL").orderType("LIMIT").quantity(q)
                    .limitPrice(px.setScale(4, RoundingMode.HALF_UP)).build());
        }
    }

    private void emit(OrderRequestMessage msg) {
        try {
            kafkaTemplate.send(SimulationKafkaTopics.ORDER_REQUEST, msg.getSymbol(),
                    objectMapper.writeValueAsString(msg));
        } catch (Exception e) {
            log.warn("order-request emit failed: {}", e.getMessage());
        }
    }
}
