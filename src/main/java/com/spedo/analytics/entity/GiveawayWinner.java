package com.spedo.analytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "giveaway_winners", indexes = {
    @Index(name = "idx_giveaway_winners_giveaway_id", columnList = "giveaway_id")
})
public class GiveawayWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "giveaway_id", nullable = false)
    private Giveaway giveaway;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(name = "user_avatar", length = 500)
    private String userAvatar;

    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "commented_at")
    private LocalDateTime commentedAt;

    @Column(name = "is_backup", nullable = false)
    private boolean isBackup = false;

    @Column(name = "winner_order", nullable = false)
    private int winnerOrder = 1;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt;

    public GiveawayWinner() {
    }

    public GiveawayWinner(Giveaway giveaway, String username, String userAvatar, String commentText,
                          LocalDateTime commentedAt, boolean isBackup, int winnerOrder, LocalDateTime selectedAt) {
        this.giveaway = giveaway;
        this.username = username;
        this.userAvatar = userAvatar;
        this.commentText = commentText;
        this.commentedAt = commentedAt;
        this.isBackup = isBackup;
        this.winnerOrder = winnerOrder;
        this.selectedAt = selectedAt != null ? selectedAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Giveaway getGiveaway() {
        return giveaway;
    }

    public void setGiveaway(Giveaway giveaway) {
        this.giveaway = giveaway;
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
