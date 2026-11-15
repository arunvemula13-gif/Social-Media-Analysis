package com.spedo.analytics.repository;

import com.spedo.analytics.entity.GiveawayWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiveawayWinnerRepository extends JpaRepository<GiveawayWinner, Long> {

    List<GiveawayWinner> findByGiveawayIdOrderByWinnerOrderAsc(Long giveawayId);

    void deleteByGiveawayId(Long giveawayId);
}
