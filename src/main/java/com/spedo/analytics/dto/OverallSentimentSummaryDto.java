package com.spedo.analytics.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class OverallSentimentSummaryDto {
    private int totalPosts;
    private int totalYouTubeVideos;
    private int totalOtherPosts;
    private Map<String, Integer> platformCounts = new HashMap<>();
    private long totalImpressions;
    private long totalLikes;
    private long totalComments;

    // News & Broadcast Headline Analysis
    private int newsPositiveCount;
    private int newsNeutralCount;
    private int newsNegativeCount;
    private BigDecimal newsPositivePercent = BigDecimal.ZERO;
    private BigDecimal newsNeutralPercent = BigDecimal.ZERO;
    private BigDecimal newsNegativePercent = BigDecimal.ZERO;

    // Audience Comments Analysis
    private BigDecimal commentsPositivePercent = BigDecimal.ZERO;
    private BigDecimal commentsNeutralPercent = BigDecimal.ZERO;
    private BigDecimal commentsNegativePercent = BigDecimal.ZERO;
    private BigDecimal averageSentimentScore = BigDecimal.ZERO;

    private int vanityAlertCount;
    private String overallVerdict;

    public OverallSentimentSummaryDto() {
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        this.totalPosts = totalPosts;
    }

    public int getTotalYouTubeVideos() {
        return totalYouTubeVideos;
    }

    public void setTotalYouTubeVideos(int totalYouTubeVideos) {
        this.totalYouTubeVideos = totalYouTubeVideos;
    }

    public int getTotalOtherPosts() {
        return totalOtherPosts;
    }

    public void setTotalOtherPosts(int totalOtherPosts) {
        this.totalOtherPosts = totalOtherPosts;
    }

    public Map<String, Integer> getPlatformCounts() {
        return platformCounts;
    }

    public void setPlatformCounts(Map<String, Integer> platformCounts) {
        this.platformCounts = platformCounts;
    }

    public long getTotalImpressions() {
        return totalImpressions;
    }

    public void setTotalImpressions(long totalImpressions) {
        this.totalImpressions = totalImpressions;
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

    public int getNewsPositiveCount() {
        return newsPositiveCount;
    }

    public void setNewsPositiveCount(int newsPositiveCount) {
        this.newsPositiveCount = newsPositiveCount;
    }

    public int getNewsNeutralCount() {
        return newsNeutralCount;
    }

    public void setNewsNeutralCount(int newsNeutralCount) {
        this.newsNeutralCount = newsNeutralCount;
    }

    public int getNewsNegativeCount() {
        return newsNegativeCount;
    }

    public void setNewsNegativeCount(int newsNegativeCount) {
        this.newsNegativeCount = newsNegativeCount;
    }

    public BigDecimal getNewsPositivePercent() {
        return newsPositivePercent;
    }

    public void setNewsPositivePercent(BigDecimal newsPositivePercent) {
        this.newsPositivePercent = newsPositivePercent;
    }

    public BigDecimal getNewsNeutralPercent() {
        return newsNeutralPercent;
    }

    public void setNewsNeutralPercent(BigDecimal newsNeutralPercent) {
        this.newsNeutralPercent = newsNeutralPercent;
    }

    public BigDecimal getNewsNegativePercent() {
        return newsNegativePercent;
    }

    public void setNewsNegativePercent(BigDecimal newsNegativePercent) {
        this.newsNegativePercent = newsNegativePercent;
    }

    public BigDecimal getCommentsPositivePercent() {
        return commentsPositivePercent;
    }

    public void setCommentsPositivePercent(BigDecimal commentsPositivePercent) {
        this.commentsPositivePercent = commentsPositivePercent;
    }

    public BigDecimal getCommentsNeutralPercent() {
        return commentsNeutralPercent;
    }

    public void setCommentsNeutralPercent(BigDecimal commentsNeutralPercent) {
        this.commentsNeutralPercent = commentsNeutralPercent;
    }

    public BigDecimal getCommentsNegativePercent() {
        return commentsNegativePercent;
    }

    public void setCommentsNegativePercent(BigDecimal commentsNegativePercent) {
        this.commentsNegativePercent = commentsNegativePercent;
    }

    public BigDecimal getAverageSentimentScore() {
        return averageSentimentScore;
    }

    public void setAverageSentimentScore(BigDecimal averageSentimentScore) {
        this.averageSentimentScore = averageSentimentScore;
    }

    public int getVanityAlertCount() {
        return vanityAlertCount;
    }

    public void setVanityAlertCount(int vanityAlertCount) {
        this.vanityAlertCount = vanityAlertCount;
    }

    public String getOverallVerdict() {
        return overallVerdict;
    }

    public void setOverallVerdict(String overallVerdict) {
        this.overallVerdict = overallVerdict;
    }
}
