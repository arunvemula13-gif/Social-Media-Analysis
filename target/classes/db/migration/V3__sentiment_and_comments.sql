-- ==============================================================
-- Flyway Database Migration: V3__sentiment_and_comments.sql
-- The Vanity Metrics Problem: Comment Sentiment Analysis & NLP Engine
-- ==============================================================

-- 1. Add Sentiment Analysis Columns to Posts Table
ALTER TABLE posts ADD COLUMN IF NOT EXISTS positive_comments_count INT NOT NULL DEFAULT 0;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS neutral_comments_count INT NOT NULL DEFAULT 0;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS negative_comments_count INT NOT NULL DEFAULT 0;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS positive_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS negative_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS sentiment_score DECIMAL(5,2) NOT NULL DEFAULT 0.00;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS vanity_alert BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS sentiment_verdict VARCHAR(500) NULL;

-- 2. Create Post Comments Table for Natural Language Text Processing
CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author VARCHAR(100) NOT NULL,
    author_avatar VARCHAR(500) NULL,
    comment_text TEXT NOT NULL,
    sentiment VARCHAR(20) NOT NULL DEFAULT 'NEUTRAL',
    sentiment_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    detected_keyword VARCHAR(100) NULL,
    commented_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_comments_post FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    INDEX idx_post_comments_post (post_id),
    INDEX idx_post_comments_sentiment (sentiment)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
