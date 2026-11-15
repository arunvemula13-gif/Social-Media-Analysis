package com.spedo.analytics.dto;

import jakarta.validation.constraints.NotBlank;

public class CompetitorRequest {

    @NotBlank(message = "Competitor name is required")
    private String name;

    private String website;
    private String industry;
    private String instagramHandle;
    private String facebookHandle;
    private String youtubeHandle;
    private String linkedinHandle;
    private String twitterHandle;

    public CompetitorRequest() {
    }

    public CompetitorRequest(String name, String website, String industry) {
        this.name = name;
        this.website = website;
        this.industry = industry;
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
}
