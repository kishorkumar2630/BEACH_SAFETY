package com.example.beach_safety_water_quality.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "beach_zones")
public class BeachZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_flag_status", nullable = false)
    private FlagStatus currentFlagStatus = FlagStatus.GREEN;

    @Column(name = "analytic_account_code", nullable = false, unique = true)
    private String analyticAccountCode;

    public BeachZone() {
    }

    public BeachZone(Long id, String name, FlagStatus currentFlagStatus, String analyticAccountCode) {
        this.id = id;
        this.name = name;
        this.currentFlagStatus = currentFlagStatus;
        this.analyticAccountCode = analyticAccountCode;
    }

    public BeachZone(String name, FlagStatus currentFlagStatus, String analyticAccountCode) {
        this.name = name;
        this.currentFlagStatus = currentFlagStatus;
        this.analyticAccountCode = analyticAccountCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlagStatus getCurrentFlagStatus() {
        return currentFlagStatus;
    }

    public void setCurrentFlagStatus(FlagStatus currentFlagStatus) {
        this.currentFlagStatus = currentFlagStatus;
    }

    public String getAnalyticAccountCode() {
        return analyticAccountCode;
    }

    public void setAnalyticAccountCode(String analyticAccountCode) {
        this.analyticAccountCode = analyticAccountCode;
    }
}

