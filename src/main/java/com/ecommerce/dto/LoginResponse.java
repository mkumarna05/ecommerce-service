package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginResponse(String token, String username, String email, String role) {
	@JsonIgnore
	public String getMaskedEmail() {
		if (email == null || !email.contains("@")) {
			return "[PROTECTED]";
		}
		String[] parts = email.split("@");
		String localPart = parts[0];
		if (localPart.length() <= 2) {
			return "**@" + parts[1];
		}
		return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + parts[1];
	}

	@Override
	public String toString() {
		return "SignupResponse{username='" + username + "', email='" + getMaskedEmail() + "', role='" + role
				+ "', token='[PROTECTED]'}";
	}
}