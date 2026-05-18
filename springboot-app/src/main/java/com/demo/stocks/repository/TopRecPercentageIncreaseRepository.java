package com.demo.stocks.repository;

import com.demo.stocks.model.RecommendationId;
import com.demo.stocks.model.TopRecPercentageIncrease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TopRecPercentageIncreaseRepository extends JpaRepository<TopRecPercentageIncrease, RecommendationId> {
    // Find the percentage-based top 10 recommendations for a specific day, sorted by rank
    List<TopRecPercentageIncrease> findByUpdatedDateOrderByRankAsc(LocalDate updatedDate);
}