package com.community.employeemanagement.controller;

import com.community.employeemanagement.config.JwtUtil;
import com.community.employeemanagement.dto.ApiResponse;
import com.community.employeemanagement.dto.LoginRequest;
import com.community.employeemanagement.dto.LoginResponse;
import com.community.employeemanagement.model.User;
import com.community.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * Authentication Controller.
 * Handles login and returns JWT token on success.
 * Endpoint: POST /api/auth/login
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Browser-friendly health endpoint.
     * GET /api/auth/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Auth service reachable",
                Map.of("status", "UP", "endpoint", "/auth/login", "method", "POST")
        ));
    }

    /**
     * Login endpoint.
     * Validates credentials, returns JWT token if valid.
     *
     * @param request { username, password }
     * @return { token, username, message }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            User user = userRepository.findByUsername(request.getUsername()).orElse(null);
            if (user == null) {
                log.warn("Login failed: user not found: {}", request.getUsername());
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid username or password"));
            }

            String storedPassword = user.getPassword();
            boolean isBcrypt = storedPassword != null && storedPassword.startsWith("$2");
            boolean authenticated;
            if (isBcrypt) {
                try {
                    authenticated = passwordEncoder.matches(request.getPassword(), storedPassword);
                } catch (IllegalArgumentException malformedHash) {
                    log.warn("Malformed BCrypt hash for user {}. Trying legacy plain-text fallback.",
                            request.getUsername());
                    authenticated = Objects.equals(request.getPassword(), storedPassword);
                    isBcrypt = false;
                }
            } else {
                authenticated = Objects.equals(request.getPassword(), storedPassword);
            }

            if (!authenticated) {
                log.warn("Login failed: invalid password for user: {}", request.getUsername());
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("Invalid username or password"));
            }

            // Migrate any legacy plain-text password to BCrypt after successful login.
            if (!isBcrypt) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                log.warn("Upgraded legacy password hash for user: {}", request.getUsername());
            }

            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("ROLE_ADMIN");
            }
            userRepository.save(user);

            // Generate JWT on success
            String role = user.getRole().replace("ROLE_", "");
            if (role.isBlank()) {
                role = "ADMIN";
            }
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(role)
                    .build();
            String token = jwtUtil.generateToken(userDetails);

            log.info("Login successful for user: {}", request.getUsername());

            return ResponseEntity.ok(ApiResponse.ok(
                    "Login successful",
                    new LoginResponse(token, request.getUsername(), "Welcome back!")
            ));

        } catch (Exception e) {
            log.error("Login error for user {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Authentication failed"));
        }
    }
}