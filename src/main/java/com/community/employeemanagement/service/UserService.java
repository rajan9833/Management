package com.community.employeemanagement.service;

import com.community.employeemanagement.model.User;
import com.community.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserService - handles all user account operations.
 * Used for creating new admin users, changing passwords,
 * and managing user accounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Create User ───────────────────────────────────────────

    /**
     * Register a new user with a BCrypt-hashed password.
     * Throws if username already exists.
     */
    public User createUser(String username, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword)) // never store plain text
                .role(role != null ? role : "ROLE_ADMIN")
                .build();

        User saved = userRepository.save(user);
        log.info("New user created: {}", saved.getUsername());
        return saved;
    }

    // ── Get Users ─────────────────────────────────────────────

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    // ── Change Password ───────────────────────────────────────

    /**
     * Change a user's password.
     * Verifies the old password before setting the new one.
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", username);
    }

    // ── Delete User ───────────────────────────────────────────

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user ID: {}", id);
    }

    // ── Check Exists ──────────────────────────────────────────

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
}