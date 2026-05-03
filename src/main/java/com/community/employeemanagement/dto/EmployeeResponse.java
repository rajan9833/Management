package com.community.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for employee data sent to the frontend.
 * Keeps the API response clean — never exposes JPA internals.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
    private Long id;
    private String name;
    private String aadhaar;
    private String phone;
    private Integer age;
    private Double salary;
    private String photoPath;
    private String department;
    private String designation;
}