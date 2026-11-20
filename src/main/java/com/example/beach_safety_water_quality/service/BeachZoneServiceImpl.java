package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.model.FlagStatus;
import com.example.beach_safety_water_quality.repo.BeachZoneRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BeachZoneServiceImpl implements BeachZoneService {

    private final BeachZoneRepo beachZoneRepo;

    public BeachZoneServiceImpl(BeachZoneRepo beachZoneRepo) {
        this.beachZoneRepo = beachZoneRepo;
    }

    @Override
    public BeachZone create(BeachZone beachZone) {
        if (beachZone.getCurrentFlagStatus() == null) {
            beachZone.setCurrentFlagStatus(FlagStatus.GREEN);
        }
        beachZone.setId(null);
        return beachZoneRepo.save(beachZone);
    }

    @Override
    public List<BeachZone> getAll() {
        return beachZoneRepo.findAll();
    }

    @Override
    public BeachZone getById(Long id) {
        return beachZoneRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beach zone not found with id: " + id));
    }

    @Override
    public BeachZone update(Long id, BeachZone beachZone) {
        BeachZone existingZone = beachZoneRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beach zone not found with id: " + id));

        existingZone.setName(beachZone.getName());
        if (beachZone.getCurrentFlagStatus() != null) {
            existingZone.setCurrentFlagStatus(beachZone.getCurrentFlagStatus());
        }
        existingZone.setAnalyticAccountCode(beachZone.getAnalyticAccountCode());

        return beachZoneRepo.save(existingZone);
    }

    @Override
    public void delete(Long id) {
        BeachZone existingZone = beachZoneRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beach zone not found with id: " + id));
        beachZoneRepo.delete(existingZone);
    }
}

