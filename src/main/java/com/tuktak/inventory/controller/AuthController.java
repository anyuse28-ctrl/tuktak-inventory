package com.tuktak.inventory.controller;

import com.tuktak.inventory.config.JwtService;
import com.tuktak.inventory.dto.ApiResponse;
import com.tuktak.inventory.dto.LoginRequest;
import com.tuktak.inventory.dto.LoginResponse;
import com.tuktak.inventory.repository.AdminUserRepository;
import com.tuktak.inventory.entity.AdminUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AdminUserRepository adminUserRepository;  // ✅ added
    private final PasswordEncoder passwordEncoder;          // ✅ added

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_ADMIN");

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .username(userDetails.getUsername())
                    .role(role)
                    .message("Login successful")
                    .build();

            log.info("User {} logged in successfully", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated"));
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_ADMIN");

        LoginResponse response = LoginResponse.builder()
                .username(authentication.getName())
                .role(role)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ✅ Change Password endpoint
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody ChangePasswordRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        AdminUser admin = adminUserRepository.findByUsername(currentEmail)
                .orElseGet(() -> adminUserRepository.findByEmail(currentEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Admin not found")));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("Old password is incorrect"));
        }

        // Set new password
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(admin);

        log.info("Password changed for user: {}", currentEmail);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    // ✅ Also change email
    @PostMapping("/change-email")
    public ResponseEntity<ApiResponse<String>> changeEmail(
            @RequestBody ChangeEmailRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        AdminUser admin = adminUserRepository.findByUsername(currentEmail)
                .orElseGet(() -> adminUserRepository.findByEmail(currentEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Admin not found")));

        // Verify password before changing email
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("Password is incorrect"));
        }

        admin.setEmail(request.getNewEmail());
        adminUserRepository.save(admin);

        log.info("Email changed for user: {} -> {}", currentEmail, request.getNewEmail());
        return ResponseEntity.ok(ApiResponse.success("Email changed successfully. Please login again.", null));
    }

    // ✅ Request DTOs
    @Data
    static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;
        @NotBlank
        private String newPassword;
    }

    @Data
    static class ChangeEmailRequest {
        @NotBlank
        private String password;
        @NotBlank
        private String newEmail;
    }
}