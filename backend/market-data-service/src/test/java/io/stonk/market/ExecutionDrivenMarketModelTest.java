package io.stonk.market;

import io.stonk.market.service.StockService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionDrivenMarketModelTest {

    @Test
    void noSchedulerDrivenPriceBulkUpdateOnStockApi() {
        boolean hasBulkRandom = Arrays.stream(StockService.class.getMethods())
                .anyMatch(m -> m.getName().equals("updateAllPrices"));
        assertThat(hasBulkRandom).isFalse();
    }
}
