package com.spedo.analytics.controller;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.GiveawayService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/giveaways")
public class GiveawayController {

    private final GiveawayService giveawayService;

    public GiveawayController(GiveawayService giveawayService) {
        this.giveawayService = giveawayService;
    }

    @PostMapping("/fetch-participants")
    public ResponseEntity<ApiResponse<FetchGiveawayParticipantsResponse>> fetchParticipants(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody FetchGiveawayParticipantsRequest request) {
        FetchGiveawayParticipantsResponse resp = giveawayService.fetchParticipants(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Participants parsed and filtered successfully", resp));
    }

    @PostMapping("/draw")
    public ResponseEntity<ApiResponse<GiveawayDto>> drawGiveaway(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DrawGiveawayRequest request) {
        GiveawayDto result = giveawayService.drawGiveaway(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Giveaway winners selected using Java SecureRandom", result));
    }

    @PostMapping("/{id}/reroll")
    public ResponseEntity<ApiResponse<GiveawayDto>> rerollWinner(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false) Long winnerId,
            @RequestParam(defaultValue = "false") boolean isBackup) {
        GiveawayDto result = giveawayService.rerollWinner(principal.getId(), id, winnerId, isBackup);
        return ResponseEntity.ok(ApiResponse.success("Winner rerolled with SecureRandom", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<GiveawayDto>>> getGiveawayHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GiveawayDto> pageResult = giveawayService.getGiveawayHistory(principal.getId(), page, size);
        PagedResponse<GiveawayDto> paged = new PagedResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
        return ResponseEntity.ok(ApiResponse.success("Giveaway history retrieved", paged));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GiveawayDto>> getGiveawayById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        GiveawayDto dto = giveawayService.getGiveawayById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Giveaway details retrieved", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGiveaway(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        giveawayService.deleteGiveaway(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Giveaway deleted successfully", null));
    }

    @GetMapping("/{id}/export-result")
    public ResponseEntity<byte[]> exportResult(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        byte[] htmlData = giveawayService.exportGiveawayHtml(principal.getId(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"result.html\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                .contentType(MediaType.parseMediaType("text/html; charset=UTF-8"))
                .body(htmlData);
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<byte[]> viewResult(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        byte[] htmlData = giveawayService.exportGiveawayHtml(principal.getId(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"result.html\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                .contentType(MediaType.parseMediaType("text/html; charset=UTF-8"))
                .body(htmlData);
    }

    @GetMapping("/{id}/export-csv")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        byte[] csvData = giveawayService.exportGiveawayCsv(principal.getId(), id);
        String filename = "Giveaway_Winners_" + id + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
