package com.spedo.analytics.controller;

import com.spedo.analytics.dto.ApiResponse;
import com.spedo.analytics.entity.User;
import com.spedo.analytics.exception.ResourceNotFoundException;
import com.spedo.analytics.repository.UserRepository;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.DemoDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final UserRepository userRepository;
    private final DemoDataService demoDataService;

    public DemoController(UserRepository userRepository, DemoDataService demoDataService) {
        this.userRepository = userRepository;
        this.demoDataService = demoDataService;
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetDemoData(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        demoDataService.seedDemoDataForUser(user);
        return ResponseEntity.ok(ApiResponse.success("Demo dataset refreshed successfully", null));
    }

    @PostMapping("/purge")
    public ResponseEntity<ApiResponse<String>> purgeDemoData(@AuthenticationPrincipal UserPrincipal principal) {
        demoDataService.purgeDemoDataForUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("All demo data purged. Production mode active with real data only.", null));
    }
}
