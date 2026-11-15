package com.spedo.analytics.dto;

import com.spedo.analytics.entity.PostType;
import com.spedo.analytics.entity.SocialPlatform;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PostDto {
    private Long id;
    private Long socialAccountId;
    private String accountName;
    private String accountHandle;
    private SocialPlatform platform;
    private String platformPostId;
    private String caption;
    private String mediaUrl;
    private String postUrl;
    private PostType postType;
    private LocalDateTime publishedAt;
    private int likesCount;
    private int commentsCount;
    private int sharesCount;
    private int impressionsCount;
    private int reachCount;
    private BigDecimal engagementRate;
    private BigDecimal positivePercent;
    private BigDecimal negativePercent;
    private BigDecimal sentimentScore;
    private boolean vanityAlert;
    private String sentimentVerdict;

    // News & Broadcast Headline NLP Analysis
    private String newsSentiment; // POSITIVE, NEUTRAL, NEGATIVE
    private BigDecimal newsSentimentScore;
    private String newsToneSummary;
    private String topNewsKeyword;

    public PostDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSocialAccountId() {
        return socialAccountId;
    }

    public void setSocialAccountId(Long socialAccountId) {
        this.socialAccountId = socialAccountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountHandle() {
        return accountHandle;
    }

    public void setAccountHandle(String accountHandle) {
        this.accountHandle = accountHandle;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public String getPlatformPostId() {
        return platformPostId;
    }

    public void setPlatformPostId(String platformPostId) {
        this.platformPostId = platformPostId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(int sharesCount) {
        this.sharesCount = sharesCount;
    }

    public int getImpressionsCount() {
        return impressionsCount;
    }

    public void setImpressionsCount(int impressionsCount) {
        this.impressionsCount = impressionsCount;
    }

    public int getReachCount() {
        return reachCount;
    }

    public void setReachCount(int reachCount) {
        this.reachCount = reachCount;
    }

    public BigDecimal getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(BigDecimal engagementRate) {
        this.engagementRate = engagementRate;
    }

    public BigDecimal getPositivePercent() {
        return positivePercent;
    }

    public void setPositivePercent(BigDecimal positivePercent) {
        this.positivePercent = positivePercent;
    }

    public BigDecimal getNegativePercent() {
        return negativePercent;
    }

    public void setNegativePercent(BigDecimal negativePercent) {
        this.negativePercent = negativePercent;
    }

    public BigDecimal getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(BigDecimal sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public boolean isVanityAlert() {
        return vanityAlert;
    }

    public void setVanityAlert(boolean vanityAlert) {
        this.vanityAlert = vanityAlert;
    }

    public String getSentimentVerdict() {
        return sentimentVerdict;
    }

    public void setSentimentVerdict(String sentimentVerdict) {
        this.sentimentVerdict = sentimentVerdict;
    }

    public String getNewsSentiment() {
        return newsSentiment;
    }

    public void setNewsSentiment(String newsSentiment) {
        this.newsSentiment = newsSentiment;
    }

    public BigDecimal getNewsSentimentScore() {
        return newsSentimentScore;
    }

    public void setNewsSentimentScore(BigDecimal newsSentimentScore) {
        this.newsSentimentScore = newsSentimentScore;
    }

    public String getNewsToneSummary() {
        return newsToneSummary;
    }

    public void setNewsToneSummary(String newsToneSummary) {
        this.newsToneSummary = newsToneSummary;
    }

    public String getTopNewsKeyword() {
        return topNewsKeyword;
    }

    public void setTopNewsKeyword(String topNewsKeyword) {
        this.topNewsKeyword = topNewsKeyword;
    }
}
