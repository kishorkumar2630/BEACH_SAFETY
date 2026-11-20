package com.example.beach_safety_water_quality.service;

import java.math.BigDecimal;

public class WaterQualityStatisticsResponse {

    private Long totalSamples;
    private Long safeSamples;
    private Long unsafeSamples;
    private BigDecimal averageEcoli;
    private BigDecimal averageEnterococci;

    public WaterQualityStatisticsResponse() {
    }

    public WaterQualityStatisticsResponse(Long totalSamples, Long safeSamples, Long unsafeSamples,
                                          BigDecimal averageEcoli, BigDecimal averageEnterococci) {
        this.totalSamples = totalSamples;
        this.safeSamples = safeSamples;
        this.unsafeSamples = unsafeSamples;
        this.averageEcoli = averageEcoli;
        this.averageEnterococci = averageEnterococci;
    }

    public Long getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(Long totalSamples) {
        this.totalSamples = totalSamples;
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

    public BigDecimal getAverageEcoli() {
        return averageEcoli;
    }

    public void setAverageEcoli(BigDecimal averageEcoli) {
        this.averageEcoli = averageEcoli;
    }

    public BigDecimal getAverageEnterococci() {
        return averageEnterococci;
    }

    public void setAverageEnterococci(BigDecimal averageEnterococci) {
        this.averageEnterococci = averageEnterococci;
    }
}
