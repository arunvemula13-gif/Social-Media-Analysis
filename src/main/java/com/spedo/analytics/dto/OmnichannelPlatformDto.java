package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import java.math.BigDecimal;

public class OmnichannelPlatformDto {
    private SocialPlatform platform;
    private String platformName;
    private String accountHandle;
    private int audience;
    private BigDecimal audienceSharePercent;
    private BigDecimal avgEngagementRate;
    private int totalPosts;
    private String primaryContentType;
    private String keyStrength;

    public OmnichannelPlatformDto() {
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getAccountHandle() {
        return accountHandle;
    }

    public void setAccountHandle(String accountHandle) {
        this.accountHandle = accountHandle;
    }

    public int getAudience() {
        return audience;
    }

    public void setAudience(int audience) {
        this.audience = audience;
    }

    public BigDecimal getAudienceSharePercent() {
        return audienceSharePercent;
    }

    public void setAudienceSharePercent(BigDecimal audienceSharePercent) {
        this.audienceSharePercent = audienceSharePercent;
    }

    public BigDecimal getAvgEngagementRate() {
        return avgEngagementRate;
    }

    public void setAvgEngagementRate(BigDecimal avgEngagementRate) {
        this.avgEngagementRate = avgEngagementRate;
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public String getPrimaryContentType() {
        return primaryContentType;
    }

    public void setPrimaryContentType(String primaryContentType) {
        this.primaryContentType = primaryContentType;
    }

    public String getKeyStrength() {
        return keyStrength;
    }

    public void setKeyStrength(String keyStrength) {
        this.keyStrength = keyStrength;
    }
}
