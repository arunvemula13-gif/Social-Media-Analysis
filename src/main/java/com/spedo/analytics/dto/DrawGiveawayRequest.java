package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DrawGiveawayRequest {

    @NotBlank(message = "Giveaway title is required")
    private String title;

    @NotNull(message = "Platform is required")
    private SocialPlatform platform;

    private String postUrl;
    private Long postId;
    private String platformPostId;
    private String postCaption;
    private String postThumbnail;

    @Min(value = 1, message = "At least 1 winner must be selected")
    private int winnersCount = 1;

    @Min(value = 0, message = "Backup winners cannot be negative")
    private int backupWinnersCount = 0;

    private boolean filterDuplicates = true;
    private String requiredKeyword;
    private String excludedUsernames;

    public DrawGiveawayRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getPlatformPostId() {
        return platformPostId;
    }

    public void setPlatformPostId(String platformPostId) {
        this.platformPostId = platformPostId;
    }

    public String getPostCaption() {
        return postCaption;
    }

    public void setPostCaption(String postCaption) {
        this.postCaption = postCaption;
    }

    public String getPostThumbnail() {
        return postThumbnail;
    }

    public void setPostThumbnail(String postThumbnail) {
        this.postThumbnail = postThumbnail;
    }

    public int getWinnersCount() {
        return winnersCount;
    }

    public void setWinnersCount(int winnersCount) {
        this.winnersCount = winnersCount;
    }

    public int getBackupWinnersCount() {
        return backupWinnersCount;
    }

    public void setBackupWinnersCount(int backupWinnersCount) {
        this.backupWinnersCount = backupWinnersCount;
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
