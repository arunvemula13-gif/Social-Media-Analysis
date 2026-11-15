package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import jakarta.validation.constraints.NotNull;

public class PlatformEmailReportRequest {

    @NotNull(message = "Platform A is required")
    private SocialPlatform platformA;

    private String channelA;

    @NotNull(message = "Platform B is required")
    private SocialPlatform platformB;

    private String channelB;

    private String recipientEmail;

    private String customNotes;

    public PlatformEmailReportRequest() {
    }

    public PlatformEmailReportRequest(SocialPlatform platformA, String channelA, SocialPlatform platformB, String channelB, String recipientEmail, String customNotes) {
        this.platformA = platformA;
        this.channelA = channelA;
        this.platformB = platformB;
        this.channelB = channelB;
        this.recipientEmail = recipientEmail;
        this.customNotes = customNotes;
    }

    public SocialPlatform getPlatformA() {
        return platformA;
    }

    public void setPlatformA(SocialPlatform platformA) {
        this.platformA = platformA;
    }

    public String getChannelA() {
        return channelA;
    }

    public void setChannelA(String channelA) {
        this.channelA = channelA;
    }

    public SocialPlatform getPlatformB() {
        return platformB;
    }

    public void setPlatformB(SocialPlatform platformB) {
        this.platformB = platformB;
    }

    public String getChannelB() {
        return channelB;
    }

    public void setChannelB(String channelB) {
        this.channelB = channelB;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getCustomNotes() {
        return customNotes;
    }

    public void setCustomNotes(String customNotes) {
        this.customNotes = customNotes;
    }
}
