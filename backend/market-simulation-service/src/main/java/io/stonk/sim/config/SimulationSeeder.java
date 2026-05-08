package io.stonk.sim.config;

import io.stonk.sim.state.MarketViewRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimulationSeeder {

    private final SimulationProperties simulationProperties;
    private final MarketViewRegistry marketViewRegistry;

    @PostConstruct
    public void seed() {
        simulationProperties.getSeedPrices().forEach(marketViewRegistry::seed);
    }
}
