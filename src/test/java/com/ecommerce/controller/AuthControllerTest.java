package com.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.SignupRequest;
import com.ecommerce.dto.SignupResponse;
import com.ecommerce.entity.Role;
import com.ecommerce.service.AuthService;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

	@InjectMocks
	private AuthController authController;

	@Mock
	private AuthService authService;

	@Mock
	private AuthenticationManager authenticationManager;

	@Test
	void register_Success() throws Exception {
		SignupRequest request = new SignupRequest("testuser", "test@example.com", "password123", Role.USER);
		SignupResponse response = new SignupResponse("jwt-token", "testuser", "test@example.com", "USER");

		when(authService.signup(any(SignupRequest.class))).thenReturn(response);

		// when
		ResponseEntity<SignupResponse> result = authController.signup(request);
		// then
		assertEquals(HttpStatus.CREATED, result.getStatusCode());

		SignupResponse body = result.getBody();
		assertNotNull(body);
		assertEquals("jwt-token", body.token());
		assertEquals("testuser", body.username());
		assertEquals("test@example.com", body.email());
		assertEquals("USER", body.role());
	}

	@Test
	void register_InvalidInput_ReturnsBadRequest() throws Exception {
		SignupRequest request = new SignupRequest("", "", "", Role.USER);
		when(authService.signup(request)).thenThrow(new IllegalArgumentException("Invalid input"));
		assertThrows(IllegalArgumentException.class, () -> authController.signup(request));
	}

	@Test
	void login_Success() throws Exception {
		LoginRequest request = new LoginRequest("testuser", "password123");
		LoginResponse response = new LoginResponse("jwt-token", "testuser", "test@example.com", "USER");

		when(authService.login(any(LoginRequest.class))).thenReturn(response);
		// when
		ResponseEntity<LoginResponse> result = authController.login(request);
		// then
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertNotNull(result.getBody());
		assertEquals("jwt-token", result.getBody().token());
		assertEquals("testuser", result.getBody().username());

		verify(authService).login(any(LoginRequest.class));
	}

	@Test
	void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
		LoginRequest request = new LoginRequest("user", "wrongpassword");

		// Simulate Spring Security throwing exception
		when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

		ResponseEntity<LoginResponse> response = authController.login(request);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

		// Assert: the LoginResponse body is empty/null
		LoginResponse body = response.getBody();
		assertEquals(null, body.token());
		assertEquals(null, body.username());
		assertEquals(null, body.role());
		assertEquals(null, body.email());
	}

}
