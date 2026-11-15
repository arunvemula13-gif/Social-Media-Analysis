package com.spedo.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OmnichannelOverviewDto {
    private int totalUnifiedAudience;
    private int consolidatedImpressions;
    private int consolidatedReach;
    private BigDecimal omnichannelEngagementRate;
    private double hoursSavedPerMonth;
    private int connectedPlatformsCount;
    private String databaseCentralizationStatus;
    private List<OmnichannelPlatformDto> platformBreakdowns = new ArrayList<>();
    private List<PostDto> crossPlatformTopPosts = new ArrayList<>();

    public OmnichannelOverviewDto() {
    }

    public int getTotalUnifiedAudience() {
        return totalUnifiedAudience;
    }

    public void setTotalUnifiedAudience(int totalUnifiedAudience) {
        this.totalUnifiedAudience = totalUnifiedAudience;
    }

    public int getConsolidatedImpressions() {
        return consolidatedImpressions;
    }

    public void setConsolidatedImpressions(int consolidatedImpressions) {
        this.consolidatedImpressions = consolidatedImpressions;
    }

    public int getConsolidatedReach() {
        return consolidatedReach;
    }

    public void setConsolidatedReach(int consolidatedReach) {
        this.consolidatedReach = consolidatedReach;
    }

    public BigDecimal getOmnichannelEngagementRate() {
        return omnichannelEngagementRate;
    }

    public void setOmnichannelEngagementRate(BigDecimal omnichannelEngagementRate) {
        this.omnichannelEngagementRate = omnichannelEngagementRate;
    }

    public double getHoursSavedPerMonth() {
        return hoursSavedPerMonth;
    }

    public void setHoursSavedPerMonth(double hoursSavedPerMonth) {
        this.hoursSavedPerMonth = hoursSavedPerMonth;
    }

    public int getConnectedPlatformsCount() {
        return connectedPlatformsCount;
    }

    public void setConnectedPlatformsCount(int connectedPlatformsCount) {
        this.connectedPlatformsCount = connectedPlatformsCount;
    }

    public String getDatabaseCentralizationStatus() {
        return databaseCentralizationStatus;
    }

    public void setDatabaseCentralizationStatus(String databaseCentralizationStatus) {
        this.databaseCentralizationStatus = databaseCentralizationStatus;
    }

    public List<OmnichannelPlatformDto> getPlatformBreakdowns() {
        return platformBreakdowns;
    }

    public void setPlatformBreakdowns(List<OmnichannelPlatformDto> platformBreakdowns) {
        this.platformBreakdowns = platformBreakdowns;
    }

    public List<PostDto> getCrossPlatformTopPosts() {
        return crossPlatformTopPosts;
    }

    public void setCrossPlatformTopPosts(List<PostDto> crossPlatformTopPosts) {
        this.crossPlatformTopPosts = crossPlatformTopPosts;
    }
}
