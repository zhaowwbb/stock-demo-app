package com.demo.stocks.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_ranking")
public class StockRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "symbol", length = 10, nullable = false)
    private String symbol;

    @Column(name = "ranking", nullable = false)
    private Integer ranking;

    @Column(name = "score", precision = 10, scale = 2, nullable = false)
    private BigDecimal score;

    @Column(name = "calculate_time", nullable = false)
    private LocalDateTime calculateTime;

    // Default constructor required by JPA
    public StockRanking() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score){
        this.score = score;
    }

    public LocalDateTime getCalculateTime() {
        return calculateTime;
    }

    public void setCalculateTime(LocalDateTime calculateTime) {
        this.calculateTime = calculateTime;
    }
}