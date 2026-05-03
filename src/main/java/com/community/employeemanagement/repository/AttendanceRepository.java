package com.community.employeemanagement.repository;

import com.community.employeemanagement.model.Attendance;
import com.community.employeemanagement.model.Attendance.AttendanceStatus;
import com.community.employeemanagement.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Attendance repository - queries for daily and monthly attendance data.
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** Find attendance record for a specific employee on a specific date */
    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);

    /** All attendance for an employee in a given month and year */
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.employee.id = :empId
          AND MONTH(a.date) = :month
          AND YEAR(a.date) = :year
        """)
    List<Attendance> findByEmployeeIdAndMonth(
            @Param("empId") Long empId,
            @Param("month") int month,
            @Param("year") int year
    );

    /** Count of a specific status for an employee in a month */
    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.employee.id = :empId
          AND a.status = :status
          AND MONTH(a.date) = :month
          AND YEAR(a.date) = :year
        """)
    long countByEmployeeAndStatusAndMonth(
            @Param("empId") Long empId,
            @Param("status") AttendanceStatus status,
            @Param("month") int month,
            @Param("year") int year
    );

    /** All records for a specific date (for today's dashboard view) */
    List<Attendance> findByDate(LocalDate date);

    /** Count employees who are PRESENT today */
    long countByDateAndStatus(LocalDate date, AttendanceStatus status);

    /** Check if attendance already marked */
    boolean existsByEmployeeAndDate(Employee employee, LocalDate date);

    /** Remove all attendance rows for an employee (required before deleting the employee FK). */
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.employee.id = :empId")
    void deleteByEmployeeId(@Param("empId") Long empId);
}