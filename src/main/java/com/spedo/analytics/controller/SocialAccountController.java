package com.spedo.analytics.controller;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.SocialAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/social-accounts")
public class SocialAccountController {

    private final SocialAccountService socialAccountService;

    public SocialAccountController(SocialAccountService socialAccountService) {
        this.socialAccountService = socialAccountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SocialAccountDto>>> getAccounts(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<SocialAccountDto> accounts = socialAccountService.getUserAccounts(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Social accounts retrieved", accounts));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SocialAccountDto>> connectAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SocialAccountConnectRequest request) {
        SocialAccountDto account = socialAccountService.connectAccount(principal.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Account connected successfully", account), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> disconnectAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        socialAccountService.disconnectAccount(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Account disconnected successfully", null));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponse<SocialAccountDto>> syncAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        SocialAccountDto account = socialAccountService.syncAccount(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Account synchronization completed", account));
    }
}
