package com.demo.stocks.repository;

import com.demo.stocks.model.RecommendationId;
import com.demo.stocks.model.TopRecAbsoluteIncrease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TopRecAbsoluteIncreaseRepository extends JpaRepository<TopRecAbsoluteIncrease, RecommendationId> {
    // Find the absolute top 10 recommendations for a specific day, sorted by rank
    List<TopRecAbsoluteIncrease> findByUpdatedDateOrderByRankAsc(LocalDate updatedDate);
}