package com.community.employeemanagement.controller;

import com.community.employeemanagement.dto.ApiResponse;
import com.community.employeemanagement.dto.AttendanceResponse;
import com.community.employeemanagement.dto.DashboardStats;
import com.community.employeemanagement.dto.MarkAttendanceRequest;
import com.community.employeemanagement.dto.SalaryReport;
import com.community.employeemanagement.model.Attendance.AttendanceStatus;
import com.community.employeemanagement.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Attendance Controller.
 * Handles marking attendance, fetching reports, and dashboard stats.
 *
 * Base path: /api/attendance
 */
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Mark attendance for a single employee.
     * POST /api/attendance/mark
     * Body: { employeeId, date, status }
     */
    @PostMapping("/mark")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @RequestBody MarkAttendanceRequest request) {
        try {
            AttendanceResponse response = attendanceService.markAttendance(
                    request.getEmployeeId(),
                    request.getDate() != null ? request.getDate() : LocalDate.now(),
                    request.getStatus()
            );
            return ResponseEntity.ok(ApiResponse.ok("Attendance marked", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Bulk mark attendance for multiple employees.
     * POST /api/attendance/mark/bulk
     * Body: [{ employeeId, date, status }, ...]
     */
    @PostMapping("/mark/bulk")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> markBulkAttendance(
            @RequestBody List<MarkAttendanceRequest> requests) {
        try {
            List<AttendanceResponse> responses = attendanceService.markBulkAttendance(requests);
            return ResponseEntity.ok(ApiResponse.ok(
                    "Bulk attendance marked for " + responses.size() + " employees", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get attendance for all employees on a specific date.
     * GET /api/attendance/date?date=2024-01-15
     */
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(
                "Success", attendanceService.getAttendanceByDate(targetDate)));
    }

    /**
     * Get monthly attendance for a specific employee.
     * GET /api/attendance/report?empId=1&month=1&year=2024
     */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMonthlyAttendance(
            @RequestParam Long empId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Success", attendanceService.getMonthlyAttendance(empId, month, year)));
    }

    /**
     * Get salary report for a specific employee.
     * GET /api/attendance/salary?empId=1&month=1&year=2024
     */
    @GetMapping("/salary")
    public ResponseEntity<ApiResponse<SalaryReport>> getSalaryReport(
            @RequestParam Long empId,
            @RequestParam int month,
            @RequestParam int year) {
        try {
            SalaryReport report = attendanceService.getSalaryReport(empId, month, year);
            return ResponseEntity.ok(ApiResponse.ok("Salary report generated", report));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get salary reports for all employees in a given month.
     * GET /api/attendance/salary/all?month=1&year=2024
     */
    @GetMapping("/salary/all")
    public ResponseEntity<ApiResponse<List<SalaryReport>>> getAllSalaryReports(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Success", attendanceService.getAllSalaryReports(month, year)));
    }

    /**
     * Get today's dashboard statistics.
     * GET /api/attendance/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.ok("Success", attendanceService.getDashboardStats()));
    }
}