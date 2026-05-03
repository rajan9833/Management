package com.community.employeemanagement.controller;

import com.community.employeemanagement.dto.ApiResponse;
import com.community.employeemanagement.dto.EmployeeResponse;
import com.community.employeemanagement.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Employee Controller.
 * REST endpoints for employee CRUD operations.
 * All endpoints require a valid JWT (enforced by SecurityConfig).
 *
 * Base path: /api/employees
 */
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Add a new employee.
     * Accepts multipart/form-data to support photo upload.
     * POST /api/employees/add
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<EmployeeResponse>> addEmployee(
            @RequestParam("name")        String name,
            @RequestParam("aadhaar")     String aadhaar,
            @RequestParam("phone")       String phone,
            @RequestParam("age")         Integer age,
            @RequestParam("salary")      Double salary,
            @RequestParam(value = "department",  required = false) String department,
            @RequestParam(value = "designation", required = false) String designation,
            @RequestParam(value = "photo",       required = false) MultipartFile photo
    ) {
        try {
            EmployeeResponse response = employeeService.addEmployee(
                    name, aadhaar, phone, age, salary, department, designation, photo);
            return ResponseEntity.ok(ApiResponse.ok("Employee added successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("Photo upload failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.error("Photo upload failed"));
        }
    }

    /**
     * Get all employees.
     * GET /api/employees/all
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.ok("Success", employeeService.getAllEmployees()));
    }

    /**
     * Search employees by name or ID.
     * GET /api/employees/search?query=<name_or_id>
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> searchEmployees(
            @RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.ok("Success", employeeService.searchEmployees(query)));
    }

    /**
     * Get single employee by ID.
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Success", employeeService.getEmployeeById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update an existing employee.
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @RequestParam(value = "name",        required = false) String name,
            @RequestParam(value = "phone",       required = false) String phone,
            @RequestParam(value = "age",         required = false) Integer age,
            @RequestParam(value = "salary",      required = false) Double salary,
            @RequestParam(value = "department",  required = false) String department,
            @RequestParam(value = "designation", required = false) String designation,
            @RequestParam(value = "photo",       required = false) MultipartFile photo
    ) {
        try {
            EmployeeResponse response = employeeService.updateEmployee(
                    id, name, phone, age, salary, department, designation, photo);
            return ResponseEntity.ok(ApiResponse.ok("Employee updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Photo upload failed"));
        }
    }

    /**
     * Delete an employee.
     * DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponse.ok("Employee deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}