package com.spedo.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PostCommentDto {
    private Long id;
    private String author;
    private String authorAvatar;
    private String commentText;
    private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
    private BigDecimal sentimentScore;
    private String detectedKeyword;
    private LocalDateTime commentedAt;

    public PostCommentDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public BigDecimal getSentimentScore() {
        return sentimentScore;
    }

    public void setSentimentScore(BigDecimal sentimentScore) {
        this.sentimentScore = sentimentScore;
    }

    public String getDetectedKeyword() {
        return detectedKeyword;
    }

    public void setDetectedKeyword(String detectedKeyword) {
        this.detectedKeyword = detectedKeyword;
    }

    public LocalDateTime getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(LocalDateTime commentedAt) {
        this.commentedAt = commentedAt;
    }
}
