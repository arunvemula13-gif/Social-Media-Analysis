package com.spedo.analytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "giveaways", indexes = {
    @Index(name = "idx_giveaways_user_id", columnList = "user_id"),
    @Index(name = "idx_giveaways_drawn_at", columnList = "drawn_at")
})
public class Giveaway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SocialPlatform platform;

    @Column(name = "post_url", length = 500)
    private String postUrl;

    @Column(name = "platform_post_id", length = 100)
    private String platformPostId;

    @Column(name = "post_caption", columnDefinition = "TEXT")
    private String postCaption;

    @Column(name = "post_thumbnail", length = 500)
    private String postThumbnail;

    @Column(name = "total_comments_fetched", nullable = false)
    private int totalCommentsFetched = 0;

    @Column(name = "unique_participants_count", nullable = false)
    private int uniqueParticipantsCount = 0;

    @Column(name = "eligible_participants_count", nullable = false)
    private int eligibleParticipantsCount = 0;

    @Column(name = "winners_count", nullable = false)
    private int winnersCount = 1;

    @Column(name = "backup_winners_count", nullable = false)
    private int backupWinnersCount = 0;

    @Column(name = "filter_duplicates", nullable = false)
    private boolean filterDuplicates = true;

    @Column(name = "required_keyword", length = 100)
    private String requiredKeyword;

    @Column(name = "excluded_usernames", columnDefinition = "TEXT")
    private String excludedUsernames;

    @Column(name = "drawn_at", nullable = false)
    private LocalDateTime drawnAt;

    @OneToMany(mappedBy = "giveaway", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("isBackup ASC, winnerOrder ASC")
    private List<GiveawayWinner> winners = new ArrayList<>();

    public Giveaway() {
    }

    public Giveaway(User user, String title, SocialPlatform platform, String postUrl, String platformPostId,
                    String postCaption, String postThumbnail, int totalCommentsFetched, int uniqueParticipantsCount,
                    int eligibleParticipantsCount, int winnersCount, int backupWinnersCount, boolean filterDuplicates,
                    String requiredKeyword, String excludedUsernames, LocalDateTime drawnAt) {
        this.user = user;
        this.title = title;
        this.platform = platform;
        this.postUrl = postUrl;
        this.platformPostId = platformPostId;
        this.postCaption = postCaption;
        this.postThumbnail = postThumbnail;
        this.totalCommentsFetched = totalCommentsFetched;
        this.uniqueParticipantsCount = uniqueParticipantsCount;
        this.eligibleParticipantsCount = eligibleParticipantsCount;
        this.winnersCount = winnersCount;
        this.backupWinnersCount = backupWinnersCount;
        this.filterDuplicates = filterDuplicates;
        this.requiredKeyword = requiredKeyword;
        this.excludedUsernames = excludedUsernames;
        this.drawnAt = drawnAt != null ? drawnAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public List<GiveawayWinner> getWinners() {
        return winners;
    }

    public void setWinners(List<GiveawayWinner> winners) {
        this.winners = winners;
    }

    public void addWinner(GiveawayWinner winner) {
        winners.add(winner);
        winner.setGiveaway(this);
    }
}
