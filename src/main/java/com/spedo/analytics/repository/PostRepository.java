package com.spedo.analytics.repository;

import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.SocialPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndSocialAccountUserId(Long id, Long userId);

    List<Post> findBySocialAccountUserId(Long userId);

    @Query("SELECT p FROM Post p WHERE p.socialAccount.user.id = :userId " +
           "AND (:platform IS NULL OR p.platform = :platform) " +
           "AND (:startDate IS NULL OR p.publishedAt >= :startDate) " +
           "AND (:endDate IS NULL OR p.publishedAt <= :endDate) " +
           "AND (:query IS NULL OR LOWER(p.caption) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Post> searchPosts(
            @Param("userId") Long userId,
            @Param("platform") SocialPlatform platform,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.socialAccount.user.id = :userId " +
           "AND (:startDate IS NULL OR p.publishedAt >= :startDate) " +
           "ORDER BY p.engagementRate DESC")
    List<Post> findTopPerformingByUserId(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.socialAccount.user.id = :userId " +
           "AND p.publishedAt BETWEEN :startDate AND :endDate")
    List<Post> findByUserIdAndPublishedAtBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    long countBySocialAccountUserId(Long userId);

    Optional<Post> findBySocialAccountIdAndPlatformPostId(Long socialAccountId, String platformPostId);

    List<Post> findBySocialAccountId(Long socialAccountId);

    void deleteBySocialAccountId(Long socialAccountId);
}
