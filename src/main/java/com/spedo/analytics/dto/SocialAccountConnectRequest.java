package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SocialAccountConnectRequest {

    @NotNull(message = "Platform is required")
    private SocialPlatform platform;

    @NotBlank(message = "Account name is required")
    private String accountName;

    @NotBlank(message = "Handle is required")
    private String handle;

    private String accountId;
    private String profileImageUrl;
    private Integer initialFollowers;
    private Integer initialFollowing;
    private String accessToken;
    private boolean isDemo = false;

    public SocialAccountConnectRequest() {
    }

    public SocialAccountConnectRequest(SocialPlatform platform, String accountName, String handle) {
        this.platform = platform;
        this.accountName = accountName;
        this.handle = handle;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public Integer getInitialFollowers() {
        return initialFollowers;
    }

    public void setInitialFollowers(Integer initialFollowers) {
        this.initialFollowers = initialFollowers;
    }

    public Integer getInitialFollowing() {
        return initialFollowing;
    }

    public void setInitialFollowing(Integer initialFollowing) {
        this.initialFollowing = initialFollowing;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
