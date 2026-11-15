package com.spedo.analytics.dto;

public class RerollWinnerRequest {

    private Long winnerId;
    private boolean isBackup = false;

    public RerollWinnerRequest() {
    }

    public RerollWinnerRequest(Long winnerId, boolean isBackup) {
        this.winnerId = winnerId;
        this.isBackup = isBackup;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public boolean isBackup() {
        return isBackup;
    }

    public void setBackup(boolean backup) {
        isBackup = backup;
    }
}
