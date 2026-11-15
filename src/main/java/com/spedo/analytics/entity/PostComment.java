package com.spedo.analytics.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments", indexes = {
    @Index(name = "idx_post_comments_post_id", columnList = "post_id"),
    @Index(name = "idx_post_comments_sentiment", columnList = "sentiment")
})
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(name = "author_avatar", length = 500)
    private String authorAvatar;

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @Column(nullable = false, length = 20)
    private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE

    @Column(name = "sentiment_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal sentimentScore = BigDecimal.ZERO; // -1.00 to +1.00

    @Column(name = "detected_keyword", length = 100)
    private String detectedKeyword; // e.g. "broken", "crash", "love", "refund"

    @Column(name = "commented_at", nullable = false)
    private LocalDateTime commentedAt;

    public PostComment() {
    }

    public PostComment(Post post, String author, String authorAvatar, String commentText, 
                       String sentiment, BigDecimal sentimentScore, String detectedKeyword, LocalDateTime commentedAt) {
        this.post = post;
        this.author = author;
        this.authorAvatar = authorAvatar;
        this.commentText = commentText;
        this.sentiment = sentiment;
        this.sentimentScore = sentimentScore;
        this.detectedKeyword = detectedKeyword;
        this.commentedAt = commentedAt != null ? commentedAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
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
