package com.tuktak.inventory.service;

import com.tuktak.inventory.dto.AdminDto;
import com.tuktak.inventory.entity.AdminUser;
import com.tuktak.inventory.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDto createAdmin(AdminDto adminDto) {
        if (adminUserRepository.existsByUsername(adminDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (adminUserRepository.existsByEmail(adminDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        AdminUser adminUser = AdminUser.builder()
                .username(adminDto.getUsername())
                .password(passwordEncoder.encode(adminDto.getPassword()))
                .email(adminDto.getEmail())
                .fullName(adminDto.getFullName())
                .role(adminDto.getRole() != null ? AdminUser.Role.valueOf(adminDto.getRole()) : AdminUser.Role.ADMIN)
                .enabled(true)
                .build();

        AdminUser savedAdmin = adminUserRepository.save(adminUser);
        return mapToDto(savedAdmin);
    }

    @Transactional(readOnly = true)
    public List<AdminDto> getAllAdmins() {
        return adminUserRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminDto getAdminById(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));
        return mapToDto(adminUser);
    }

    @Transactional(readOnly = true)
    public AdminDto getAdminByUsername(String username) {
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with username: " + username));
        return mapToDto(adminUser);
    }

    public AdminDto updateAdmin(Long id, AdminDto adminDto) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + id));

        if (adminDto.getEmail() != null && !adminDto.getEmail().equals(adminUser.getEmail())) {
            if (adminUserRepository.existsByEmail(adminDto.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            adminUser.setEmail(adminDto.getEmail());
        }

        if (adminDto.getFullName() != null) {
            adminUser.setFullName(adminDto.getFullName());
        }

        if (adminDto.getPassword() != null && !adminDto.getPassword().isEmpty()) {
            adminUser.setPassword(passwordEncoder.encode(adminDto.getPassword()));
        }

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        return mapToDto(updatedAdmin);
    }

    public void deleteAdmin(Long id) {
        if (!adminUserRepository.existsById(id)) {
            throw new IllegalArgumentException("Admin not found with id: " + id);
        }
        adminUserRepository.deleteById(id);
    }

    private AdminDto mapToDto(AdminUser adminUser) {
        return AdminDto.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .email(adminUser.getEmail())
                .fullName(adminUser.getFullName())
                .role(adminUser.getRole().name())
                .build();
    }
}
