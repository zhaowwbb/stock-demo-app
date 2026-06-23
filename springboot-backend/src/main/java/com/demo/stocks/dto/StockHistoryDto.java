
package com.demo.stocks.dto;

import com.demo.stocks.model.StockHistory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class StockHistoryDto {

    private String symbol;
    private String companyName; // Added field
    private LocalDate updatedDate;
    private BigDecimal currentPrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private Long volume;
    private ZonedDateTime createdAt;

    // Default Constructor
    public StockHistoryDto() {}

    /**
     * Convenient constructor to convert a StockHistory entity and an 
     * external company name into a clean DTO snapshot.
     */
    public StockHistoryDto(StockHistory entity, String companyName) {
        this.symbol = entity.getSymbol();
        this.companyName = companyName;
        this.updatedDate = entity.getUpdatedDate();
        this.currentPrice = entity.getCurrentPrice();
        this.openPrice = entity.getOpenPrice();
        this.highPrice = entity.getHighPrice();
        this.lowPrice = entity.getLowPrice();
        this.volume = entity.getVolume();
        this.createdAt = entity.getCreatedAt();
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }

    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }

    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}