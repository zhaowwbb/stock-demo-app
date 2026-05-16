package com.demo.stocks.controller;

import com.demo.stocks.model.Stock;
import com.demo.stocks.model.StockRanking;
import com.demo.stocks.repository.StockRankingRepository;
import com.demo.stocks.repository.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class StockController {
    // 1. Define the logger instance for this class
    private static final Logger log = LoggerFactory.getLogger(StockController.class);

    private final StockRepository stockRepository;
    private final StockRankingRepository stockRankingRepository;
    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public StockController(StockRepository stockRepository, S3Client s3Client,
            StockRankingRepository stockRankingRepository) {
        this.stockRepository = stockRepository;
        this.stockRankingRepository = stockRankingRepository;
        this.s3Client = s3Client;
        this.objectMapper = new ObjectMapper();
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
}
