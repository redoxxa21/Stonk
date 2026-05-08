package io.stonk.market.sim;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Rolling log-return stdev → coarse annualized % vol for simulation. */
@Component
public class ReturnVolatilityTracker {

    private static final int WINDOW = 48;
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private final Map<String, Deque<Double>> logReturns = new ConcurrentHashMap<>();

    /**
     * @param fromPrice price immediately before this execution (e.g. previous last trade)
     */
    public void recordPriceChange(String symbol, BigDecimal fromPrice, BigDecimal toPrice) {
        if (fromPrice == null || fromPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        double r = Math.log(toPrice.divide(fromPrice, MC).doubleValue());
        Deque<Double> q = logReturns.computeIfAbsent(symbol, k -> new ArrayDeque<>());
        q.addLast(r);
        while (q.size() > WINDOW) {
            q.removeFirst();
        }
    }

    public BigDecimal annualizedVolPercent(String symbol) {
        Deque<Double> q = logReturns.get(symbol);
        if (q == null || q.size() < 5) {
            return BigDecimal.ZERO;
        }
        double mean = q.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double var = q.stream().mapToDouble(x -> {
            double d = x - mean;
            return d * d;
        }).average().orElse(0);
        double stdev = Math.sqrt(var);
        double annual = stdev * Math.sqrt(252.0) * 100;
        return BigDecimal.valueOf(annual).setScale(4, RoundingMode.HALF_UP);
    }
}
