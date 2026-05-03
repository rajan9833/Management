package com.community.employeemanagement.service;

import com.community.employeemanagement.dto.EmployeeResponse;
import com.community.employeemanagement.model.Employee;
import com.community.employeemanagement.repository.AttendanceRepository;
import com.community.employeemanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Employee Service.
 * Contains all business logic for employee CRUD operations,
 * photo upload handling, and search functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ── Add Employee ─────────────────────────────────────────

    public EmployeeResponse addEmployee(
            String name, String aadhaar, String phone,
            Integer age, Double salary, String department,
            String designation, MultipartFile photo
    ) throws IOException {

        // Validate unique Aadhaar
        if (employeeRepository.existsByAadhaar(aadhaar)) {
            throw new IllegalArgumentException("Employee with Aadhaar " + aadhaar + " already exists");
        }

        // Handle photo upload
        String photoPath = null;
        if (photo != null && !photo.isEmpty()) {
            photoPath = savePhoto(photo);
        }

        Employee employee = Employee.builder()
                .name(name)
                .aadhaar(aadhaar)
                .phone(phone)
                .age(age)
                .salary(salary)
                .department(department)
                .designation(designation)
                .photoPath(photoPath)
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("Added employee: {} (ID: {})", saved.getName(), saved.getId());

        return toResponse(saved);
    }

    // ── Get All / Search ──────────────────────────────────────

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> searchEmployees(String query) {
        // Try to parse as ID first, then fall back to name search
        try {
            Long id = Long.parseLong(query);
            return employeeRepository.findById(id)
                    .map(e -> List.of(toResponse(e)))
                    .orElse(List.of());
        } catch (NumberFormatException e) {
            return employeeRepository.searchByName(query)
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
        return toResponse(emp);
    }

    // ── Update ────────────────────────────────────────────────

    public EmployeeResponse updateEmployee(Long id, String name, String phone,
                                           Integer age, Double salary, String department, String designation,
                                           MultipartFile photo) throws IOException {

        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));

        if (name != null) emp.setName(name);
        if (phone != null) emp.setPhone(phone);
        if (age != null) emp.setAge(age);
        if (salary != null) emp.setSalary(salary);
        if (department != null) emp.setDepartment(department);
        if (designation != null) emp.setDesignation(designation);

        if (photo != null && !photo.isEmpty()) {
            emp.setPhotoPath(savePhoto(photo));
        }

        return toResponse(employeeRepository.save(emp));
    }

    // ── Delete ────────────────────────────────────────────────

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found: " + id);
        }
        attendanceRepository.deleteByEmployeeId(id);
        employeeRepository.deleteById(id);
        log.info("Deleted employee ID: {} (and related attendance)", id);
    }

    // ── File Upload ───────────────────────────────────────────

    private String savePhoto(MultipartFile photo) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Generate unique filename to prevent collisions
        String originalFilename = photo.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID() + extension;

        Path filePath = uploadPath.resolve(filename);
        Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.debug("Photo saved: {}", filename);
        return filename;
    }

    // ── Mapper ────────────────────────────────────────────────

    public EmployeeResponse toResponse(Employee emp) {
        return EmployeeResponse.builder()
                .id(emp.getId())
                .name(emp.getName())
                .aadhaar(emp.getAadhaar())
                .phone(emp.getPhone())
                .age(emp.getAge())
                .salary(emp.getSalary())
                .photoPath(emp.getPhotoPath() != null ? "/uploads/" + emp.getPhotoPath() : null)
                .department(emp.getDepartment())
                .designation(emp.getDesignation())
                .build();
    }

    public Employee getEntityById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }
}