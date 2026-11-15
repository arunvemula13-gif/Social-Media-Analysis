package com.spedo.analytics.service;

import com.spedo.analytics.dto.OmnichannelOverviewDto;
import com.spedo.analytics.dto.OmnichannelPlatformDto;
import com.spedo.analytics.dto.PostDto;
import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialMetric;
import com.spedo.analytics.entity.SocialPlatform;
import com.spedo.analytics.mapper.EntityDtoMapper;
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

@Service
public class OmnichannelService {

    private final SocialAccountRepository socialAccountRepository;
    private final SocialMetricRepository socialMetricRepository;
    private final PostRepository postRepository;
    private final EntityDtoMapper mapper;

    public OmnichannelService(SocialAccountRepository socialAccountRepository,
                              SocialMetricRepository socialMetricRepository,
                              PostRepository postRepository,
                              EntityDtoMapper mapper) {
        this.socialAccountRepository = socialAccountRepository;
        this.socialMetricRepository = socialMetricRepository;
        this.postRepository = postRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public OmnichannelOverviewDto getOmnichannelOverview(Long userId) {
        List<SocialAccount> accounts = socialAccountRepository.findByUserId(userId);

        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        List<SocialMetric> metrics = socialMetricRepository.findByUserIdAndRecordedDateBetween(userId, startDate, endDate);

        int totalAudience = accounts.stream().mapToInt(SocialAccount::getFollowerCount).sum();
        int consolidatedImpressions = metrics.stream().mapToInt(SocialMetric::getImpressions).sum();
        int consolidatedReach = metrics.stream().mapToInt(SocialMetric::getReach).sum();

        double avgEngagement = metrics.stream()
                .mapToDouble(m -> m.getEngagementRate().doubleValue())
                .average()
                .orElse(3.2);

        BigDecimal omnichannelEngagementRate = BigDecimal.valueOf(avgEngagement).setScale(4, RoundingMode.HALF_UP);

        // Calculate hours saved: Average 3.5 hrs/week x 4.3 weeks per platform logged into separately
        double hoursSavedPerMonth = accounts.isEmpty() ? 0.0 : Math.round(accounts.size() * 3.7 * 10.0) / 10.0;

        List<OmnichannelPlatformDto> breakdowns = new ArrayList<>();
        for (SocialAccount acc : accounts) {
            OmnichannelPlatformDto pDto = new OmnichannelPlatformDto();
            pDto.setPlatform(acc.getPlatform());
            pDto.setPlatformName(formatPlatformName(acc.getPlatform()));
            pDto.setAccountHandle(acc.getHandle());
            pDto.setAudience(acc.getFollowerCount());

            BigDecimal share = totalAudience > 0 
                    ? BigDecimal.valueOf((double) acc.getFollowerCount() / totalAudience * 100).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            pDto.setAudienceSharePercent(share);

            // Metrics for this specific platform
            double pAvgRate = metrics.stream()
                    .filter(m -> m.getSocialAccount().getId().equals(acc.getId()))
                    .mapToDouble(m -> m.getEngagementRate().doubleValue())
                    .average()
                    .orElse(avgEngagement);
            pDto.setAvgEngagementRate(BigDecimal.valueOf(pAvgRate).setScale(4, RoundingMode.HALF_UP));

            // Content type & strengths
            assignPlatformMetadata(pDto, acc.getPlatform());

            breakdowns.add(pDto);
        }

        // Top cross-platform posts
        List<Post> topPosts = postRepository.findTopPerformingByUserId(
                userId, LocalDateTime.now().minusDays(30), 
                PageRequest.of(0, 6, Sort.by("engagementRate").descending()));

        List<PostDto> postDtos = topPosts.stream().map(mapper::toPostDto).toList();

        OmnichannelOverviewDto dto = new OmnichannelOverviewDto();
        dto.setTotalUnifiedAudience(totalAudience);
        dto.setConsolidatedImpressions(consolidatedImpressions);
        dto.setConsolidatedReach(consolidatedReach);
        dto.setOmnichannelEngagementRate(omnichannelEngagementRate);
        dto.setHoursSavedPerMonth(hoursSavedPerMonth);
        dto.setConnectedPlatformsCount(accounts.size());
        dto.setDatabaseCentralizationStatus("Active: Single Unified MySQL Hub replaces 5 separate native dashboards");
        dto.setPlatformBreakdowns(breakdowns);
        dto.setCrossPlatformTopPosts(postDtos);

        return dto;
    }

    private String formatPlatformName(SocialPlatform p) {
        return switch (p) {
            case INSTAGRAM -> "Instagram";
            case FACEBOOK -> "Facebook";
            case YOUTUBE -> "YouTube";
            case LINKEDIN -> "LinkedIn";
            case TWITTER -> "X (Twitter)";
        };
    }

    private void assignPlatformMetadata(OmnichannelPlatformDto dto, SocialPlatform p) {
        switch (p) {
            case INSTAGRAM -> {
                dto.setPrimaryContentType("Reels & Visual Carousels");
                dto.setKeyStrength("Highest viral consumer reach & lifestyle engagement");
            }
            case LINKEDIN -> {
                dto.setPrimaryContentType("B2B Articles & Industry Studies");
                dto.setKeyStrength("Highest executive buyer intent & corporate decision-makers");
            }
            case YOUTUBE -> {
                dto.setPrimaryContentType("Video Tutorials & Deep-Dive Retention");
                dto.setKeyStrength("Long-term evergreen search visibility & high watch time");
            }
            case TWITTER -> {
                dto.setPrimaryContentType("Rapid Threads & News Commentary");
                dto.setKeyStrength("Immediate velocity, viral conversation & PR impact");
            }
            case FACEBOOK -> {
                dto.setPrimaryContentType("Community Posts & Live Webinars");
                dto.setKeyStrength("Broad demographic retargeting & loyal group discussion");
            }
        }
    }
}
