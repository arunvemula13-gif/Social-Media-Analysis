package com.spedo.analytics.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PostSentimentSummaryDto {
    private Long postId;
    private int likesCount;
    private int totalComments;
    private int positiveCount;
    private int neutralCount;
    private int negativeCount;
    private BigDecimal positivePercent;
    private BigDecimal neutralPercent;
    private BigDecimal negativePercent;
    private BigDecimal sentimentScore;
    private boolean vanityAlert;
    private String sentimentVerdict;
    private int realPostCommentsCount;
    private String postCaption;
    private String platform;
    private String postUrl;
    private List<String> topIssues = new ArrayList<>();
    private List<String> topPraises = new ArrayList<>();
    private List<PostCommentDto> comments = new ArrayList<>();

    public PostSentimentSummaryDto() {
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public int getPositiveCount() {
        return positiveCount;
    }

    public void setPositiveCount(int positiveCount) {
        this.positiveCount = positiveCount;
    }

    public int getNeutralCount() {
        return neutralCount;
    }

    public void setNeutralCount(int neutralCount) {
        this.neutralCount = neutralCount;
    }

    public int getNegativeCount() {
        return negativeCount;
    }

    public void setNegativeCount(int negativeCount) {
        this.negativeCount = negativeCount;
    }

    public BigDecimal getPositivePercent() {
        return positivePercent;
    }

    public void setPositivePercent(BigDecimal positivePercent) {
        this.positivePercent = positivePercent;
    }

    public BigDecimal getNeutralPercent() {
        return neutralPercent;
    }

    public void setNeutralPercent(BigDecimal neutralPercent) {
        this.neutralPercent = neutralPercent;
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

    public int getRealPostCommentsCount() {
        return realPostCommentsCount;
    }

    public void setRealPostCommentsCount(int realPostCommentsCount) {
        this.realPostCommentsCount = realPostCommentsCount;
    }

    public String getPostCaption() {
        return postCaption;
    }

    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public List<String> getTopIssues() {
        return topIssues;
    }

    public void setTopIssues(List<String> topIssues) {
        this.topIssues = topIssues;
    }

    public List<String> getTopPraises() {
        return topPraises;
    }

    public void setTopPraises(List<String> topPraises) {
        this.topPraises = topPraises;
    }

    public List<PostCommentDto> getComments() {
        return comments;
    }

    public void setComments(List<PostCommentDto> comments) {
        this.comments = comments;
    }
}
