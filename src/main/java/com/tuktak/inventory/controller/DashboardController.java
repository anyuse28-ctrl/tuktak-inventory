package com.tuktak.inventory.controller;

import com.tuktak.inventory.dto.ApiResponse;
import com.tuktak.inventory.dto.DashboardDto;
import com.tuktak.inventory.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardDto>> getDashboardSummary() {
        DashboardDto dashboard = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
