package com.spedo.analytics.controller;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "30") int days) {
        DashboardSummaryDto summary = dashboardService.getSummary(principal.getId(), days);
        return ResponseEntity.ok(ApiResponse.success("Dashboard summary retrieved", summary));
    }

    @GetMapping("/growth")
    public ResponseEntity<ApiResponse<GrowthDataDto>> getGrowthChart(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "30") int days) {
        GrowthDataDto growthData = dashboardService.getGrowthChartData(principal.getId(), days);
        return ResponseEntity.ok(ApiResponse.success("Growth data retrieved", growthData));
    }

    @GetMapping("/platform-comparison")
    public ResponseEntity<ApiResponse<List<PlatformComparisonDto>>> getPlatformComparison(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "30") int days) {
        List<PlatformComparisonDto> comparison = dashboardService.getPlatformComparison(principal.getId(), days);
        return ResponseEntity.ok(ApiResponse.success("Platform comparison retrieved", comparison));
    }
}
