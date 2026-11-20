package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.BeachZone;
import java.util.List;

public interface BeachZoneService {

    BeachZone create(BeachZone beachZone);

    List<BeachZone> getAll();

    BeachZone getById(Long id);

    BeachZone update(Long id, BeachZone beachZone);

    void delete(Long id);
}

