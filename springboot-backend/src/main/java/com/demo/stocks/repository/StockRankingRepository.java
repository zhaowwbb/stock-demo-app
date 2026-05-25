package com.demo.stocks.repository;

import com.demo.stocks.model.StockRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRankingRepository extends JpaRepository<StockRanking, String> {
    
/**
     * Fetches the top 10 stock ranking records, ordered by the ranking column ascending
     * (e.g., 1, 2, 3... up to 10).
     */
    List<StockRanking> findTop10ByOrderByRankingAsc();
}