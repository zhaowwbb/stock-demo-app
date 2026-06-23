package com.demo.stocks.service;

import com.demo.stocks.StockDemoApplication;
import com.demo.stocks.dto.FinnhubQuoteResponse;
import com.demo.stocks.model.StockPrice;
import com.demo.stocks.repository.StockHistoryRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StockPriceService {

    // private static final java.util.logging.Logger log =
    // LoggerFactory.getLogger(StockPriceService.class);

    private static final Logger log = LoggerFactory.getLogger(StockPriceService.class);

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.url}")
    private String apiUrl;

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final StockHistoryRepository stockHistoryRepository;

    public StockPriceService(JdbcTemplate jdbcTemplate, StockHistoryRepository stockHistoryRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.stockHistoryRepository = stockHistoryRepository;
    }

    public void fetchAndBatchSave(List<String> symbols) {
        List<StockPrice> batchList = new ArrayList<>();

        for (String symbol : symbols) {
            try {
                // 1. Hit Finnhub API
                String url = String.format("%s?symbol=%s&token=%s", apiUrl, symbol, apiKey);
                FinnhubQuoteResponse response = restTemplate.getForObject(url, FinnhubQuoteResponse.class);

                if (response != null && response.open != null) {
                    StockPrice sp = new StockPrice();
                    sp.setSymbol(symbol);
                    sp.setOpen(response.open);
                    sp.setHigh(response.high);
                    sp.setLow(response.low);
                    sp.setClose(response.close);
                    sp.setVolume(response.volume);
                    sp.setUpdatedDate(Instant.ofEpochSecond(response.timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalDateTime());

                    batchList.add(sp);

                    LocalDate updatedDate = Instant.ofEpochSecond(response.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    // Execute high performance DB internal upsert statement
                    stockHistoryRepository.upsertStockPrice(
                            symbol.toUpperCase(),
                            updatedDate,
                            response.close, // current_price
                            response.open, // open_price
                            response.high, // high_price
                            response.low, // low_price
                            response.volume);
                }

                // 2. Write to DB when batch reaches 50 records
                if (batchList.size() == 50) {
                    executeJdbcBatch(batchList);
                    batchList.clear();
                }

                // 3. Strictly rate limit (60 requests/min = ~1000ms delay per call)
                Thread.sleep(1010);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error fetching data for symbol " + symbol + ": " + e.getMessage());
            }
        }

        // Save any leftover elements down the list
        if (!batchList.isEmpty()) {
            executeJdbcBatch(batchList);
        }
    }

    // High performance routing that plays nice with SERIAL auto-increment
    private void executeJdbcBatch(List<StockPrice> stockPrices) {
        String sql = "INSERT INTO stock_price (symbol, open, high, low, close, volume, updated_date) VALUES (?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, stockPrices, stockPrices.size(), (PreparedStatement ps, StockPrice sp) -> {
            ps.setString(1, sp.getSymbol());
            ps.setBigDecimal(2, sp.getOpen());
            ps.setBigDecimal(3, sp.getHighValue());
            ps.setBigDecimal(4, sp.getLow());
            ps.setBigDecimal(5, sp.getClose());
            ps.setLong(6, sp.getVolume() != null ? sp.getVolume() : 0L);
            ps.setTimestamp(7, Timestamp.valueOf(sp.getUpdatedDate()));
        });
        log.info("Successfully flushed a batch of " + stockPrices.size() + " records to PostgreSQL.");
    }
}
