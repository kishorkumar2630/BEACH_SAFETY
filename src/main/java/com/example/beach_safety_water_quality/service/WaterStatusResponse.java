package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.FlagStatus;

import java.time.LocalDateTime;

public class WaterStatusResponse {

    private Long zoneId;
    private String zoneName;
    private FlagStatus currentFlagStatus;
    private Long latestSampleId;
    private LocalDateTime latestSampleTime;
    private Integer enterococciCount;
    private Integer ecoliCount;
    private Boolean isSafe;

    public WaterStatusResponse() {
    }

    public WaterStatusResponse(Long zoneId,
                               String zoneName,
                               FlagStatus currentFlagStatus,
                               Long latestSampleId,
                               LocalDateTime latestSampleTime,
                               Integer enterococciCount,
                               Integer ecoliCount,
                               Boolean isSafe) {
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.currentFlagStatus = currentFlagStatus;
        this.latestSampleId = latestSampleId;
        this.latestSampleTime = latestSampleTime;
        this.enterococciCount = enterococciCount;
        this.ecoliCount = ecoliCount;
        this.isSafe = isSafe;
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

    public FlagStatus getCurrentFlagStatus() {
        return currentFlagStatus;
    }

    public void setCurrentFlagStatus(FlagStatus currentFlagStatus) {
        this.currentFlagStatus = currentFlagStatus;
    }

    public Long getLatestSampleId() {
        return latestSampleId;
    }

    public void setLatestSampleId(Long latestSampleId) {
        this.latestSampleId = latestSampleId;
    }

    public LocalDateTime getLatestSampleTime() {
        return latestSampleTime;
    }

    public void setLatestSampleTime(LocalDateTime latestSampleTime) {
        this.latestSampleTime = latestSampleTime;
    }

    public Integer getEnterococciCount() {
        return enterococciCount;
    }

    public void setEnterococciCount(Integer enterococciCount) {
        this.enterococciCount = enterococciCount;
    }

    public Integer getEcoliCount() {
        return ecoliCount;
    }

    public void setEcoliCount(Integer ecoliCount) {
        this.ecoliCount = ecoliCount;
    }

    public Boolean getIsSafe() {
        return isSafe;
    }

    public void setIsSafe(Boolean isSafe) {
        this.isSafe = isSafe;
    }
}
