package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.FlagStatus;
import com.example.beach_safety_water_quality.model.WaterSample;

import java.util.List;

public class ZoneMonitoringResponse {

    private Long zoneId;
    private String zoneName;
    private String analyticAccountCode;
    private FlagStatus currentFlagStatus;
    private WaterSample latestSample;
    private List<WaterSample> history;

    public ZoneMonitoringResponse() {
    }

    public ZoneMonitoringResponse(Long zoneId,
                                  String zoneName,
                                  String analyticAccountCode,
                                  FlagStatus currentFlagStatus,
                                  WaterSample latestSample,
                                  List<WaterSample> history) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.analyticAccountCode = analyticAccountCode;
        this.currentFlagStatus = currentFlagStatus;
        this.latestSample = latestSample;
        this.history = history;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getAnalyticAccountCode() {
        return analyticAccountCode;
    }

    public void setAnalyticAccountCode(String analyticAccountCode) {
        this.analyticAccountCode = analyticAccountCode;
    }

    public FlagStatus getCurrentFlagStatus() {
        return currentFlagStatus;
    }

    public void setCurrentFlagStatus(FlagStatus currentFlagStatus) {
        this.currentFlagStatus = currentFlagStatus;
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
}
