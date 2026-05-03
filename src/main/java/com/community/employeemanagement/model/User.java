package com.community.employeemanagement.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * User entity - maps to the 'users' table.
 * Used for authentication only. Passwords are BCrypt-hashed.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    /** BCrypt-hashed password - NEVER store plain text */
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private String role = "ROLE_ADMIN";
}
