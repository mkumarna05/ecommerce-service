package com.ecommerce.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.LoginResponse;
import com.ecommerce.dto.SignupRequest;
import com.ecommerce.dto.SignupResponse;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final AuthenticationManager authenticationManager;

	public SignupResponse signup(SignupRequest request) {

		if (userRepository.existsByUsername(request.username())) {
			throw new IllegalArgumentException("Username already exists");
		}

		if (userRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("Email already exists");
		}

		User user = User.builder().username(request.username()).email(request.email())
				.password(passwordEncoder.encode(request.password())).role(request.role())
				.createdAt(LocalDateTime.now()).build();

		userRepository.save(user);

		String token = jwtUtil.generateToken(user.getUsername());

		return new SignupResponse(token, user.getUsername(), user.getEmail(), user.getRole().name());
	}

	public LoginResponse login(@Valid LoginRequest request) {
		log.info("User login attempt: {}", request.username());

		// Authenticate user
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		// Fetch user details
		User user = userRepository.findByUsername(request.username())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Generate JWT token
		String token = jwtUtil.generateToken(user.getUsername());

		log.info("User logged in successfully: {}", user.getUsername());

		return new LoginResponse(token, user.getUsername(), user.getEmail(), user.getRole().name());
	}
}