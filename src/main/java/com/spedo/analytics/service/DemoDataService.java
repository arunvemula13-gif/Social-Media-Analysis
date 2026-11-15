package com.spedo.analytics.service;

import com.spedo.analytics.entity.*;
import com.spedo.analytics.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DemoDataService {

    private final SocialAccountRepository socialAccountRepository;
    private final SocialMetricRepository socialMetricRepository;
    private final PostRepository postRepository;
    private final CompetitorRepository competitorRepository;
    private final CompetitorMetricRepository competitorMetricRepository;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;
    private final CommentSentimentService commentSentimentService;
    private final com.spedo.analytics.repository.PostCommentRepository postCommentRepository;

    public DemoDataService(SocialAccountRepository socialAccountRepository,
                           SocialMetricRepository socialMetricRepository,
                           PostRepository postRepository,
                           CompetitorRepository competitorRepository,
                           CompetitorMetricRepository competitorMetricRepository,
                           ReportRepository reportRepository,
                           NotificationRepository notificationRepository,
                           CommentSentimentService commentSentimentService,
                           com.spedo.analytics.repository.PostCommentRepository postCommentRepository) {
        this.socialAccountRepository = socialAccountRepository;
        this.socialMetricRepository = socialMetricRepository;
        this.postRepository = postRepository;
        this.competitorRepository = competitorRepository;
        this.competitorMetricRepository = competitorMetricRepository;
        this.reportRepository = reportRepository;
        this.notificationRepository = notificationRepository;
        this.commentSentimentService = commentSentimentService;
        this.postCommentRepository = postCommentRepository;
    }

    @Transactional
    public void seedDemoDataForUser(User user) {
        // If user already has accounts, ensure comments & sentiment are initialized
        if (!socialAccountRepository.findByUserId(user.getId()).isEmpty()) {
            ensureCommentsSeeded(user);
            return;
        }

        Random random = new Random(42); // Deterministic seed for reproducible baseline

        // 1. Create Social Accounts
        SocialAccount ig = new SocialAccount(user, SocialPlatform.INSTAGRAM, "ig_" + user.getId(), user.getFullName() + " Official", "@" + user.getFullName().toLowerCase().replace(" ", "") + "_tech");
        ig.setProfileImageUrl("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150");
        ig.setFollowerCount(142500);
        ig.setFollowingCount(380);
        ig.setDemo(true);
        ig.setLastSyncedAt(LocalDateTime.now().minusHours(2));
        socialAccountRepository.save(ig);

        SocialAccount fb = new SocialAccount(user, SocialPlatform.FACEBOOK, "fb_" + user.getId(), user.getFullName() + " Global", "@" + user.getFullName().toLowerCase().replace(" ", "") + "_global");
        fb.setProfileImageUrl("https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=150");
        fb.setFollowerCount(98200);
        fb.setFollowingCount(120);
        fb.setDemo(true);
        fb.setLastSyncedAt(LocalDateTime.now().minusHours(2));
        socialAccountRepository.save(fb);

        SocialAccount yt = new SocialAccount(user, SocialPlatform.YOUTUBE, "yt_" + user.getId(), user.getFullName() + " HQ", "@" + user.getFullName().toLowerCase().replace(" ", "") + "hq");
        yt.setProfileImageUrl("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150");
        yt.setFollowerCount(64800);
        yt.setFollowingCount(45);
        yt.setDemo(true);
        yt.setLastSyncedAt(LocalDateTime.now().minusHours(2));
        socialAccountRepository.save(yt);

        SocialAccount li = new SocialAccount(user, SocialPlatform.LINKEDIN, "li_" + user.getId(), user.getFullName() + " Technologies", "@" + user.getFullName().toLowerCase().replace(" ", "") + "-tech");
        li.setProfileImageUrl("https://images.unsplash.com/photo-1557683316-973673baf926?w=150");
        li.setFollowerCount(41300);
        li.setFollowingCount(210);
        li.setDemo(true);
        li.setLastSyncedAt(LocalDateTime.now().minusHours(2));
        socialAccountRepository.save(li);

        SocialAccount tw = new SocialAccount(user, SocialPlatform.TWITTER, "tw_" + user.getId(), user.getFullName(), "@" + user.getFullName().toLowerCase().replace(" ", "") + "_app");
        tw.setProfileImageUrl("https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=150");
        tw.setFollowerCount(85900);
        tw.setFollowingCount(530);
        tw.setDemo(true);
        tw.setLastSyncedAt(LocalDateTime.now().minusHours(2));
        socialAccountRepository.save(tw);

        // 2. Seed Daily Historical Metrics for each account (last 30 days)
        List<SocialAccount> accounts = List.of(ig, fb, yt, li, tw);
        LocalDate today = LocalDate.now();

        for (SocialAccount account : accounts) {
            int currentFollowers = account.getFollowerCount();
            int baseGrowth = switch (account.getPlatform()) {
                case INSTAGRAM -> 260;
                case FACEBOOK -> 120;
                case YOUTUBE -> 110;
                case LINKEDIN -> 80;
                case TWITTER -> 180;
            };

            for (int i = 30; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int growth = Math.max(20, baseGrowth + random.nextInt(120) - 40);
                int followersOnDate = currentFollowers - ((i) * baseGrowth);
                if (followersOnDate < 1000) followersOnDate = 1000;

                int impressions = (int) (followersOnDate * (0.35 + (random.nextDouble() * 0.15)));
                int reach = (int) (impressions * 0.78);
                int likes = (int) (reach * (0.045 + (random.nextDouble() * 0.02)));
                int comments = (int) (likes * 0.08);
                int shares = (int) (likes * 0.12);

                int interactions = likes + comments + shares;
                BigDecimal rate = BigDecimal.valueOf((double) interactions / reach * 100)
                        .setScale(4, RoundingMode.HALF_UP);

                SocialMetric metric = new SocialMetric(
                        account, date, followersOnDate, growth, impressions, reach, likes, comments, shares, rate
                );
                socialMetricRepository.save(metric);
            }
        }

        // 3. Seed Posts
        Post p1 = createPost(ig, SocialPlatform.INSTAGRAM, "post_ig_" + user.getId() + "_1",
                "🚀 Introducing our next-gen social intelligence suite. Benchmarking competitor growth has never been this seamless! What feature are you most excited about? #SaaS #Analytics #GrowthHacking",
                "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800", "https://instagram.com/p/spedo101",
                PostType.CAROUSEL, LocalDateTime.now().minusDays(1).withHour(14).withMinute(30), 4820, 312, 540, 68400, 52100, new BigDecimal("4.2250"));

        Post p2 = createPost(ig, SocialPlatform.INSTAGRAM, "post_ig_" + user.getId() + "_2",
                "Stop guessing what your audience wants. Analyze real-time engagement patterns across every social channel with SPEDO. Link in bio to start your trial. 📊✨",
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800", "https://instagram.com/p/spedo102",
                PostType.IMAGE, LocalDateTime.now().minusDays(3).withHour(11), 3650, 184, 290, 49200, 38400, new BigDecimal("2.9700"));

        Post p3 = createPost(ig, SocialPlatform.INSTAGRAM, "post_ig_" + user.getId() + "_3",
                "Behind the scenes at our product sprint! Coffee, whiteboards, and lots of data science magic. ☕💡 Tag your favorite work buddy!",
                "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800", "https://instagram.com/p/spedo103",
                PostType.IMAGE, LocalDateTime.now().minusDays(6).withHour(16), 2910, 240, 115, 39800, 31200, new BigDecimal("2.3100"));

        createPost(fb, SocialPlatform.FACEBOOK, "post_fb_" + user.getId() + "_1",
                "We are thrilled to be recognized as a Category Leader in Social Media Analytics for Q3 2026! A huge thank you to our global users and partners.",
                "https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=800", "https://facebook.com/spedo/posts/201",
                PostType.IMAGE, LocalDateTime.now().minusDays(2).withHour(15), 2140, 185, 430, 42100, 33500, new BigDecimal("2.8200"));

        createPost(fb, SocialPlatform.FACEBOOK, "post_fb_" + user.getId() + "_2",
                "Webinar Alert 🔴: Unlocking High-Converting Social Content in 2027. Join our VP of Product and guest speakers this Thursday at 2 PM EST.",
                "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=800", "https://facebook.com/spedo/posts/202",
                PostType.IMAGE, LocalDateTime.now().minusDays(5).withHour(12), 1650, 120, 210, 31200, 24800, new BigDecimal("2.0100"));

        Post pYt = createPost(yt, SocialPlatform.YOUTUBE, "post_yt_" + user.getId() + "_1",
                "Full Masterclass: How to Build a Competitor Intelligence Dashboard from Scratch [45 Min Deep Dive]",
                "https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=800", "https://youtube.com/watch?v=spedo301",
                PostType.VIDEO, LocalDateTime.now().minusDays(4).withHour(18), 5840, 620, 890, 88200, 71500, new BigDecimal("4.8500"));

        Post pLi = createPost(li, SocialPlatform.LINKEDIN, "post_li_" + user.getId() + "_1",
                "The modern CMO does not look at follower counts anymore. They look at engagement velocity, audience affinity, and competitive share of voice. Here is our 2026 B2B Benchmark Study.",
                "https://images.unsplash.com/photo-1557804506-669a67965ba0?w=800", "https://linkedin.com/feed/update/spedo401",
                PostType.IMAGE, LocalDateTime.now().minusDays(2).withHour(9), 3120, 410, 640, 48500, 39200, new BigDecimal("4.3800"));

        Post pTw = createPost(tw, SocialPlatform.TWITTER, "post_tw_" + user.getId() + "_1",
                "Thread: 7 subtle signals your competitors are scaling up their social spend before you even see their ads 🧵👇",
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800", "https://x.com/spedo_app/status/501",
                PostType.TEXT, LocalDateTime.now().minusDays(1).withHour(19), 4910, 520, 1840, 94200, 78900, new BigDecimal("4.6700"));

        createPost(tw, SocialPlatform.TWITTER, "post_tw_" + user.getId() + "_2",
                "If you are publishing 5x a week without benchmarking performance against top industry leaders, you are driving with your eyes closed.",
                "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800", "https://x.com/spedo_app/status/502",
                PostType.TEXT, LocalDateTime.now().minusDays(4).withHour(15), 3240, 310, 890, 61400, 51200, new BigDecimal("3.3900"));

        // Seed Sentiment Comments to Demonstrate The Vanity Metrics Problem
        seedCommentsForProductLaunch(p1);
        seedCommentsForLinkedInStudy(pLi);
        seedCommentsForYouTubeMasterclass(pYt);
        seedCommentsForTwitterThread(pTw);

        // 4. Seed Competitors & Competitor Metrics
        Competitor c1 = new Competitor(user, "ApexMetrics", "https://apexmetrics.io", "SaaS Analytics");
        c1.setInstagramHandle("@apex_metrics");
        c1.setTwitterHandle("@apexmetrics");
        c1.setLinkedinHandle("@apex-metrics");
        c1.setDemo(true);
        competitorRepository.save(c1);
        seedCompMetric(c1, SocialPlatform.INSTAGRAM, 118400, 180, new BigDecimal("4.5"), 2940, 185, 240, new BigDecimal("2.8400"));
        seedCompMetric(c1, SocialPlatform.TWITTER, 76200, 120, new BigDecimal("8.2"), 2810, 210, 740, new BigDecimal("3.1200"));
        seedCompMetric(c1, SocialPlatform.LINKEDIN, 36800, 95, new BigDecimal("3.0"), 1620, 110, 180, new BigDecimal("2.9100"));

        Competitor c2 = new Competitor(user, "PulseTrack", "https://pulsetrack.com", "Marketing Intelligence");
        c2.setInstagramHandle("@pulsetrack_hq");
        c2.setTwitterHandle("@pulsetrack");
        c2.setLinkedinHandle("@pulse-track");
        c2.setDemo(true);
        competitorRepository.save(c2);
        seedCompMetric(c2, SocialPlatform.INSTAGRAM, 165000, 310, new BigDecimal("6.0"), 5210, 340, 490, new BigDecimal("3.6600"));
        seedCompMetric(c2, SocialPlatform.TWITTER, 104500, 240, new BigDecimal("12.0"), 4650, 410, 1420, new BigDecimal("4.2800"));
        seedCompMetric(c2, SocialPlatform.LINKEDIN, 58200, 160, new BigDecimal("4.5"), 2890, 240, 360, new BigDecimal("4.1200"));

        Competitor c3 = new Competitor(user, "OmniVibe Media", "https://omnivibe.co", "Social Marketing");
        c3.setInstagramHandle("@omnivibe_social");
        c3.setTwitterHandle("@omnivibe");
        c3.setLinkedinHandle("@omnivibe-media");
        c3.setDemo(true);
        competitorRepository.save(c3);
        seedCompMetric(c3, SocialPlatform.INSTAGRAM, 92300, 140, new BigDecimal("3.5"), 1980, 130, 160, new BigDecimal("2.4600"));
        seedCompMetric(c3, SocialPlatform.TWITTER, 64100, 85, new BigDecimal("5.0"), 1750, 140, 430, new BigDecimal("2.7800"));
        seedCompMetric(c3, SocialPlatform.LINKEDIN, 28400, 60, new BigDecimal("2.5"), 980, 75, 110, new BigDecimal("2.2100"));

        Competitor c4 = new Competitor(user, "DataEcho", "https://dataecho.io", "BI & Analytics");
        c4.setInstagramHandle("@dataecho_ai");
        c4.setTwitterHandle("@dataecho_ai");
        c4.setLinkedinHandle("@dataecho");
        c4.setDemo(true);
        competitorRepository.save(c4);
        seedCompMetric(c4, SocialPlatform.INSTAGRAM, 134200, 220, new BigDecimal("5.0"), 3820, 250, 340, new BigDecimal("3.2900"));
        seedCompMetric(c4, SocialPlatform.TWITTER, 91500, 190, new BigDecimal("9.0"), 3940, 330, 1180, new BigDecimal("3.9600"));
        seedCompMetric(c4, SocialPlatform.LINKEDIN, 49600, 130, new BigDecimal("3.8"), 2250, 190, 270, new BigDecimal("3.4800"));

        // 5. Seed Reports
        Report r1 = new Report(user, "Weekly Performance Summary", ReportType.WEEKLY, today.minusDays(7), today, ReportFormat.CSV);
        r1.setReportDataJson("{\"totalFollowers\":432700,\"growthRate\":2.4,\"avgEngagementRate\":3.81,\"topPlatform\":\"INSTAGRAM\"}");
        r1.setDownloadUrl("/api/reports/1/csv");
        reportRepository.save(r1);

        Report r2 = new Report(user, "Q3 Competitor Benchmarking Audit", ReportType.COMPETITOR_COMPARISON, today.minusDays(90), today, ReportFormat.PDF);
        r2.setReportDataJson("{\"competitorCount\":4,\"leaderEngagement\":\"PulseTrack\",\"ourRank\":2}");
        r2.setDownloadUrl("/api/reports/2/content");
        reportRepository.save(r2);

        // 6. Seed Notifications
        Notification n1 = new Notification(user, "Weekly Report Ready", "Your Weekly Performance Summary is ready for export.", NotificationType.REPORT_GENERATED, "/reports.html");
        notificationRepository.save(n1);

        Notification n2 = new Notification(user, "Follower Milestone Reached", "Congratulations! Your Instagram channel crossed 142,000 followers.", NotificationType.METRIC_MILESTONE, "/dashboard.html");
        notificationRepository.save(n2);

        Notification n3 = new Notification(user, "Social Sync Completed", "All 5 connected social channels synced successfully.", NotificationType.SYNC_STATUS, "/social-accounts.html");
        n3.setRead(true);
        notificationRepository.save(n3);
    }

    private Post createPost(SocialAccount account, SocialPlatform platform, String platformPostId,
                            String caption, String mediaUrl, String postUrl, PostType postType,
                            LocalDateTime publishedAt, int likes, int comments, int shares,
                            int impressions, int reach, BigDecimal engagementRate) {
        Post post = new Post();
        post.setSocialAccount(account);
        post.setPlatform(platform);
        post.setPlatformPostId(platformPostId);
        post.setCaption(caption);
        post.setMediaUrl(mediaUrl);
        post.setPostUrl(postUrl);
        post.setPostType(postType);
        post.setPublishedAt(publishedAt);
        post.setLikesCount(likes);
        post.setCommentsCount(comments);
        post.setSharesCount(shares);
        post.setImpressionsCount(impressions);
        post.setReachCount(reach);
        post.setEngagementRate(engagementRate);
        return postRepository.save(post);
    }

    private void ensureCommentsSeeded(User user) {
        List<Post> posts = postRepository.findTopPerformingByUserId(user.getId(), LocalDateTime.now().minusDays(30), 
                org.springframework.data.domain.PageRequest.of(0, 10));

        for (Post post : posts) {
            long existingCount = postCommentRepository.countByPostIdAndSentiment(post.getId(), "NEGATIVE")
                    + postCommentRepository.countByPostIdAndSentiment(post.getId(), "POSITIVE")
                    + postCommentRepository.countByPostIdAndSentiment(post.getId(), "NEUTRAL");

            if (existingCount == 0) {
                if (post.getPlatform() == SocialPlatform.INSTAGRAM && post.getLikesCount() > 4000) {
                    seedCommentsForProductLaunch(post);
                } else if (post.getPlatform() == SocialPlatform.LINKEDIN) {
                    seedCommentsForLinkedInStudy(post);
                } else if (post.getPlatform() == SocialPlatform.YOUTUBE) {
                    seedCommentsForYouTubeMasterclass(post);
                } else {
                    seedCommentsForTwitterThread(post);
                }
            }
        }
    }

    private void seedCommentsForProductLaunch(Post post) {
        // High likes (4,820 likes), but 62.5% negative feedback -> Demonstrating The Vanity Metrics Problem
        List<String[]> comments = List.of(
            new String[]{"Sarah Jenkins", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100", "The new update completely broke our reports. Charts will not load at all!"},
            new String[]{"Marcus Vance", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100", "App crashes every time I try to export PDF. Very disappointing for a paid tool."},
            new String[]{"Elena Rostova", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100", "Support has been unresponsive for 48 hours. Can someone please help with this bug?"},
            new String[]{"David Miller", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100", "Love the new UI design! The competitor leaderboard looks super clean."},
            new String[]{"Priya Sharma", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100", "Worst release yet. Constantly lagging and freezing on mobile."},
            new String[]{"Thomas Wright", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100", "Please issue a refund if this crash issue isn't fixed today."},
            new String[]{"Claire Dupont", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=100", "Is YouTube shorts tracking included in this plan?"},
            new String[]{"Liam O'Connor", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=100", "Awesome features and great potential once the stability bugs are resolved."}
        );

        for (String[] c : comments) {
            commentSentimentService.addCommentAndAnalyze(post.getId(), c[0], c[1], c[2]);
        }
    }

    private void seedCommentsForLinkedInStudy(Post post) {
        // High quality B2B engagement (83% positive)
        List<String[]> comments = List.of(
            new String[]{"Robert Chen (VP Marketing)", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100", "Incredible benchmark study! Shared directly with our executive steering team."},
            new String[]{"Amanda Lewis (Head of Growth)", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100", "Gamechanger methodology for comparing B2B organic reach against competitor ads."},
            new String[]{"Daniel Thorne", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100", "Best analysis on cross-platform data consolidation I have seen all year."},
            new String[]{"Jessica Tan", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100", "Where can we request custom white-label reports for our agency clients?"},
            new String[]{"Vikram Malhotra", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100", "Brilliant insights. Saves our marketing analysts countless hours every week."},
            new String[]{"Sophie Martin", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100", "Excellent quality research. Looking forward to the next quarter edition!"}
        );

        for (String[] c : comments) {
            commentSentimentService.addCommentAndAnalyze(post.getId(), c[0], c[1], c[2]);
        }
    }

    private void seedCommentsForYouTubeMasterclass(Post post) {
        List<String[]> comments = List.of(
            new String[]{"Alex Rivera", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100", "This masterclass is fantastic. Explained everything from database to charts."},
            new String[]{"Maria Santos", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100", "Awesome tutorial! Learned more in 45 mins than in a full course."},
            new String[]{"Kevin Brooks", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100", "What font are you using in the IDE preview?"},
            new String[]{"Natalie Wood", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100", "Great quality video, thank you for sharing the architecture blueprint."}
        );

        for (String[] c : comments) {
            commentSentimentService.addCommentAndAnalyze(post.getId(), c[0], c[1], c[2]);
        }
    }

    private void seedCommentsForTwitterThread(Post post) {
        List<String[]> comments = List.of(
            new String[]{"TechInsider", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100", "Spot on analysis. Multi-channel fragmentation really kills marketing agility."},
            new String[]{"GrowthHackerX", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=100", "Love this thread! Bookmarked for our next quarterly review."},
            new String[]{"Chloe Bennett", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100", "How often do you refresh competitor benchmarks?"}
        );

        for (String[] c : comments) {
            commentSentimentService.addCommentAndAnalyze(post.getId(), c[0], c[1], c[2]);
        }
    }

    private void seedCompMetric(Competitor competitor, SocialPlatform platform, int followers, int growth,
                                BigDecimal freq, int likes, int comments, int shares, BigDecimal rate) {
        CompetitorMetric cm = new CompetitorMetric();
        cm.setCompetitor(competitor);
        cm.setPlatform(platform);
        cm.setRecordedDate(LocalDate.now());
        cm.setFollowers(followers);
        cm.setFollowerGrowth(growth);
        cm.setPostingFrequencyPerWeek(freq);
        cm.setAvgLikes(likes);
        cm.setAvgComments(comments);
        cm.setAvgShares(shares);
        cm.setEngagementRate(rate);
        competitorMetricRepository.save(cm);
    }

    @Transactional
    public void purgeDemoDataForUser(Long userId) {
        // Delete all demo accounts and associated posts/comments/metrics
        List<SocialAccount> demoAccounts = socialAccountRepository.findByUserId(userId).stream()
                .filter(SocialAccount::isDemo)
                .toList();

        for (SocialAccount acc : demoAccounts) {
            List<Post> posts = postRepository.findBySocialAccountId(acc.getId());
            for (Post p : posts) {
                postCommentRepository.deleteByPostId(p.getId());
                postRepository.delete(p);
            }
            socialMetricRepository.deleteBySocialAccountId(acc.getId());
            socialAccountRepository.delete(acc);
        }

        // Delete all demo competitors and metrics
        List<Competitor> demoCompetitors = competitorRepository.findByUserId(userId).stream()
                .filter(Competitor::isDemo)
                .toList();

        for (Competitor c : demoCompetitors) {
            competitorMetricRepository.deleteByCompetitorId(c.getId());
            competitorRepository.delete(c);
        }
    }
}
