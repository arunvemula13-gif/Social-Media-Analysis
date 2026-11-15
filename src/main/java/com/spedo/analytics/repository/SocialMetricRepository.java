package com.spedo.analytics.repository;

import com.spedo.analytics.entity.SocialMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocialMetricRepository extends JpaRepository<SocialMetric, Long> {

    Optional<SocialMetric> findBySocialAccountIdAndRecordedDate(Long socialAccountId, LocalDate recordedDate);

    List<SocialMetric> findBySocialAccountIdAndRecordedDateBetweenOrderByRecordedDateAsc(
            Long socialAccountId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT sm FROM SocialMetric sm " +
           "WHERE sm.socialAccount.user.id = :userId " +
           "AND sm.recordedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sm.recordedDate ASC")
    List<SocialMetric> findByUserIdAndRecordedDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT sm FROM SocialMetric sm " +
           "WHERE sm.socialAccount.user.id = :userId " +
           "AND sm.socialAccount.platform = com.spedo.analytics.entity.SocialPlatform.INSTAGRAM " +
           "AND sm.recordedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sm.recordedDate ASC")
    List<SocialMetric> findByUserIdAndPlatformAndRecordedDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    void deleteBySocialAccountId(Long socialAccountId);
}
