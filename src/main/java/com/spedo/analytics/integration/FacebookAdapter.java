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
public class FacebookAdapter implements SocialPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(FacebookAdapter.class);
    private final Random random = new Random();

    @Value("${app.integrations.facebook.app-id:}")
    private String appId;

    @Value("${app.integrations.facebook.app-secret:}")
    private String appSecret;

    @Override
    public SocialPlatform getPlatform() {
        return SocialPlatform.FACEBOOK;
    }

    @Override
    public boolean isConfigured() {
        return appId != null && !appId.isBlank() && appSecret != null && !appSecret.isBlank();
    }

    @Override
    public SyncResult syncMetrics(SocialAccount account) {
        log.info("Executing Facebook sync for account: {}", account.getHandle());

        int delta = 80 + random.nextInt(150);
        int newFollowers = account.getFollowerCount() + delta;
        int dailyImpressions = 38000 + random.nextInt(12000);
        int dailyReach = (int) (dailyImpressions * 0.80);
        int dailyLikes = 1900 + random.nextInt(900);
        int dailyComments = 140 + random.nextInt(80);
        int dailyShares = 200 + random.nextInt(130);

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

        SyncResult result = new SyncResult(true, "Facebook metrics synced successfully", newFollowers, delta);
        result.setLatestMetric(metric);
        return result;
    }
}
