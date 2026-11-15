package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GiveawayDto {

    private Long id;
    private String title;
    private SocialPlatform platform;
    private String postUrl;
    private String platformPostId;
    private String postCaption;
    private String postThumbnail;
    private int totalCommentsFetched;
    private int uniqueParticipantsCount;
    private int eligibleParticipantsCount;
    private int winnersCount;
    private int backupWinnersCount;
    private boolean filterDuplicates;
    private String requiredKeyword;
    private String excludedUsernames;
    private LocalDateTime drawnAt;
    private List<GiveawayWinnerDto> winners = new ArrayList<>();
    private List<GiveawayWinnerDto> backupWinners = new ArrayList<>();

    public GiveawayDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getTotalCommentsFetched() {
        return totalCommentsFetched;
    }

    public void setTotalCommentsFetched(int totalCommentsFetched) {
        this.totalCommentsFetched = totalCommentsFetched;
    }

    public int getUniqueParticipantsCount() {
        return uniqueParticipantsCount;
    }

    public void setUniqueParticipantsCount(int uniqueParticipantsCount) {
        this.uniqueParticipantsCount = uniqueParticipantsCount;
    }

    public int getEligibleParticipantsCount() {
        return eligibleParticipantsCount;
    }

    public void setEligibleParticipantsCount(int eligibleParticipantsCount) {
        this.eligibleParticipantsCount = eligibleParticipantsCount;
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

    public LocalDateTime getDrawnAt() {
        return drawnAt;
    }

    public void setDrawnAt(LocalDateTime drawnAt) {
        this.drawnAt = drawnAt;
    }

    public List<GiveawayWinnerDto> getWinners() {
        return winners;
    }

    public void setWinners(List<GiveawayWinnerDto> winners) {
        this.winners = winners;
    }

    public List<GiveawayWinnerDto> getBackupWinners() {
        return backupWinners;
    }

    public void setBackupWinners(List<GiveawayWinnerDto> backupWinners) {
        this.backupWinners = backupWinners;
    }
}
