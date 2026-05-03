package com.community.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO for login response.
 * Returned to frontend after successful authentication.
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String message;
}
