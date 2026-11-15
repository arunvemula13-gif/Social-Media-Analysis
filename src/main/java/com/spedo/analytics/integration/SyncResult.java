package com.spedo.analytics.integration;

import java.util.List;
import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.SocialMetric;

public class SyncResult {
    private boolean success;
    private String message;
    private int currentFollowers;
    private int followerGrowth;
    private SocialMetric latestMetric;
    private List<Post> newOrUpdatedPosts;

    public SyncResult() {
    }

    public SyncResult(boolean success, String message, int currentFollowers, int followerGrowth) {
        this.success = success;
        this.message = message;
        this.currentFollowers = currentFollowers;
        this.followerGrowth = followerGrowth;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCurrentFollowers() {
        return currentFollowers;
    }

    public void setCurrentFollowers(int currentFollowers) {
        this.currentFollowers = currentFollowers;
    }

    public int getFollowerGrowth() {
        return followerGrowth;
    }

    public void setFollowerGrowth(int followerGrowth) {
        this.followerGrowth = followerGrowth;
    }

    public SocialMetric getLatestMetric() {
        return latestMetric;
    }

    public void setLatestMetric(SocialMetric latestMetric) {
        this.latestMetric = latestMetric;
    }

    public List<Post> getNewOrUpdatedPosts() {
        return newOrUpdatedPosts;
    }

    public void setNewOrUpdatedPosts(List<Post> newOrUpdatedPosts) {
        this.newOrUpdatedPosts = newOrUpdatedPosts;
    }
}
