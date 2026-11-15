package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import java.math.BigDecimal;
import java.util.List;

public class PlatformHeadToHeadDto {

    // Platform A details
    private SocialPlatform platformA;
    private String platformAName;
    private String channelTitleA;
    private String handleA;
    private boolean connectedA;
    private long followersA;
    private long postsCountA;
    private long viewsOrReachA;
    private long totalInteractionsA;
    private BigDecimal engagementRateA;
    private double sentimentPositiveA;
    private double sentimentNeutralA;
    private double sentimentNegativeA;
    private List<String> strengthsA;

    // Platform B details
    private SocialPlatform platformB;
    private String platformBName;
    private String channelTitleB;
    private String handleB;
    private boolean connectedB;
    private long followersB;
    private long postsCountB;
    private long viewsOrReachB;
    private long totalInteractionsB;
    private BigDecimal engagementRateB;
    private double sentimentPositiveB;
    private double sentimentNeutralB;
    private double sentimentNegativeB;
    private List<String> strengthsB;

    // Head-to-Head Comparative Highlights
    private String audienceLeader;
    private String engagementLeader;
    private String sentimentLeader;
    private String velocityLeader;
    private String overallWinner;
    private String executiveSummary;
    private List<String> recommendations;

    // Metadata
    private String recipientEmail;
    private String generatedAt;

    public PlatformHeadToHeadDto() {
    }

    public SocialPlatform getPlatformA() {
        return platformA;
    }

    public void setPlatformA(SocialPlatform platformA) {
        this.platformA = platformA;
    }

    public String getPlatformAName() {
        return platformAName;
    }

    public void setPlatformAName(String platformAName) {
        this.platformAName = platformAName;
    }

    public String getChannelTitleA() {
        return channelTitleA;
    }

    public void setChannelTitleA(String channelTitleA) {
        this.channelTitleA = channelTitleA;
    }

    public String getHandleA() {
        return handleA;
    }

    public void setHandleA(String handleA) {
        this.handleA = handleA;
    }

    public boolean isConnectedA() {
        return connectedA;
    }

    public void setConnectedA(boolean connectedA) {
        this.connectedA = connectedA;
    }

    public long getFollowersA() {
        return followersA;
    }

    public void setFollowersA(long followersA) {
        this.followersA = followersA;
    }

    public long getPostsCountA() {
        return postsCountA;
    }

    public void setPostsCountA(long postsCountA) {
        this.postsCountA = postsCountA;
    }

    public long getViewsOrReachA() {
        return viewsOrReachA;
    }

    public void setViewsOrReachA(long viewsOrReachA) {
        this.viewsOrReachA = viewsOrReachA;
    }

    public long getTotalInteractionsA() {
        return totalInteractionsA;
    }

    public void setTotalInteractionsA(long totalInteractionsA) {
        this.totalInteractionsA = totalInteractionsA;
    }

    public BigDecimal getEngagementRateA() {
        return engagementRateA;
    }

    public void setEngagementRateA(BigDecimal engagementRateA) {
        this.engagementRateA = engagementRateA;
    }

    public double getSentimentPositiveA() {
        return sentimentPositiveA;
    }

    public void setSentimentPositiveA(double sentimentPositiveA) {
        this.sentimentPositiveA = sentimentPositiveA;
    }

    public double getSentimentNeutralA() {
        return sentimentNeutralA;
    }

    public void setSentimentNeutralA(double sentimentNeutralA) {
        this.sentimentNeutralA = sentimentNeutralA;
    }

    public double getSentimentNegativeA() {
        return sentimentNegativeA;
    }

    public void setSentimentNegativeA(double sentimentNegativeA) {
        this.sentimentNegativeA = sentimentNegativeA;
    }

    public List<String> getStrengthsA() {
        return strengthsA;
    }

    public void setStrengthsA(List<String> strengthsA) {
        this.strengthsA = strengthsA;
    }

    public SocialPlatform getPlatformB() {
        return platformB;
    }

    public void setPlatformB(SocialPlatform platformB) {
        this.platformB = platformB;
    }

    public String getPlatformBName() {
        return platformBName;
    }

    public void setPlatformBName(String platformBName) {
        this.platformBName = platformBName;
    }

    public String getChannelTitleB() {
        return channelTitleB;
    }

    public void setChannelTitleB(String channelTitleB) {
        this.channelTitleB = channelTitleB;
    }

    public String getHandleB() {
        return handleB;
    }

    public void setHandleB(String handleB) {
        this.handleB = handleB;
    }

    public boolean isConnectedB() {
        return connectedB;
    }

    public void setConnectedB(boolean connectedB) {
        this.connectedB = connectedB;
    }

    public long getFollowersB() {
        return followersB;
    }

    public void setFollowersB(long followersB) {
        this.followersB = followersB;
    }

    public long getPostsCountB() {
        return postsCountB;
    }

    public void setPostsCountB(long postsCountB) {
        this.postsCountB = postsCountB;
    }

    public long getViewsOrReachB() {
        return viewsOrReachB;
    }

    public void setViewsOrReachB(long viewsOrReachB) {
        this.viewsOrReachB = viewsOrReachB;
    }

    public long getTotalInteractionsB() {
        return totalInteractionsB;
    }

    public void setTotalInteractionsB(long totalInteractionsB) {
        this.totalInteractionsB = totalInteractionsB;
    }

    public BigDecimal getEngagementRateB() {
        return engagementRateB;
    }

    public void setEngagementRateB(BigDecimal engagementRateB) {
        this.engagementRateB = engagementRateB;
    }

    public double getSentimentPositiveB() {
        return sentimentPositiveB;
    }

    public void setSentimentPositiveB(double sentimentPositiveB) {
        this.sentimentPositiveB = sentimentPositiveB;
    }

    public double getSentimentNeutralB() {
        return sentimentNeutralB;
    }

    public void setSentimentNeutralB(double sentimentNeutralB) {
        this.sentimentNeutralB = sentimentNeutralB;
    }

    public double getSentimentNegativeB() {
        return sentimentNegativeB;
    }

    public void setSentimentNegativeB(double sentimentNegativeB) {
        this.sentimentNegativeB = sentimentNegativeB;
    }

    public List<String> getStrengthsB() {
        return strengthsB;
    }

    public void setStrengthsB(List<String> strengthsB) {
        this.strengthsB = strengthsB;
    }

    public String getAudienceLeader() {
        return audienceLeader;
    }

    public void setAudienceLeader(String audienceLeader) {
        this.audienceLeader = audienceLeader;
    }

    public String getEngagementLeader() {
        return engagementLeader;
    }

    public void setEngagementLeader(String engagementLeader) {
        this.engagementLeader = engagementLeader;
    }

    public String getSentimentLeader() {
        return sentimentLeader;
    }

    public void setSentimentLeader(String sentimentLeader) {
        this.sentimentLeader = sentimentLeader;
    }

    public String getVelocityLeader() {
        return velocityLeader;
    }

    public void setVelocityLeader(String velocityLeader) {
        this.velocityLeader = velocityLeader;
    }

    public String getOverallWinner() {
        return overallWinner;
    }

    public void setOverallWinner(String overallWinner) {
        this.overallWinner = overallWinner;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }
}
