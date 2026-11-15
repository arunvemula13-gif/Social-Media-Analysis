package com.spedo.analytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_metrics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"social_account_id", "recorded_date"})
})
public class SocialMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "social_account_id", nullable = false)
    private SocialAccount socialAccount;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(nullable = false)
    private int followers = 0;

    @Column(name = "follower_growth", nullable = false)
    private int followerGrowth = 0;

    @Column(nullable = false)
    private int impressions = 0;

    @Column(nullable = false)
    private int reach = 0;

    @Column(name = "total_likes", nullable = false)
    private int totalLikes = 0;

    @Column(name = "total_comments", nullable = false)
    private int totalComments = 0;

    @Column(name = "total_shares", nullable = false)
    private int totalShares = 0;

    @Column(name = "engagement_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal engagementRate = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SocialMetric() {
    }

    public SocialMetric(SocialAccount socialAccount, LocalDate recordedDate, int followers, int followerGrowth,
                        int impressions, int reach, int totalLikes, int totalComments, int totalShares, BigDecimal engagementRate) {
        this.socialAccount = socialAccount;
        this.recordedDate = recordedDate;
        this.followers = followers;
        this.followerGrowth = followerGrowth;
        this.impressions = impressions;
        this.reach = reach;
        this.totalLikes = totalLikes;
        this.totalComments = totalComments;
        this.totalShares = totalShares;
        this.engagementRate = engagementRate != null ? engagementRate : BigDecimal.ZERO;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public LocalDate getRecordedDate() {
        return recordedDate;
    }

    public void setRecordedDate(LocalDate recordedDate) {
        this.recordedDate = recordedDate;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowerGrowth() {
        return followerGrowth;
    }

    public void setFollowerGrowth(int followerGrowth) {
        this.followerGrowth = followerGrowth;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public int getReach() {
        return reach;
    }

    public void setReach(int reach) {
        this.reach = reach;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public int getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    public int getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(int totalShares) {
        this.totalShares = totalShares;
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
}
