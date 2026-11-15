package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import java.time.LocalDateTime;

public class GiveawayParticipantDto {

    private String username;
    private String userAvatar;
    private String commentText;
    private LocalDateTime commentedAt;
    private SocialPlatform platform;
    private boolean eligible = true;
    private String disqualificationReason;

    public GiveawayParticipantDto() {
    }

    public GiveawayParticipantDto(String username, String userAvatar, String commentText,
                                  LocalDateTime commentedAt, SocialPlatform platform,
                                  boolean eligible, String disqualificationReason) {
        this.username = username;
        this.userAvatar = userAvatar;
        this.commentText = commentText;
        this.commentedAt = commentedAt;
        this.platform = platform;
        this.eligible = eligible;
        this.disqualificationReason = disqualificationReason;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public LocalDateTime getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(LocalDateTime commentedAt) {
        this.commentedAt = commentedAt;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getDisqualificationReason() {
        return disqualificationReason;
    }

    public void setDisqualificationReason(String disqualificationReason) {
        this.disqualificationReason = disqualificationReason;
    }
}
