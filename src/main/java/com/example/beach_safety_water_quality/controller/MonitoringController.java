package com.example.beach_safety_water_quality.controller;

import com.example.beach_safety_water_quality.model.WaterSample;
import com.example.beach_safety_water_quality.service.WaterSampleService;
import com.example.beach_safety_water_quality.service.WaterStatusResponse;
import com.example.beach_safety_water_quality.service.ZoneMonitoringResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beach/monitoring")
public class MonitoringController {

    private final WaterSampleService waterSampleService;

    public MonitoringController(WaterSampleService waterSampleService) {
        this.waterSampleService = waterSampleService;
    }

    @GetMapping("/latest/{zoneId}")
    public ResponseEntity<WaterSample> getLatestWaterSample(@PathVariable Long zoneId) {
        WaterSample sample = waterSampleService.getLatestWaterSample(zoneId);
        return ResponseEntity.ok(sample);
    }

    @GetMapping("/status/{zoneId}")
    public ResponseEntity<WaterStatusResponse> getWaterStatus(@PathVariable Long zoneId) {
        WaterStatusResponse status = waterSampleService.getWaterStatus(zoneId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/history/{zoneId}")
    public ResponseEntity<List<WaterSample>> getWaterQualityHistory(@PathVariable Long zoneId) {
        List<WaterSample> history = waterSampleService.getWaterQualityHistory(zoneId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<ZoneMonitoringResponse> getZoneMonitoring(@PathVariable Long zoneId) {
        ZoneMonitoringResponse response = waterSampleService.getZoneMonitoring(zoneId);
        return ResponseEntity.ok(response);
    }
}
