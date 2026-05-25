package com.demo.stocks.repository;

import com.demo.stocks.model.StockHistory;
import com.demo.stocks.model.StockHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, StockHistoryId> {
    // Find historical data for a specific ticker symbol
    List<StockHistory> findBySymbolOrderByUpdatedDateDesc(String symbol);
    
    // Find all stock entries for a specific day
    List<StockHistory> findByUpdatedDate(LocalDate updatedDate);
}