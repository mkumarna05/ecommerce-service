package com.ecommerce.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;

public class JwtUtilTest {

	private JwtUtil jwtUtil;

	// 256-bit Base64 encoded secret
	private static final String TEST_SECRET = Base64.getEncoder()
			.encodeToString("my-super-secret-key-my-super-secret-key".getBytes());

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();

		ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
		ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L); // 1 minute
	}

	@Test
	@DisplayName("Generate token and extract username")
	void generateToken_ShouldCreateValidToken() {
		String username = "testuser";

		String token = jwtUtil.generateToken(username);

		assertNotNull(token);
		assertEquals(username, jwtUtil.extractUsername(token));
	}

	@Test
	@DisplayName("Extract expiration date")
	void extractExpiration_ShouldReturnFutureDate() {
		String token = jwtUtil.generateToken("user");

		Date expiration = jwtUtil.extractExpiration(token);

		assertNotNull(expiration);
		assertTrue(expiration.after(new Date()));
	}

	@Test
	@DisplayName("Validate token")
	void validateToken_ValidToken_ShouldReturnTrue() {
		String username = "validuser";

		UserDetails userDetails = User.builder().username(username).password("password").authorities("ROLE_USER")
				.build();

		String token = jwtUtil.generateToken(username);

		assertTrue(jwtUtil.validateToken(token, userDetails));
	}

	@Test
	@DisplayName("USERNAME mismatch")
	void validateToken_WrongUsername_ShouldReturnFalse() {
		String token = jwtUtil.generateToken("user1");

		UserDetails userDetails = User.builder().username("user2").password("password").authorities("ROLE_USER")
				.build();

		assertFalse(jwtUtil.validateToken(token, userDetails));
	}

	@Test
	@DisplayName("Expired token throws exception")
	void validateToken_ExpiredToken_ShouldThrowException() {
		// Force expiration in the past
		ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);

		UserDetails userDetails = User.builder().username("expireduser").password("password").authorities("ROLE_USER")
				.build();

		String token = jwtUtil.generateToken("expireduser");

		assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(token, userDetails));
	}
}
