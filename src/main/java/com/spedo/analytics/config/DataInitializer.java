package com.spedo.analytics.config;

import com.spedo.analytics.dto.SocialAccountConnectRequest;
import com.spedo.analytics.entity.Post;
import com.spedo.analytics.entity.Role;
import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialMetric;
import com.spedo.analytics.entity.SocialPlatform;
import com.spedo.analytics.entity.User;
import com.spedo.analytics.repository.PostRepository;
import com.spedo.analytics.repository.SocialAccountRepository;
import com.spedo.analytics.repository.SocialMetricRepository;
import com.spedo.analytics.repository.UserRepository;
import com.spedo.analytics.service.DemoDataService;
import com.spedo.analytics.service.SocialAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoDataService demoDataService;
    private final SocialAccountRepository socialAccountRepository;
    private final SocialAccountService socialAccountService;
    private final PostRepository postRepository;
    private final SocialMetricRepository socialMetricRepository;

    @org.springframework.beans.factory.annotation.Value("${app.demo-mode.enabled:false}")
    private boolean demoModeEnabled;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           DemoDataService demoDataService,
                           SocialAccountRepository socialAccountRepository,
                           SocialAccountService socialAccountService,
                           PostRepository postRepository,
                           SocialMetricRepository socialMetricRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoDataService = demoDataService;
        this.socialAccountRepository = socialAccountRepository;
        this.socialAccountService = socialAccountService;
        this.postRepository = postRepository;
        this.socialMetricRepository = socialMetricRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing SPEDO platform baseline accounts...");

        // Configurable Administrator Account for Production Deploy
        String adminEmail = System.getenv().getOrDefault("DEFAULT_ADMIN_EMAIL", "arunvemula13@gmail.com");
        String adminPassword = System.getenv().getOrDefault("DEFAULT_ADMIN_PASSWORD", "7207562840");
        String adminName = System.getenv().getOrDefault("DEFAULT_ADMIN_NAME", "Arun Vemula");
        String adminOrg = System.getenv().getOrDefault("DEFAULT_ADMIN_ORG", "SPEDO Analytics");

        User adminUser = userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User newAdmin = new User(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    adminName,
                    adminOrg,
                    Role.ROLE_ADMIN
            );
            return userRepository.save(newAdmin);
        });

        adminUser.setFullName(adminName);
        adminUser.setOrganization(adminOrg);
        if (!passwordEncoder.matches(adminPassword, adminUser.getPassword())) {
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
        }
        adminUser = userRepository.save(adminUser);

        // Auto-connect default YouTube channel for admin if empty
        connectDefaultYouTubeIfEmpty(adminUser);

        // Seed demo accounts ONLY if demo-mode is explicitly enabled via DEMO_MODE_ENABLED=true
        if (demoModeEnabled) {
            String demoEmail = "demo@spedo.io";
            User demoUser = userRepository.findByEmail(demoEmail).orElseGet(() -> {
                User user = new User(
                        demoEmail,
                        passwordEncoder.encode("Password123!"),
                        "Demo User",
                        "SPEDO Analytics",
                        Role.ROLE_USER
                );
                return userRepository.save(user);
            });
            demoDataService.seedDemoDataForUser(demoUser);
            log.info("Demo mode enabled: Seeded sample accounts for {}", demoEmail);
        } else {
            log.info("Production Real-Data Mode Active: Skipping demo seed generation. Only authentic user data will be tracked.");
        }

        log.info("SPEDO baseline initialization complete. Ready for real accounts.");
    }

    private void syncRealAccountsToUserIfEmpty(User targetUser, User sourceUser) {
        if (targetUser == null || targetUser.getId() == null || sourceUser == null || sourceUser.getId() == null) return;
        List<SocialAccount> targetAccounts = socialAccountRepository.findByUserId(targetUser.getId());
        long targetFollowers = targetAccounts.stream().mapToLong(SocialAccount::getFollowerCount).sum();
        if (targetAccounts.isEmpty() || targetFollowers == 0) {
            List<SocialAccount> sourceAccounts = socialAccountRepository.findByUserId(sourceUser.getId());
            for (SocialAccount sa : sourceAccounts) {
                boolean exists = targetAccounts.stream().anyMatch(a -> a.getAccountId().equals(sa.getAccountId()));
                if (!exists) {
                    SocialAccount clone = new SocialAccount();
                    clone.setUser(targetUser);
                    clone.setPlatform(sa.getPlatform());
                    clone.setAccountId(sa.getAccountId());
                    clone.setAccountName(sa.getAccountName());
                    clone.setHandle(sa.getHandle());
                    clone.setProfileImageUrl(sa.getProfileImageUrl());
                    clone.setFollowerCount(sa.getFollowerCount());
                    clone.setFollowingCount(sa.getFollowingCount());
                    clone.setChannelViews(sa.getChannelViews());
                    clone.setVideoCount(sa.getVideoCount());
                    clone.setStatus(sa.getStatus());
                    clone.setLastSyncedAt(sa.getLastSyncedAt());
                    clone.setDemo(sa.isDemo());
                    SocialAccount savedClone = socialAccountRepository.save(clone);

                    // Copy posts
                    List<Post> posts = postRepository.findBySocialAccountId(sa.getId());
                    for (Post p : posts) {
                        Post postClone = new Post();
                        postClone.setSocialAccount(savedClone);
                        postClone.setPlatform(p.getPlatform() != null ? p.getPlatform() : savedClone.getPlatform());
                        postClone.setPlatformPostId(p.getPlatformPostId());
                        postClone.setCaption(p.getCaption());
                        postClone.setMediaUrl(p.getMediaUrl());
                        postClone.setPostUrl(p.getPostUrl());
                        postClone.setPostType(p.getPostType());
                        postClone.setPublishedAt(p.getPublishedAt());
                        postClone.setLikesCount(p.getLikesCount());
                        postClone.setCommentsCount(p.getCommentsCount());
                        postClone.setSharesCount(p.getSharesCount());
                        postClone.setImpressionsCount(p.getImpressionsCount());
                        postClone.setReachCount(p.getReachCount());
                        postClone.setEngagementRate(p.getEngagementRate());
                        postClone.setPositivePercent(p.getPositivePercent());
                        postClone.setNegativePercent(p.getNegativePercent());
                        postClone.setSentimentScore(p.getSentimentScore());
                        postClone.setVanityAlert(p.isVanityAlert());
                        postClone.setSentimentVerdict(p.getSentimentVerdict());
                        postRepository.save(postClone);
                    }

                    // Copy metrics
                    List<SocialMetric> metrics = socialMetricRepository.findAll().stream()
                            .filter(m -> m.getSocialAccount().getId().equals(sa.getId()))
                            .toList();
                    for (SocialMetric m : metrics) {
                        SocialMetric metricClone = new SocialMetric();
                        metricClone.setSocialAccount(savedClone);
                        metricClone.setRecordedDate(m.getRecordedDate());
                        metricClone.setFollowers(m.getFollowers());
                        metricClone.setFollowerGrowth(m.getFollowerGrowth());
                        metricClone.setImpressions(m.getImpressions());
                        metricClone.setReach(m.getReach());
                        metricClone.setTotalLikes(m.getTotalLikes());
                        metricClone.setTotalComments(m.getTotalComments());
                        metricClone.setTotalShares(m.getTotalShares());
                        metricClone.setEngagementRate(m.getEngagementRate());
                        socialMetricRepository.save(metricClone);
                    }
                }
            }
            log.info("Synced authentic channels to user {}", targetUser.getEmail());
        }
    }

    private void connectDefaultYouTubeIfEmpty(User user) {
        if (user == null || user.getId() == null) return;
        if (socialAccountRepository.findByUserId(user.getId()).isEmpty()) {
            try {
                SocialAccountConnectRequest req = new SocialAccountConnectRequest();
                req.setPlatform(SocialPlatform.YOUTUBE);
                req.setAccountId("UCbtRM4Y4n7Id6trpT38zJ3g");
                req.setHandle("@way4news999");
                req.setAccountName("Way4News");
                req.setDemo(false);
                socialAccountService.connectAccount(user.getId(), req);
                log.info("Auto-connected real YouTube channel @way4news999 for user {}", user.getEmail());
            } catch (Exception e) {
                log.warn("Could not pre-connect real YouTube channel for {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }
}
