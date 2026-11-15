package com.spedo.analytics.dto;

import com.spedo.analytics.entity.PostType;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreatePostRequest {

    @NotNull(message = "Social account ID is required")
    private Long socialAccountId;

    private String caption;
    private String mediaUrl;
    private String postUrl;
    private PostType postType = PostType.IMAGE;

    private int likesCount;
    private int commentsCount;
    private int sharesCount;
    private int impressionsCount;
    private int reachCount;

    private List<String> comments;

    public CreatePostRequest() {
    }

    public Long getSocialAccountId() {
        return socialAccountId;
    }

    public void setSocialAccountId(Long socialAccountId) {
        this.socialAccountId = socialAccountId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(int sharesCount) {
        this.sharesCount = sharesCount;
    }

    public int getImpressionsCount() {
        return impressionsCount;
    }

    public void setImpressionsCount(int impressionsCount) {
        this.impressionsCount = impressionsCount;
    }

    public int getReachCount() {
        return reachCount;
    }

    public void setReachCount(int reachCount) {
        this.reachCount = reachCount;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}
