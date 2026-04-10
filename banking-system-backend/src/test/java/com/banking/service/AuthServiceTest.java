package com.banking.service;

import com.banking.config.JwtUtil;
import com.banking.dto.AuthResponse;
import com.banking.dto.LoginRequest;
import com.banking.dto.RegisterRequest;
import com.banking.model.User;
import com.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Mary Anger");
        testUser.setEmail("mary@example.com");
        testUser.setPassword("encoded_password");
        testUser.setRole("USER");
    }

    // ============================
    // REGISTER TESTS
    // ============================

    @Test
    @DisplayName("Register - Success")
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Mary Anger", "mary@example.com", "123456");

        when(userRepository.existsByEmail("mary@example.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertTrue(response.isSuccess());
        assertEquals("Registration successful", response.getMessage());
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("Mary Anger", response.getName());
        assertEquals("mary@example.com", response.getEmail());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Email Already Exists")
    void register_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Mary Anger", "mary@example.com", "123456");

        when(userRepository.existsByEmail("mary@example.com")).thenReturn(true);

        AuthResponse response = authService.register(request);

        assertFalse(response.isSuccess());
        assertEquals("Email already exists", response.getMessage());
        assertNull(response.getToken());

        verify(userRepository, never()).save(any(User.class));
    }

    // ============================
    // LOGIN TESTS
    // ============================

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest request = new LoginRequest("mary@example.com", "123456");

        when(userRepository.findByEmail("mary@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("123456", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
    }

    @Test
    @DisplayName("Login - Invalid Password")
    void login_InvalidPassword() {
        LoginRequest request = new LoginRequest("mary@example.com", "wrongpassword");

        when(userRepository.findByEmail("mary@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        AuthResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertEquals("Invalid password", response.getMessage());
        assertNull(response.getToken());
    }

    @Test
    @DisplayName("Login - User Not Found")
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "123456");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AuthResponse response = authService.login(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("User not found"));
        assertNull(response.getToken());
    }
}
