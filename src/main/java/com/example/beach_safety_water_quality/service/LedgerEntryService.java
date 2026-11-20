package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.LedgerEntry;

import java.util.List;

public interface LedgerEntryService {

    LedgerEntry create(LedgerEntry ledgerEntry);

    List<LedgerEntry> getAll();

    LedgerEntry getById(Long id);

    LedgerEntry update(Long id, LedgerEntry ledgerEntry);

    void delete(Long id);

    FinancialSummary getFinancialSummary();

    FinancialSummary getFinancialSummaryByZone(Long zoneId);
}
