package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.model.FlagStatus;
import com.example.beach_safety_water_quality.model.WaterSample;
import com.example.beach_safety_water_quality.repo.BeachZoneRepo;
import com.example.beach_safety_water_quality.repo.WaterSampleRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class WaterSampleServiceImpl implements WaterSampleService {

    private final WaterSampleRepo waterSampleRepo;
    private final BeachZoneRepo beachZoneRepo;

    public WaterSampleServiceImpl(
            WaterSampleRepo waterSampleRepo,
            BeachZoneRepo beachZoneRepo) {

        this.waterSampleRepo = waterSampleRepo;
        this.beachZoneRepo = beachZoneRepo;
    }

    @Override
    @Transactional
    public WaterSample createWaterSample(WaterSample waterSample) {

        boolean unsafe =
                waterSample.getEcoliCount() > 235
                || waterSample.getEnterococciCount() > 104;

        BeachZone beachZone = beachZoneRepo.findById(waterSample.getZoneId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Beach zone not found with id: "
                                + waterSample.getZoneId()
                        )
                );

        if (unsafe) {
            waterSample.setIsSafe(false);
            beachZone.setCurrentFlagStatus(FlagStatus.RED);
        } else {
            waterSample.setIsSafe(true);
            beachZone.setCurrentFlagStatus(FlagStatus.GREEN);
        }

        beachZoneRepo.save(beachZone);

        return waterSampleRepo.save(waterSample);
    }

    @Override
    public List<WaterSample> getAllWaterSamples() {
        return waterSampleRepo.findAll();
    }

    @Override
    public WaterSample getWaterSampleById(Long id) {

        return waterSampleRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Water sample not found with id: " + id
                        )
                );
    }

    @Override
    public WaterSample updateWaterSample(
            Long id,
            WaterSample waterSample) {

        WaterSample existingSample = waterSampleRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Water sample not found with id: " + id
                        )
                );

        existingSample.setZoneId(waterSample.getZoneId());
        existingSample.setEnterococciCount(
                waterSample.getEnterococciCount()
        );
        existingSample.setEcoliCount(
                waterSample.getEcoliCount()
        );
        existingSample.setSampledAt(
                waterSample.getSampledAt()
        );
        existingSample.setIsSafe(
                waterSample.getIsSafe()
        );

        return waterSampleRepo.save(existingSample);
    }

    @Override
    public void deleteWaterSample(Long id) {

        if (!waterSampleRepo.existsById(id)) {
            throw new RuntimeException(
                    "Water sample not found with id: " + id
            );
        }

        waterSampleRepo.deleteById(id);
    }

    @Override
    public WaterSample getLatestWaterSample(Long zoneId) {
        if (!beachZoneRepo.existsById(zoneId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Beach zone not found with id: " + zoneId
            );
        }

        return waterSampleRepo.findFirstByZoneIdOrderBySampledAtDescIdDesc(zoneId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Water sample not found for zone id: " + zoneId
                        )
                );
    }

    @Override
    public WaterStatusResponse getWaterStatus(Long zoneId) {
        BeachZone beachZone = beachZoneRepo.findById(zoneId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Beach zone not found with id: " + zoneId
                        )
                );

        Optional<WaterSample> latestSampleOpt = waterSampleRepo.findFirstByZoneIdOrderBySampledAtDescIdDesc(zoneId);

        if (latestSampleOpt.isPresent()) {
            WaterSample latest = latestSampleOpt.get();
            return new WaterStatusResponse(
                    beachZone.getId(),
                    beachZone.getName(),
                    beachZone.getCurrentFlagStatus(),
                    latest.getId(),
                    latest.getSampledAt(),
                    latest.getEnterococciCount(),
                    latest.getEcoliCount(),
                    latest.getIsSafe()
            );
        } else {
            return new WaterStatusResponse(
                    beachZone.getId(),
                    beachZone.getName(),
                    beachZone.getCurrentFlagStatus(),
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    @Override
    public List<WaterSample> getWaterQualityHistory(Long zoneId) {
        if (!beachZoneRepo.existsById(zoneId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Beach zone not found with id: " + zoneId
            );
        }

        return waterSampleRepo.findByZoneIdOrderBySampledAtDescIdDesc(zoneId);
    }

    @Override
    public ZoneMonitoringResponse getZoneMonitoring(Long zoneId) {
        BeachZone beachZone = beachZoneRepo.findById(zoneId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Beach zone not found with id: " + zoneId
                        )
                );

        List<WaterSample> history = waterSampleRepo.findByZoneIdOrderBySampledAtDescIdDesc(zoneId);
        WaterSample latestSample = history.isEmpty() ? null : history.get(0);

        return new ZoneMonitoringResponse(
                beachZone.getId(),
                beachZone.getName(),
                beachZone.getAnalyticAccountCode(),
                beachZone.getCurrentFlagStatus(),
                latestSample,
                history
        );
    }
}
