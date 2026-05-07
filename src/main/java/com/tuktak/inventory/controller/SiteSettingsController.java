package com.tuktak.inventory.controller;

import com.tuktak.inventory.dto.ApiResponse;
import com.tuktak.inventory.service.SiteSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SiteSettingsController {

    private final SiteSettingsService siteSettingsService;

    // Admin - get all settings
    @GetMapping("/api/settings")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(siteSettingsService.getAllSettings()));
    }

    // Admin - update settings
    @PutMapping("/api/settings")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateSettings(
            @RequestBody Map<String, String> settings) {
        return ResponseEntity.ok(ApiResponse.success("Settings updated",
                siteSettingsService.updateSettings(settings)));
    }

    // Public - get settings for customer app
    @GetMapping("/api/public/settings")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPublicSettings() {
        return ResponseEntity.ok(ApiResponse.success(siteSettingsService.getAllSettings()));
    }
}