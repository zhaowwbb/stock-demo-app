package com.demo.stocks.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class RecommendationId implements Serializable {
    private Integer rank;
    private LocalDate updatedDate;

    public RecommendationId() {}

    public RecommendationId(Integer rank, LocalDate updatedDate) {
        this.rank = rank;
        this.updatedDate = updatedDate;
    }

    // Getters and Setters
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationId that = (RecommendationId) o;
        return Objects.equals(rank, that.rank) && Objects.equals(updatedDate, that.updatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, updatedDate);
    }
}