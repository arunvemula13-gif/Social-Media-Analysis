package com.spedo.analytics.controller;

import com.spedo.analytics.security.JwtTokenProvider;
import com.spedo.analytics.security.UserPrincipal;
import com.spedo.analytics.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private String validJwtToken;

    @BeforeEach
    void setUp() {
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername("demo@spedo.io");
        validJwtToken = "Bearer " + tokenProvider.generateToken(principal);
    }

    @Test
    @DisplayName("GET /api/dashboard/summary - Returns 200 with aggregated metrics")
    void testGetSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary?days=30")
                        .header("Authorization", validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalFollowers").isNumber())
                .andExpect(jsonPath("$.data.engagementRate").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/dashboard/platform-comparison - Returns platform breakdowns")
    void testGetPlatformComparison() throws Exception {
        mockMvc.perform(get("/api/dashboard/platform-comparison?days=30")
                        .header("Authorization", validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/summary - Rejects request without token with 401")
    void testGetSummary_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary?days=30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
