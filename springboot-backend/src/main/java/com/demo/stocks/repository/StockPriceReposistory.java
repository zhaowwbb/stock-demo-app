package com.demo.stocks.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.stocks.model.StockPrice;

public interface StockPriceReposistory extends JpaRepository<StockPrice, Long> {
    List<StockPrice> findBySymbolIgnoreCaseOrderByUpdatedDateAsc(String symbol);
}
