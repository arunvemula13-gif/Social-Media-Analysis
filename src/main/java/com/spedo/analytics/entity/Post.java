package com.spedo.analytics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "social_account_id", nullable = false)
    private SocialAccount socialAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialPlatform platform;

    @Column(name = "platform_post_id", nullable = false, length = 100)
    private String platformPostId;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "post_url", length = 500)
    private String postUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 50)
    private PostType postType = PostType.IMAGE;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "likes_count", nullable = false)
    private int likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount = 0;

    @Column(name = "shares_count", nullable = false)
    private int sharesCount = 0;

    @Column(name = "impressions_count", nullable = false)
    private int impressionsCount = 0;

    @Column(name = "reach_count", nullable = false)
    private int reachCount = 0;

    @Column(name = "engagement_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal engagementRate = BigDecimal.ZERO;

    @Column(name = "positive_comments_count")
    private Integer positiveCommentsCount = 0;

    @Column(name = "neutral_comments_count")
    private Integer neutralCommentsCount = 0;

    @Column(name = "negative_comments_count")
    private Integer negativeCommentsCount = 0;

    @Column(name = "positive_percent", precision = 5, scale = 2)
    private BigDecimal positivePercent = BigDecimal.ZERO;

    @Column(name = "negative_percent", precision = 5, scale = 2)
    private BigDecimal negativePercent = BigDecimal.ZERO;

    @Column(name = "sentiment_score", precision = 5, scale = 2)
    private BigDecimal sentimentScore = BigDecimal.ZERO;

    @Column(name = "vanity_alert")
    private Boolean vanityAlert = false;

    @Column(name = "sentiment_verdict", length = 500)
    private String sentimentVerdict;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Post() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SocialAccount getSocialAccount() {
        return socialAccount;
    }

    public void setSocialAccount(SocialAccount socialAccount) {
        this.socialAccount = socialAccount;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getPositiveCommentsCount() {
        return positiveCommentsCount != null ? positiveCommentsCount : 0;
    }

    public void setPositiveCommentsCount(Integer positiveCommentsCount) {
        this.positiveCommentsCount = positiveCommentsCount != null ? positiveCommentsCount : 0;
    }

    public Integer getNeutralCommentsCount() {
        return neutralCommentsCount != null ? neutralCommentsCount : 0;
    }

    public void setNeutralCommentsCount(Integer neutralCommentsCount) {
        this.neutralCommentsCount = neutralCommentsCount != null ? neutralCommentsCount : 0;
    }

    public Integer getNegativeCommentsCount() {
        return negativeCommentsCount != null ? negativeCommentsCount : 0;
    }

    public void setNegativeCommentsCount(Integer negativeCommentsCount) {
        this.negativeCommentsCount = negativeCommentsCount != null ? negativeCommentsCount : 0;
    }

    public BigDecimal getPositivePercent() {
        return positivePercent != null ? positivePercent : BigDecimal.ZERO;
    }

    public void setPositivePercent(BigDecimal positivePercent) {
        this.positivePercent = positivePercent != null ? positivePercent : BigDecimal.ZERO;
    }

    public BigDecimal getNegativePercent() {
        return negativePercent != null ? negativePercent : BigDecimal.ZERO;
    }

    public void setNegativePercent(BigDecimal negativePercent) {
        this.negativePercent = negativePercent != null ? negativePercent : BigDecimal.ZERO;
    }

    public BigDecimal getSentimentScore() {
        return sentimentScore != null ? sentimentScore : BigDecimal.ZERO;
    }

    public void setSentimentScore(BigDecimal sentimentScore) {
        this.sentimentScore = sentimentScore != null ? sentimentScore : BigDecimal.ZERO;
    }

    public Boolean isVanityAlert() {
        return Boolean.TRUE.equals(vanityAlert);
    }

    public void setVanityAlert(Boolean vanityAlert) {
        this.vanityAlert = vanityAlert != null ? vanityAlert : false;
    }

    public String getSentimentVerdict() {
        return sentimentVerdict;
    }

    public void setSentimentVerdict(String sentimentVerdict) {
        this.sentimentVerdict = sentimentVerdict;
    }

    public List<PostComment> getComments() {
        return comments;
    }

    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }
}
