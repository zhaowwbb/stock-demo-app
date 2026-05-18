package com.demo.stocks.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "top_rec_absolute_increase")
@IdClass(RecommendationId.class)
public class TopRecAbsoluteIncrease {

    @Id
    @Column(name = "rank")
    private Integer rank;

    @Id
    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;

    @Column(name = "price_high", nullable = false, precision = 12, scale = 4)
    private BigDecimal priceHigh;

    @Column(name = "price_low", nullable = false, precision = 12, scale = 4)
    private BigDecimal priceLow;

    @Column(name = "volume", nullable = false)
    private Long volume;

    @Column(name = "price_increase_amt", nullable = false, precision = 12, scale = 4)
    private BigDecimal priceIncreaseAmt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    // Getters and Setters
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getPriceHigh() { return priceHigh; }
    public void setPriceHigh(BigDecimal priceHigh) { this.priceHigh = priceHigh; }
    public BigDecimal getPriceLow() { return priceLow; }
    public void setPriceLow(BigDecimal priceLow) { this.priceLow = priceLow; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public BigDecimal getPriceIncreaseAmt() { return priceIncreaseAmt; }
    public void setPriceIncreaseAmt(BigDecimal priceIncreaseAmt) { this.priceIncreaseAmt = priceIncreaseAmt; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
}