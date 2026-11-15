-- ==============================================================
-- SPEDO Social Media Analytics & Competitive Intelligence Platform
-- Flyway Database Migration: V1__init_schema.sql
-- ==============================================================

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    organization VARCHAR(150) NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Social Accounts Table
CREATE TABLE IF NOT EXISTS social_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    account_id VARCHAR(100) NOT NULL,
    account_name VARCHAR(150) NOT NULL,
    handle VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500) NULL,
    follower_count INT NOT NULL DEFAULT 0,
    following_count INT NOT NULL DEFAULT 0,
    channel_views BIGINT NOT NULL DEFAULT 0,
    video_count INT NOT NULL DEFAULT 0,
    access_token VARCHAR(1000) NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CONNECTED',
    last_synced_at TIMESTAMP NULL,
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_platform_account UNIQUE (user_id, platform, account_id),
    INDEX idx_social_accounts_user_platform (user_id, platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Social Metrics Table
CREATE TABLE IF NOT EXISTS social_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    social_account_id BIGINT NOT NULL,
    recorded_date DATE NOT NULL,
    followers INT NOT NULL DEFAULT 0,
    follower_growth INT NOT NULL DEFAULT 0,
    impressions INT NOT NULL DEFAULT 0,
    reach INT NOT NULL DEFAULT 0,
    total_likes INT NOT NULL DEFAULT 0,
    total_comments INT NOT NULL DEFAULT 0,
    total_shares INT NOT NULL DEFAULT 0,
    engagement_rate DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_social_metrics_account FOREIGN KEY (social_account_id) REFERENCES social_accounts(id) ON DELETE CASCADE,
    CONSTRAINT uk_account_date UNIQUE (social_account_id, recorded_date),
    INDEX idx_social_metrics_date (recorded_date),
    INDEX idx_social_metrics_account_date (social_account_id, recorded_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Posts Table
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    social_account_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    platform_post_id VARCHAR(100) NOT NULL,
    caption TEXT NULL,
    media_url VARCHAR(500) NULL,
    post_url VARCHAR(500) NULL,
    post_type VARCHAR(50) NOT NULL DEFAULT 'IMAGE',
    published_at TIMESTAMP NOT NULL,
    likes_count INT NOT NULL DEFAULT 0,
    comments_count INT NOT NULL DEFAULT 0,
    shares_count INT NOT NULL DEFAULT 0,
    impressions_count INT NOT NULL DEFAULT 0,
    reach_count INT NOT NULL DEFAULT 0,
    engagement_rate DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_account FOREIGN KEY (social_account_id) REFERENCES social_accounts(id) ON DELETE CASCADE,
    INDEX idx_posts_account_platform (social_account_id, platform),
    INDEX idx_posts_published (published_at DESC),
    INDEX idx_posts_engagement (engagement_rate DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Competitors Table
CREATE TABLE IF NOT EXISTS competitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    website VARCHAR(255) NULL,
    industry VARCHAR(100) NULL,
    instagram_handle VARCHAR(100) NULL,
    facebook_handle VARCHAR(100) NULL,
    youtube_handle VARCHAR(100) NULL,
    linkedin_handle VARCHAR(100) NULL,
    twitter_handle VARCHAR(100) NULL,
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_competitors_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_competitors_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Competitor Metrics Table
CREATE TABLE IF NOT EXISTS competitor_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competitor_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    recorded_date DATE NOT NULL,
    followers INT NOT NULL DEFAULT 0,
    follower_growth INT NOT NULL DEFAULT 0,
    posting_frequency_per_week DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    avg_likes INT NOT NULL DEFAULT 0,
    avg_comments INT NOT NULL DEFAULT 0,
    avg_shares INT NOT NULL DEFAULT 0,
    engagement_rate DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comp_metrics_competitor FOREIGN KEY (competitor_id) REFERENCES competitors(id) ON DELETE CASCADE,
    INDEX idx_comp_metrics_lookup (competitor_id, platform, recorded_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Reports Table
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    date_from DATE NOT NULL,
    date_to DATE NOT NULL,
    file_format VARCHAR(20) NOT NULL DEFAULT 'CSV',
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    report_data_json LONGTEXT NULL,
    download_url VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reports_user_created (user_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    link VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user_read (user_id, is_read, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
