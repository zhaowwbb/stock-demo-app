package com.demo.stocks.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StockHistoryId implements Serializable {
    private String symbol;
    private LocalDate updatedDate;

    // Default Constructor
    public StockHistoryId() {}

    public StockHistoryId(String symbol, LocalDate updatedDate) {
        this.symbol = symbol;
        this.updatedDate = updatedDate;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }

    // equals and hashCode are mandatory for IdClasses
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockHistoryId that = (StockHistoryId) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(updatedDate, that.updatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, updatedDate);
    }
}