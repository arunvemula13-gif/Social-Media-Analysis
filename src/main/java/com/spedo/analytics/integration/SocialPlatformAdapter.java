package com.spedo.analytics.integration;

import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialPlatform;

public interface SocialPlatformAdapter {
    SocialPlatform getPlatform();
    boolean isConfigured();
    SyncResult syncMetrics(SocialAccount account);
}
