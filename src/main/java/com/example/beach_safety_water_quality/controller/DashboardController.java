package com.example.beach_safety_water_quality.controller;

import com.example.beach_safety_water_quality.service.DashboardService;
import com.example.beach_safety_water_quality.service.OverallDashboardResponse;
import com.example.beach_safety_water_quality.service.ZoneDashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/beach/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<OverallDashboardResponse> getOverallDashboard() {
        OverallDashboardResponse response = dashboardService.getOverallDashboard();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<ZoneDashboardResponse> getZoneDashboard(@PathVariable Long zoneId) {
        ZoneDashboardResponse response = dashboardService.getZoneDashboard(zoneId);
        return ResponseEntity.ok(response);
    }
}
