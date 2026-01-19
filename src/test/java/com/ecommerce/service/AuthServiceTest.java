package com.ecommerce.service;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.SignupRequest;
import com.ecommerce.dto.SignupResponse;
import com.ecommerce.entity.Role;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .username("manoj")
                .email("manoj@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    // ---------------- SIGNUP ----------------

    @Test
    void signup_success() {
        SignupRequest request = new SignupRequest(
                "manoj",
                "manoj@test.com",
                "password",
                Role.USER
        );

        when(userRepository.existsByUsername("manoj")).thenReturn(false);
        when(userRepository.existsByEmail("manoj@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtUtil.generateToken("manoj")).thenReturn("jwt-token");

        SignupResponse response = authService.signup(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("manoj", response.username());
        assertEquals("manoj@test.com", response.email());
        assertEquals("USER", response.role());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_shouldFail_whenUsernameExists() {
        SignupRequest request = new SignupRequest(
                "manoj",
                "manoj@test.com",
                "password",
                Role.USER
        );

        when(userRepository.existsByUsername("manoj")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.signup(request));
    }

    @Test
    void signup_shouldFail_whenEmailExists() {
        SignupRequest request = new SignupRequest(
                "manoj",
                "manoj@test.com",
                "password",
                Role.USER
        );

        when(userRepository.existsByUsername("manoj")).thenReturn(false);
        when(userRepository.existsByEmail("manoj@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> authService.signup(request));
    }

    // ---------------- LOGIN ----------------

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("manoj", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findByUsername("manoj"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken("manoj"))
                .thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("manoj", response.username());
        assertEquals("manoj@test.com", response.email());
        assertEquals("USER", response.role());
    }

    @Test
    void login_shouldFail_whenUserNotFound() {
        LoginRequest request = new LoginRequest("manoj", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findByUsername("manoj"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> authService.login(request));
    }
}
