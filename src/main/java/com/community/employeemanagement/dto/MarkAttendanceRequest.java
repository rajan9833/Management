package com.community.employeemanagement.dto;

import com.community.employeemanagement.model.Attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for marking attendance request body.
 * Received from frontend: POST /api/attendance/mark
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {
    private Long employeeId;
    private LocalDate date;
    private AttendanceStatus status;
}