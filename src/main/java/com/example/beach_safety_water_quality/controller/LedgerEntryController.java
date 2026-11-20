package com.example.beach_safety_water_quality.controller;

import com.example.beach_safety_water_quality.model.LedgerEntry;
import com.example.beach_safety_water_quality.service.FinancialSummary;
import com.example.beach_safety_water_quality.service.LedgerEntryService;
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
@RequestMapping("/api/ledger")
public class LedgerEntryController {

    private final LedgerEntryService ledgerEntryService;

    public LedgerEntryController(LedgerEntryService ledgerEntryService) {
        this.ledgerEntryService = ledgerEntryService;
    }

    @PostMapping("/create")
    public ResponseEntity<LedgerEntry> create(@RequestBody LedgerEntry ledgerEntry) {
        LedgerEntry createdEntry = ledgerEntryService.create(ledgerEntry);
        return new ResponseEntity<>(createdEntry, HttpStatus.CREATED);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<LedgerEntry>> getAll() {
        List<LedgerEntry> entries = ledgerEntryService.getAll();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<LedgerEntry> getById(@PathVariable Long id) {
        LedgerEntry entry = ledgerEntryService.getById(id);
        return ResponseEntity.ok(entry);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<LedgerEntry> update(@PathVariable Long id, @RequestBody LedgerEntry ledgerEntry) {
        LedgerEntry updatedEntry = ledgerEntryService.update(id, ledgerEntry);
        return ResponseEntity.ok(updatedEntry);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ledgerEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummary> getFinancialSummary() {
        FinancialSummary summary = ledgerEntryService.getFinancialSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/zone/{zoneId}")
    public ResponseEntity<FinancialSummary> getFinancialSummaryByZone(@PathVariable Long zoneId) {
        FinancialSummary summary = ledgerEntryService.getFinancialSummaryByZone(zoneId);
        return ResponseEntity.ok(summary);
    }
}
