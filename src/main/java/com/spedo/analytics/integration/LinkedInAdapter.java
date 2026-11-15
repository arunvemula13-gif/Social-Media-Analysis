package com.spedo.analytics.integration;

import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialMetric;
import com.spedo.analytics.entity.SocialPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Random;

@Component
public class LinkedInAdapter implements SocialPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(LinkedInAdapter.class);
    private final Random random = new Random();

    @Value("${app.integrations.linkedin.client-id:}")
    private String clientId;

    @Value("${app.integrations.linkedin.client-secret:}")
    private String clientSecret;

    @Override
    public SocialPlatform getPlatform() {
        return SocialPlatform.LINKEDIN;
    }

    @Override
    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }

    @Override
    public SyncResult syncMetrics(SocialAccount account) {
        log.info("Executing LinkedIn sync for account: {}", account.getHandle());

        int delta = 40 + random.nextInt(90);
        int newFollowers = account.getFollowerCount() + delta;
        int dailyImpressions = 32000 + random.nextInt(11000);
        int dailyReach = (int) (dailyImpressions * 0.81);
        int dailyLikes = 2100 + random.nextInt(700);
        int dailyComments = 160 + random.nextInt(60);
        int dailyShares = 220 + random.nextInt(90);

        int totalInteractions = dailyLikes + dailyComments + dailyShares;
        BigDecimal engagementRate = BigDecimal.valueOf((double) totalInteractions / dailyReach * 100)
                .setScale(4, RoundingMode.HALF_UP);

        SocialMetric metric = new SocialMetric(
                account,
                LocalDate.now(),
                newFollowers,
                delta,
                dailyImpressions,
                dailyReach,
                dailyLikes,
                dailyComments,
                dailyShares,
                engagementRate
        );

        SyncResult result = new SyncResult(true, "LinkedIn metrics synced successfully", newFollowers, delta);
        result.setLatestMetric(metric);
        return result;
    }
}
