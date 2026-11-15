package com.spedo.analytics.controller;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.entity.SocialPlatform;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.service.PostService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final com.spedo.analytics.service.CommentSentimentService commentSentimentService;

    public PostController(PostService postService,
                          com.spedo.analytics.service.CommentSentimentService commentSentimentService) {
        this.postService = postService;
        this.commentSentimentService = commentSentimentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PostDto>>> getPosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) SocialPlatform platform,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<PostDto> posts = postService.getPosts(
                principal.getId(), platform, startDate, endDate, query, sortBy, sortDir, page, size);
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved", posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> getPostById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        PostDto post = postService.getPostById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Post details retrieved", post));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDto>> createPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @jakarta.validation.Valid @RequestBody CreatePostRequest request) {
        PostDto created = postService.createPost(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Post created and indexed", created));
    }

    @GetMapping("/sentiment-summary")
    public ResponseEntity<ApiResponse<com.spedo.analytics.dto.OverallSentimentSummaryDto>> getOverallSentimentSummary(
            @AuthenticationPrincipal UserPrincipal principal) {
        com.spedo.analytics.dto.OverallSentimentSummaryDto summary = postService.getOverallSentimentSummary(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Overall sentiment summary retrieved", summary));
    }

    @GetMapping("/top-performing")
    public ResponseEntity<ApiResponse<List<PostDto>>> getTopPerforming(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit) {
        List<PostDto> posts = postService.getTopPerformingPosts(principal.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success("Top performing posts retrieved", posts));
    }

    @GetMapping("/{id}/sentiment")
    public ResponseEntity<ApiResponse<PostSentimentSummaryDto>> getPostSentiment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        PostSentimentSummaryDto summary = postService.getOrSyncPostSentiment(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Post sentiment analysis generated via Java NLP", summary));
    }

    @PostMapping("/{id}/sync-comments")
    public ResponseEntity<ApiResponse<PostSentimentSummaryDto>> syncPostComments(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        PostSentimentSummaryDto summary = postService.syncCommentsForPost(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Comments synced and sentiment re-evaluated successfully", summary));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<PostCommentDto>> addComment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> payload) {
        postService.getPostById(principal.getId(), id);
        String text = payload.getOrDefault("commentText", payload.getOrDefault("text", ""));
        String author = payload.getOrDefault("author", principal.getFullName());
        String avatar = payload.getOrDefault("authorAvatar", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100");

        PostCommentDto comment = commentSentimentService.addCommentAndAnalyze(id, author, avatar, text);
        return ResponseEntity.ok(ApiResponse.success("Comment analyzed and indexed", comment));
    }
}
