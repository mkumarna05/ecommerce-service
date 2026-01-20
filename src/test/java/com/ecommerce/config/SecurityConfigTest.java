package com.ecommerce.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ecommerce.filter.JwtAuthenticationFilter;
import com.ecommerce.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthFilter;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Test
    void authenticationProvider_shouldReturnDaoAuthenticationProvider() {
        AuthenticationProvider authProvider = securityConfig.authenticationProvider();
        assertNotNull(authProvider);
        assertEquals("org.springframework.security.authentication.dao.DaoAuthenticationProvider",
                authProvider.getClass().getName());
    }

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        assertEquals("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder",
                encoder.getClass().getName());
    }

    @Test
    void corsConfigurationSource_devMode_allOrigins() {
        List<String> allowedOrigins = List.of("*");
        CorsConfigurationSource source = securityConfig.corsConfigurationSource(allowedOrigins);

        assertNotNull(source);
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);

        CorsConfiguration config = ((UrlBasedCorsConfigurationSource) source).getCorsConfigurations().get("/**");
        assertNotNull(config);
        assertEquals(List.of("*"), config.getAllowedOriginPatterns());
        assertFalse(config.getAllowCredentials());
        assertEquals(List.of("GET","POST","PUT","DELETE","OPTIONS"), config.getAllowedMethods());
        assertEquals(List.of("Authorization","Content-Type"), config.getAllowedHeaders());
    }

    @Test
    void corsConfigurationSource_prodMode_specificOrigins() {
        List<String> allowedOrigins = List.of("http://localhost:3000", "https://ecommerce-ui.com");
        CorsConfigurationSource source = securityConfig.corsConfigurationSource(allowedOrigins);

        assertNotNull(source);
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);

        CorsConfiguration config = ((UrlBasedCorsConfigurationSource) source).getCorsConfigurations().get("/**");
        assertNotNull(config);
        assertEquals(allowedOrigins, config.getAllowedOrigins());
        assertTrue(config.getAllowCredentials());
    }

    @Test
    void securityFilterChain_shouldBuildWithoutException() throws Exception {
        // Mock HttpSecurity
        org.springframework.security.config.annotation.web.builders.HttpSecurity http = mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class, RETURNS_DEEP_STUBS);
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http);
        assertNotNull(filterChain);
    }

    @Test
    void authenticationManager_shouldReturnNonNull() throws Exception {
        org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration config =
                mock(org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration.class);
        when(config.getAuthenticationManager()).thenReturn(mock(AuthenticationManager.class));

        AuthenticationManager manager = securityConfig.authenticationManager(config);
        assertNotNull(manager);
    }
}
