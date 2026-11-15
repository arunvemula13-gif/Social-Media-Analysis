package com.spedo.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public class GrowthDataDto {
    private List<String> labels; // e.g. ["2026-10-20", "2026-10-21", ...]
    private List<Long> followers;
    private List<Long> impressions;
    private List<Long> reach;
    private List<BigDecimal> engagementRates;

    public GrowthDataDto() {
    }

    public GrowthDataDto(List<String> labels, List<Long> followers, List<Long> impressions, List<Long> reach, List<BigDecimal> engagementRates) {
        this.labels = labels;
        this.followers = followers;
        this.impressions = impressions;
        this.reach = reach;
        this.engagementRates = engagementRates;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Long> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Long> followers) {
        this.followers = followers;
    }

    public List<Long> getImpressions() {
        return impressions;
    }

    public void setImpressions(List<Long> impressions) {
        this.impressions = impressions;
    }

    public List<Long> getReach() {
        return reach;
    }

    public void setReach(List<Long> reach) {
        this.reach = reach;
    }

    public List<BigDecimal> getEngagementRates() {
        return engagementRates;
    }

    public void setEngagementRates(List<BigDecimal> engagementRates) {
        this.engagementRates = engagementRates;
    }
}
