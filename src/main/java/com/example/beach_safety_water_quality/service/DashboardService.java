package com.example.beach_safety_water_quality.service;

public interface DashboardService {

    OverallDashboardResponse getOverallDashboard();

    ZoneDashboardResponse getZoneDashboard(Long zoneId);
}
