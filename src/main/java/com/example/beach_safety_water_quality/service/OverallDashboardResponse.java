package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.model.WaterSample;

public class OverallDashboardResponse {

    private Long totalZones;
    private Long greenZones;
    private Long redZones;
    private Long totalWaterSamples;
    private Long safeSamples;
    private Long unsafeSamples;
    private FinancialSummary financialSummary;
    private WaterSample latestSample;
    private BeachZone latestZone;

    public OverallDashboardResponse() {
    }

    public OverallDashboardResponse(Long totalZones, Long greenZones, Long redZones,
                                    Long totalWaterSamples, Long safeSamples, Long unsafeSamples,
                                    FinancialSummary financialSummary, WaterSample latestSample,
                                    BeachZone latestZone) {
        this.totalZones = totalZones;
        this.greenZones = greenZones;
        this.redZones = redZones;
        this.totalWaterSamples = totalWaterSamples;
        this.safeSamples = safeSamples;
        this.unsafeSamples = unsafeSamples;
        this.financialSummary = financialSummary;
        this.latestSample = latestSample;
        this.latestZone = latestZone;
    }

    public Long getTotalZones() {
        return totalZones;
    }

    public void setTotalZones(Long totalZones) {
        this.totalZones = totalZones;
    }

    public Long getGreenZones() {
        return greenZones;
    }

    public void setGreenZones(Long greenZones) {
        this.greenZones = greenZones;
    }

    public Long getRedZones() {
        return redZones;
    }

    public void setRedZones(Long redZones) {
        this.redZones = redZones;
    }

    public Long getTotalWaterSamples() {
        return totalWaterSamples;
    }

    public void setTotalWaterSamples(Long totalWaterSamples) {
        this.totalWaterSamples = totalWaterSamples;
    }

    public Long getSafeSamples() {
        return safeSamples;
    }

    public void setSafeSamples(Long safeSamples) {
        this.safeSamples = safeSamples;
    }

    public Long getUnsafeSamples() {
        return unsafeSamples;
    }

    public void setUnsafeSamples(Long unsafeSamples) {
        this.unsafeSamples = unsafeSamples;
    }

    public FinancialSummary getFinancialSummary() {
        return financialSummary;
    }

    public void setFinancialSummary(FinancialSummary financialSummary) {
        this.financialSummary = financialSummary;
    }

    public WaterSample getLatestSample() {
        return latestSample;
    }

    public void setLatestSample(WaterSample latestSample) {
        this.latestSample = latestSample;
    }

    public BeachZone getLatestZone() {
        return latestZone;
    }

    public void setLatestZone(BeachZone latestZone) {
        this.latestZone = latestZone;
    }
}
