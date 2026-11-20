package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.LedgerEntry;
import com.example.beach_safety_water_quality.model.LedgerEntryType;

import java.time.LocalDateTime;
import java.util.List;

public interface FinancialReportService {

    /**
     * Generates a financial report.
     * Note: LedgerEntry entity does not have a date/time field, so from/to date parameters
     * are validated but not filtered against the ledger records.
     */
    FinancialReportResponse getFinancialReport(Long zoneId, LocalDateTime from, LocalDateTime to, LedgerEntryType type);

    /**
     * Retrieves filtered ledger entries.
     */
    List<LedgerEntry> getFilteredLedgerEntries(Long zoneId, Long contactId, LedgerEntryType type);
}
