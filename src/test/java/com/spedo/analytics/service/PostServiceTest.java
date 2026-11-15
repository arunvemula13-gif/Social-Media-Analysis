package com.spedo.analytics.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostServiceTest {

    @Test
    @DisplayName("Calculate Engagement Rate: standard impressions denominator")
    void testCalculateEngagementRate_WithImpressions() {
        // (100 likes + 20 comments + 30 shares = 150) / 5000 impressions = 0.03 * 100 = 3.0000%
        BigDecimal rate = PostService.calculateEngagementRate(100, 20, 30, 5000, 4000, 10000);
        assertEquals(new BigDecimal("3.0000"), rate);
    }

    @Test
    @DisplayName("Calculate Engagement Rate: fallback to reach when impressions is zero")
    void testCalculateEngagementRate_FallbackToReach() {
        // 150 interactions / 3000 reach = 0.05 * 100 = 5.0000%
        BigDecimal rate = PostService.calculateEngagementRate(100, 20, 30, 0, 3000, 10000);
        assertEquals(new BigDecimal("5.0000"), rate);
    }

    @Test
    @DisplayName("Calculate Engagement Rate: fallback to followers when impressions & reach are zero")
    void testCalculateEngagementRate_FallbackToFollowers() {
        // 150 interactions / 10000 followers = 0.015 * 100 = 1.5000%
        BigDecimal rate = PostService.calculateEngagementRate(100, 20, 30, 0, 0, 10000);
        assertEquals(new BigDecimal("1.5000"), rate);
    }

    @Test
    @DisplayName("Calculate Engagement Rate: handles zero denominator gracefully")
    void testCalculateEngagementRate_ZeroDenominator() {
        BigDecimal rate = PostService.calculateEngagementRate(100, 20, 30, 0, 0, 0);
        assertEquals(new BigDecimal("0.0000"), rate);
    }
}
