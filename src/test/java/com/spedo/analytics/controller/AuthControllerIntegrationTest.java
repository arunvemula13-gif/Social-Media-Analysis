package com.spedo.analytics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spedo.analytics.dto.LoginRequest;
import com.spedo.analytics.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/login - Successfully authenticates seeded demo user")
    void testLogin_DemoUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("demo@spedo.io", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("demo@spedo.io"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Successfully registers new user and generates token")
    void testRegister_NewUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "sarah.connor@spedo.io",
                "SecretPass123!",
                "Sarah Connor",
                "Cyberdyne Media"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("sarah.connor@spedo.io"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Rejects invalid password with 401")
    void testLogin_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("demo@spedo.io", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
