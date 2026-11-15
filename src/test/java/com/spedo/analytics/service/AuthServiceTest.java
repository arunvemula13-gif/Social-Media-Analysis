package com.spedo.analytics.service;

import com.spedo.analytics.dto.AuthResponse;
import com.spedo.analytics.dto.RegisterRequest;
import com.spedo.analytics.dto.UserDto;
import com.spedo.analytics.entity.Role;
import com.spedo.analytics.entity.User;
import com.spedo.analytics.exception.BadRequestException;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.UserRepository;
import com.spedo.analytics.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private EntityDtoMapper mapper;

    @Mock
    private DemoDataService demoDataService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Register: successfully creates user and returns token")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("test@spedo.io", "Password123!", "Test User", "SPEDO Corp");

        when(userRepository.existsByEmail("test@spedo.io")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedHash");

        User savedUser = new User("test@spedo.io", "$2a$10$encodedHash", "Test User", "SPEDO Corp", Role.ROLE_USER);
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(tokenProvider.generateToken(any())).thenReturn("mocked-jwt-token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        UserDto userDto = new UserDto(10L, "test@spedo.io", "Test User", "SPEDO Corp", "ROLE_USER", LocalDateTime.now());
        when(mapper.toUserDto(savedUser)).thenReturn(userDto);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("test@spedo.io", response.getUser().getEmail());
        verify(demoDataService, never()).seedDemoDataForUser(any());
    }

    @Test
    @DisplayName("Register: seeds sample data only when demo mode is enabled")
    void testRegister_DemoModeEnabled_SeedsDemoData() {
        ReflectionTestUtils.setField(authService, "demoModeEnabled", true);
        RegisterRequest request = new RegisterRequest("demo-test@spedo.io", "Password123!", "Demo User", "SPEDO Corp");
        User savedUser = new User("demo-test@spedo.io", "$2a$10$encodedHash", "Demo User", "SPEDO Corp", Role.ROLE_USER);
        savedUser.setId(11L);

        when(userRepository.existsByEmail("demo-test@spedo.io")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateToken(any())).thenReturn("mocked-jwt-token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(mapper.toUserDto(savedUser)).thenReturn(
                new UserDto(11L, "demo-test@spedo.io", "Demo User", "SPEDO Corp", "ROLE_USER", LocalDateTime.now()));

        authService.register(request);

        verify(demoDataService).seedDemoDataForUser(savedUser);
    }

    @Test
    @DisplayName("Register: rejects duplicate email with BadRequestException")
    void testRegister_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest("existing@spedo.io", "Password123!", "Existing User", null);
        when(userRepository.existsByEmail("existing@spedo.io")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}
