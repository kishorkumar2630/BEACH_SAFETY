package com.example.beach_safety_water_quality.controller;

import com.example.beach_safety_water_quality.model.LedgerEntry;
import com.example.beach_safety_water_quality.model.LedgerEntryType;
import com.example.beach_safety_water_quality.model.WaterSample;
import com.example.beach_safety_water_quality.service.FinancialReportResponse;
import com.example.beach_safety_water_quality.service.FinancialReportService;
import com.example.beach_safety_water_quality.service.WaterQualityReportService;
import com.example.beach_safety_water_quality.service.WaterQualityStatisticsResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/beach/reports")
public class ReportController {

    private final WaterQualityReportService waterQualityReportService;
    private final FinancialReportService financialReportService;

    public ReportController(
            WaterQualityReportService waterQualityReportService,
            FinancialReportService financialReportService) {
        this.waterQualityReportService = waterQualityReportService;
        this.financialReportService = financialReportService;
    }

    @GetMapping("/samples")
    public ResponseEntity<List<WaterSample>> getSamples(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) Boolean safe) {
        List<WaterSample> samples = waterQualityReportService.getFilteredSamples(zoneId, from, to, safe);
        return ResponseEntity.ok(samples);
    }

    @GetMapping("/water-quality")
    public ResponseEntity<WaterQualityStatisticsResponse> getWaterQualityStatistics(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        WaterQualityStatisticsResponse stats = waterQualityReportService.getWaterQualityStatistics(zoneId, from, to);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/financial")
    public ResponseEntity<FinancialReportResponse> getFinancialReport(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) LedgerEntryType type) {
        FinancialReportResponse report = financialReportService.getFinancialReport(zoneId, from, to, type);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/ledger")
    public ResponseEntity<List<LedgerEntry>> getLedgerEntries(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long contactId,
            @RequestParam(required = false) LedgerEntryType type) {
        List<LedgerEntry> entries = financialReportService.getFilteredLedgerEntries(zoneId, contactId, type);
        return ResponseEntity.ok(entries);
    }
}
