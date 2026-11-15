package com.spedo.analytics.mapper;

import com.spedo.analytics.dto.CompetitorBenchmarkDto;
import com.spedo.analytics.dto.CompetitorDto;
import com.spedo.analytics.dto.GiveawayDto;
import com.spedo.analytics.dto.GiveawayWinnerDto;
import com.spedo.analytics.dto.NotificationDto;
import com.spedo.analytics.dto.PostDto;
import com.spedo.analytics.dto.ReportDto;
import com.spedo.analytics.dto.SocialAccountDto;
import com.spedo.analytics.dto.UserDto;
import com.spedo.analytics.entity.Competitor;
import com.spedo.analytics.entity.Giveaway;
import com.spedo.analytics.entity.GiveawayWinner;
import com.spedo.analytics.entity.Notification;
import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.Report;
import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityDtoMapper {

    public UserDto toUserDto(User user) {
        if (user == null) return null;
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getOrganization(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }

    public SocialAccountDto toSocialAccountDto(SocialAccount account) {
        if (account == null) return null;
        SocialAccountDto dto = new SocialAccountDto();
        dto.setId(account.getId());
        dto.setPlatform(account.getPlatform());
        dto.setAccountId(account.getAccountId());
        dto.setAccountName(account.getAccountName());
        dto.setHandle(account.getHandle());
        dto.setProfileImageUrl(account.getProfileImageUrl());
        dto.setFollowerCount(account.getFollowerCount());
        dto.setFollowingCount(account.getFollowingCount());
        dto.setChannelViews(account.getChannelViews());
        dto.setVideoCount(account.getVideoCount());
        dto.setStatus(account.getStatus());
        dto.setLastSyncedAt(account.getLastSyncedAt());
        dto.setDemo(account.isDemo());
        return dto;
    }

    public PostDto toPostDto(Post post) {
        if (post == null) return null;
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        if (post.getSocialAccount() != null) {
            dto.setSocialAccountId(post.getSocialAccount().getId());
            dto.setAccountName(post.getSocialAccount().getAccountName());
            dto.setAccountHandle(post.getSocialAccount().getHandle());
        }
        dto.setPlatform(post.getPlatform());
        dto.setPlatformPostId(post.getPlatformPostId());
        dto.setCaption(post.getCaption());
        dto.setMediaUrl(post.getMediaUrl());
        dto.setPostUrl(post.getPostUrl());
        dto.setPostType(post.getPostType());
        dto.setPublishedAt(post.getPublishedAt());
        dto.setLikesCount(post.getLikesCount());
        dto.setCommentsCount(post.getCommentsCount());
        dto.setSharesCount(post.getSharesCount());
        dto.setImpressionsCount(post.getImpressionsCount());
        dto.setReachCount(post.getReachCount());
        dto.setEngagementRate(post.getEngagementRate());
        dto.setPositivePercent(post.getPositivePercent());
        dto.setNegativePercent(post.getNegativePercent());
        dto.setSentimentScore(post.getSentimentScore());
        dto.setVanityAlert(post.isVanityAlert());
        dto.setSentimentVerdict(post.getSentimentVerdict());

        // Perform Java NLP News Sentiment Analysis on Post Caption / News Headline
        if (post.getCaption() != null && !post.getCaption().isBlank()) {
            com.spedo.analytics.service.CommentSentimentService.TextAnalysisResult analysis =
                    com.spedo.analytics.service.CommentSentimentService.analyzeText(post.getCaption());
            dto.setNewsSentiment(analysis.sentiment);
            dto.setNewsSentimentScore(analysis.score);
            dto.setTopNewsKeyword(analysis.detectedKeyword);

            if ("POSITIVE".equalsIgnoreCase(analysis.sentiment)) {
                dto.setNewsToneSummary("🟢 Positive News: " + (analysis.detectedKeyword != null ? "Keyword '" + analysis.detectedKeyword + "'" : "Favorable report"));
            } else if ("NEGATIVE".equalsIgnoreCase(analysis.sentiment)) {
                dto.setNewsToneSummary("🔴 Alert / Critical News: " + (analysis.detectedKeyword != null ? "Keyword '" + analysis.detectedKeyword + "'" : "Adverse / warning"));
            } else {
                dto.setNewsToneSummary("⚪ Neutral / Informational News Report");
            }
        } else {
            dto.setNewsSentiment("NEUTRAL");
            dto.setNewsSentimentScore(java.math.BigDecimal.ZERO);
            dto.setNewsToneSummary("⚪ Neutral / Informational News Report");
        }

        return dto;
    }

    public CompetitorDto toCompetitorDto(Competitor competitor, List<CompetitorBenchmarkDto> benchmarks) {
        if (competitor == null) return null;
        CompetitorDto dto = new CompetitorDto();
        dto.setId(competitor.getId());
        dto.setName(competitor.getName());
        dto.setWebsite(competitor.getWebsite());
        dto.setIndustry(competitor.getIndustry());
        dto.setInstagramHandle(competitor.getInstagramHandle());
        dto.setFacebookHandle(competitor.getFacebookHandle());
        dto.setYoutubeHandle(competitor.getYoutubeHandle());
        dto.setLinkedinHandle(competitor.getLinkedinHandle());
        dto.setTwitterHandle(competitor.getTwitterHandle());
        dto.setDemo(competitor.isDemo());
        dto.setPlatformMetrics(benchmarks);
        return dto;
    }

    public ReportDto toReportDto(Report report) {
        if (report == null) return null;
        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setTitle(report.getTitle());
        dto.setReportType(report.getReportType());
        dto.setDateFrom(report.getDateFrom());
        dto.setDateTo(report.getDateTo());
        dto.setFileFormat(report.getFileFormat());
        dto.setStatus(report.getStatus());
        dto.setReportDataJson(report.getReportDataJson());
        dto.setDownloadUrl(report.getDownloadUrl());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }

    public NotificationDto toNotificationDto(Notification notification) {
        if (notification == null) return null;
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setRead(notification.isRead());
        dto.setLink(notification.getLink());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    public GiveawayWinnerDto toGiveawayWinnerDto(GiveawayWinner winner) {
        if (winner == null) return null;
        GiveawayWinnerDto dto = new GiveawayWinnerDto();
        dto.setId(winner.getId());
        dto.setGiveawayId(winner.getGiveaway() != null ? winner.getGiveaway().getId() : null);
        dto.setUsername(winner.getUsername());
        dto.setUserAvatar(winner.getUserAvatar());
        dto.setCommentText(winner.getCommentText());
        dto.setCommentedAt(winner.getCommentedAt());
        dto.setBackup(winner.isBackup());
        dto.setWinnerOrder(winner.getWinnerOrder());
        dto.setSelectedAt(winner.getSelectedAt());
        return dto;
    }

    public GiveawayDto toGiveawayDto(Giveaway giveaway) {
        if (giveaway == null) return null;
        GiveawayDto dto = new GiveawayDto();
        dto.setId(giveaway.getId());
        dto.setTitle(giveaway.getTitle());
        dto.setPlatform(giveaway.getPlatform());
        dto.setPostUrl(giveaway.getPostUrl());
        dto.setPlatformPostId(giveaway.getPlatformPostId());
        dto.setPostCaption(giveaway.getPostCaption());
        dto.setPostThumbnail(giveaway.getPostThumbnail());
        dto.setTotalCommentsFetched(giveaway.getTotalCommentsFetched());
        dto.setUniqueParticipantsCount(giveaway.getUniqueParticipantsCount());
        dto.setEligibleParticipantsCount(giveaway.getEligibleParticipantsCount());
        dto.setWinnersCount(giveaway.getWinnersCount());
        dto.setBackupWinnersCount(giveaway.getBackupWinnersCount());
        dto.setFilterDuplicates(giveaway.isFilterDuplicates());
        dto.setRequiredKeyword(giveaway.getRequiredKeyword());
        dto.setExcludedUsernames(giveaway.getExcludedUsernames());
        dto.setDrawnAt(giveaway.getDrawnAt());

        if (giveaway.getWinners() != null) {
            for (GiveawayWinner w : giveaway.getWinners()) {
                GiveawayWinnerDto wDto = toGiveawayWinnerDto(w);
                if (w.isBackup()) {
                    dto.getBackupWinners().add(wDto);
                } else {
                    dto.getWinners().add(wDto);
                }
            }
        }
        return dto;
    }
}
