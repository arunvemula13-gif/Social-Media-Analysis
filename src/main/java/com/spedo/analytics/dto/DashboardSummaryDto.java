package com.spedo.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryDto {
    private long totalFollowers;
    private long totalPosts;
    private long totalLikes;
    private long totalComments;
    private long totalShares;
    private long totalImpressions;
    private long totalReach;
    private BigDecimal engagementRate;
    private BigDecimal followerGrowthPercentage;
    private PostDto bestPerformingPost;
    private List<NotificationDto> recentActivities;
    private int vanityAlertCount;
    private BigDecimal netSentimentScore;
    private double hoursSavedByCentralHub;
    private String sentimentHealthVerdict;

    public DashboardSummaryDto() {
    }

    public long getTotalFollowers() {
        return totalFollowers;
    }

    public void setTotalFollowers(long totalFollowers) {
        this.totalFollowers = totalFollowers;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public long getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(long totalShares) {
        this.totalShares = totalShares;
    }

    public long getTotalImpressions() {
        return totalImpressions;
    }

    public void setTotalImpressions(long totalImpressions) {
        this.totalImpressions = totalImpressions;
    }

    public long getTotalReach() {
        return totalReach;
    }

    public void setTotalReach(long totalReach) {
        this.totalReach = totalReach;
    }

    public BigDecimal getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(BigDecimal engagementRate) {
        this.engagementRate = engagementRate;
    }

    public BigDecimal getFollowerGrowthPercentage() {
        return followerGrowthPercentage;
    }

    public void setFollowerGrowthPercentage(BigDecimal followerGrowthPercentage) {
        this.followerGrowthPercentage = followerGrowthPercentage;
    }

    public PostDto getBestPerformingPost() {
        return bestPerformingPost;
    }

    public void setBestPerformingPost(PostDto bestPerformingPost) {
        this.bestPerformingPost = bestPerformingPost;
    }

    public List<NotificationDto> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<NotificationDto> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public int getVanityAlertCount() {
        return vanityAlertCount;
    }

    public void setVanityAlertCount(int vanityAlertCount) {
        this.vanityAlertCount = vanityAlertCount;
    }

    public BigDecimal getNetSentimentScore() {
        return netSentimentScore;
    }

    public void setNetSentimentScore(BigDecimal netSentimentScore) {
        this.netSentimentScore = netSentimentScore;
    }

    public double getHoursSavedByCentralHub() {
        return hoursSavedByCentralHub;
    }

    public void setHoursSavedByCentralHub(double hoursSavedByCentralHub) {
        this.hoursSavedByCentralHub = hoursSavedByCentralHub;
    }

    public String getSentimentHealthVerdict() {
        return sentimentHealthVerdict;
    }

    public void setSentimentHealthVerdict(String sentimentHealthVerdict) {
        this.sentimentHealthVerdict = sentimentHealthVerdict;
    }
}
