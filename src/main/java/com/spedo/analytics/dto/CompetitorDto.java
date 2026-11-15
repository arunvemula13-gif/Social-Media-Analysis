package com.spedo.analytics.dto;

import java.util.List;

public class CompetitorDto {
    private Long id;
    private String name;
    private String website;
    private String industry;
    private String instagramHandle;
    private String facebookHandle;
    private String youtubeHandle;
    private String linkedinHandle;
    private String twitterHandle;
    private boolean isDemo;
    private List<CompetitorBenchmarkDto> platformMetrics;

    public CompetitorDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getInstagramHandle() {
        return instagramHandle;
    }

    public void setInstagramHandle(String instagramHandle) {
        this.instagramHandle = instagramHandle;
    }

    public String getFacebookHandle() {
        return facebookHandle;
    }

    public void setFacebookHandle(String facebookHandle) {
        this.facebookHandle = facebookHandle;
    }

    public String getYoutubeHandle() {
        return youtubeHandle;
    }

    public void setYoutubeHandle(String youtubeHandle) {
        this.youtubeHandle = youtubeHandle;
    }

    public String getLinkedinHandle() {
        return linkedinHandle;
    }

    public void setLinkedinHandle(String linkedinHandle) {
        this.linkedinHandle = linkedinHandle;
    }

    public String getTwitterHandle() {
        return twitterHandle;
    }

    public void setTwitterHandle(String twitterHandle) {
        this.twitterHandle = twitterHandle;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public List<CompetitorBenchmarkDto> getPlatformMetrics() {
        return platformMetrics;
    }

    public void setPlatformMetrics(List<CompetitorBenchmarkDto> platformMetrics) {
        this.platformMetrics = platformMetrics;
    }
}
