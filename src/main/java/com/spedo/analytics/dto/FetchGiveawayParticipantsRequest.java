package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;

public class FetchGiveawayParticipantsRequest {

    private SocialPlatform platform;
    private String postUrl;
    private Long postId;
    private boolean filterDuplicates = true;
    private String requiredKeyword;
    private String excludedUsernames;

    public FetchGiveawayParticipantsRequest() {
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public boolean isFilterDuplicates() {
        return filterDuplicates;
    }

    public void setFilterDuplicates(boolean filterDuplicates) {
        this.filterDuplicates = filterDuplicates;
    }

    public String getRequiredKeyword() {
        return requiredKeyword;
    }

    public void setRequiredKeyword(String requiredKeyword) {
        this.requiredKeyword = requiredKeyword;
    }

    public String getExcludedUsernames() {
        return excludedUsernames;
    }

    public void setExcludedUsernames(String excludedUsernames) {
        this.excludedUsernames = excludedUsernames;
    }
}
