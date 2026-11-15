package com.spedo.analytics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spedo.analytics.dto.CompetitorBenchmarkDto;
import com.spedo.analytics.dto.DashboardSummaryDto;
import com.spedo.analytics.dto.PlatformComparisonDto;
import com.spedo.analytics.dto.ReportDto;
import com.spedo.analytics.dto.ReportGenerateRequest;
import com.spedo.analytics.entity.*;
import com.spedo.analytics.exception.ResourceNotFoundException;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.PostRepository;
import com.spedo.analytics.repository.ReportRepository;
import com.spedo.analytics.repository.SocialAccountRepository;
import com.spedo.analytics.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DashboardService dashboardService;
    private final CompetitorService competitorService;
    private final PostRepository postRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final NotificationService notificationService;
    private final EntityDtoMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         DashboardService dashboardService,
                         CompetitorService competitorService,
                         PostRepository postRepository,
                         SocialAccountRepository socialAccountRepository,
                         NotificationService notificationService,
                         EntityDtoMapper mapper) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
        this.competitorService = competitorService;
        this.postRepository = postRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ReportDto> getUserReports(Long userId) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toReportDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportDto getReportById(Long userId, Long reportId) {
        Report report = reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with ID: " + reportId));
        return mapper.toReportDto(report);
    }

    @Transactional
    public ReportDto generateReport(Long userId, ReportGenerateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int days = (int) Math.max(1, ChronoUnit.DAYS.between(request.getDateFrom(), request.getDateTo()));

        // Collect actual compiled analytics for the report
        DashboardSummaryDto summary = dashboardService.getSummary(userId, days);
        List<PlatformComparisonDto> platformBreakdown = dashboardService.getPlatformComparison(userId, days);
        List<CompetitorBenchmarkDto> competitors = competitorService.compareCompetitors(userId, null, "engagement");

        Map<String, Object> reportPayload = new HashMap<>();
        reportPayload.put("reportTitle", request.getTitle());
        reportPayload.put("type", request.getReportType());
        reportPayload.put("dateFrom", request.getDateFrom().toString());
        reportPayload.put("dateTo", request.getDateTo().toString());
        reportPayload.put("summary", summary);
        reportPayload.put("platforms", platformBreakdown);
        reportPayload.put("competitors", competitors);

        String json;
        try {
            json = objectMapper.writeValueAsString(reportPayload);
        } catch (JsonProcessingException e) {
            json = "{}";
        }

        Report report = new Report(
                user,
                request.getTitle(),
                request.getReportType(),
                request.getDateFrom(),
                request.getDateTo(),
                request.getFileFormat()
        );
        report.setReportDataJson(json);
        report.setStatus(ReportStatus.COMPLETED);

        Report saved = reportRepository.save(report);

        // Set download URL
        String downloadUrl = (request.getFileFormat() == ReportFormat.CSV)
                ? "/api/reports/" + saved.getId() + "/csv"
                : "/api/reports/" + saved.getId() + "/download";
        saved.setDownloadUrl(downloadUrl);
        saved = reportRepository.save(saved);

        notificationService.createNotification(
                user,
                "Report Ready: " + saved.getTitle(),
                "Your " + saved.getReportType() + " report (" + saved.getFileFormat() + ") is compiled and ready.",
                NotificationType.REPORT_GENERATED,
                "/reports.html"
        );

        return mapper.toReportDto(saved);
    }

    @Transactional(readOnly = true)
    public String generateCsv(Long userId, Long reportId) {
        Report report = reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        int days = (int) Math.max(1, ChronoUnit.DAYS.between(report.getDateFrom(), report.getDateTo()));
        DashboardSummaryDto summary = dashboardService.getSummary(userId, days);
        List<PlatformComparisonDto> platforms = dashboardService.getPlatformComparison(userId, days);
        List<CompetitorBenchmarkDto> competitors = competitorService.compareCompetitors(userId, null, "engagement");

        StringBuilder sb = new StringBuilder();
        sb.append("SPEDO Social Media Analytics Report\n");
        sb.append("Title,").append(escapeCsv(report.getTitle())).append("\n");
        sb.append("Type,").append(report.getReportType()).append("\n");
        sb.append("Period,").append(report.getDateFrom()).append(" to ").append(report.getDateTo()).append("\n");
        sb.append("Generated At,").append(report.getCreatedAt()).append("\n\n");

        sb.append("SUMMARY METRICS\n");
        sb.append("Total Followers,").append(summary.getTotalFollowers()).append("\n");
        sb.append("Total Posts,").append(summary.getTotalPosts()).append("\n");
        sb.append("Total Likes,").append(summary.getTotalLikes()).append("\n");
        sb.append("Total Comments,").append(summary.getTotalComments()).append("\n");
        sb.append("Total Shares,").append(summary.getTotalShares()).append("\n");
        sb.append("Total Impressions,").append(summary.getTotalImpressions()).append("\n");
        sb.append("Total Reach,").append(summary.getTotalReach()).append("\n");
        sb.append("Average Engagement Rate %,").append(summary.getEngagementRate()).append("\n");
        sb.append("Follower Growth Rate %,").append(summary.getFollowerGrowthPercentage()).append("\n\n");

        sb.append("PLATFORM COMPARISON\n");
        sb.append("Platform,Followers,Total Engagements,Engagement Rate %,Share of Audience %\n");
        for (PlatformComparisonDto p : platforms) {
            sb.append(escapeCsv(p.getPlatformName())).append(",")
                    .append(p.getFollowers()).append(",")
                    .append(p.getTotalEngagements()).append(",")
                    .append(p.getEngagementRate()).append(",")
                    .append(p.getShareOfAudiencePercentage()).append("\n");
        }
        sb.append("\n");

        sb.append("COMPETITOR BENCHMARK RANKING\n");
        sb.append("Rank,Brand Name,Platform,Followers,Follower Growth,Posting Freq/Wk,Avg Likes,Avg Comments,Avg Shares,Engagement Rate %\n");
        for (CompetitorBenchmarkDto c : competitors) {
            sb.append(c.getRank()).append(",")
                    .append(escapeCsv(c.getName())).append(",")
                    .append(c.getPlatform() != null ? c.getPlatform().name() : "ALL").append(",")
                    .append(c.getFollowers()).append(",")
                    .append(c.getFollowerGrowth()).append(",")
                    .append(c.getPostingFrequencyPerWeek()).append(",")
                    .append(c.getAvgLikes()).append(",")
                    .append(c.getAvgComments()).append(",")
                    .append(c.getAvgShares()).append(",")
                    .append(c.getEngagementRate()).append("\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
