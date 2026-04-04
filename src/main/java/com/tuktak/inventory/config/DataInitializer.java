package com.tuktak.inventory.config;

import com.tuktak.inventory.entity.AdminUser;
import com.tuktak.inventory.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminUserRepository.count() == 0) {
            AdminUser superAdmin = AdminUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@tuktak.com")
                    .fullName("Super Administrator")
                    .role(AdminUser.Role.SUPER_ADMIN)
                    .enabled(true)
                    .build();

            adminUserRepository.save(superAdmin);
            log.info("Default super admin created - Username: admin, Password: admin123");
        }
    }
}
