package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import java.math.BigDecimal;

public class PlatformComparisonDto {
    private SocialPlatform platform;
    private String platformName;
    private long followers;
    private long postCount;
    private long totalEngagements;
    private BigDecimal engagementRate;
    private double shareOfAudiencePercentage;
    private String handle;
    private String accountName;

    public PlatformComparisonDto() {
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
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

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }

    public long getTotalEngagements() {
        return totalEngagements;
    }

    public void setTotalEngagements(long totalEngagements) {
        this.totalEngagements = totalEngagements;
    }

    public BigDecimal getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(BigDecimal engagementRate) {
        this.engagementRate = engagementRate;
    }

    public double getShareOfAudiencePercentage() {
        return shareOfAudiencePercentage;
    }

    public void setShareOfAudiencePercentage(double shareOfAudiencePercentage) {
        this.shareOfAudiencePercentage = shareOfAudiencePercentage;
    }
}
