package com.example.beach_safety_water_quality.controller;

import com.example.beach_safety_water_quality.model.WaterSample;
import com.example.beach_safety_water_quality.service.WaterSampleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beach/samples")
public class WaterSampleController {

    private final WaterSampleService waterSampleService;

    public WaterSampleController(WaterSampleService waterSampleService) {
        this.waterSampleService = waterSampleService;
    }

    @PostMapping("/create")
    public ResponseEntity<WaterSample> create(@RequestBody WaterSample waterSample) {
        WaterSample createdSample = waterSampleService.createWaterSample(waterSample);
        return new ResponseEntity<>(createdSample, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<WaterSample>> getAll() {
        List<WaterSample> samples = waterSampleService.getAllWaterSamples();
        return ResponseEntity.ok(samples);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<WaterSample> getById(@PathVariable Long id) {
        WaterSample sample = waterSampleService.getWaterSampleById(id);
        return ResponseEntity.ok(sample);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<WaterSample> update(@PathVariable Long id, @RequestBody WaterSample waterSample) {
        WaterSample updatedSample = waterSampleService.updateWaterSample(id, waterSample);
        return ResponseEntity.ok(updatedSample);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        waterSampleService.deleteWaterSample(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
