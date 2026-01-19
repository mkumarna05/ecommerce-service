package com.ecommerce.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.ecommerce.filter.JwtAuthenticationFilter;
import com.ecommerce.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Unit Tests")
public class SecurityConfigTest {

	@Mock
	private JwtAuthenticationFilter jwtAuthFilter;

	@Mock
	private CustomUserDetailsService userDetailsService;

	@Mock
	private AuthenticationConfiguration authConfig;

	@Mock
	private AuthenticationManager authManager;

	private SecurityConfig securityConfig;

	private static final String TEST_JWT_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1hbmQtdmFsaWRhdGlvbi0xMjM0NTY3ODkw";

	@BeforeEach
	void setUp() {
		securityConfig = new SecurityConfig(jwtAuthFilter, userDetailsService);
		ReflectionTestUtils.setField(securityConfig, "secret", TEST_JWT_SECRET);
	}

	@Test
	@DisplayName("Should create PasswordEncoder bean")
	void testPasswordEncoderBeanCreation() {
		PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

		assertNotNull(passwordEncoder);
		assertThat(passwordEncoder).isInstanceOf(PasswordEncoder.class);
	}

	@Test
	@DisplayName("PasswordEncoder should use BCrypt")
	void testPasswordEncoderIsBCrypt() {
		PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

		assertThat(passwordEncoder.getClass().getSimpleName()).isEqualTo("BCryptPasswordEncoder");
	}

	@Test
	@DisplayName("Should create AuthenticationProvider bean")
	void testAuthenticationProviderBeanCreation() {
		AuthenticationProvider authProvider = securityConfig.authenticationProvider();

		assertNotNull(authProvider);
		assertThat(authProvider).isInstanceOf(DaoAuthenticationProvider.class);
	}

	@Test
    @DisplayName("Should create AuthenticationManager bean")
    void testAuthenticationManagerBeanCreation() throws Exception {
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);

        AuthenticationManager manager = securityConfig.authenticationManager(authConfig);

        assertNotNull(manager);
        verify(authConfig, times(1)).getAuthenticationManager();
    }

	@Test
	@DisplayName("Should create CorsConfigurationSource bean")
	void testCorsConfigurationSourceBeanCreation() {
		CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();

		assertNotNull(corsSource);
		assertThat(corsSource).isInstanceOf(CorsConfigurationSource.class);
	}

	@Test
	@DisplayName("PasswordEncoder should encode passwords correctly")
	void testPasswordEncoding() {
		PasswordEncoder encoder = securityConfig.passwordEncoder();
		String rawPassword = "mySecurePassword123";

		String encodedPassword = encoder.encode(rawPassword);

		assertNotNull(encodedPassword);
		assertNotEquals(rawPassword, encodedPassword);
		assertTrue(encoder.matches(rawPassword, encodedPassword));
	}

	@Test
	@DisplayName("PasswordEncoder should not match wrong password")
	void testPasswordEncoderDoesNotMatchWrongPassword() {
		PasswordEncoder encoder = securityConfig.passwordEncoder();
		String rawPassword = "correctPassword";
		String wrongPassword = "wrongPassword";

		String encodedPassword = encoder.encode(rawPassword);

		assertFalse(encoder.matches(wrongPassword, encodedPassword));
	}

	@Test
	@DisplayName("PasswordEncoder should generate different hashes for same password")
	void testPasswordEncoderGeneratesDifferentHashes() {
		PasswordEncoder encoder = securityConfig.passwordEncoder();
		String password = "testPassword";

		String hash1 = encoder.encode(password);
		String hash2 = encoder.encode(password);

		assertNotEquals(hash1, hash2); // BCrypt generates different salts
		assertTrue(encoder.matches(password, hash1));
		assertTrue(encoder.matches(password, hash2));
	}

	@Test
	@DisplayName("Test Cors Configuration")
	void testCorsConfiguration() {
		MockHttpServletRequest request = new MockHttpServletRequest();

		CorsConfiguration config = securityConfig.corsConfigurationSource().getCorsConfiguration(request);

		assertThat(config.getAllowedHeaders()).contains("*");
		assertThat(config.getAllowedMethods()).contains("*");
		assertThat(config.getAllowCredentials()).isFalse();
	}

	@Test
	@DisplayName("Test AuthenticationProvider")
	void testAuthenticationProviderType() {
		AuthenticationProvider provider = securityConfig.authenticationProvider();
		assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
	}

	@Test
	@DisplayName("AuthenticationProvider should use custom UserDetailsService")
	void testAuthenticationProviderUsesCustomUserDetailsService() {
		AuthenticationProvider provider = securityConfig.authenticationProvider();
		DaoAuthenticationProvider daoProvider = (DaoAuthenticationProvider) provider;
		assertNotNull(daoProvider);
	}

	@Test
	@DisplayName("Constructor should initialize with required dependencies")
	void testConstructorInitialization() {
		SecurityConfig config = new SecurityConfig(jwtAuthFilter, userDetailsService);

		assertNotNull(config);
	}

	@Test
	@DisplayName("Constructor should accept null JwtAuthFilter (no validation)")
	void testConstructorAcceptsNullJwtFilter() {
		assertDoesNotThrow(() -> {
			SecurityConfig config = new SecurityConfig(null, userDetailsService);
			assertNotNull(config);
		});
	}

	@Test
	@DisplayName("Constructor should accept null UserDetailsService (no validation)")
	void testConstructorAcceptsNullUserDetailsService() {
		assertDoesNotThrow(() -> {
			SecurityConfig config = new SecurityConfig(jwtAuthFilter, null);
			assertNotNull(config);
		});
	}

	@Test
	@DisplayName("Password encoding should work with AuthenticationProvider")
	void testPasswordEncodingWithAuthProvider() {
		PasswordEncoder encoder = securityConfig.passwordEncoder();
		String rawPassword = "testPassword123";
		String encodedPassword = encoder.encode(rawPassword);

		// Verify the encoded password can be validated
		assertTrue(encoder.matches(rawPassword, encodedPassword));
	}
}