package com.example.beach_safety_water_quality.controller;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.service.BeachZoneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beach/zones")
public class BeachZoneController {

    private final BeachZoneService beachZoneService;

    public BeachZoneController(BeachZoneService beachZoneService) {
        this.beachZoneService = beachZoneService;
    }

    @PostMapping("/create")
    public ResponseEntity<BeachZone> create(@RequestBody BeachZone beachZone) {
        BeachZone createdZone = beachZoneService.create(beachZone);
        return new ResponseEntity<>(createdZone, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<BeachZone>> getAll() {
        List<BeachZone> zones = beachZoneService.getAll();
        return ResponseEntity.ok(zones);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<BeachZone> getById(@PathVariable Long id) {
        BeachZone zone = beachZoneService.getById(id);
        return ResponseEntity.ok(zone);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<BeachZone> update(@PathVariable Long id, @RequestBody BeachZone beachZone) {
        BeachZone updatedZone = beachZoneService.update(id, beachZone);
        return ResponseEntity.ok(updatedZone);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        beachZoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

