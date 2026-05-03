package com.community.employeemanagement.controller;

import com.community.employeemanagement.config.JwtUtil;
import com.community.employeemanagement.dto.ApiResponse;
import com.community.employeemanagement.dto.LoginRequest;
import com.community.employeemanagement.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Browser-friendly health endpoint.
     * GET /api/auth/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        return ResponseEntity.ok(ApiResponse.ok(
                "Auth service reachable",
                Map.of("status", "UP", "endpoint", "/api/auth/login", "method", "POST")
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
            // Authenticate username + password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Generate JWT on success
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            log.info("Login successful for user: {}", request.getUsername());

            return ResponseEntity.ok(ApiResponse.ok(
                    "Login successful",
                    new LoginResponse(token, request.getUsername(), "Welcome back!")
            ));

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", request.getUsername());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Authentication failed"));
        }
    }
}