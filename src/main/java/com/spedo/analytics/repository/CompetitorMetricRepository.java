package com.spedo.analytics.repository;

import com.spedo.analytics.entity.CompetitorMetric;
import com.spedo.analytics.entity.SocialPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitorMetricRepository extends JpaRepository<CompetitorMetric, Long> {

    Optional<CompetitorMetric> findByCompetitorIdAndPlatformAndRecordedDate(
            Long competitorId, SocialPlatform platform, LocalDate recordedDate);

    List<CompetitorMetric> findByCompetitorIdOrderByRecordedDateDesc(Long competitorId);

    List<CompetitorMetric> findByCompetitorIdAndRecordedDateBetweenOrderByRecordedDateAsc(
            Long competitorId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT cm FROM CompetitorMetric cm " +
           "WHERE cm.competitor.user.id = :userId " +
           "AND (:platform IS NULL OR cm.platform = :platform) " +
           "AND cm.recordedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY cm.recordedDate ASC")
    List<CompetitorMetric> findByUserAndPlatformAndDateBetween(
            @Param("userId") Long userId,
            @Param("platform") SocialPlatform platform,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT cm FROM CompetitorMetric cm " +
           "WHERE cm.competitor.id = :competitorId " +
           "AND (:platform IS NULL OR cm.platform = :platform) " +
           "ORDER BY cm.recordedDate DESC")
    List<CompetitorMetric> findLatestByCompetitorAndPlatform(
            @Param("competitorId") Long competitorId,
            @Param("platform") SocialPlatform platform);

    void deleteByCompetitorId(Long competitorId);
}
