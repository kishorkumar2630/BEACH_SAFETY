package com.example.beach_safety_water_quality.service;

import com.example.beach_safety_water_quality.model.LedgerEntry;
import com.example.beach_safety_water_quality.model.LedgerEntryType;
import com.example.beach_safety_water_quality.repo.BeachZoneRepo;
import com.example.beach_safety_water_quality.repo.ContactRepo;
import com.example.beach_safety_water_quality.repo.LedgerEntryRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FinancialReportServiceImpl implements FinancialReportService {

    private final LedgerEntryRepo ledgerEntryRepo;
    private final BeachZoneRepo beachZoneRepo;
    private final ContactRepo contactRepo;
    private final LedgerEntryService ledgerEntryService;

    public FinancialReportServiceImpl(
            LedgerEntryRepo ledgerEntryRepo,
            BeachZoneRepo beachZoneRepo,
            ContactRepo contactRepo,
            LedgerEntryService ledgerEntryService) {
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.beachZoneRepo = beachZoneRepo;
        this.contactRepo = contactRepo;
        this.ledgerEntryService = ledgerEntryService;
    }

    @Override
    public FinancialReportResponse getFinancialReport(Long zoneId, LocalDateTime from, LocalDateTime to, LedgerEntryType type) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' date cannot be after 'to' date");
        }

        if (zoneId != null && !beachZoneRepo.existsById(zoneId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Beach zone not found with id: " + zoneId);
        }

        // Note: LedgerEntry entity does not have a date/time field.
        // Date parameters are validated above, but cannot be filtered against the ledger records.
        if (type == null) {
            FinancialSummary summary = (zoneId != null)
                    ? ledgerEntryService.getFinancialSummaryByZone(zoneId)
                    : ledgerEntryService.getFinancialSummary();
            List<LedgerEntry> entries = ledgerEntryRepo.findFilteredLedgerEntries(zoneId, null, null);
            return new FinancialReportResponse(
                    summary.getTotalIncome(),
                    summary.getTotalExpense(),
                    summary.getNetBalance(),
                    entries.size()
            );
        } else {
            List<LedgerEntry> entries = ledgerEntryRepo.findFilteredLedgerEntries(zoneId, null, type);
            BigDecimal totalIncome = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalExpense = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            for (LedgerEntry entry : entries) {
                BigDecimal amount = entry.getAmount() != null ? entry.getAmount() : BigDecimal.ZERO;
                if (entry.getType() == LedgerEntryType.INCOME_PERMIT) {
                    totalIncome = totalIncome.add(amount);
                } else if (entry.getType() == LedgerEntryType.EXPENSE_LAB) {
                    totalExpense = totalExpense.add(amount);
                }
            }

            BigDecimal netBalance = totalIncome.subtract(totalExpense);
            return new FinancialReportResponse(totalIncome, totalExpense, netBalance, entries.size());
        }
    }

    @Override
    public List<LedgerEntry> getFilteredLedgerEntries(Long zoneId, Long contactId, LedgerEntryType type) {
        if (zoneId != null && !beachZoneRepo.existsById(zoneId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Beach zone not found with id: " + zoneId);
        }

        if (contactId != null && !contactRepo.existsById(contactId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found with id: " + contactId);
        }

        return ledgerEntryRepo.findFilteredLedgerEntries(zoneId, contactId, type);
    }
}
