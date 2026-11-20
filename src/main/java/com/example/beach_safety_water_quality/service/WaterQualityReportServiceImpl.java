package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.WaterSample;
import com.example.beach_safety_water_quality.repo.BeachZoneRepo;
import com.example.beach_safety_water_quality.repo.WaterSampleRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaterQualityReportServiceImpl implements WaterQualityReportService {

    private final WaterSampleRepo waterSampleRepo;
    private final BeachZoneRepo beachZoneRepo;

    public WaterQualityReportServiceImpl(WaterSampleRepo waterSampleRepo, BeachZoneRepo beachZoneRepo) {
        this.waterSampleRepo = waterSampleRepo;
        this.beachZoneRepo = beachZoneRepo;
    }

    @Override
    public List<WaterSample> getFilteredSamples(Long zoneId, LocalDateTime from, LocalDateTime to, Boolean safe) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' date cannot be after 'to' date");
        }

        if (zoneId != null && !beachZoneRepo.existsById(zoneId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Beach zone not found with id: " + zoneId);
        }

        return waterSampleRepo.findFilteredSamples(zoneId, from, to, safe);
    }

    @Override
    public WaterQualityStatisticsResponse getWaterQualityStatistics(Long zoneId, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' date cannot be after 'to' date");
        }

        if (zoneId != null && !beachZoneRepo.existsById(zoneId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Beach zone not found with id: " + zoneId);
        }

        List<WaterSample> samples = waterSampleRepo.findFilteredSamples(zoneId, from, to, null);

        if (samples.isEmpty()) {
            return new WaterQualityStatisticsResponse(
                    0L,
                    0L,
                    0L,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
            );
        }

        long totalSamples = samples.size();
        long safeSamples = 0;
        long unsafeSamples = 0;
        long totalEcoli = 0;
        long totalEnterococci = 0;

        for (WaterSample sample : samples) {
            if (Boolean.TRUE.equals(sample.getIsSafe())) {
                safeSamples++;
            } else {
                unsafeSamples++;
            }

            if (sample.getEcoliCount() != null) {
                totalEcoli += sample.getEcoliCount();
            }
            if (sample.getEnterococciCount() != null) {
                totalEnterococci += sample.getEnterococciCount();
            }
        }

        BigDecimal averageEcoli = BigDecimal.valueOf(totalEcoli)
                .divide(BigDecimal.valueOf(totalSamples), 2, RoundingMode.HALF_UP);
        BigDecimal averageEnterococci = BigDecimal.valueOf(totalEnterococci)
                .divide(BigDecimal.valueOf(totalSamples), 2, RoundingMode.HALF_UP);

        return new WaterQualityStatisticsResponse(
                totalSamples,
                safeSamples,
                unsafeSamples,
                averageEcoli,
                averageEnterococci
        );
    }
}
