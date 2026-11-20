package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.model.FlagStatus;
import com.example.beach_safety_water_quality.model.WaterSample;
import com.example.beach_safety_water_quality.repo.BeachZoneRepo;
import com.example.beach_safety_water_quality.repo.WaterSampleRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final BeachZoneRepo beachZoneRepo;
    private final WaterSampleRepo waterSampleRepo;
    private final WaterSampleService waterSampleService;
    private final LedgerEntryService ledgerEntryService;

    public DashboardServiceImpl(
            BeachZoneRepo beachZoneRepo,
            WaterSampleRepo waterSampleRepo,
            WaterSampleService waterSampleService,
            LedgerEntryService ledgerEntryService) {
        this.beachZoneRepo = beachZoneRepo;
        this.waterSampleRepo = waterSampleRepo;
        this.waterSampleService = waterSampleService;
        this.ledgerEntryService = ledgerEntryService;
    }

    @Override
    public OverallDashboardResponse getOverallDashboard() {
        long totalZones = beachZoneRepo.count();
        long greenZones = beachZoneRepo.countByCurrentFlagStatus(FlagStatus.GREEN);
        long redZones = beachZoneRepo.countByCurrentFlagStatus(FlagStatus.RED);

        long totalWaterSamples = waterSampleRepo.count();
        long safeSamples = waterSampleRepo.countByIsSafe(true);
        long unsafeSamples = waterSampleRepo.countByIsSafe(false);

        FinancialSummary financialSummary = ledgerEntryService.getFinancialSummary();
        if (financialSummary == null) {
            financialSummary = new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Optional<WaterSample> latestSampleOpt = waterSampleRepo.findFirstByOrderBySampledAtDescIdDesc();
        WaterSample latestSample = latestSampleOpt.orElse(null);

        BeachZone latestZone = null;
        if (latestSample != null && latestSample.getZoneId() != null) {
            latestZone = beachZoneRepo.findById(latestSample.getZoneId()).orElse(null);
        }

        return new OverallDashboardResponse(
                totalZones,
                greenZones,
                redZones,
                totalWaterSamples,
                safeSamples,
                unsafeSamples,
                financialSummary,
                latestSample,
                latestZone
        );
    }

    @Override
    public ZoneDashboardResponse getZoneDashboard(Long zoneId) {
        BeachZone zone = beachZoneRepo.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Beach zone not found with id: " + zoneId
                ));

        List<WaterSample> history = waterSampleService.getWaterQualityHistory(zoneId);
        if (history == null) {
            history = Collections.emptyList();
        }

        WaterSample latestSample = !history.isEmpty() ? history.get(0) : null;

        FinancialSummary financialSummary = ledgerEntryService.getFinancialSummaryByZone(zoneId);
        if (financialSummary == null) {
            financialSummary = new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return new ZoneDashboardResponse(
                zone,
                latestSample,
                history,
                financialSummary
        );
    }
}
