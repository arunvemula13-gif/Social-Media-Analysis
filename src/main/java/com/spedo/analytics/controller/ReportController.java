package com.spedo.analytics.controller;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportDto>>> getReports(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ReportDto> reports = reportService.getUserReports(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved", reports));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReportDto>> generateReport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReportGenerateRequest request) {
        ReportDto report = reportService.generateReport(principal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Report generation complete", report), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportDto>> getReportById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        ReportDto report = reportService.getReportById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Report details retrieved", report));
    }

    @GetMapping(value = "/{id}/csv", produces = "text/csv")
    public ResponseEntity<String> downloadCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {

        Long effectiveUserId = principal != null ? principal.getId() : (userId != null ? userId : 1L);
        String csvContent = reportService.generateCsv(effectiveUserId, id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"spedo_report_" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvContent);
    }
}
