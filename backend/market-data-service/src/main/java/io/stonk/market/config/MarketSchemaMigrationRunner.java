package io.stonk.market.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Keeps market_db schema compatible across rolling model changes.
 */
@Slf4j
@Component
public class MarketSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public MarketSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE stocks ADD COLUMN IF NOT EXISTS cumulative_volume BIGINT");
            jdbcTemplate.execute("ALTER TABLE stocks ADD COLUMN IF NOT EXISTS realized_volatility NUMERIC(12,6)");
            jdbcTemplate.execute("ALTER TABLE stocks ADD COLUMN IF NOT EXISTS liquidity_score NUMERIC(14,6)");

            jdbcTemplate.update(
                    "UPDATE stocks SET cumulative_volume = ? WHERE cumulative_volume IS NULL",
                    0L
            );
            jdbcTemplate.update(
                    "UPDATE stocks SET realized_volatility = ? WHERE realized_volatility IS NULL",
                    BigDecimal.ZERO
            );
            jdbcTemplate.update(
                    "UPDATE stocks SET liquidity_score = ? WHERE liquidity_score IS NULL",
                    BigDecimal.valueOf(1000)
            );

            jdbcTemplate.execute("ALTER TABLE stocks ALTER COLUMN cumulative_volume SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE stocks ALTER COLUMN realized_volatility SET NOT NULL");
            jdbcTemplate.execute("ALTER TABLE stocks ALTER COLUMN liquidity_score SET NOT NULL");

            log.info("Market schema migration ensured for stocks metrics columns.");
        } catch (Exception ex) {
            log.error("Market schema migration failed on startup", ex);
            throw ex;
        }
    }
}
