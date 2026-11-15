package com.spedo.analytics.repository;

import com.spedo.analytics.entity.Competitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long> {
    List<Competitor> findByUserId(Long userId);
    List<Competitor> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Competitor> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
