package com.community.employeemanagement.service;

import com.community.employeemanagement.dto.AttendanceResponse;
import com.community.employeemanagement.dto.DashboardStats;
import com.community.employeemanagement.dto.MarkAttendanceRequest;
import com.community.employeemanagement.dto.SalaryReport;
import com.community.employeemanagement.model.Attendance;
import com.community.employeemanagement.model.Attendance.AttendanceStatus;
import com.community.employeemanagement.model.Employee;
import com.community.employeemanagement.repository.AttendanceRepository;
import com.community.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Attendance Service.
 * Handles marking attendance, fetching records,
 * and computing prorated salary reports.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // ── Mark Attendance ───────────────────────────────────────

    public AttendanceResponse markAttendance(Long employeeId, LocalDate date, AttendanceStatus status) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        // Update if already marked, else create new
        Attendance attendance = attendanceRepository
                .findByEmployeeAndDate(employee, date)
                .orElse(Attendance.builder().employee(employee).date(date).build());

        attendance.setStatus(status);
        Attendance saved = attendanceRepository.save(attendance);

        log.info("Attendance marked: Employee {} → {} on {}", employee.getName(), status, date);
        return toResponse(saved);
    }

    /** Mark attendance for all employees at once */
    public List<AttendanceResponse> markBulkAttendance(List<MarkAttendanceRequest> requests) {
        return requests.stream()
                .map(req -> markAttendance(req.getEmployeeId(), req.getDate(), req.getStatus()))
                .collect(Collectors.toList());
    }

    // ── Get Attendance ────────────────────────────────────────

    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getMonthlyAttendance(Long employeeId, int month, int year) {
        return attendanceRepository.findByEmployeeIdAndMonth(employeeId, month, year)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Salary Report ─────────────────────────────────────────

    /**
     * Computes prorated salary for an employee in a given month.
     * Formula: calculatedSalary = (presentDays / totalWorkingDays) * monthlySalary
     */
    public SalaryReport getSalaryReport(Long employeeId, int month, int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        long presentDays = attendanceRepository.countByEmployeeAndStatusAndMonth(
                employeeId, AttendanceStatus.PRESENT, month, year);

        long absentDays = attendanceRepository.countByEmployeeAndStatusAndMonth(
                employeeId, AttendanceStatus.ABSENT, month, year);

        // Total days in the month
        long totalWorkingDays = YearMonth.of(year, month).lengthOfMonth();

        // Prorated salary calculation
        double calculatedSalary = totalWorkingDays > 0
                ? (presentDays / (double) totalWorkingDays) * employee.getSalary()
                : 0.0;

        return SalaryReport.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getName())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .monthlySalary(employee.getSalary())
                .presentDays(presentDays)
                .absentDays(absentDays)
                .totalWorkingDays(totalWorkingDays)
                .calculatedSalary(Math.round(calculatedSalary * 100.0) / 100.0)
                .month(month)
                .year(year)
                .build();
    }

    /** Generate salary reports for all employees in a month */
    public List<SalaryReport> getAllSalaryReports(int month, int year) {
        return employeeRepository.findAll()
                .stream()
                .map(emp -> getSalaryReport(emp.getId(), month, year))
                .collect(Collectors.toList());
    }

    // ── Dashboard Stats ───────────────────────────────────────

    public DashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();
        long total = employeeRepository.count();
        long present = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.ABSENT);
        long notMarked = total - present - absent;

        return DashboardStats.builder()
                .totalEmployees(total)
                .presentToday(present)
                .absentToday(absent)
                .notMarkedToday(Math.max(notMarked, 0))
                .build();
    }

    // ── Mapper ────────────────────────────────────────────────

    private AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeName(a.getEmployee().getName())
                .date(a.getDate())
                .status(a.getStatus())
                .build();
    }
}