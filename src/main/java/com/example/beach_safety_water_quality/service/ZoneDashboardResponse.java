package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.model.WaterSample;

import java.util.List;

public class ZoneDashboardResponse {

    private BeachZone zone;
    private WaterSample latestSample;
    private List<WaterSample> history;
    private FinancialSummary financialSummary;

    public ZoneDashboardResponse() {
    }

    public ZoneDashboardResponse(BeachZone zone, WaterSample latestSample, List<WaterSample> history, FinancialSummary financialSummary) {
        this.zone = zone;
        this.latestSample = latestSample;
        this.history = history;
        this.financialSummary = financialSummary;
    }

    public BeachZone getZone() {
        return zone;
    }

    public void setZone(BeachZone zone) {
        this.zone = zone;
    }

    public WaterSample getLatestSample() {
        return latestSample;
    }

    public void setLatestSample(WaterSample latestSample) {
        this.latestSample = latestSample;
    }

    public List<WaterSample> getHistory() {
        return history;
    }

    public void setHistory(List<WaterSample> history) {
        this.history = history;
    }

    public FinancialSummary getFinancialSummary() {
        return financialSummary;
    }

    public void setFinancialSummary(FinancialSummary financialSummary) {
        this.financialSummary = financialSummary;
    }
}
