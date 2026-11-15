package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import java.math.BigDecimal;

public class CompetitorBenchmarkDto {
    private Long competitorId;
    private String name;
    private SocialPlatform platform;
    private int followers;
    private int followerGrowth;
    private BigDecimal followerGrowthRate;
    private BigDecimal postingFrequencyPerWeek;
    private int avgLikes;
    private int avgComments;
    private int avgShares;
    private BigDecimal engagementRate;
    private int rank;
    private boolean isSelf;

    public CompetitorBenchmarkDto() {
    }

    public Long getCompetitorId() {
        return competitorId;
    }

    public void setCompetitorId(Long competitorId) {
        this.competitorId = competitorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
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

    public BigDecimal getFollowerGrowthRate() {
        return followerGrowthRate;
    }

    public void setFollowerGrowthRate(BigDecimal followerGrowthRate) {
        this.followerGrowthRate = followerGrowthRate;
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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }
}
