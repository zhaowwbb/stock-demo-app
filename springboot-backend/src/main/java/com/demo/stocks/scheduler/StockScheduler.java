package com.demo.stocks.scheduler;

import com.demo.stocks.StockDemoApplication;
import com.demo.stocks.service.StockPriceService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StockScheduler {

    // private static final java.util.logging.Logger log = LoggerFactory.getLogger(StockScheduler.class);

    private static final Logger log =
            LoggerFactory.getLogger(StockScheduler.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final StockPriceService stockPriceService;

    public StockScheduler(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    // Runs once every day at 1:00 AM
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyStockSync() {
        log.info("⏰ Daily stock price synchronization job started...");

        // 1. Fetch symbols from history
        @SuppressWarnings("unchecked")
        List<String> symbols = entityManager
                .createNativeQuery("SELECT DISTINCT symbol FROM stock_history")
                .getResultList();
        log.info("Found " + symbols + " symbols");

        if (symbols.isEmpty()) {
            log.warn("❌ Aborted: No symbols found in stock_history table.");
            return;
        }

        log.info("Processing sync for " + symbols.size() + " stock symbols.");

        // 2. Execute the sync process (Rate limit handling is inside this service)
        stockPriceService.fetchAndBatchSave(symbols);

        log.info("✅ Daily stock price synchronization job finished.");
    }
}