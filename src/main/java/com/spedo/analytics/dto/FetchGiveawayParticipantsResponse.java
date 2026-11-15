package com.spedo.analytics.dto;

import com.spedo.analytics.entity.SocialPlatform;
import java.util.ArrayList;
import java.util.List;

public class FetchGiveawayParticipantsResponse {

    private SocialPlatform platform;
    private String postUrl;
    private String platformPostId;
    private String postCaption;
    private String postThumbnail;
    private int totalCommentsFetched = 0;
    private int uniqueUsersCount = 0;
    private int eligibleCount = 0;
    private int duplicateCount = 0;
    private int keywordFilteredCount = 0;
    private int excludedUsersCount = 0;
    private List<GiveawayParticipantDto> participants = new ArrayList<>();

    public FetchGiveawayParticipantsResponse() {
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

    public int getUniqueUsersCount() {
        return uniqueUsersCount;
    }

    public void setUniqueUsersCount(int uniqueUsersCount) {
        this.uniqueUsersCount = uniqueUsersCount;
    }

    public int getEligibleCount() {
        return eligibleCount;
    }

    public void setEligibleCount(int eligibleCount) {
        this.eligibleCount = eligibleCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public int getKeywordFilteredCount() {
        return keywordFilteredCount;
    }

    public void setKeywordFilteredCount(int keywordFilteredCount) {
        this.keywordFilteredCount = keywordFilteredCount;
    }

    public int getExcludedUsersCount() {
        return excludedUsersCount;
    }

    public void setExcludedUsersCount(int excludedUsersCount) {
        this.excludedUsersCount = excludedUsersCount;
    }

    public List<GiveawayParticipantDto> getParticipants() {
        return participants;
    }

    public void setParticipants(List<GiveawayParticipantDto> participants) {
        this.participants = participants;
    }
}
