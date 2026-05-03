package com.community.employeemanagement.config;

import com.community.employeemanagement.model.Employee;
import com.community.employeemanagement.model.User;
import com.community.employeemanagement.repository.EmployeeRepository;
import com.community.employeemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

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
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    @Value("${app.seed.admin.username:admin}")
    private String defaultAdminUsername;

    @Value("${app.seed.admin.password:admin123}")
    private String defaultAdminPassword;

    @Value("${app.seed.admin.reset-on-start:false}")
    private boolean resetAdminOnStart;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedSampleEmployees();
    }

    private void seedAdminUser() {
        Optional<User> existingAdmin = userRepository.findByUsername(defaultAdminUsername);

        if (existingAdmin.isEmpty()) {
            User admin = User.builder()
                    .username(defaultAdminUsername)
                    .password(passwordEncoder.encode(defaultAdminPassword))
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("✅ Default admin user created → username: {}", defaultAdminUsername);
            return;
        }

        User admin = existingAdmin.get();
        boolean bcryptStored = BCRYPT_PATTERN.matcher(admin.getPassword()).matches();

        if (!bcryptStored) {
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            if (admin.getRole() == null || admin.getRole().isBlank()) {
                admin.setRole("ROLE_ADMIN");
            }
            userRepository.save(admin);
            log.warn("⚠️ Admin password was not BCrypt. Re-encoded default password for user: {}", defaultAdminUsername);
            return;
        }

        boolean roleMissing = admin.getRole() == null || admin.getRole().isBlank();
        if (roleMissing) {
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            log.warn("⚠️ Admin role was missing. Restored ROLE_ADMIN for user: {}", defaultAdminUsername);
        }

        if (resetAdminOnStart && !passwordEncoder.matches(defaultAdminPassword, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            userRepository.save(admin);
            log.warn("⚠️ Admin password reset on startup for user: {}", defaultAdminUsername);
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