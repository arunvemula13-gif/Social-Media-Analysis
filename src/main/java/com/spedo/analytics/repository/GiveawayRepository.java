package com.spedo.analytics.repository;

import com.spedo.analytics.entity.Giveaway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiveawayRepository extends JpaRepository<Giveaway, Long> {

    List<Giveaway> findByUserIdOrderByDrawnAtDesc(Long userId);

    Page<Giveaway> findByUserIdOrderByDrawnAtDesc(Long userId, Pageable pageable);

    Optional<Giveaway> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
