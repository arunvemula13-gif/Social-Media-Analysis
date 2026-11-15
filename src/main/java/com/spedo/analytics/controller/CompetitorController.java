package com.spedo.analytics.controller;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.entity.SocialPlatform;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.CompetitorService;
import com.spedo.analytics.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/competitors")
public class CompetitorController {

    private final CompetitorService competitorService;
    private final EmailService emailService;

    public CompetitorController(CompetitorService competitorService, EmailService emailService) {
        this.competitorService = competitorService;
        this.emailService = emailService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompetitorDto>>> getCompetitors(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<CompetitorDto> competitors = competitorService.getCompetitors(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Competitors list retrieved", competitors));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompetitorDto>> addCompetitor(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompetitorRequest request) {
        CompetitorDto competitor = competitorService.addCompetitor(principal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Competitor added successfully", competitor), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompetitorDto>> getCompetitorById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        CompetitorDto competitor = competitorService.getCompetitorById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Competitor details retrieved", competitor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompetitorDto>> updateCompetitor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody CompetitorRequest request) {
        CompetitorDto competitor = competitorService.updateCompetitor(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Competitor updated successfully", competitor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompetitor(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        competitorService.deleteCompetitor(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Competitor removed successfully", null));
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<List<CompetitorBenchmarkDto>>> compareCompetitors(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) SocialPlatform platform,
            @RequestParam(defaultValue = "engagement") String sortBy) {
        List<CompetitorBenchmarkDto> comparison = competitorService.compareCompetitors(principal.getId(), platform, sortBy);
        return ResponseEntity.ok(ApiResponse.success("Competitor comparison benchmarks retrieved", comparison));
    }

    @GetMapping("/compare-platforms")
    public ResponseEntity<ApiResponse<PlatformHeadToHeadDto>> comparePlatforms(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "YOUTUBE") SocialPlatform platformA,
            @RequestParam(required = false) String channelA,
            @RequestParam(defaultValue = "YOUTUBE") SocialPlatform platformB,
            @RequestParam(required = false) String channelB) {
        PlatformHeadToHeadDto comparison = competitorService.compareTwoPlatforms(principal.getId(), platformA, channelA, platformB, channelB);
        return ResponseEntity.ok(ApiResponse.success("Platform comparison generated successfully", comparison));
    }

    @PostMapping("/compare-platforms/email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendPlatformComparisonEmail(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PlatformEmailReportRequest request) {
        Map<String, Object> result = competitorService.sendPlatformComparisonEmail(
                principal.getId(),
                request.getPlatformA(),
                request.getChannelA(),
                request.getPlatformB(),
                request.getChannelB(),
                request.getRecipientEmail(),
                request.getCustomNotes()
        );
        String msg = "SENT_VIA_SMTP".equals(result.get("deliveryStatus"))
                ? "Comparison report successfully delivered to " + result.get("recipientEmail") + " via SMTP"
                : "Comparison report generated & ready for dispatch to " + result.get("recipientEmail");
        return ResponseEntity.ok(ApiResponse.success(msg, result));
    }

    @GetMapping("/smtp-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSmtpStatus() {
        return ResponseEntity.ok(ApiResponse.success("SMTP mail gateway status", emailService.getSmtpStatus()));
    }

    @PostMapping("/smtp-config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateSmtpConfig(
            @RequestBody Map<String, String> payload) {
        Map<String, Object> updated = emailService.updateSmtpConfig(
                payload.get("host"),
                payload.get("port"),
                payload.get("username"),
                payload.get("password")
        );
        return ResponseEntity.ok(ApiResponse.success("SMTP mail gateway settings updated", updated));
    }

    @PostMapping("/test-smtp")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testSmtp(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) Map<String, String> payload) {
        String testEmail = (payload != null && payload.get("recipientEmail") != null && !payload.get("recipientEmail").isBlank())
                ? payload.get("recipientEmail")
                : principal.getUsername();
        Map<String, Object> testResult = emailService.sendTestEmail(testEmail);
        return ResponseEntity.ok(ApiResponse.success((String) testResult.get("message"), testResult));
    }
}
