package com.demo.stocks.repository;

import com.demo.stocks.model.StockHistory;
import com.demo.stocks.model.StockHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, StockHistoryId> {
    // Find historical data for a specific ticker symbol
    List<StockHistory> findBySymbolOrderByUpdatedDateDesc(String symbol);

    // Find all stock entries for a specific day
    List<StockHistory> findByUpdatedDate(LocalDate updatedDate);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO stock_history (symbol, updated_date, current_price, open_price, high_price, low_price, volume)
            VALUES (:symbol, :updatedDate, :currentPrice, :openPrice, :highPrice, :lowPrice, :volume)
            ON CONFLICT (symbol, updated_date)
            DO UPDATE SET
                current_price = EXCLUDED.current_price,
                open_price = EXCLUDED.open_price,
                high_price = EXCLUDED.high_price,
                low_price = EXCLUDED.low_price,
                volume = EXCLUDED.volume
            """, nativeQuery = true)
    void upsertStockPrice(
            @Param("symbol") String symbol,
            @Param("updatedDate") LocalDate updatedDate,
            @Param("currentPrice") BigDecimal currentPrice,
            @Param("openPrice") BigDecimal openPrice,
            @Param("highPrice") BigDecimal highPrice,
            @Param("lowPrice") BigDecimal lowPrice,
            @Param("volume") Long volume);
}