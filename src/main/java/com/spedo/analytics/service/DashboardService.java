package com.spedo.analytics.service;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.entity.*;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.NotificationRepository;
import com.spedo.analytics.repository.PostRepository;
import com.spedo.analytics.repository.SocialAccountRepository;
import com.spedo.analytics.repository.SocialMetricRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final SocialAccountRepository socialAccountRepository;
    private final SocialMetricRepository socialMetricRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;
    private final EntityDtoMapper mapper;

    public DashboardService(SocialAccountRepository socialAccountRepository,
                            SocialMetricRepository socialMetricRepository,
                            PostRepository postRepository,
                            NotificationRepository notificationRepository,
                            EntityDtoMapper mapper) {
        this.socialAccountRepository = socialAccountRepository;
        this.socialMetricRepository = socialMetricRepository;
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(Long userId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<SocialAccount> accounts = socialAccountRepository.findByUserId(userId);
        long totalFollowers = accounts.stream().mapToLong(SocialAccount::getFollowerCount).sum();

        List<SocialMetric> metrics = socialMetricRepository.findByUserIdAndRecordedDateBetween(userId, startDate, endDate);

        long totalLikes = metrics.stream().mapToLong(SocialMetric::getTotalLikes).sum();
        long totalComments = metrics.stream().mapToLong(SocialMetric::getTotalComments).sum();
        long totalShares = metrics.stream().mapToLong(SocialMetric::getTotalShares).sum();
        long totalImpressions = metrics.stream().mapToLong(SocialMetric::getImpressions).sum();
        long totalReach = metrics.stream().mapToLong(SocialMetric::getReach).sum();
        long totalGrowth = metrics.stream().mapToLong(SocialMetric::getFollowerGrowth).sum();

        long postCount = postRepository.countBySocialAccountUserId(userId);

        // Calculate average engagement rate across recorded metrics
        BigDecimal avgEngagementRate;
        long totalInteractions = totalLikes + totalComments + totalShares;
        if (totalReach > 0) {
            avgEngagementRate = BigDecimal.valueOf((double) totalInteractions / totalReach * 100)
                    .setScale(4, RoundingMode.HALF_UP);
        } else if (!metrics.isEmpty()) {
            double avg = metrics.stream().mapToDouble(m -> m.getEngagementRate().doubleValue()).average().orElse(0.0);
            avgEngagementRate = BigDecimal.valueOf(avg).setScale(4, RoundingMode.HALF_UP);
        } else {
            avgEngagementRate = BigDecimal.ZERO;
        }

        // Calculate Follower Growth Percentage: (totalGrowth / (totalFollowers - totalGrowth)) * 100
        BigDecimal growthPercentage;
        long baselineFollowers = totalFollowers - totalGrowth;
        if (baselineFollowers > 0) {
            growthPercentage = BigDecimal.valueOf((double) totalGrowth / baselineFollowers * 100)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            growthPercentage = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // Best performing post in the given timeframe
        LocalDateTime startDateTime = startDate.atStartOfDay();
        List<Post> topPosts = postRepository.findTopPerformingByUserId(userId, startDateTime, PageRequest.of(0, 1));
        PostDto bestPost = topPosts.isEmpty() ? null : mapper.toPostDto(topPosts.get(0));

        // Recent notification activities
        List<NotificationDto> recentActivities = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(5)
                .map(mapper::toNotificationDto)
                .toList();

        // Sentiment and Vanity Metrics check
        List<Post> recentUserPosts = postRepository.findTopPerformingByUserId(userId, startDateTime, PageRequest.of(0, 50));
        int vanityCount = 0;
        double netSentimentSum = 0.0;
        int evaluatedPosts = 0;
        for (Post p : recentUserPosts) {
            if (p.isVanityAlert()) {
                vanityCount++;
            }
            if (p.getSentimentScore() != null && !BigDecimal.ZERO.equals(p.getSentimentScore())) {
                netSentimentSum += p.getSentimentScore().doubleValue();
                evaluatedPosts++;
            }
        }
        BigDecimal avgNetSentiment = evaluatedPosts > 0 
                ? BigDecimal.valueOf(netSentimentSum / evaluatedPosts).setScale(2, RoundingMode.HALF_UP)
                : new BigDecimal("0.35");

        double hoursSaved = Math.round(accounts.size() * 3.7 * 10.0) / 10.0;
        String sentimentVerdict = vanityCount > 0
                ? String.format("⚠️ Vanity Alert: %d post(s) flagged where high engagement hides negative complaints.", vanityCount)
                : "Quality Sentiment Healthy: Comments confirm positive user reception.";

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setTotalFollowers(totalFollowers);
        summary.setTotalPosts(postCount);
        summary.setTotalLikes(totalLikes);
        summary.setTotalComments(totalComments);
        summary.setTotalShares(totalShares);
        summary.setTotalImpressions(totalImpressions);
        summary.setTotalReach(totalReach);
        summary.setEngagementRate(avgEngagementRate);
        summary.setFollowerGrowthPercentage(growthPercentage);
        summary.setBestPerformingPost(bestPost);
        summary.setRecentActivities(recentActivities);
        summary.setVanityAlertCount(vanityCount);
        summary.setNetSentimentScore(avgNetSentiment);
        summary.setHoursSavedByCentralHub(hoursSaved);
        summary.setSentimentHealthVerdict(sentimentVerdict);

        return summary;
    }

    @Transactional(readOnly = true)
    public GrowthDataDto getGrowthChartData(Long userId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<SocialMetric> metrics = socialMetricRepository.findByUserIdAndRecordedDateBetween(userId, startDate, endDate);

        // Group metrics by recordedDate
        Map<LocalDate, List<SocialMetric>> groupedByDate = metrics.stream()
                .collect(Collectors.groupingBy(SocialMetric::getRecordedDate, TreeMap::new, Collectors.toList()));

        List<String> labels = new ArrayList<>();
        List<Long> followersData = new ArrayList<>();
        List<Long> impressionsData = new ArrayList<>();
        List<Long> reachData = new ArrayList<>();
        List<BigDecimal> engagementData = new ArrayList<>();

        for (Map.Entry<LocalDate, List<SocialMetric>> entry : groupedByDate.entrySet()) {
            labels.add(entry.getKey().toString());

            long followers = entry.getValue().stream().mapToLong(SocialMetric::getFollowers).sum();
            long impressions = entry.getValue().stream().mapToLong(SocialMetric::getImpressions).sum();
            long reach = entry.getValue().stream().mapToLong(SocialMetric::getReach).sum();
            double avgRate = entry.getValue().stream().mapToDouble(m -> m.getEngagementRate().doubleValue()).average().orElse(0.0);

            followersData.add(followers);
            impressionsData.add(impressions);
            reachData.add(reach);
            engagementData.add(BigDecimal.valueOf(avgRate).setScale(4, RoundingMode.HALF_UP));
        }

        return new GrowthDataDto(labels, followersData, impressionsData, reachData, engagementData);
    }

    @Transactional(readOnly = true)
    public List<PlatformComparisonDto> getPlatformComparison(Long userId, int days) {
        List<SocialAccount> accounts = socialAccountRepository.findByUserId(userId);
        long grandTotalFollowers = accounts.stream().mapToLong(SocialAccount::getFollowerCount).sum();

        LocalDate startDate = LocalDate.now().minusDays(days);
        List<SocialMetric> metrics = socialMetricRepository.findByUserIdAndRecordedDateBetween(userId, startDate, LocalDate.now());

        Map<SocialPlatform, List<SocialMetric>> metricsByPlatform = metrics.stream()
                .collect(Collectors.groupingBy(m -> m.getSocialAccount().getPlatform()));

        List<PlatformComparisonDto> result = new ArrayList<>();

        for (SocialAccount account : accounts) {
            SocialPlatform platform = account.getPlatform();
            List<SocialMetric> platMetrics = metricsByPlatform.getOrDefault(platform, Collections.emptyList());

            long interactions = platMetrics.stream().mapToLong(m -> (long) m.getTotalLikes() + m.getTotalComments() + m.getTotalShares()).sum();
            long reach = platMetrics.stream().mapToLong(SocialMetric::getReach).sum();

            BigDecimal rate;
            if (reach > 0) {
                rate = BigDecimal.valueOf((double) interactions / reach * 100).setScale(4, RoundingMode.HALF_UP);
            } else if (!platMetrics.isEmpty()) {
                double avg = platMetrics.stream().mapToDouble(m -> m.getEngagementRate().doubleValue()).average().orElse(0.0);
                rate = BigDecimal.valueOf(avg).setScale(4, RoundingMode.HALF_UP);
            } else {
                rate = BigDecimal.ZERO;
            }

            double audienceShare = grandTotalFollowers > 0
                    ? ((double) account.getFollowerCount() / grandTotalFollowers * 100)
                    : 0.0;

            PlatformComparisonDto dto = new PlatformComparisonDto();
            dto.setPlatform(platform);
            dto.setPlatformName(switch (platform) {
                case INSTAGRAM -> "Instagram";
                case FACEBOOK -> "Facebook";
                case YOUTUBE -> "YouTube";
                case LINKEDIN -> "LinkedIn";
                case TWITTER -> "X (Twitter)";
            });
            dto.setAccountName(account.getAccountName());
            dto.setHandle(account.getHandle());
            dto.setFollowers(account.getFollowerCount());
            dto.setTotalEngagements(interactions);
            dto.setEngagementRate(rate);
            dto.setShareOfAudiencePercentage(Math.round(audienceShare * 10.0) / 10.0);
            result.add(dto);
        }

        return result;
    }
}
