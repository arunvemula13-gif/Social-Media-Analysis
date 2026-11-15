-- ==============================================================
-- SPEDO Social Media Analytics & Competitive Intelligence Platform
-- Flyway Database Migration: V2__seed_demo_data.sql
-- ==============================================================

-- 1. Seed Demo User (password: Password123!)
-- Hash: $2a$10$wN3kK6k8Rgn2t7t2p8zIEOZ5rL/z4OQpQyqMhO1f4xR6LqZqjC3sO
INSERT INTO users (id, email, password, full_name, organization, role, enabled, created_at, updated_at)
VALUES (
    1,
    'demo@spedo.io',
    '$2a$10$wN3kK6k8Rgn2t7t2p8zIEOZ5rL/z4OQpQyqMhO1f4xR6LqZqjC3sO',
    'Alex Rivers',
    'SPEDO Global Inc.',
    'ROLE_USER',
    TRUE,
    '2026-07-01 09:00:00',
    '2026-10-26 12:00:00'
) ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 2. Seed Social Accounts for Demo User
INSERT INTO social_accounts (id, user_id, platform, account_id, account_name, handle, profile_image_url, follower_count, following_count, status, last_synced_at, is_demo, created_at, updated_at)
VALUES 
(1, 1, 'INSTAGRAM', 'ig_spedo_official', 'SPEDO Official', '@spedotech', 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150', 142500, 380, 'CONNECTED', '2026-10-26 10:15:00', TRUE, '2026-07-01 10:00:00', '2026-10-26 10:15:00'),
(2, 1, 'FACEBOOK', 'fb_spedo_global', 'SPEDO Global', '@spedoglobal', 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=150', 98200, 120, 'CONNECTED', '2026-10-26 10:15:00', TRUE, '2026-07-01 10:00:00', '2026-10-26 10:15:00'),
(3, 1, 'YOUTUBE', 'yt_spedo_hq', 'SPEDO HQ', '@spedohq', 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150', 64800, 45, 'CONNECTED', '2026-10-26 10:15:00', TRUE, '2026-07-01 10:00:00', '2026-10-26 10:15:00'),
(4, 1, 'LINKEDIN', 'li_spedo_tech', 'SPEDO Technologies', '@spedo-enterprise', 'https://images.unsplash.com/photo-1557683316-973673baf926?w=150', 41300, 210, 'CONNECTED', '2026-10-26 10:15:00', TRUE, '2026-07-01 10:00:00', '2026-10-26 10:15:00'),
(5, 1, 'TWITTER', 'tw_spedo_app', 'SPEDO', '@spedo_app', 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=150', 85900, 530, 'CONNECTED', '2026-10-26 10:15:00', TRUE, '2026-07-01 10:00:00', '2026-10-26 10:15:00')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 3. Seed Posts across Accounts
INSERT INTO posts (id, social_account_id, platform, platform_post_id, caption, media_url, post_url, post_type, published_at, likes_count, comments_count, shares_count, impressions_count, reach_count, engagement_rate, created_at, updated_at)
VALUES
(1, 1, 'INSTAGRAM', 'post_ig_101', '🚀 Introducing our next-gen social intelligence suite. Benchmarking competitor growth has never been this seamless! What feature are you most excited about? #SaaS #Analytics #GrowthHacking #MarTech', 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800', 'https://instagram.com/p/spedo101', 'CAROUSEL', '2026-10-25 14:30:00', 4820, 312, 540, 68400, 52100, 4.2250, '2026-10-25 14:30:00', '2026-10-25 14:30:00'),
(2, 1, 'INSTAGRAM', 'post_ig_102', 'Stop guessing what your audience wants. Analyze real-time engagement patterns across every social channel with SPEDO. Link in bio to start your trial. 📊✨', 'https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800', 'https://instagram.com/p/spedo102', 'IMAGE', '2026-10-23 11:00:00', 3650, 184, 290, 49200, 38400, 2.9700, '2026-10-23 11:00:00', '2026-10-23 11:00:00'),
(3, 1, 'INSTAGRAM', 'post_ig_103', 'Behind the scenes at our product sprint! Coffee, whiteboards, and lots of data science magic. ☕💡 Tag your favorite work buddy!', 'https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800', 'https://instagram.com/p/spedo103', 'IMAGE', '2026-10-20 16:45:00', 2910, 240, 115, 39800, 31200, 2.3100, '2026-10-20 16:45:00', '2026-10-20 16:45:00'),
(4, 1, 'INSTAGRAM', 'post_ig_104', 'Did you know? Brands that benchmark weekly increase audience retention by 37%. Are you monitoring your competitors effectively?', 'https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800', 'https://instagram.com/p/spedo104', 'IMAGE', '2026-10-18 10:15:00', 4150, 295, 410, 56300, 44200, 3.4800, '2026-10-18 10:15:00', '2026-10-18 10:15:00'),
(5, 1, 'INSTAGRAM', 'post_ig_105', 'Community spotlight: How Fintech scale-up Nexa amplified their organic reach by 4x using SPEDO competitor alerts. 🏆', 'https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=800', 'https://instagram.com/p/spedo105', 'CAROUSEL', '2026-10-14 09:30:00', 3420, 175, 320, 45100, 35600, 2.7600, '2026-10-14 09:30:00', '2026-10-14 09:30:00'),

(6, 2, 'FACEBOOK', 'post_fb_201', 'We are thrilled to be recognized as a Category Leader in Social Media Analytics for Q3 2026! A huge thank you to our global users and partners who drive us forward every day. Read the full benchmark report: spedo.io/reports/q3', 'https://images.unsplash.com/photo-1551836022-d5d88e9218df?w=800', 'https://facebook.com/spedo/posts/201', 'IMAGE', '2026-10-24 15:00:00', 2140, 185, 430, 42100, 33500, 2.8200, '2026-10-24 15:00:00', '2026-10-24 15:00:00'),
(7, 2, 'FACEBOOK', 'post_fb_202', 'Webinar Alert 🔴: Unlocking High-Converting Social Content in 2027. Join our VP of Product and guest speakers this Thursday at 2 PM EST.', 'https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?w=800', 'https://facebook.com/spedo/posts/202', 'IMAGE', '2026-10-21 12:30:00', 1650, 120, 210, 31200, 24800, 2.0100, '2026-10-21 12:30:00', '2026-10-21 12:30:00'),
(8, 2, 'FACEBOOK', 'post_fb_203', '5 common metrics that mislead marketing leaders (and 3 you should actually watch). Swipe through to see our breakdown.', 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=800', 'https://facebook.com/spedo/posts/203', 'CAROUSEL', '2026-10-16 14:00:00', 2780, 260, 520, 51000, 41200, 3.6300, '2026-10-16 14:00:00', '2026-10-16 14:00:00'),

(9, 3, 'YOUTUBE', 'post_yt_301', 'Full Masterclass: How to Build a Competitor Intelligence Dashboard from Scratch [45 Min Deep Dive]', 'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?w=800', 'https://youtube.com/watch?v=spedo301', 'VIDEO', '2026-10-22 18:00:00', 5840, 620, 890, 88200, 71500, 4.8500, '2026-10-22 18:00:00', '2026-10-22 18:00:00'),
(10, 3, 'YOUTUBE', 'post_yt_302', 'SPEDO Product Tour 2026 — Track, Benchmark, and Outperform in 5 Minutes', 'https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800', 'https://youtube.com/watch?v=spedo302', 'VIDEO', '2026-10-15 17:30:00', 4320, 390, 480, 62300, 49800, 3.9100, '2026-10-15 17:30:00', '2026-10-15 17:30:00'),

(11, 4, 'LINKEDIN', 'post_li_401', 'The modern CMO does not look at follower counts anymore. They look at engagement velocity, audience affinity, and competitive share of voice. Here is our 2026 B2B Benchmark Study with data from 1,200 enterprise tech brands.', 'https://images.unsplash.com/photo-1557804506-669a67965ba0?w=800', 'https://linkedin.com/feed/update/spedo401', 'IMAGE', '2026-10-24 09:00:00', 3120, 410, 640, 48500, 39200, 4.3800, '2026-10-24 09:00:00', '2026-10-24 09:00:00'),
(12, 4, 'LINKEDIN', 'post_li_402', 'Excited to announce our new ISO 27001 & SOC 2 Type II compliance certifications. As we expand across enterprise finance and healthcare, security remains paramount.', 'https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800', 'https://linkedin.com/feed/update/spedo402', 'TEXT', '2026-10-19 13:15:00', 2190, 145, 230, 34200, 27600, 3.1200, '2026-10-19 13:15:00', '2026-10-19 13:15:00'),

(13, 5, 'TWITTER', 'post_tw_501', 'Thread: 7 subtle signals your competitors are scaling up their social spend before you even see their ads 🧵👇', 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800', 'https://x.com/spedo_app/status/501', 'TEXT', '2026-10-25 19:20:00', 4910, 520, 1840, 94200, 78900, 4.6700, '2026-10-25 19:20:00', '2026-10-25 19:20:00'),
(14, 5, 'TWITTER', 'post_tw_502', 'If you are publishing 5x a week without benchmarking performance against top industry leaders, you are driving with your eyes closed.', 'https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800', 'https://x.com/spedo_app/status/502', 'TEXT', '2026-10-22 15:40:00', 3240, 310, 890, 61400, 51200, 3.3900, '2026-10-22 15:40:00', '2026-10-22 15:40:00'),
(15, 5, 'TWITTER', 'post_tw_503', 'New update: Custom PDF and CSV report schedules are live in SPEDO! Generate board-ready decks in 10 seconds flat. 🚀', 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800', 'https://x.com/spedo_app/status/503', 'IMAGE', '2026-10-17 11:00:00', 2650, 210, 620, 44200, 36500, 2.9800, '2026-10-17 11:00:00', '2026-10-17 11:00:00')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4. Seed Daily Metrics (social_metrics) for Recent Days
INSERT INTO social_metrics (social_account_id, recorded_date, followers, follower_growth, impressions, reach, total_likes, total_comments, total_shares, engagement_rate, created_at)
VALUES
-- Account 1 (Instagram)
(1, '2026-10-20', 140200, 210, 58400, 46100, 3100, 210, 180, 2.4900, '2026-10-20 23:59:59'),
(1, '2026-10-21', 140550, 350, 61200, 48300, 3350, 240, 210, 2.7000, '2026-10-21 23:59:59'),
(1, '2026-10-22', 140980, 430, 64900, 51400, 3620, 275, 260, 2.9400, '2026-10-22 23:59:59'),
(1, '2026-10-23', 141420, 440, 68100, 53900, 3940, 290, 290, 3.2000, '2026-10-23 23:59:59'),
(1, '2026-10-24', 141900, 480, 71500, 56700, 4210, 310, 340, 3.4200, '2026-10-24 23:59:59'),
(1, '2026-10-25', 142210, 310, 75200, 59800, 4820, 360, 410, 3.9300, '2026-10-25 23:59:59'),
(1, '2026-10-26', 142500, 290, 78100, 61400, 5100, 380, 440, 4.1500, '2026-10-26 23:59:59'),

-- Account 2 (Facebook)
(2, '2026-10-20', 97100, 110, 38100, 30200, 1850, 140, 160, 2.2100, '2026-10-20 23:59:59'),
(2, '2026-10-21', 97320, 220, 39400, 31500, 1980, 155, 185, 2.3800, '2026-10-21 23:59:59'),
(2, '2026-10-22', 97510, 190, 41200, 32900, 2100, 160, 210, 2.5300, '2026-10-22 23:59:59'),
(2, '2026-10-23', 97750, 240, 42800, 34100, 2240, 175, 240, 2.7100, '2026-10-23 23:59:59'),
(2, '2026-10-24', 97980, 230, 44900, 35800, 2450, 190, 280, 2.9800, '2026-10-24 23:59:59'),
(2, '2026-10-25', 98100, 120, 46100, 36900, 2520, 195, 305, 3.0800, '2026-10-25 23:59:59'),
(2, '2026-10-26', 98200, 100, 47200, 37500, 2610, 205, 320, 3.1900, '2026-10-26 23:59:59'),

-- Account 3 (YouTube)
(3, '2026-10-20', 63900, 90, 68000, 54000, 4200, 310, 380, 3.4200, '2026-10-20 23:59:59'),
(3, '2026-10-21', 64050, 150, 71200, 56500, 4450, 335, 410, 3.6300, '2026-10-21 23:59:59'),
(3, '2026-10-22', 64220, 170, 76800, 61000, 4800, 380, 490, 4.0200, '2026-10-22 23:59:59'),
(3, '2026-10-23', 64410, 190, 79200, 62800, 5100, 410, 520, 4.2900, '2026-10-23 23:59:59'),
(3, '2026-10-24', 64590, 180, 82500, 65400, 5350, 430, 560, 4.4900, '2026-10-24 23:59:59'),
(3, '2026-10-25', 64710, 120, 85100, 67500, 5600, 460, 610, 4.7100, '2026-10-25 23:59:59'),
(3, '2026-10-26', 64800, 90, 87400, 69200, 5840, 480, 650, 4.8900, '2026-10-26 23:59:59'),

-- Account 4 (LinkedIn)
(4, '2026-10-20', 40700, 80, 32100, 25800, 1890, 120, 180, 2.6800, '2026-10-20 23:59:59'),
(4, '2026-10-21', 40820, 120, 33500, 26900, 2010, 135, 195, 2.8500, '2026-10-21 23:59:59'),
(4, '2026-10-22', 40950, 130, 35100, 28200, 2180, 145, 220, 3.0900, '2026-10-22 23:59:59'),
(4, '2026-10-23', 41080, 130, 36800, 29500, 2340, 160, 245, 3.3200, '2026-10-23 23:59:59'),
(4, '2026-10-24', 41190, 110, 38900, 31200, 2520, 180, 270, 3.5800, '2026-10-24 23:59:59'),
(4, '2026-10-25', 41250, 60, 40100, 32100, 2610, 185, 285, 3.7100, '2026-10-25 23:59:59'),
(4, '2026-10-26', 41300, 50, 41200, 33000, 2700, 195, 300, 3.8400, '2026-10-26 23:59:59'),

-- Account 5 (Twitter/X)
(5, '2026-10-20', 84900, 140, 62000, 49600, 3800, 340, 1100, 3.2500, '2026-10-20 23:59:59'),
(5, '2026-10-21', 85110, 210, 65400, 52300, 4050, 370, 1220, 3.4900, '2026-10-21 23:59:59'),
(5, '2026-10-22', 85320, 210, 69800, 55800, 4390, 410, 1380, 3.8200, '2026-10-22 23:59:59'),
(5, '2026-10-23', 85550, 230, 73500, 58800, 4680, 440, 1510, 4.1000, '2026-10-23 23:59:59'),
(5, '2026-10-24', 85740, 190, 78200, 62500, 5020, 480, 1690, 4.4300, '2026-10-24 23:59:59'),
(5, '2026-10-25', 85850, 110, 81900, 65500, 5340, 510, 1820, 4.7200, '2026-10-25 23:59:59'),
(5, '2026-10-26', 85900, 50, 84300, 67400, 5580, 530, 1910, 4.9300, '2026-10-26 23:59:59')
ON DUPLICATE KEY UPDATE followers = VALUES(followers);

-- 5. Seed Competitors for Demo User
INSERT INTO competitors (id, user_id, name, website, industry, instagram_handle, facebook_handle, youtube_handle, linkedin_handle, twitter_handle, is_demo, created_at, updated_at)
VALUES
(1, 1, 'ApexMetrics', 'https://apexmetrics.io', 'SaaS Analytics', '@apex_metrics', '@apexmetricsapp', '@apexmetricshq', '@apex-metrics', '@apexmetrics', TRUE, '2026-07-10 12:00:00', '2026-10-26 12:00:00'),
(2, 1, 'PulseTrack', 'https://pulsetrack.com', 'Marketing Intelligence', '@pulsetrack_hq', '@pulsetrackofficial', '@pulsetracktv', '@pulse-track', '@pulsetrack', TRUE, '2026-07-15 14:00:00', '2026-10-26 12:00:00'),
(3, 1, 'OmniVibe Media', 'https://omnivibe.co', 'Social Marketing', '@omnivibe_social', '@omnivibeglobal', '@omnivibechannel', '@omnivibe-media', '@omnivibe', TRUE, '2026-08-01 10:00:00', '2026-10-26 12:00:00'),
(4, 1, 'DataEcho', 'https://dataecho.io', 'BI & Analytics', '@dataecho_ai', '@dataecholabs', '@dataechotech', '@dataecho', '@dataecho_ai', TRUE, '2026-08-20 16:00:00', '2026-10-26 12:00:00')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 6. Seed Competitor Metrics
INSERT INTO competitor_metrics (id, competitor_id, platform, recorded_date, followers, follower_growth, posting_frequency_per_week, avg_likes, avg_comments, avg_shares, engagement_rate, created_at)
VALUES
-- ApexMetrics
(1, 1, 'INSTAGRAM', '2026-10-26', 118400, 180, 4.5, 2940, 185, 240, 2.8400, '2026-10-26 12:00:00'),
(2, 1, 'TWITTER', '2026-10-26', 76200, 120, 8.2, 2810, 210, 740, 3.1200, '2026-10-26 12:00:00'),
(3, 1, 'LINKEDIN', '2026-10-26', 36800, 95, 3.0, 1620, 110, 180, 2.9100, '2026-10-26 12:00:00'),

-- PulseTrack
(4, 2, 'INSTAGRAM', '2026-10-26', 165000, 310, 6.0, 5210, 340, 490, 3.6600, '2026-10-26 12:00:00'),
(5, 2, 'TWITTER', '2026-10-26', 104500, 240, 12.0, 4650, 410, 1420, 4.2800, '2026-10-26 12:00:00'),
(6, 2, 'LINKEDIN', '2026-10-26', 58200, 160, 4.5, 2890, 240, 360, 4.1200, '2026-10-26 12:00:00'),

-- OmniVibe Media
(7, 3, 'INSTAGRAM', '2026-10-26', 92300, 140, 3.5, 1980, 130, 160, 2.4600, '2026-10-26 12:00:00'),
(8, 3, 'TWITTER', '2026-10-26', 64100, 85, 5.0, 1750, 140, 430, 2.7800, '2026-10-26 12:00:00'),
(9, 3, 'LINKEDIN', '2026-10-26', 28400, 60, 2.5, 980, 75, 110, 2.2100, '2026-10-26 12:00:00'),

-- DataEcho
(10, 4, 'INSTAGRAM', '2026-10-26', 134200, 220, 5.0, 3820, 250, 340, 3.2900, '2026-10-26 12:00:00'),
(11, 4, 'TWITTER', '2026-10-26', 91500, 190, 9.0, 3940, 330, 1180, 3.9600, '2026-10-26 12:00:00'),
(12, 4, 'LINKEDIN', '2026-10-26', 49600, 130, 3.8, 2250, 190, 270, 3.4800, '2026-10-26 12:00:00')
ON DUPLICATE KEY UPDATE followers = VALUES(followers);

-- 7. Seed Sample Reports
INSERT INTO reports (id, user_id, title, report_type, date_from, date_to, file_format, status, report_data_json, download_url, created_at)
VALUES
(1, 1, 'Weekly Performance Summary - Week 43', 'WEEKLY', '2026-10-19', '2026-10-25', 'CSV', 'COMPLETED', '{"totalFollowers":432700,"growthRate":2.4,"avgEngagementRate":3.81,"topPlatform":"INSTAGRAM"}', '/api/reports/1/csv', '2026-10-25 23:00:00'),
(2, 1, 'Q3 Competitor Benchmarking Audit', 'COMPETITOR_COMPARISON', '2026-07-01', '2026-09-30', 'PDF', 'COMPLETED', '{"competitorCount":4,"leaderEngagement":"PulseTrack","ourRank":2}', '/api/reports/2/content', '2026-10-01 10:00:00'),
(3, 1, 'Cross-Platform Channel Comparison', 'PLATFORM_PERFORMANCE', '2026-10-01', '2026-10-26', 'CSV', 'COMPLETED', '{"platforms":["INSTAGRAM","FACEBOOK","YOUTUBE","LINKEDIN","TWITTER"],"highestReach":"TWITTER"}', '/api/reports/3/csv', '2026-10-26 09:30:00')
ON DUPLICATE KEY UPDATE status = 'COMPLETED';

-- 8. Seed Sample Notifications
INSERT INTO notifications (id, user_id, title, message, type, is_read, link, created_at)
VALUES
(1, 1, 'Weekly Report Ready', 'Your Weekly Performance Summary for Week 43 has been successfully compiled and is ready for export.', 'REPORT_GENERATED', FALSE, '/reports.html', '2026-10-25 23:01:00'),
(2, 1, 'Follower Milestone Reached', 'Congratulations! Your Instagram channel (@spedotech) crossed 142,000 total followers.', 'METRIC_MILESTONE', FALSE, '/dashboard.html', '2026-10-25 15:45:00'),
(3, 1, 'Social Sync Completed', 'All 5 connected social channels (Instagram, Facebook, YouTube, LinkedIn, X) synced successfully.', 'SYNC_STATUS', TRUE, '/social-accounts.html', '2026-10-26 10:15:00'),
(4, 1, 'Competitor Activity Alert', 'PulseTrack published 3 high-engagement posts in the last 24 hours on Twitter/X.', 'SYSTEM_ALERT', TRUE, '/competitors.html', '2026-10-24 18:20:00')
ON DUPLICATE KEY UPDATE is_read = VALUES(is_read);
