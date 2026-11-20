package com.example.beach_safety_water_quality.repo;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.model.FlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeachZoneRepo extends JpaRepository<BeachZone, Long> {

    long countByCurrentFlagStatus(FlagStatus currentFlagStatus);
}
