package com.demo.stocks.repository;

import com.demo.stocks.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
/**
     * Finds all stock records matching the given symbol, 
     * ordered by updated_time in ascending order (oldest to newest).
     * 
     * Spring Data JPA translates this to:
     * "SELECT * FROM stocks WHERE symbol = ? ORDER BY updated_time ASC"
     * 
     * @param symbol The ticker/stock symbol (e.g., "AAPL")
     * @return A list of Stock entities matching the symbol, sorted by update time
     */
    List<Stock> findBySymbolIgnoreCaseOrderByUpdatedTimeAsc(String symbol);
}
