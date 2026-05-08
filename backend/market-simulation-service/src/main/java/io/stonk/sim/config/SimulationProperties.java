package io.stonk.sim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {
    private long tickMs = 500;
    private long eventIntervalMs = 45000;
    private Map<String, BigDecimal> seedPrices = new HashMap<>();
}
