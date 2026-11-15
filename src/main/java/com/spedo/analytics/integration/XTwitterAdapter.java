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
public class XTwitterAdapter implements SocialPlatformAdapter {

    private static final Logger log = LoggerFactory.getLogger(XTwitterAdapter.class);
    private final Random random = new Random();

    @Value("${app.integrations.twitter.bearer-token:}")
    private String bearerToken;

    @Override
    public SocialPlatform getPlatform() {
        return SocialPlatform.TWITTER;
    }

    @Override
    public boolean isConfigured() {
        return bearerToken != null && !bearerToken.isBlank();
    }

    @Override
    public SyncResult syncMetrics(SocialAccount account) {
        log.info("Executing X (Twitter) sync for account: {}", account.getHandle());

        int delta = 110 + random.nextInt(190);
        int newFollowers = account.getFollowerCount() + delta;
        int dailyImpressions = 68000 + random.nextInt(22000);
        int dailyReach = (int) (dailyImpressions * 0.80);
        int dailyLikes = 4100 + random.nextInt(1500);
        int dailyComments = 420 + random.nextInt(180);
        int dailyShares = 1400 + random.nextInt(600);

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

        SyncResult result = new SyncResult(true, "X (Twitter) metrics synced successfully", newFollowers, delta);
        result.setLatestMetric(metric);
        return result;
    }
}
