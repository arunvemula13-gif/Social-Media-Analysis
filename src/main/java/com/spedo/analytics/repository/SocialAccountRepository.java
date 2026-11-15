package com.spedo.analytics.repository;

import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    List<SocialAccount> findByUserId(Long userId);
    Optional<SocialAccount> findByIdAndUserId(Long id, Long userId);
    List<SocialAccount> findByUserIdAndPlatform(Long userId, SocialPlatform platform);
    boolean existsByUserIdAndPlatformAndAccountId(Long userId, SocialPlatform platform, String accountId);

    @Query("SELECT sa FROM SocialAccount sa JOIN FETCH sa.user")
    List<SocialAccount> findAllWithUser();
}
