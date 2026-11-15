package com.spedo.analytics.service;

import com.spedo.analytics.dto.CreatePostRequest;
import com.spedo.analytics.dto.PagedResponse;
import com.spedo.analytics.dto.PostDto;
import com.spedo.analytics.dto.PostSentimentSummaryDto;
import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.PostComment;
import com.spedo.analytics.entity.PostType;
import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialPlatform;
import com.spedo.analytics.exception.ResourceNotFoundException;
import com.spedo.analytics.integration.YouTubeAdapter;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.PostCommentRepository;
import com.spedo.analytics.repository.PostRepository;
import com.spedo.analytics.repository.SocialAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final EntityDtoMapper mapper;
    private final CommentSentimentService commentSentimentService;
    private final YouTubeAdapter youTubeAdapter;

    public PostService(PostRepository postRepository,
                       PostCommentRepository postCommentRepository,
                       SocialAccountRepository socialAccountRepository,
                       EntityDtoMapper mapper,
                       @Lazy CommentSentimentService commentSentimentService,
                       @Lazy YouTubeAdapter youTubeAdapter) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.mapper = mapper;
        this.commentSentimentService = commentSentimentService;
        this.youTubeAdapter = youTubeAdapter;
    }

    /**
     * Calculates engagement rate using the standard formula:
     * Engagement Rate = ((Likes + Comments + Shares) / Denominator) * 100
     * where Denominator prefers impressions, falls back to reach, then follower count.
     */
    public static BigDecimal calculateEngagementRate(int likes, int comments, int shares, int impressions, int reach, int followers) {
        long totalInteractions = (long) likes + comments + shares;
        long denominator = impressions > 0 ? impressions : (reach > 0 ? reach : followers);

        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf((double) totalInteractions / denominator * 100)
                .setScale(4, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PostDto> getPosts(Long userId, SocialPlatform platform, LocalDateTime startDate,
                                           LocalDateTime endDate, String query, String sortBy,
                                           String sortDir, int page, int size) {

        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        Page<Post> postsPage = postRepository.searchPosts(
                userId,
                platform,
                startDate,
                endDate,
                (query != null && !query.isBlank()) ? query.trim() : null,
                pageable
        );

        List<PostDto> content = postsPage.getContent().stream()
                .map(mapper::toPostDto)
                .toList();

        return new PagedResponse<>(
                content,
                postsPage.getNumber(),
                postsPage.getSize(),
                postsPage.getTotalElements(),
                postsPage.getTotalPages(),
                postsPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PostDto getPostById(Long userId, Long postId) {
        Post post = postRepository.findByIdAndSocialAccountUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
        return mapper.toPostDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostDto> getTopPerformingPosts(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limit), Sort.by("engagementRate").descending());
        List<Post> posts = postRepository.findTopPerformingByUserId(userId, LocalDateTime.now().minusDays(30), pageable);
        return posts.stream().map(mapper::toPostDto).toList();
    }

    @Transactional
    public PostDto createPost(Long userId, CreatePostRequest request) {
        SocialAccount account = socialAccountRepository.findByIdAndUserId(request.getSocialAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found"));

        Post post = new Post();
        post.setSocialAccount(account);
        post.setPlatform(account.getPlatform());
        post.setPlatformPostId("post_real_" + System.currentTimeMillis());
        post.setCaption(request.getCaption() != null ? request.getCaption().trim() : "");
        post.setMediaUrl(request.getMediaUrl() != null && !request.getMediaUrl().isBlank()
                ? request.getMediaUrl().trim()
                : "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800");
        post.setPostUrl(request.getPostUrl() != null && !request.getPostUrl().isBlank()
                ? request.getPostUrl().trim()
                : "https://" + account.getPlatform().name().toLowerCase() + ".com");
        post.setPostType(request.getPostType() != null ? request.getPostType() : PostType.IMAGE);
        post.setPublishedAt(LocalDateTime.now());
        post.setLikesCount(request.getLikesCount());
        post.setCommentsCount(request.getCommentsCount());
        post.setSharesCount(request.getSharesCount());
        post.setImpressionsCount(request.getImpressionsCount());
        post.setReachCount(request.getReachCount());

        BigDecimal er = calculateEngagementRate(
                request.getLikesCount(),
                request.getCommentsCount(),
                request.getSharesCount(),
                request.getImpressionsCount(),
                request.getReachCount(),
                account.getFollowerCount()
        );
        post.setEngagementRate(er);

        Post saved = postRepository.save(post);

        // Analyze any attached customer comments with Java NLP
        if (request.getComments() != null && !request.getComments().isEmpty()) {
            for (String commentText : request.getComments()) {
                if (commentText != null && !commentText.isBlank()) {
                    commentSentimentService.addCommentAndAnalyze(
                            saved.getId(),
                            "Verified Follower",
                            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                            commentText
                    );
                }
            }
        }

        return mapper.toPostDto(saved);
    }

    @Transactional(readOnly = true)
    public com.spedo.analytics.dto.OverallSentimentSummaryDto getOverallSentimentSummary(Long userId) {
        List<Post> posts = postRepository.findBySocialAccountUserId(userId);
        com.spedo.analytics.dto.OverallSentimentSummaryDto dto = new com.spedo.analytics.dto.OverallSentimentSummaryDto();
        dto.setTotalPosts(posts.size());

        int ytCount = 0;
        int otherCount = 0;
        long totalImpr = 0;
        long totalLikes = 0;
        long totalComments = 0;

        int newsPos = 0;
        int newsNeu = 0;
        int newsNeg = 0;

        int postsWithComments = 0;
        double sumPosPercent = 0.0;
        double sumNeuPercent = 0.0;
        double sumNegPercent = 0.0;
        double sumScore = 0.0;
        int vanityCount = 0;

        Map<String, Integer> platformMap = new HashMap<>();

        for (Post p : posts) {
            String plat = p.getPlatform() != null ? p.getPlatform().name() : "UNKNOWN";
            platformMap.merge(plat, 1, Integer::sum);

            if ("YOUTUBE".equalsIgnoreCase(plat)) {
                ytCount++;
            } else {
                otherCount++;
            }

            totalImpr += p.getImpressionsCount();
            totalLikes += p.getLikesCount();
            totalComments += p.getCommentsCount();

            // Analyze News Headline / Caption
            if (p.getCaption() != null && !p.getCaption().isBlank()) {
                CommentSentimentService.TextAnalysisResult newsAnalysis =
                        CommentSentimentService.analyzeText(p.getCaption());
                if ("POSITIVE".equalsIgnoreCase(newsAnalysis.sentiment)) {
                    newsPos++;
                } else if ("NEGATIVE".equalsIgnoreCase(newsAnalysis.sentiment)) {
                    newsNeg++;
                } else {
                    newsNeu++;
                }
            } else {
                newsNeu++;
            }

            if (p.getPositivePercent() != null || p.getNegativePercent() != null) {
                double posP = p.getPositivePercent() != null ? p.getPositivePercent().doubleValue() : 0.0;
                double negP = p.getNegativePercent() != null ? p.getNegativePercent().doubleValue() : 0.0;
                double neuP = Math.max(0.0, 100.0 - posP - negP);

                sumPosPercent += posP;
                sumNegPercent += negP;
                sumNeuPercent += neuP;
                if (p.getSentimentScore() != null) {
                    sumScore += p.getSentimentScore().doubleValue();
                }
                postsWithComments++;
            }

            if (p.isVanityAlert()) {
                vanityCount++;
            }
        }

        dto.setTotalYouTubeVideos(ytCount);
        dto.setTotalOtherPosts(otherCount);
        dto.setPlatformCounts(platformMap);
        dto.setTotalImpressions(totalImpr);
        dto.setTotalLikes(totalLikes);
        dto.setTotalComments(totalComments);

        int totalP = posts.size();
        dto.setNewsPositiveCount(newsPos);
        dto.setNewsNeutralCount(newsNeu);
        dto.setNewsNegativeCount(newsNeg);

        if (totalP > 0) {
            dto.setNewsPositivePercent(java.math.BigDecimal.valueOf(((double) newsPos / totalP) * 100.0).setScale(1, java.math.RoundingMode.HALF_UP));
            dto.setNewsNeutralPercent(java.math.BigDecimal.valueOf(((double) newsNeu / totalP) * 100.0).setScale(1, java.math.RoundingMode.HALF_UP));
            dto.setNewsNegativePercent(java.math.BigDecimal.valueOf(((double) newsNeg / totalP) * 100.0).setScale(1, java.math.RoundingMode.HALF_UP));
        }

        if (postsWithComments > 0) {
            dto.setCommentsPositivePercent(java.math.BigDecimal.valueOf(sumPosPercent / postsWithComments).setScale(1, java.math.RoundingMode.HALF_UP));
            dto.setCommentsNeutralPercent(java.math.BigDecimal.valueOf(sumNeuPercent / postsWithComments).setScale(1, java.math.RoundingMode.HALF_UP));
            dto.setCommentsNegativePercent(java.math.BigDecimal.valueOf(sumNegPercent / postsWithComments).setScale(1, java.math.RoundingMode.HALF_UP));
            dto.setAverageSentimentScore(java.math.BigDecimal.valueOf(sumScore / postsWithComments).setScale(2, java.math.RoundingMode.HALF_UP));
        }

        dto.setVanityAlertCount(vanityCount);

        // Overall Verdict
        if (totalP == 0) {
            dto.setOverallVerdict("No posts or videos tracked yet.");
        } else if (newsNeg > newsPos) {
            dto.setOverallVerdict("Critical / Weather & News Alert Tone Predominant: " + dto.getNewsNegativePercent() + "% adverse news coverage.");
        } else if (newsPos > newsNeg) {
            dto.setOverallVerdict("Positive News & Public Reception Active: " + dto.getNewsPositivePercent() + "% positive coverage across channels.");
        } else {
            dto.setOverallVerdict("Balanced & Informational News Coverage across channels.");
        }

        return dto;
    }

    /**
     * Retrieves the post sentiment summary. If the post is from YouTube and has
     * more comments on YouTube than currently stored in the DB, it auto-syncs
     * the full comment threads so accurate sentiment and total comments are displayed.
     */
    @Transactional
    public PostSentimentSummaryDto getOrSyncPostSentiment(Long userId, Long postId) {
        Post post = postRepository.findByIdAndSocialAccountUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        List<PostComment> existing = postCommentRepository.findByPostIdOrderByCommentedAtDesc(postId);

        // Auto-fetch if YouTube post and stored comments are noticeably fewer than YouTube's comment count
        if (post.getPlatform() == SocialPlatform.YOUTUBE && post.getCommentsCount() > 0 && existing.size() < Math.min(post.getCommentsCount(), 100)) {
            String vId = YouTubeAdapter.extractVideoId(post.getPlatformPostId());
            if (vId == null) {
                vId = YouTubeAdapter.extractVideoId(post.getPostUrl());
            }
            if (vId != null && youTubeAdapter != null) {
                try {
                    List<PostComment> freshComments = youTubeAdapter.fetchCommentsForVideo(post, vId, 200);
                    if (!freshComments.isEmpty() && freshComments.size() > existing.size()) {
                        postCommentRepository.deleteByPostId(postId);
                        for (PostComment c : freshComments) {
                            c.setPost(post);
                        }
                        postCommentRepository.saveAll(freshComments);
                        post.setCommentsCount(Math.max(post.getCommentsCount(), freshComments.size()));
                        postRepository.save(post);
                    }
                } catch (Exception e) {
                    log.warn("Automatic comment ingestion failed for post {}: {}", postId, e.getMessage());
                }
            }
        }

        return commentSentimentService.evaluatePostSentiment(postId);
    }

    /**
     * Explicit on-demand sync: fetches all comments (up to 200) from YouTube Data API v3,
     * updates the database, and recalculates accurate NLP sentiment metrics.
     */
    @Transactional
    public PostSentimentSummaryDto syncCommentsForPost(Long userId, Long postId) {
        Post post = postRepository.findByIdAndSocialAccountUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        String vId = null;
        if (post.getPlatform() == SocialPlatform.YOUTUBE) {
            vId = YouTubeAdapter.extractVideoId(post.getPlatformPostId());
            if (vId == null) {
                vId = YouTubeAdapter.extractVideoId(post.getPostUrl());
            }
        }

        if (vId != null && youTubeAdapter != null) {
            List<PostComment> freshComments = youTubeAdapter.fetchCommentsForVideo(post, vId, 250);
            if (!freshComments.isEmpty()) {
                postCommentRepository.deleteByPostId(postId);
                for (PostComment c : freshComments) {
                    c.setPost(post);
                }
                postCommentRepository.saveAll(freshComments);
                post.setCommentsCount(Math.max(post.getCommentsCount(), freshComments.size()));
                postRepository.save(post);
            }
        }

        return commentSentimentService.evaluatePostSentiment(postId);
    }
}
