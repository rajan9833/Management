package com.community.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Generic API response wrapper.
 * Every controller endpoint returns this shape:
 * {
 *   "success": true/false,
 *   "message": "...",
 *   "data": { ... }
 * }
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /** Use for successful responses */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** Use for error responses */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}