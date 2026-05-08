package io.stonk.wallet.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures wallet schema changes are applied safely on startup.
 * This keeps existing databases compatible after the Wallet id/userId refactor.
 */
@Slf4j
@Component
public class WalletSchemaMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public WalletSchemaMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE wallets ADD COLUMN IF NOT EXISTS user_id BIGINT");
            jdbcTemplate.execute("UPDATE wallets SET user_id = id WHERE user_id IS NULL");
            jdbcTemplate.execute("ALTER TABLE wallets ALTER COLUMN user_id SET NOT NULL");
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_wallets_user_id ON wallets(user_id)");

            jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS wallets_id_seq");
            jdbcTemplate.execute("ALTER TABLE wallets ALTER COLUMN id SET DEFAULT nextval('wallets_id_seq')");
            jdbcTemplate.execute(
                    "SELECT setval('wallets_id_seq', COALESCE((SELECT MAX(id) FROM wallets), 1), true)"
            );

            log.info("Wallet schema migration ensured: user_id + unique index + id sequence/default.");
        } catch (Exception ex) {
            log.error("Wallet schema migration failed on startup", ex);
            throw ex;
        }
    }
}
