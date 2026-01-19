package com.ecommerce.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // disable CSRF for tests
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // allow all requests
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Minimal in-memory users for @WithMockUser
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{noop}password").roles("ADMIN").build(),
                User.withUsername("user").password("{noop}password").roles("USER").build()
        );
    }
}
