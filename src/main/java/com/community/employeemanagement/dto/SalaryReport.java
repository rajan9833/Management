package com.community.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for monthly salary report.
 * Contains attendance counts and prorated salary calculation.
 * Formula: calculatedSalary = (presentDays / totalWorkingDays) * monthlySalary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryReport {
    private Long employeeId;
    private String employeeName;
    private String department;
    private String designation;
    private Double monthlySalary;
    private long presentDays;
    private long absentDays;
    private long totalWorkingDays;
    private Double calculatedSalary;
    private int month;
    private int year;
}