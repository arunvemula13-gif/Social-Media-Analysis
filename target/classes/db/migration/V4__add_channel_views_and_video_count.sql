-- ==============================================================
-- Flyway Database Migration: V4__add_channel_views_and_video_count.sql
-- ==============================================================

ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS channel_views BIGINT NOT NULL DEFAULT 0;
ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS video_count INT NOT NULL DEFAULT 0;
ALTER TABLE social_accounts ADD COLUMN IF NOT EXISTS access_token VARCHAR(1000) NULL;
