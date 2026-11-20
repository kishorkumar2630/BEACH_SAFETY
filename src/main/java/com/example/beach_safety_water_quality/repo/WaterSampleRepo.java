package com.example.beach_safety_water_quality.repo;

import com.example.beach_safety_water_quality.model.WaterSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaterSampleRepo extends JpaRepository<WaterSample, Long> {

    Optional<WaterSample> findFirstByZoneIdOrderBySampledAtDescIdDesc(Long zoneId);

    List<WaterSample> findByZoneIdOrderBySampledAtDescIdDesc(Long zoneId);

    long countByIsSafe(Boolean isSafe);

    Optional<WaterSample> findFirstByOrderBySampledAtDescIdDesc();

    @Query("SELECT w FROM WaterSample w WHERE " +
           "(:zoneId IS NULL OR w.zoneId = :zoneId) AND " +
           "(:from IS NULL OR w.sampledAt >= :from) AND " +
           "(:to IS NULL OR w.sampledAt <= :to) AND " +
           "(:safe IS NULL OR w.isSafe = :safe) " +
           "ORDER BY w.sampledAt DESC, w.id DESC")
    List<WaterSample> findFilteredSamples(
            @Param("zoneId") Long zoneId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("safe") Boolean safe
    );
}
