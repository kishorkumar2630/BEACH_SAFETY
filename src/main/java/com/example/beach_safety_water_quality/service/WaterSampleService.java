package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.WaterSample;

import java.util.List;

public interface WaterSampleService {

    WaterSample createWaterSample(WaterSample waterSample);

    List<WaterSample> getAllWaterSamples();

    WaterSample getWaterSampleById(Long id);

    WaterSample updateWaterSample(Long id, WaterSample waterSample);

    void deleteWaterSample(Long id);

    WaterSample getLatestWaterSample(Long zoneId);

    WaterStatusResponse getWaterStatus(Long zoneId);

    List<WaterSample> getWaterQualityHistory(Long zoneId);

    ZoneMonitoringResponse getZoneMonitoring(Long zoneId);
}
