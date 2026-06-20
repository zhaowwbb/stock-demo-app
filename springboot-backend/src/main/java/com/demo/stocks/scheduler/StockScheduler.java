package com.demo.stocks.scheduler;

import com.demo.stocks.service.StockPriceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockScheduler {

    @PersistenceContext
    private EntityManager entityManager;

    private final StockPriceService stockPriceService;

    public StockScheduler(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    // Runs once every day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyStockSync() {
        System.out.println("⏰ Daily stock price synchronization job started...");

        // 1. Fetch symbols from history
        @SuppressWarnings("unchecked")
        List<String> symbols = entityManager
                .createNativeQuery("SELECT DISTINCT symbol FROM stock_history")
                .getResultList();

        if (symbols.isEmpty()) {
            System.out.println("❌ Aborted: No symbols found in stock_history table.");
            return;
        }

        System.out.println("Processing sync for " + symbols.size() + " stock symbols.");
        
        // 2. Execute the sync process (Rate limit handling is inside this service)
        stockPriceService.fetchAndBatchSave(symbols);
        
        System.out.println("✅ Daily stock price synchronization job finished.");
    }
}