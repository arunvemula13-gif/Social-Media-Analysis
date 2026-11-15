package com.spedo.analytics.controller;

import com.spedo.analytics.dto.ApiResponse;
import com.spedo.analytics.dto.OmnichannelOverviewDto;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.OmnichannelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/omnichannel")
public class OmnichannelController {

    private final OmnichannelService omnichannelService;

    public OmnichannelController(OmnichannelService omnichannelService) {
        this.omnichannelService = omnichannelService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OmnichannelOverviewDto>> getOmnichannelOverview(
            @AuthenticationPrincipal UserPrincipal principal) {
        OmnichannelOverviewDto overview = omnichannelService.getOmnichannelOverview(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Omnichannel central database intelligence retrieved", overview));
    }
}
