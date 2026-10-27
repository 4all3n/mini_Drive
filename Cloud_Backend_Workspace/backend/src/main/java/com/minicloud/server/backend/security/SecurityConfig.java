package com.minicloud.server.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration — Stateless JWT Architecture.
 * 
 * Key design decisions:
 * - STATELESS sessions: no HTTP session or cookies; every request is authenticated via JWT.
 * - CSRF disabled: safe because we use stateless bearer tokens (CSRF attacks exploit cookies).
 * - /api/auth/** is public: login and registration endpoints must be accessible without a token.
 * - /admin/** is public: admin servlets use their own JDBC connection (legacy, separate from Spring).
 * - /api/files/** is protected: requires a valid JWT Bearer token in the Authorization header.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * BCrypt password encoder bean — cost factor 10 (default).
     * Used by AuthController for hashing on registration and verifying on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP Security Filter Chain — defines which endpoints require authentication.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF — stateless JWT tokens are not vulnerable to CSRF attacks
            .csrf(csrf -> csrf.disable())

            // 2. Stateless session — no server-side session storage
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. URL-based authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — no token required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers("/admin/**").permitAll()
                .requestMatchers("/error").permitAll()
                // All other endpoints (especially /api/files/**) require authentication
                .anyRequest().authenticated())

            // 4. Add JWT filter before Spring's default username/password filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
