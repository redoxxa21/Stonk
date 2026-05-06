package io.stonk.market.repository;

import io.stonk.market.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data-access layer for {@link Stock} entities.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
}
