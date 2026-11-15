package com.spedo.analytics.dto;

import java.time.LocalDateTime;

public class GiveawayWinnerDto {

    private Long id;
    private Long giveawayId;
    private String username;
    private String userAvatar;
    private String commentText;
    private LocalDateTime commentedAt;
    private boolean isBackup;
    private int winnerOrder;
    private LocalDateTime selectedAt;

    public GiveawayWinnerDto() {
    }

    public GiveawayWinnerDto(Long id, Long giveawayId, String username, String userAvatar, String commentText,
                             LocalDateTime commentedAt, boolean isBackup, int winnerOrder, LocalDateTime selectedAt) {
        this.id = id;
        this.giveawayId = giveawayId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.commentText = commentText;
        this.commentedAt = commentedAt;
        this.isBackup = isBackup;
        this.winnerOrder = winnerOrder;
        this.selectedAt = selectedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGiveawayId() {
        return giveawayId;
    }

    public void setGiveawayId(Long giveawayId) {
        this.giveawayId = giveawayId;
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

    public boolean isBackup() {
        return isBackup;
    }

    public void setBackup(boolean backup) {
        isBackup = backup;
    }

    public int getWinnerOrder() {
        return winnerOrder;
    }

    public void setWinnerOrder(int winnerOrder) {
        this.winnerOrder = winnerOrder;
    }

    public LocalDateTime getSelectedAt() {
        return selectedAt;
    }

    public void setSelectedAt(LocalDateTime selectedAt) {
        this.selectedAt = selectedAt;
    }
}
