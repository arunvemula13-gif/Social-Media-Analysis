package com.spedo.analytics.service;

import com.spedo.analytics.dto.SocialAccountConnectRequest;
import com.spedo.analytics.dto.SocialAccountDto;
import com.spedo.analytics.entity.AccountStatus;
import com.spedo.analytics.entity.NotificationType;
import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.PostComment;
import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialMetric;
import com.spedo.analytics.entity.User;
import com.spedo.analytics.exception.BadRequestException;
import com.spedo.analytics.exception.ResourceNotFoundException;
import com.spedo.analytics.integration.PlatformAdapterFactory;
import com.spedo.analytics.integration.SocialPlatformAdapter;
import com.spedo.analytics.integration.SyncResult;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.PostRepository;
import com.spedo.analytics.repository.SocialAccountRepository;
import com.spedo.analytics.repository.SocialMetricRepository;
import com.spedo.analytics.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class SocialAccountService {

    private static final Logger log = LoggerFactory.getLogger(SocialAccountService.class);

    private final SocialAccountRepository socialAccountRepository;
    private final SocialMetricRepository socialMetricRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PlatformAdapterFactory adapterFactory;
    private final NotificationService notificationService;
    private final EntityDtoMapper mapper;

    public SocialAccountService(SocialAccountRepository socialAccountRepository,
                                SocialMetricRepository socialMetricRepository,
                                PostRepository postRepository,
                                UserRepository userRepository,
                                PlatformAdapterFactory adapterFactory,
                                NotificationService notificationService,
                                EntityDtoMapper mapper) {
        this.socialAccountRepository = socialAccountRepository;
        this.socialMetricRepository = socialMetricRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.adapterFactory = adapterFactory;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<SocialAccountDto> getUserAccounts(Long userId) {
        return socialAccountRepository.findByUserId(userId).stream()
                .map(mapper::toSocialAccountDto)
                .toList();
    }

    @Transactional
    public SocialAccountDto connectAccount(Long userId, SocialAccountConnectRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String rawHandle = request.getHandle() != null ? request.getHandle().trim() : "";
        String cleanHandle = cleanHandle(rawHandle);
        String formattedHandle = "@" + cleanHandle;

        String accountId = request.getAccountId() != null && !request.getAccountId().isBlank()
                ? request.getAccountId().trim()
                : request.getPlatform().name().toLowerCase() + "_" + cleanHandle;

        if (socialAccountRepository.existsByUserIdAndPlatformAndAccountId(userId, request.getPlatform(), accountId)) {
            throw new BadRequestException("This account is already connected to your SPEDO profile.");
        }

        String displayName = request.getAccountName() != null && !request.getAccountName().isBlank()
                ? request.getAccountName().trim()
                : cleanHandle;

        SocialAccount account = new SocialAccount();
        account.setUser(user);
        account.setPlatform(request.getPlatform());
        account.setAccountId(accountId);
        account.setAccountName(displayName);
        account.setHandle(formattedHandle);
        account.setProfileImageUrl(request.getProfileImageUrl() != null && !request.getProfileImageUrl().isBlank()
                ? request.getProfileImageUrl()
                : "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150");
        account.setStatus(AccountStatus.CONNECTED);
        account.setDemo(request.isDemo());

        if (request.getAccessToken() != null && !request.getAccessToken().isBlank()) {
            account.setAccessToken(request.getAccessToken().trim());
        }

        int initialFollowers;
        if (request.getInitialFollowers() != null && request.getInitialFollowers() >= 0) {
            initialFollowers = request.getInitialFollowers();
        } else if (request.isDemo()) {
            initialFollowers = 15000 + new Random().nextInt(45000);
        } else {
            initialFollowers = 0;
        }

        int initialFollowing;
        if (request.getInitialFollowing() != null && request.getInitialFollowing() >= 0) {
            initialFollowing = request.getInitialFollowing();
        } else if (request.isDemo()) {
            initialFollowing = 150 + new Random().nextInt(300);
        } else {
            initialFollowing = 0;
        }

        account.setFollowerCount(initialFollowers);
        account.setFollowingCount(initialFollowing);
        account.setLastSyncedAt(LocalDateTime.now());

        SocialAccount savedAccount = socialAccountRepository.save(account);

        // Run initial metric sync through platform adapter
        SocialPlatformAdapter adapter = adapterFactory.getAdapter(request.getPlatform());
        SyncResult syncResult;
        try {
            syncResult = adapter.syncMetrics(savedAccount);
        } catch (Exception e) {
            log.warn("Initial sync failed for account {} ({}): {}", savedAccount.getId(), savedAccount.getHandle(), e.getMessage());
            syncResult = SyncResult.failed("Initial sync pending: " + e.getMessage(), savedAccount.getFollowerCount());
        }
        if (syncResult.isSuccess()) {
            savedAccount.setFollowerCount(syncResult.getCurrentFollowers());
            savedAccount.setStatus(AccountStatus.SYNCED);
        }
        savedAccount = socialAccountRepository.save(savedAccount);

        if (syncResult.getLatestMetric() != null) {
            saveOrUpdateMetric(savedAccount, syncResult.getLatestMetric());
        }
        if (syncResult.getNewOrUpdatedPosts() != null && !syncResult.getNewOrUpdatedPosts().isEmpty()) {
            saveOrUpdatePosts(savedAccount, syncResult.getNewOrUpdatedPosts());
        }

        notificationService.createNotification(
                user,
                "Account Connected: " + savedAccount.getAccountName(),
                "Successfully linked " + savedAccount.getPlatform() + " profile (" + savedAccount.getHandle() + ") to SPEDO.",
                NotificationType.ACCOUNT_CONNECTED,
                "/social-accounts.html"
        );

        return mapper.toSocialAccountDto(savedAccount);
    }

    @Transactional
    public SocialAccountDto syncAccount(Long userId, Long accountId) {
        SocialAccount account = socialAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found"));

        SocialPlatformAdapter adapter = adapterFactory.getAdapter(account.getPlatform());
        SyncResult syncResult;
        try {
            syncResult = adapter.syncMetrics(account);
        } catch (Exception e) {
            log.warn("Platform adapter sync failed for account {} ({}): {}", account.getId(), account.getHandle(), e.getMessage());
            syncResult = SyncResult.failed("Sync warning: " + e.getMessage(), account.getFollowerCount());
        }

        account.setFollowerCount(syncResult.getCurrentFollowers());
        account.setLastSyncedAt(LocalDateTime.now());
        account.setStatus(syncResult.isSuccess() ? AccountStatus.SYNCED : AccountStatus.CONNECTED);
        SocialAccount updated = socialAccountRepository.save(account);

        if (syncResult.getLatestMetric() != null) {
            saveOrUpdateMetric(updated, syncResult.getLatestMetric());
        }
        if (syncResult.getNewOrUpdatedPosts() != null && !syncResult.getNewOrUpdatedPosts().isEmpty()) {
            saveOrUpdatePosts(updated, syncResult.getNewOrUpdatedPosts());
        }

        notificationService.createNotification(
                account.getUser(),
                "Account Synced: " + account.getHandle(),
                syncResult.getMessage() != null ? syncResult.getMessage() : "Metrics refreshed for " + account.getPlatform() + ".",
                NotificationType.SYNC_STATUS,
                "/social-accounts.html"
        );

        return mapper.toSocialAccountDto(updated);
    }

    private void saveOrUpdateMetric(SocialAccount account, SocialMetric newMetric) {
        if (newMetric == null) return;
        socialMetricRepository.findBySocialAccountIdAndRecordedDate(account.getId(), newMetric.getRecordedDate())
                .ifPresentOrElse(existing -> {
                    existing.setFollowers(newMetric.getFollowers());
                    existing.setFollowerGrowth(newMetric.getFollowerGrowth());
                    existing.setImpressions(newMetric.getImpressions());
                    existing.setReach(newMetric.getReach());
                    existing.setTotalLikes(newMetric.getTotalLikes());
                    existing.setTotalComments(newMetric.getTotalComments());
                    existing.setTotalShares(newMetric.getTotalShares());
                    existing.setEngagementRate(newMetric.getEngagementRate());
                    socialMetricRepository.save(existing);
                }, () -> {
                    newMetric.setSocialAccount(account);
                    socialMetricRepository.save(newMetric);
                });
    }

    private void saveOrUpdatePosts(SocialAccount account, List<Post> posts) {
        if (posts == null || posts.isEmpty()) return;
        for (Post p : posts) {
            p.setSocialAccount(account);
            if (p.getComments() != null) {
                for (PostComment c : p.getComments()) {
                    c.setPost(p);
                }
            }
            if (p.getPlatformPostId() != null && !p.getPlatformPostId().isBlank()) {
                postRepository.findBySocialAccountIdAndPlatformPostId(account.getId(), p.getPlatformPostId())
                        .ifPresentOrElse(existing -> {
                            existing.setCaption(p.getCaption());
                            existing.setMediaUrl(p.getMediaUrl());
                            existing.setPostUrl(p.getPostUrl());
                            existing.setPostType(p.getPostType());
                            existing.setPublishedAt(p.getPublishedAt());
                            existing.setLikesCount(p.getLikesCount());
                            existing.setCommentsCount(p.getCommentsCount());
                            existing.setSharesCount(p.getSharesCount());
                            existing.setImpressionsCount(p.getImpressionsCount());
                            existing.setReachCount(p.getReachCount());
                            existing.setEngagementRate(p.getEngagementRate());
                            existing.setPositiveCommentsCount(p.getPositiveCommentsCount());
                            existing.setNeutralCommentsCount(p.getNeutralCommentsCount());
                            existing.setNegativeCommentsCount(p.getNegativeCommentsCount());
                            existing.setPositivePercent(p.getPositivePercent());
                            existing.setNegativePercent(p.getNegativePercent());
                            existing.setSentimentScore(p.getSentimentScore());
                            existing.setVanityAlert(p.isVanityAlert());
                            existing.setSentimentVerdict(p.getSentimentVerdict());

                            if (p.getComments() != null && !p.getComments().isEmpty()) {
                                existing.getComments().clear();
                                for (PostComment c : p.getComments()) {
                                    c.setPost(existing);
                                    existing.getComments().add(c);
                                }
                            }
                            postRepository.save(existing);
                        }, () -> {
                            postRepository.save(p);
                        });
            } else {
                postRepository.save(p);
            }
        }
    }

    public static String cleanHandle(String raw) {
        if (raw == null || raw.isBlank()) return "unknown";
        String s = raw.trim();
        if (s.startsWith("http://") || s.startsWith("https://")) {
            try {
                java.net.URI uri = java.net.URI.create(s);
                String path = uri.getPath();
                if (path != null) {
                    String[] segments = path.split("/");
                    for (String seg : segments) {
                        if (!seg.isBlank()) {
                            s = seg;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return s.replaceAll("^[@/]+", "").replaceAll("[/?#].*$", "").trim();
    }

    @Transactional
    public void disconnectAccount(Long userId, Long accountId) {
        SocialAccount account = socialAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Social account not found"));

        postRepository.deleteBySocialAccountId(accountId);
        socialMetricRepository.deleteBySocialAccountId(accountId);
        socialAccountRepository.delete(account);
    }
}
