package com.tuktak.inventory.controller;

import com.tuktak.inventory.dto.AdminDto;
import com.tuktak.inventory.dto.ApiResponse;
import com.tuktak.inventory.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminUserService adminUserService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminDto>> createAdmin(@Valid @RequestBody AdminDto adminDto) {
        AdminDto createdAdmin = adminUserService.createAdmin(adminDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin created successfully", createdAdmin));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminDto>>> getAllAdmins() {
        List<AdminDto> admins = adminUserService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success(admins));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminDto>> getAdminById(@PathVariable Long id) {
        AdminDto admin = adminUserService.getAdminById(id);
        return ResponseEntity.ok(ApiResponse.success(admin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminDto>> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminDto adminDto) {
        AdminDto updatedAdmin = adminUserService.updateAdmin(id, adminDto);
        return ResponseEntity.ok(ApiResponse.success("Admin updated successfully", updatedAdmin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        adminUserService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Admin deleted successfully", null));
    }
}
