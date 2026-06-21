package com.demo.stocks.controller;

import com.demo.stocks.StockDemoApplication;
import com.demo.stocks.model.Stock;
import com.demo.stocks.model.StockRanking;
import com.demo.stocks.model.TopRecAbsoluteIncrease;
import com.demo.stocks.model.TopRecPercentageIncrease;
import com.demo.stocks.repository.StockRankingRepository;
import com.demo.stocks.repository.StockRepository;
import com.demo.stocks.repository.TopRecAbsoluteIncreaseRepository;
import com.demo.stocks.repository.TopRecPercentageIncreaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.demo.stocks.service.StockPriceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*") // For development. Replace "*" with your production React URL later.
@RequestMapping("/")
public class StockController {
    // 1. Define the logger instance for this class
    // private static final java.util.logging.Logger log = LoggerFactory.getLogger(StockController.class);

    private static final Logger log =
            LoggerFactory.getLogger(StockController.class);    

    private final StockRepository stockRepository;
    private final StockRankingRepository stockRankingRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final StockPriceService stockPriceService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TopRecAbsoluteIncreaseRepository absoluteRepository;

    @Autowired
    private TopRecPercentageIncreaseRepository percentageRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public StockController(StockRepository stockRepository, S3Client s3Client,
            StockRankingRepository stockRankingRepository, StockPriceService stockPriceService) {
        this.stockRepository = stockRepository;
        this.stockRankingRepository = stockRankingRepository;
        this.s3Client = s3Client;
        this.objectMapper = new ObjectMapper();
        this.stockPriceService = stockPriceService;
    }

    @GetMapping("/api/stocks/dbtop10")
    @ResponseBody
    public List<StockRanking> getTop10FromDB() {
        List<StockRanking> topStocks = new ArrayList<>();
        try {
            topStocks = stockRankingRepository.findTop10ByOrderByRankingAsc();
        } catch (Exception e) {
            // Log the exception in production environments
            log.error("Unexpected system error:", e);
        }
        return topStocks;
    }

    @GetMapping("/api/stocks/top10")
    @ResponseBody
    public List<Map<String, Object>> getTop10FromS3() throws Exception {
        log.info("Test S3 bucket, {}", bucketName);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key("top10.json")
                .build();

        try (InputStream is = s3Client.getObject(getObjectRequest)) {
            return objectMapper.readValue(is, List.class);
        }
    }

    @GetMapping("/api/stocks/{symbol}")
    @ResponseBody
    public List<Stock> getStockDetails(@PathVariable String symbol) {
        log.info("Fetching stock details for symbol: [{}]", symbol);
        List<Stock> list = stockRepository.findBySymbolIgnoreCaseOrderByUpdatedTimeAsc(symbol);
        long count = stockRepository.count();

        log.info("Total Stock count: {}", count);
        log.info("Fetching stock list: {}", list);
        return list;
    }

    @GetMapping
    public String viewTop10Page(Model model) {
        try {
            // List<Map<String, Object>> topStocks = getTop10FromS3();

            List<StockRanking> list = getTop10FromDB();

            model.addAttribute("stockRankings", list);
        } catch (Exception e) {
            model.addAttribute("error", "Could not fetch top ranking data from S3.");
        }
        return "top10";
    }

    @GetMapping("/stocks/{symbol}")
    public String viewStockHistory(@PathVariable String symbol, Model model) {
        List<Stock> history = getStockDetails(symbol);
        model.addAttribute("history", history);
        // model.addAttribute("symbol", symbol);
        return "stock-detail";
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        // In production, your Lambda script saves records under the execution date.
        // We will default to looking for today's generated data rows.
        LocalDate today = LocalDate.now();

        // List<TopRecAbsoluteIncrease> absoluteList =
        // absoluteRepository.findByUpdatedDateOrderByRankAsc(today);
        // List<TopRecPercentageIncrease> percentageList =
        // percentageRepository.findByUpdatedDateOrderByRankAsc(today);

        List<TopRecAbsoluteIncrease> absoluteList = absoluteRepository.findAll();
        List<TopRecPercentageIncrease> percentageList = percentageRepository.findAll();

        // Fallback: If your Lambda hasn't run today yet, fetch the most recent data
        // point available
        if (absoluteList.isEmpty()) {
            // Note: If today is empty, you could write a fallback custom query to find the
            // max date.
            // For this UI template, we send the lists directly to the view layout model.
        }

        model.addAttribute("absoluteRecommendations", absoluteList);
        model.addAttribute("percentageRecommendations", percentageList);
        model.addAttribute("currentDate", today);

        return "dashboard"; // Maps to src/main/resources/templates/dashboard.html
    }

    // ==========================================
    // REACT JS INTERACTIVE REST API ENDPOINTS
    // ==========================================

