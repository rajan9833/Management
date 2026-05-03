package com.community.employeemanagement.config;

import com.community.employeemanagement.model.Employee;
import com.community.employeemanagement.model.User;
import com.community.employeemanagement.repository.EmployeeRepository;
import com.community.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer.
 * Runs on application startup to seed the database with:
 *  - Default admin user (admin / admin123)
 *  - Sample employees
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedSampleEmployees();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("✅ Default admin user created → username: admin | password: admin123");
        }
    }

    private void seedSampleEmployees() {
        if (employeeRepository.count() == 0) {
            Employee[] employees = {
                    Employee.builder()
                            .name("Arjun Sharma").aadhaar("123456789012").phone("9876543210")
                            .age(28).salary(55000.0).department("Engineering").designation("Software Engineer")
                            .photoPath(null).build(),
                    Employee.builder()
                            .name("Priya Patel").aadhaar("234567890123").phone("8765432109")
                            .age(32).salary(72000.0).department("Engineering").designation("Senior Engineer")
                            .photoPath(null).build(),
                    Employee.builder()
                            .name("Rahul Verma").aadhaar("345678901234").phone("7654321098")
                            .age(25).salary(42000.0).department("HR").designation("HR Executive")
                            .photoPath(null).build(),
                    Employee.builder()
                            .name("Sneha Iyer").aadhaar("456789012345").phone("6543210987")
                            .age(29).salary(65000.0).department("Finance").designation("Accountant")
                            .photoPath(null).build(),
                    Employee.builder()
                            .name("Vikram Singh").aadhaar("567890123456").phone("9543210876")
                            .age(35).salary(90000.0).department("Management").designation("Manager")
                            .photoPath(null).build(),
            };

            for (Employee emp : employees) {
                employeeRepository.save(emp);
            }
            log.info("✅ {} sample employees seeded into database", employees.length);
        }
    }
}