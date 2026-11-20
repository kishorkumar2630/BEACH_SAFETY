package com.example.beach_safety_water_quality.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "water_samples")
public class WaterSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "enterococci_count", nullable = false)
    private Integer enterococciCount;

    @Column(name = "ecoli_count", nullable = false)
    private Integer ecoliCount;

    @Column(name = "sampled_at", nullable = false)
    private LocalDateTime sampledAt;

    @Column(name = "is_safe", nullable = false)
    private Boolean isSafe;

    public WaterSample() {
    }

    public WaterSample(Long id, Long zoneId, Integer enterococciCount, Integer ecoliCount, LocalDateTime sampledAt, Boolean isSafe) {
        this.id = id;
        this.zoneId = zoneId;
        this.enterococciCount = enterococciCount;
        this.ecoliCount = ecoliCount;
        this.sampledAt = sampledAt;
        this.isSafe = isSafe;
    }

    public WaterSample(Long zoneId, Integer enterococciCount, Integer ecoliCount, LocalDateTime sampledAt, Boolean isSafe) {
        this.zoneId = zoneId;
        this.enterococciCount = enterococciCount;
        this.ecoliCount = ecoliCount;
        this.sampledAt = sampledAt;
        this.isSafe = isSafe;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
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

    public LocalDateTime getSampledAt() {
        return sampledAt;
    }

    public void setSampledAt(LocalDateTime sampledAt) {
        this.sampledAt = sampledAt;
    }

    public Boolean getIsSafe() {
        return isSafe;
    }

    public void setIsSafe(Boolean isSafe) {
        this.isSafe = isSafe;
    }
}

