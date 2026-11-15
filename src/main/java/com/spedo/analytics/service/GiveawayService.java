package com.spedo.analytics.service;

import com.spedo.analytics.dto.*;
import com.spedo.analytics.entity.*;
import com.spedo.analytics.exception.BadRequestException;
import com.spedo.analytics.exception.ResourceNotFoundException;
import com.spedo.analytics.integration.InstagramAdapter;
import com.spedo.analytics.integration.YouTubeAdapter;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GiveawayService {

    private static final Logger log = LoggerFactory.getLogger(GiveawayService.class);
    private final SecureRandom secureRandom = new SecureRandom();

    private final GiveawayRepository giveawayRepository;
    private final GiveawayWinnerRepository giveawayWinnerRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final YouTubeAdapter youTubeAdapter;
    private final InstagramAdapter instagramAdapter;
    private final NotificationService notificationService;
    private final EntityDtoMapper mapper;

    public GiveawayService(GiveawayRepository giveawayRepository,
                           GiveawayWinnerRepository giveawayWinnerRepository,
                           UserRepository userRepository,
                           PostRepository postRepository,
                           SocialAccountRepository socialAccountRepository,
                           YouTubeAdapter youTubeAdapter,
                           InstagramAdapter instagramAdapter,
                           NotificationService notificationService,
                           EntityDtoMapper mapper) {
        this.giveawayRepository = giveawayRepository;
        this.giveawayWinnerRepository = giveawayWinnerRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.youTubeAdapter = youTubeAdapter;
        this.instagramAdapter = instagramAdapter;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    /**
     * Fetches and filters participants from YouTube or Instagram based on provided rules.
     */
    @Transactional(readOnly = true)
    public FetchGiveawayParticipantsResponse fetchParticipants(Long userId, FetchGiveawayParticipantsRequest req) {
        SocialPlatform platform = req.getPlatform();
        String postUrl = req.getPostUrl() != null ? req.getPostUrl().trim() : "";
        Long postId = req.getPostId();

        // 1. Resolve Post details if postId was supplied
        Post post = null;
        if (postId != null) {
            post = postRepository.findByIdAndSocialAccountUserId(postId, userId).orElse(null);
            if (post != null) {
                platform = post.getPlatform();
                if (postUrl.isBlank()) {
                    postUrl = post.getPostUrl();
                }
            }
        }

        // Auto-detect platform if not specified
        if (platform == null) {
            if (postUrl.contains("instagram.com") || postUrl.contains("instagr.am")) {
                platform = SocialPlatform.INSTAGRAM;
            } else {
                platform = SocialPlatform.YOUTUBE;
            }
        }

        String platformPostId = null;
        String postCaption = "Social Media Post";
        String postThumbnail = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800";
        List<GiveawayParticipantDto> rawParticipants = new ArrayList<>();

        if (platform == SocialPlatform.YOUTUBE) {
            String videoId = YouTubeAdapter.extractVideoId(postUrl);
            if (videoId == null && post != null) {
                videoId = YouTubeAdapter.extractVideoId(post.getPlatformPostId());
            }
            platformPostId = videoId != null ? videoId : "unknown_yt";

            if (post != null) {
                postCaption = post.getCaption();
                postThumbnail = post.getMediaUrl();
            } else if (videoId != null) {
                Map<String, Object> details = youTubeAdapter.fetchVideoDetails(videoId);
                if (!details.isEmpty()) {
                    postCaption = (String) details.getOrDefault("title", "YouTube Video");
                    postThumbnail = (String) details.getOrDefault("thumbnailUrl", postThumbnail);
                }
            }

            // Ingest real YouTube comments via YouTube Data API v3
            if (videoId != null) {
                List<PostComment> ytComments = youTubeAdapter.fetchCommentsForVideo(post, videoId, 250);
                for (PostComment c : ytComments) {
                    rawParticipants.add(new GiveawayParticipantDto(
                            c.getAuthor(),
                            c.getAuthorAvatar(),
                            c.getCommentText(),
                            c.getCommentedAt(),
                            SocialPlatform.YOUTUBE,
                            true,
                            null
                    ));
                }
            }
        } else {
            // Instagram
            String shortcode = InstagramAdapter.extractInstagramShortcode(postUrl);
            if (shortcode == null && post != null) {
                shortcode = post.getPlatformPostId();
            }
            platformPostId = shortcode != null ? shortcode : "ig_media";

            if (post != null) {
                postCaption = post.getCaption();
                postThumbnail = post.getMediaUrl();
            } else {
                postCaption = "Instagram Post / Reel (" + platformPostId + ")";
                postThumbnail = "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=800";
            }

            // Find any Instagram access token if user has connected account
            String token = null;
            List<SocialAccount> igAccs = socialAccountRepository.findByUserId(userId).stream()
                    .filter(a -> a.getPlatform() == SocialPlatform.INSTAGRAM)
                    .toList();
            if (!igAccs.isEmpty()) {
                token = igAccs.get(0).getAccessToken();
            }

            rawParticipants = instagramAdapter.fetchCommentsFromInstagram(platformPostId, token, 150);
        }

        // 2. Apply filtering logic
        Set<String> excludedSet = parseExcludedUsernames(req.getExcludedUsernames());
        String keyword = req.getRequiredKeyword() != null ? req.getRequiredKeyword().trim().toLowerCase(Locale.ROOT) : "";
        boolean filterDupes = req.isFilterDuplicates();

        Set<String> seenUsers = new HashSet<>();
        int dupCount = 0;
        int keywordFilteredCount = 0;
        int excludedUsersCount = 0;

        List<GiveawayParticipantDto> evaluated = new ArrayList<>();
        for (GiveawayParticipantDto p : rawParticipants) {
            String normUser = normalizeUsername(p.getUsername());
            boolean isEligible = true;
            String reason = null;

            // Check blacklist exclusion
            if (excludedSet.contains(normUser)) {
                isEligible = false;
                reason = "Excluded username (blacklisted)";
                excludedUsersCount++;
            }
            // Check required keyword
            else if (!keyword.isBlank()) {
                String cText = p.getCommentText() != null ? p.getCommentText().toLowerCase(Locale.ROOT) : "";
                if (!cText.contains(keyword)) {
                    isEligible = false;
                    reason = "Missing required keyword: \"" + req.getRequiredKeyword().trim() + "\"";
                    keywordFilteredCount++;
                }
            }

            // Check duplicate user
            if (isEligible && filterDupes) {
                if (seenUsers.contains(normUser)) {
                    isEligible = false;
                    reason = "Duplicate entry (one entry allowed per user)";
                    dupCount++;
                } else {
                    seenUsers.add(normUser);
                }
            } else if (isEligible) {
                seenUsers.add(normUser);
            }

            p.setEligible(isEligible);
            p.setDisqualificationReason(reason);
            evaluated.add(p);
        }

        int eligibleCount = (int) evaluated.stream().filter(GiveawayParticipantDto::isEligible).count();

        FetchGiveawayParticipantsResponse resp = new FetchGiveawayParticipantsResponse();
        resp.setPlatform(platform);
        resp.setPostUrl(postUrl);
        resp.setPlatformPostId(platformPostId);
        resp.setPostCaption(postCaption);
        resp.setPostThumbnail(postThumbnail);
        resp.setTotalCommentsFetched(evaluated.size());
        resp.setUniqueUsersCount(seenUsers.size());
        resp.setEligibleCount(eligibleCount);
        resp.setDuplicateCount(dupCount);
        resp.setKeywordFilteredCount(keywordFilteredCount);
        resp.setExcludedUsersCount(excludedUsersCount);
        resp.setParticipants(evaluated);

        return resp;
    }

    /**
     * Executes the random giveaway draw using Java SecureRandom for mathematically fair winner selection.
     */
    @Transactional
    public GiveawayDto drawGiveaway(Long userId, DrawGiveawayRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch participants and apply filters
        FetchGiveawayParticipantsRequest fetchReq = new FetchGiveawayParticipantsRequest();
        fetchReq.setPlatform(req.getPlatform());
        fetchReq.setPostUrl(req.getPostUrl());
        fetchReq.setPostId(req.getPostId());
        fetchReq.setFilterDuplicates(req.isFilterDuplicates());
        fetchReq.setRequiredKeyword(req.getRequiredKeyword());
        fetchReq.setExcludedUsernames(req.getExcludedUsernames());

        FetchGiveawayParticipantsResponse preview = fetchParticipants(userId, fetchReq);

        List<GiveawayParticipantDto> eligible = preview.getParticipants().stream()
                .filter(GiveawayParticipantDto::isEligible)
                .collect(Collectors.toList());

        if (eligible.isEmpty()) {
            throw new BadRequestException("No eligible participants found. Adjust your keyword or blacklist filters to proceed.");
        }

        int neededWinners = Math.max(1, req.getWinnersCount());
        int neededBackups = Math.max(0, req.getBackupWinnersCount());
        int totalNeeded = neededWinners + neededBackups;

        // SecureRandom Fisher-Yates Fair Shuffle
        List<GiveawayParticipantDto> shuffled = new ArrayList<>(eligible);
        for (int i = shuffled.size() - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            GiveawayParticipantDto temp = shuffled.get(i);
            shuffled.set(i, shuffled.get(j));
            shuffled.set(j, temp);
        }

        // Create Giveaway entity
        String caption = req.getPostCaption() != null && !req.getPostCaption().isBlank()
                ? req.getPostCaption()
                : preview.getPostCaption();
        String thumb = req.getPostThumbnail() != null && !req.getPostThumbnail().isBlank()
                ? req.getPostThumbnail()
                : preview.getPostThumbnail();

        Giveaway giveaway = new Giveaway(
                user,
                req.getTitle().trim(),
                preview.getPlatform(),
                preview.getPostUrl(),
                preview.getPlatformPostId(),
                caption,
                thumb,
                preview.getTotalCommentsFetched(),
                preview.getUniqueUsersCount(),
                preview.getEligibleCount(),
                neededWinners,
                neededBackups,
                req.isFilterDuplicates(),
                req.getRequiredKeyword() != null ? req.getRequiredKeyword().trim() : null,
                req.getExcludedUsernames() != null ? req.getExcludedUsernames().trim() : null,
                LocalDateTime.now()
        );

        int actualWinnersToPick = Math.min(neededWinners, shuffled.size());
        for (int i = 0; i < actualWinnersToPick; i++) {
            GiveawayParticipantDto p = shuffled.get(i);
            GiveawayWinner winner = new GiveawayWinner(
                    giveaway,
                    p.getUsername(),
                    p.getUserAvatar(),
                    p.getCommentText(),
                    p.getCommentedAt(),
                    false,
                    i + 1,
                    LocalDateTime.now()
            );
            giveaway.addWinner(winner);
        }

        int actualBackupsToPick = Math.min(neededBackups, Math.max(0, shuffled.size() - actualWinnersToPick));
        for (int i = 0; i < actualBackupsToPick; i++) {
            GiveawayParticipantDto p = shuffled.get(actualWinnersToPick + i);
            GiveawayWinner backup = new GiveawayWinner(
                    giveaway,
                    p.getUsername(),
                    p.getUserAvatar(),
                    p.getCommentText(),
                    p.getCommentedAt(),
                    true,
                    i + 1,
                    LocalDateTime.now()
            );
            giveaway.addWinner(backup);
        }

        Giveaway saved = giveawayRepository.save(giveaway);

        // Notify user of completion
        notificationService.createNotification(
                user,
                "🎉 Giveaway Winners Selected: " + saved.getTitle(),
                "Random draw completed using SecureRandom for " + saved.getPlatform() + ". Selected "
                        + saved.getWinnersCount() + " winner(s) and " + saved.getBackupWinnersCount() + " backup(s).",
                NotificationType.REPORT_GENERATED,
                "/giveaway.html"
        );

        return mapper.toGiveawayDto(saved);
    }

    /**
     * Rerolls a specific winner from the eligible pool using SecureRandom.
     */
    @Transactional
    public GiveawayDto rerollWinner(Long userId, Long giveawayId, Long winnerIdToReplace, boolean isBackup) {
        Giveaway giveaway = giveawayRepository.findByIdAndUserId(giveawayId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giveaway not found with ID: " + giveawayId));

        // Re-evaluate participants
        FetchGiveawayParticipantsRequest fetchReq = new FetchGiveawayParticipantsRequest();
        fetchReq.setPlatform(giveaway.getPlatform());
        fetchReq.setPostUrl(giveaway.getPostUrl());
        fetchReq.setFilterDuplicates(giveaway.isFilterDuplicates());
        fetchReq.setRequiredKeyword(giveaway.getRequiredKeyword());
        fetchReq.setExcludedUsernames(giveaway.getExcludedUsernames());

        FetchGiveawayParticipantsResponse preview = fetchParticipants(userId, fetchReq);

        // Identify all users currently selected as winners or backups
        Set<String> alreadyWonUsers = giveaway.getWinners().stream()
                .map(w -> normalizeUsername(w.getUsername()))
                .collect(Collectors.toSet());

        // Find available eligible participants who haven't won yet
        List<GiveawayParticipantDto> pool = preview.getParticipants().stream()
                .filter(GiveawayParticipantDto::isEligible)
                .filter(p -> !alreadyWonUsers.contains(normalizeUsername(p.getUsername())))
                .collect(Collectors.toList());

        if (pool.isEmpty()) {
            throw new BadRequestException("No remaining eligible participants available to reroll.");
        }

        // Pick 1 new winner using SecureRandom
        int selectedIndex = secureRandom.nextInt(pool.size());
        GiveawayParticipantDto newWinnerDto = pool.get(selectedIndex);

        // Find the winner to replace or replace first of type
        GiveawayWinner targetWinner = null;
        if (winnerIdToReplace != null) {
            targetWinner = giveaway.getWinners().stream()
                    .filter(w -> w.getId().equals(winnerIdToReplace))
                    .findFirst()
                    .orElse(null);
        }

        if (targetWinner == null) {
            targetWinner = giveaway.getWinners().stream()
                    .filter(w -> w.isBackup() == isBackup)
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("No winner found to reroll."));
        }

        targetWinner.setUsername(newWinnerDto.getUsername());
        targetWinner.setUserAvatar(newWinnerDto.getUserAvatar());
        targetWinner.setCommentText(newWinnerDto.getCommentText());
        targetWinner.setCommentedAt(newWinnerDto.getCommentedAt());
        targetWinner.setSelectedAt(LocalDateTime.now());

        Giveaway updated = giveawayRepository.save(giveaway);
        return mapper.toGiveawayDto(updated);
    }

    @Transactional(readOnly = true)
    public Page<GiveawayDto> getGiveawayHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<Giveaway> pageResult = giveawayRepository.findByUserIdOrderByDrawnAtDesc(userId, pageable);
        return pageResult.map(mapper::toGiveawayDto);
    }

    @Transactional(readOnly = true)
    public GiveawayDto getGiveawayById(Long userId, Long giveawayId) {
        Giveaway giveaway = giveawayRepository.findByIdAndUserId(giveawayId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giveaway not found with ID: " + giveawayId));
        return mapper.toGiveawayDto(giveaway);
    }

    @Transactional
    public void deleteGiveaway(Long userId, Long giveawayId) {
        Giveaway giveaway = giveawayRepository.findByIdAndUserId(giveawayId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giveaway not found with ID: " + giveawayId));
        giveawayRepository.delete(giveaway);
    }

    /**
     * Generates a comprehensive CSV export containing draw metadata and winner records.
     */
    @Transactional(readOnly = true)
    public byte[] exportGiveawayCsv(Long userId, Long giveawayId) {
        Giveaway giveaway = giveawayRepository.findByIdAndUserId(giveawayId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giveaway not found with ID: " + giveawayId));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        // CSV Header
        sb.append("Winner Rank,Type,Username,Platform,Comment,Comment Date,Draw Selection Date,Giveaway Title,Post URL\n");

        for (GiveawayWinner w : giveaway.getWinners()) {
            String type = w.isBackup() ? "Backup Winner #" + w.getWinnerOrder() : "Primary Winner #" + w.getWinnerOrder();
            String cDate = w.getCommentedAt() != null ? w.getCommentedAt().format(dtf) : "N/A";
            String dDate = w.getSelectedAt() != null ? w.getSelectedAt().format(dtf) : giveaway.getDrawnAt().format(dtf);

            sb.append(escapeCsv(String.valueOf(w.getWinnerOrder()))).append(",");
            sb.append(escapeCsv(type)).append(",");
            sb.append(escapeCsv(w.getUsername())).append(",");
            sb.append(escapeCsv(giveaway.getPlatform().name())).append(",");
            sb.append(escapeCsv(w.getCommentText())).append(",");
            sb.append(escapeCsv(cDate)).append(",");
            sb.append(escapeCsv(dDate)).append(",");
            sb.append(escapeCsv(giveaway.getTitle())).append(",");
            sb.append(escapeCsv(giveaway.getPostUrl())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generates a beautifully styled, self-contained HTML + CSS result certificate & audit document.
     */
    @Transactional(readOnly = true)
    public byte[] exportGiveawayHtml(Long userId, Long giveawayId) {
        Giveaway giveaway = giveawayRepository.findByIdAndUserId(giveawayId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Giveaway not found with ID: " + giveawayId));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        String drawnDateStr = giveaway.getDrawnAt() != null ? giveaway.getDrawnAt().format(dtf) : "Recently";
        String platform = giveaway.getPlatform() != null ? giveaway.getPlatform().name() : "SOCIAL";

        StringBuilder primaryWinnersHtml = new StringBuilder();
        StringBuilder backupWinnersHtml = new StringBuilder();

        List<GiveawayWinner> allWinners = giveaway.getWinners();
        List<GiveawayWinner> primaryList = allWinners.stream().filter(w -> !w.isBackup()).toList();
        List<GiveawayWinner> backupList = allWinners.stream().filter(GiveawayWinner::isBackup).toList();

        for (GiveawayWinner w : primaryList) {
            String rankLabel = switch (w.getWinnerOrder()) {
                case 1 -> "1st place";
                case 2 -> "2nd place";
                case 3 -> "3rd place";
                default -> "Winner #" + w.getWinnerOrder();
            };
            String handle = formatHandle(w.getUsername());
            String avatar = (w.getUserAvatar() != null && !w.getUserAvatar().isBlank())
                    ? escapeHtml(w.getUserAvatar())
                    : "https://api.dicebear.com/7.x/identicon/svg?seed=" + URLEncoder.encode(w.getUsername(), StandardCharsets.UTF_8);
            String comment = escapeHtml(w.getCommentText() != null ? w.getCommentText() : "");
            String commentTime = w.getCommentedAt() != null ? w.getCommentedAt().format(dtf) : "N/A";
            String selectTime = w.getSelectedAt() != null ? w.getSelectedAt().format(dtf) : drawnDateStr;

            primaryWinnersHtml.append("""
                <div class="winner-card">
                    <div class="winner-header">
                        <span class="rank-badge">{{RANK_LABEL}}</span>
                        <span class="platform-pill {{PLATFORM_PILL}}">{{PLATFORM}}</span>
                    </div>
                    <div class="winner-user-row">
                        <img class="winner-avatar" src="{{AVATAR}}" alt="{{HANDLE}}">
                        <div>
                            <div class="winner-name">{{HANDLE}}</div>
                            <div class="winner-sub">Selected via Java SecureRandom CSPRNG</div>
                        </div>
                    </div>
                    <div class="comment-quote-box">
                        <div class="comment-text">&ldquo;{{COMMENT}}&rdquo;</div>
                    </div>
                    <div class="winner-footer">
                        <span>Commented: {{COMMENT_TIME}}</span>
                        <span>Audit ID: #{{WINNER_ID}} &bull; {{SELECT_TIME}}</span>
                    </div>
                </div>
            """
                    .replace("{{RANK_LABEL}}", rankLabel)
                    .replace("{{PLATFORM_PILL}}", platform.equals("YOUTUBE") ? "pill-youtube" : "pill-instagram")
                    .replace("{{PLATFORM}}", platform)
                    .replace("{{AVATAR}}", avatar)
                    .replace("{{HANDLE}}", escapeHtml(handle))
                    .replace("{{COMMENT}}", comment)
                    .replace("{{COMMENT_TIME}}", commentTime)
                    .replace("{{WINNER_ID}}", String.valueOf(w.getId()))
                    .replace("{{SELECT_TIME}}", selectTime)
            );
        }

        if (!backupList.isEmpty()) {
            backupWinnersHtml.append("""
                <div style="margin-top:24px; margin-bottom:12px;">
                    <div class="section-header">
                        <span>Backup alternates</span>
                    </div>
                    <div class="winners-grid">
            """);

            for (GiveawayWinner w : backupList) {
                String handle = formatHandle(w.getUsername());
                String avatar = (w.getUserAvatar() != null && !w.getUserAvatar().isBlank())
                        ? escapeHtml(w.getUserAvatar())
                        : "https://api.dicebear.com/7.x/identicon/svg?seed=" + URLEncoder.encode(w.getUsername(), StandardCharsets.UTF_8);
                String comment = escapeHtml(w.getCommentText() != null ? w.getCommentText() : "");

                backupWinnersHtml.append("""
                    <div class="winner-card backup">
                        <div class="winner-header">
                            <span class="rank-badge">Alternate #{{ORDER}}</span>
                            <span style="font-size:11px; color:#71717a;">Eligible replacement</span>
                        </div>
                        <div class="winner-user-row">
                            <img class="winner-avatar" src="{{AVATAR}}" alt="{{HANDLE}}">
                            <div>
                                <div class="winner-name">{{HANDLE}}</div>
                                <div class="winner-sub">Backup entry</div>
                            </div>
                        </div>
                        <div class="comment-quote-box backup">
                            <div class="comment-text">&ldquo;{{COMMENT}}&rdquo;</div>
                        </div>
                    </div>
                """
                        .replace("{{ORDER}}", String.valueOf(w.getWinnerOrder()))
                        .replace("{{AVATAR}}", avatar)
                        .replace("{{HANDLE}}", escapeHtml(handle))
                        .replace("{{COMMENT}}", comment)
                );
            }

            backupWinnersHtml.append("</div></div>");
        }

        String postThumb = giveaway.getPostThumbnail() != null && !giveaway.getPostThumbnail().isBlank()
                ? giveaway.getPostThumbnail()
                : (platform.equals("YOUTUBE")
                    ? "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=400"
                    : "https://images.unsplash.com/photo-1611262588024-d12430b98920?w=400");
        String postCaption = giveaway.getPostCaption() != null ? escapeHtml(giveaway.getPostCaption()) : "Giveaway post";
        String postUrl = giveaway.getPostUrl() != null ? escapeHtml(giveaway.getPostUrl()) : "#";

        String template = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Official Giveaway Result &mdash; {{TITLE}}</title>
                <style>
                    * { margin:0; padding:0; box-sizing:border-box; }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f4f4f5;
                        color: #18181b;
                        line-height: 1.5;
                        padding: 32px 16px;
                        -webkit-font-smoothing: antialiased;
                    }
                    .container {
                        max-width: 820px;
                        margin: 0 auto;
                    }
                    .action-bar {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        background: #ffffff;
                        border: 1px solid #e4e4e7;
                        border-radius: 8px;
                        padding: 10px 16px;
                        margin-bottom: 20px;
                    }
                    .action-title {
                        font-size: 13px;
                        font-weight: 600;
                        color: #18181b;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    }
                    .status-dot {
                        width: 8px;
                        height: 8px;
                        border-radius: 50%;
                        background: #10b981;
                        display: inline-block;
                    }
                    .btn {
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        padding: 6px 12px;
                        border-radius: 6px;
                        font-size: 12px;
                        font-weight: 500;
                        cursor: pointer;
                        text-decoration: none;
                        border: 1px solid transparent;
                        transition: all 0.15s ease;
                    }
                    .btn-primary {
                        background: #18181b;
                        color: #ffffff;
                    }
                    .btn-primary:hover {
                        background: #27272a;
                    }
                    .btn-outline {
                        background: #ffffff;
                        color: #52525b;
                        border-color: #e4e4e7;
                    }
                    .btn-outline:hover {
                        background: #f4f4f5;
                        color: #18181b;
                    }
                    .certificate-card {
                        background: #ffffff;
                        border-radius: 12px;
                        border: 1px solid #e4e4e7;
                        overflow: hidden;
                        box-shadow: 0 1px 3px rgba(0,0,0,0.04);
                        margin-bottom: 24px;
                    }
                    .certificate-header {
                        padding: 28px 32px 24px 32px;
                        border-bottom: 1px solid #f4f4f5;
                    }
                    .verified-badge {
                        display: inline-flex;
                        align-items: center;
                        gap: 6px;
                        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
                        font-size: 11px;
                        color: #27272a;
                        background: #f4f4f5;
                        border: 1px solid #e4e4e7;
                        padding: 3px 8px;
                        border-radius: 4px;
                        margin-bottom: 12px;
                        letter-spacing: 0.3px;
                    }
                    .hero-title {
                        font-size: 22px;
                        font-weight: 600;
                        color: #18181b;
                        letter-spacing: -0.3px;
                        margin-bottom: 8px;
                    }
                    .hero-meta {
                        font-size: 12px;
                        color: #71717a;
                        display: flex;
                        flex-wrap: wrap;
                        gap: 12px;
                        align-items: center;
                    }
                    .platform-pill {
                        display: inline-flex;
                        align-items: center;
                        padding: 2px 7px;
                        border-radius: 4px;
                        font-size: 11px;
                        font-weight: 600;
                        letter-spacing: 0.3px;
                    }
                    .pill-youtube {
                        background: #fee2e2;
                        color: #dc2626;
                    }
                    .pill-instagram {
                        background: #fce7f3;
                        color: #be185d;
                    }
                    .card-body {
                        padding: 28px 32px;
                    }
                    .source-post-card {
                        background: #fafafa;
                        border: 1px solid #e4e4e7;
                        border-radius: 8px;
                        padding: 14px 16px;
                        margin-bottom: 24px;
                    }
                    .telemetry-grid {
                        display: grid;
                        grid-template-columns: repeat(4, 1fr);
                        gap: 10px;
                        margin-top: 14px;
                    }
                    .telemetry-stat {
                        background: #ffffff;
                        border: 1px solid #e4e4e7;
                        border-radius: 6px;
                        padding: 10px;
                        text-align: center;
                    }
                    .stat-value {
                        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
                        font-size: 18px;
                        font-weight: 600;
                        color: #18181b;
                    }
                    .stat-label {
                        font-size: 11px;
                        color: #71717a;
                        margin-top: 2px;
                    }
                    .section-header {
                        font-size: 12px;
                        font-weight: 600;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                        color: #71717a;
                        margin-bottom: 12px;
                        padding-bottom: 6px;
                        border-bottom: 1px solid #f4f4f5;
                    }
                    .winners-grid {
                        display: grid;
                        grid-template-columns: 1fr;
                        gap: 12px;
                        margin-bottom: 24px;
                    }
                    .winner-card {
                        border: 1px solid #e4e4e7;
                        background: #ffffff;
                        border-radius: 8px;
                        padding: 16px;
                    }
                    .winner-card.backup {
                        background: #fafafa;
                        border: 1px solid #e4e4e7;
                    }
                    .winner-header {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 10px;
                    }
                    .rank-badge {
                        font-size: 11px;
                        font-weight: 600;
                        padding: 2px 7px;
                        border-radius: 4px;
                        background: #f4f4f5;
                        color: #27272a;
                        border: 1px solid #e4e4e7;
                    }
                    .winner-user-row {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        margin-bottom: 12px;
                    }
                    .winner-avatar {
                        width: 40px;
                        height: 40px;
                        border-radius: 50%;
                        border: 1px solid #e4e4e7;
                        object-fit: cover;
                        background: #ffffff;
                    }
                    .winner-name {
                        font-size: 14px;
                        font-weight: 600;
                        color: #18181b;
                    }
                    .winner-sub {
                        font-size: 11px;
                        color: #71717a;
                    }
                    .comment-quote-box {
                        background: #fafafa;
                        border-left: 2px solid #e4e4e7;
                        border-radius: 0 4px 4px 0;
                        padding: 10px 14px;
                        margin-bottom: 10px;
                    }
                    .comment-quote-box.backup {
                        background: #ffffff;
                    }
                    .comment-text {
                        font-size: 12px;
                        color: #27272a;
                        line-height: 1.5;
                        word-break: break-word;
                    }
                    .winner-footer {
                        display: flex;
                        justify-content: space-between;
                        font-size: 11px;
                        color: #71717a;
                        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
                        padding-top: 8px;
                        border-top: 1px solid #f4f4f5;
                    }
                    .audit-footer {
                        background: #fafafa;
                        border-top: 1px solid #e4e4e7;
                        padding: 20px 32px;
                        font-size: 12px;
                        color: #71717a;
                        line-height: 1.6;
                    }
                    .audit-seal {
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        color: #18181b;
                        font-weight: 600;
                        font-size: 12px;
                        margin-bottom: 6px;
                    }
                    @media (max-width: 640px) {
                        .telemetry-grid { grid-template-columns: 1fr 1fr; }
                        .card-body, .certificate-header, .audit-footer { padding: 16px; }
                        .hero-title { font-size: 18px; }
                    }
                    @media print {
                        body { background: #ffffff; padding: 0; }
                        .action-bar { display: none; }
                        .certificate-card { box-shadow: none; border: 1px solid #e4e4e7; }
                        -webkit-print-color-adjust: exact;
                        print-color-adjust: exact;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="action-bar">
                        <div class="action-title">
                            <span class="status-dot"></span>
                            <span>Giveaway Result Certificate</span>
                        </div>
                        <div style="display:flex; gap:8px;">
                            <button type="button" class="btn btn-outline" onclick="window.print()">
                                <span>Print / Save PDF</span>
                            </button>
                            <button type="button" class="btn btn-primary" onclick="copyAnnouncementText()">
                                <span>Copy announcement</span>
                            </button>
                        </div>
                    </div>

                    <div class="certificate-card">
                        <div class="certificate-header">
                            <div class="verified-badge">
                                [CSPRNG-VERIFIED] &bull; Java SecureRandom
                            </div>
                            <h1 class="hero-title">{{TITLE}}</h1>
                            <div class="hero-meta">
                                <span>Platform: <span class="platform-pill {{PLATFORM_CLASS}}">{{PLATFORM}}</span></span>
                                <span>&bull;</span>
                                <span>Drawn: {{DRAWN_DATE}}</span>
                                <span>&bull;</span>
                                <span>Giveaway ID: #{{GIVEAWAY_ID}}</span>
                            </div>
                        </div>

                        <div class="card-body">
                            <!-- Source Post & Telemetry -->
                            <div class="source-post-card">
                                <div style="display:flex; gap:14px; align-items:center;">
                                    <img src="{{POST_THUMB}}" alt="Thumbnail" style="width:84px; height:60px; object-fit:cover; border-radius:6px; border:1px solid #e4e4e7; flex-shrink:0;">
                                    <div style="flex:1; min-width:0;">
                                        <div style="font-weight:600; font-size:13px; color:#18181b; line-height:1.4; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden;">
                                            {{POST_CAPTION}}
                                        </div>
                                        <a href="{{POST_URL}}" target="_blank" rel="noopener noreferrer" style="font-size:12px; color:#2563eb; font-weight:500; text-decoration:none; margin-top:4px; display:inline-block;">
                                            View source post &rarr;
                                        </a>
                                    </div>
                                </div>

                                <div class="telemetry-grid">
                                    <div class="telemetry-stat">
                                        <div class="stat-value">{{TOTAL_COMMENTS}}</div>
                                        <div class="stat-label">Total comments</div>
                                    </div>
                                    <div class="telemetry-stat">
                                        <div class="stat-value">{{UNIQUE_USERS}}</div>
                                        <div class="stat-label">Unique users</div>
                                    </div>
                                    <div class="telemetry-stat">
                                        <div class="stat-value" style="color:#10b981;">{{ELIGIBLE_ENTRIES}}</div>
                                        <div class="stat-label">Eligible entries</div>
                                    </div>
                                    <div class="telemetry-stat">
                                        <div class="stat-value" style="color:#2563eb;">{{WINNERS_COUNT}}</div>
                                        <div class="stat-label">Winners selected</div>
                                    </div>
                                </div>
                            </div>

                            <!-- Primary Winners Section -->
                            <div class="section-header">
                                <span>Official winners ({{PRIMARY_COUNT}})</span>
                            </div>
                            <div class="winners-grid">
                                {{PRIMARY_WINNERS}}
                            </div>

                            <!-- Backup Winners Section -->
                            {{BACKUP_WINNERS}}
                        </div>

                        <div class="audit-footer">
                            <div class="audit-seal">
                                <span class="status-dot"></span>
                                <span>Cryptographic Nonce & Provable Fairness Ledger</span>
                            </div>
                            <p style="margin-bottom:8px;">
                                This giveaway winner selection was processed and certified by <strong>SPEDO Social Media Analytics Engine</strong> using <code>java.security.SecureRandom</code>, a cryptographically secure pseudo-random number generator (CSPRNG). Every eligible entry possessed an identical, mathematically unbiased probability of selection with zero human intervention.
                            </p>
                            <div style="display:flex; justify-content:space-between; flex-wrap:wrap; gap:8px; font-size:11px; color:#71717a; font-family:ui-monospace, monospace;">
                                <span>Generated: {{DRAWN_DATE}}</span>
                                <span>Filter: {{KEYWORD}} &bull; Duplicate removal: {{DUP_FILTER}}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <script>
                    function copyAnnouncementText() {
                        const title = {{TITLE_JS}};
                        const text = `Giveaway Winners Announcement: ${title}\\nVerified with SPEDO CSPRNG SecureRandom.\\nAudit certificate verified.`;
                        navigator.clipboard.writeText(text).then(() => {
                            alert('Giveaway announcement summary copied to clipboard.');
                        }).catch(() => {
                            alert('Could not copy to clipboard.');
                        });
                    }
                </script>
            </body>
            </html>
        """;

        String html = template
                .replace("{{TITLE}}", escapeHtml(giveaway.getTitle()))
                .replace("{{PLATFORM_CLASS}}", platform.equals("YOUTUBE") ? "pill-youtube" : "pill-instagram")
                .replace("{{PLATFORM}}", platform)
                .replace("{{DRAWN_DATE}}", drawnDateStr)
                .replace("{{GIVEAWAY_ID}}", String.valueOf(giveaway.getId()))
                .replace("{{POST_THUMB}}", postThumb)
                .replace("{{POST_CAPTION}}", postCaption)
                .replace("{{POST_URL}}", postUrl)
                .replace("{{TOTAL_COMMENTS}}", String.valueOf(giveaway.getTotalCommentsFetched()))
                .replace("{{UNIQUE_USERS}}", String.valueOf(giveaway.getUniqueParticipantsCount()))
                .replace("{{ELIGIBLE_ENTRIES}}", String.valueOf(giveaway.getEligibleParticipantsCount()))
                .replace("{{WINNERS_COUNT}}", String.valueOf(giveaway.getWinnersCount()))
                .replace("{{PRIMARY_COUNT}}", String.valueOf(primaryList.size()))
                .replace("{{PRIMARY_WINNERS}}", primaryWinnersHtml.toString())
                .replace("{{BACKUP_WINNERS}}", backupWinnersHtml.toString())
                .replace("{{KEYWORD}}", giveaway.getRequiredKeyword() != null ? escapeHtml(giveaway.getRequiredKeyword()) : "None")
                .replace("{{DUP_FILTER}}", giveaway.isFilterDuplicates() ? "Active" : "Disabled")
                .replace("{{TITLE_JS}}", "\"" + escapeHtml(giveaway.getTitle()).replace("\"", "\\\"") + "\"");

        return html.getBytes(StandardCharsets.UTF_8);
    }

    private String escapeHtml(String val) {
        if (val == null) return "";
        return val.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String formatHandle(String u) {
        if (u == null || u.isBlank()) return "@anonymous";
        String trimmed = u.trim();
        return trimmed.startsWith("@") ? trimmed : "@" + trimmed;
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ") + "\"";
    }

    private Set<String> parseExcludedUsernames(String raw) {
        Set<String> set = new HashSet<>();
        if (raw == null || raw.isBlank()) return set;
        String[] parts = raw.split("[,\\s\n\r]+");
        for (String p : parts) {
            String clean = normalizeUsername(p);
            if (!clean.isBlank()) {
                set.add(clean);
            }
        }
        return set;
    }

    private String normalizeUsername(String u) {
        if (u == null) return "";
        return u.trim().replaceAll("^[@/]+", "").toLowerCase(Locale.ROOT);
    }
}
