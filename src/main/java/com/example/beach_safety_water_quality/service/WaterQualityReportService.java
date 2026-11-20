package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.WaterSample;

import java.time.LocalDateTime;
import java.util.List;

public interface WaterQualityReportService {

    List<WaterSample> getFilteredSamples(Long zoneId, LocalDateTime from, LocalDateTime to, Boolean safe);

    WaterQualityStatisticsResponse getWaterQualityStatistics(Long zoneId, LocalDateTime from, LocalDateTime to);
}
