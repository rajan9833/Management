package com.community.employeemanagement.controller;

import com.community.employeemanagement.dto.ApiResponse;
import com.community.employeemanagement.model.User;
import com.community.employeemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController - REST endpoints for user account management.
 * All endpoints require a valid JWT (enforced by SecurityConfig).
 *
 * Base path: /api/users
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get all users.
     * GET /api/users/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Success", userService.getAllUsers()));
    }

    /**
     * Create a new admin user.
     * POST /api/users/create
     * Body: { "username": "...", "password": "...", "role": "ROLE_ADMIN" }
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createUser(@RequestBody Map<String, String> body) {
        try {
            userService.createUser(
                    body.get("username"),
                    body.get("password"),
                    body.get("role")
            );
            return ResponseEntity.ok(ApiResponse.ok(
                    "User created successfully", body.get("username")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Change password for a user.
     * PUT /api/users/change-password
     * Body: { "username": "...", "oldPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody Map<String, String> body) {
        try {
            userService.changePassword(
                    body.get("username"),
                    body.get("oldPassword"),
                    body.get("newPassword")
            );
            return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete a user by ID.
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.ok("User deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if a username is already taken.
     * GET /api/users/exists?username=admin
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Success", userService.usernameExists(username)));
    }
}