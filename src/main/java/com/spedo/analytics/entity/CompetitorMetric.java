package com.spedo.analytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "competitor_metrics")
public class CompetitorMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "competitor_id", nullable = false)
    private Competitor competitor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialPlatform platform;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(nullable = false)
    private int followers = 0;

    @Column(name = "follower_growth", nullable = false)
    private int followerGrowth = 0;

    @Column(name = "posting_frequency_per_week", nullable = false, precision = 6, scale = 2)
    private BigDecimal postingFrequencyPerWeek = BigDecimal.ZERO;

    @Column(name = "avg_likes", nullable = false)
    private int avgLikes = 0;

    @Column(name = "avg_comments", nullable = false)
    private int avgComments = 0;

    @Column(name = "avg_shares", nullable = false)
    private int avgShares = 0;

    @Column(name = "engagement_rate", nullable = false, precision = 8, scale = 4)
    private BigDecimal engagementRate = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CompetitorMetric() {
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

    public Competitor getCompetitor() {
        return competitor;
    }

    public void setCompetitor(Competitor competitor) {
        this.competitor = competitor;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
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

    public BigDecimal getPostingFrequencyPerWeek() {
        return postingFrequencyPerWeek;
    }

    public void setPostingFrequencyPerWeek(BigDecimal postingFrequencyPerWeek) {
        this.postingFrequencyPerWeek = postingFrequencyPerWeek;
    }

    public int getAvgLikes() {
        return avgLikes;
    }

    public void setAvgLikes(int avgLikes) {
        this.avgLikes = avgLikes;
    }

    public int getAvgComments() {
        return avgComments;
    }

    public void setAvgComments(int avgComments) {
        this.avgComments = avgComments;
    }

    public int getAvgShares() {
        return avgShares;
    }

    public void setAvgShares(int avgShares) {
        this.avgShares = avgShares;
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
