package com.community.employeemanagement.repository;

import com.community.employeemanagement.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Employee repository - DB operations for Employee entity.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByAadhaar(String aadhaar);

    Optional<Employee> findByAadhaar(String aadhaar);

    /** Case-insensitive search by name OR match by ID string */
    @Query("SELECT e FROM Employee e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Employee> searchByName(@Param("query") String query);

    List<Employee> findByDepartment(String department);
}