    public int calculateAndSaveTopPercentageIncreases() throws Exception {
        // 1. Clear old data for today to prevent Unique Constraint violations if re-run
        entityManager.createNativeQuery("DELETE FROM top_rec_absolute_increase WHERE updated_date = CURRENT_DATE")
                .executeUpdate();

        String insertAbsoluteSql = "INSERT INTO top_rec_absolute_increase (rank, updated_date, symbol, price_high, price_low, volume, price_increase_amt) "
                +
                "SELECT " +
                "  DENSE_RANK() OVER (ORDER BY (close - open) DESC) as rank, " +
                "  CURRENT_DATE, symbol, high, low, volume, (close - open) as price_increase_amt " +
                "FROM stock_price " +
                "WHERE    OPEN > 0 and CLOSE > 0 " +
                "ORDER BY price_increase_amt DESC " +
                "LIMIT 10";

        // Execute batch insertion calculations
        int absInserted = entityManager.createNativeQuery(insertAbsoluteSql).executeUpdate();

        log.info(String.format(
                "Rankings updated successfully for today. Inserted %d percentage records.",
                absInserted));

        return absInserted;
    }

    public int calculateAndSaveTopAbsoluteIncreases() throws Exception {
        entityManager.createNativeQuery("DELETE FROM dm.top_rec_percentage_increase WHERE updated_date = CURRENT_DATE")
                .executeUpdate();
        String insertPercentageSql = "INSERT INTO top_rec_percentage_increase (rank, updated_date, symbol, price_high, price_low, volume, price_increase_pct) "
                +
                "SELECT " +
                "  DENSE_RANK() OVER (ORDER BY (CASE WHEN open = 0 THEN 0 ELSE ((close - open) / open) * 100 END) DESC) as rank, "
                +
                "  CURRENT_DATE, symbol, ROUND(high::numeric, 4), ROUND(low::numeric, 4), volume, " +
                "  ROUND((CASE WHEN open = 0 THEN 0 ELSE ((close - open) / open) * 100 END)::numeric, 2) as price_increase_pct "
                +
                "FROM stock_price " +
                "WHERE    OPEN > 0 and CLOSE > 0 " +
                "ORDER BY price_increase_pct DESC " +
                "LIMIT 10";

        int pctInserted = entityManager.createNativeQuery(insertPercentageSql).executeUpdate();

        log.info(String.format(
                "Rankings updated successfully for today. Inserted %d absolute records.",
                pctInserted));
        return pctInserted;
    }

    @GetMapping("/api/stocks/percentage")
    @Transactional
    @ResponseBody
    public ResponseEntity<List<TopRecPercentageIncrease>> getTopPercentageGains() {
        log.info("REST request received for top percentage increase stocks");
        try {
            calculateAndSaveTopPercentageIncreases();
            LocalDate today = LocalDate.now();
            List<TopRecPercentageIncrease> list = percentageRepository.findByUpdatedDateOrderByRankAsc(today);

            // Fallback: If Lambda pipeline hasn't loaded data for today yet, fetch
            // available historical entries
            if (list.isEmpty()) {
                log.warn("No percentage data found for snapshot date [{}]. Cascading to historical table scan.", today);
                list = percentageRepository.findAll();
            }

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Failed to compile percentage analytics data matrix:", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/stocks/absolute")
    @Transactional
    @ResponseBody
    public ResponseEntity<List<TopRecAbsoluteIncrease>> getTopAbsoluteGains() {
        log.info("REST request received for top absolute value increase stocks");
        try {
            calculateAndSaveTopAbsoluteIncreases();
            LocalDate today = LocalDate.now();
            List<TopRecAbsoluteIncrease> list = absoluteRepository.findByUpdatedDateOrderByRankAsc(today);

            // Fallback: If Lambda pipeline hasn't loaded data for today yet, fetch
            // available historical entries
            if (list.isEmpty()) {
                log.warn("No absolute data found for snapshot date [{}]. Cascading to historical table scan.", today);
                list = absoluteRepository.findAll();
            }

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("Failed to compile absolute analytics data matrix:", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/api/stocks/sync-prices")
    public ResponseEntity<String> syncStockPrices() {
        // Step 1: Query stock_history table to get all unique symbols
        @SuppressWarnings("unchecked")
        List<String> symbols = entityManager
                .createNativeQuery("SELECT DISTINCT symbol FROM stock_history")
                .getResultList();
        log.info("symbols to be processed: " + symbols.size());

        if (symbols.isEmpty()) {
            return ResponseEntity.ok("No symbols found in stock_history table to sync.");
        }

        // Step 2: Trigger Async/Background operation so HTTP request doesn't timeout
        new Thread(() -> stockPriceService.fetchAndBatchSave(symbols)).start();

        return ResponseEntity.accepted()
                .body("Sync started for " + symbols.size() + " symbols. Processing batches of 50...");
    }

}
