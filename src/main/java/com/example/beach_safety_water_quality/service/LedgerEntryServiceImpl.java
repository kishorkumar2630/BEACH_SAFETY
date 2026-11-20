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
import java.util.List;

@Service
public class LedgerEntryServiceImpl implements LedgerEntryService {

    private final LedgerEntryRepo ledgerEntryRepo;
    private final BeachZoneRepo beachZoneRepo;
    private final ContactRepo contactRepo;

    public LedgerEntryServiceImpl(
            LedgerEntryRepo ledgerEntryRepo,
            BeachZoneRepo beachZoneRepo,
            ContactRepo contactRepo) {
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.beachZoneRepo = beachZoneRepo;
        this.contactRepo = contactRepo;
    }

    @Override
    public LedgerEntry create(LedgerEntry ledgerEntry) {
        if (ledgerEntry.getZoneId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "zoneId cannot be null");
        }
        if (ledgerEntry.getContactId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contactId cannot be null");
        }

        if (!beachZoneRepo.existsById(ledgerEntry.getZoneId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Beach zone not found with id: " + ledgerEntry.getZoneId());
        }
        if (!contactRepo.existsById(ledgerEntry.getContactId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact not found with id: " + ledgerEntry.getContactId());
        }

        ledgerEntry.setId(null);
        return ledgerEntryRepo.save(ledgerEntry);
    }

    @Override
    public List<LedgerEntry> getAll() {
        return ledgerEntryRepo.findAll();
    }

    @Override
    public LedgerEntry getById(Long id) {
        return ledgerEntryRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ledger entry not found with id: " + id));
    }

    @Override
    public LedgerEntry update(Long id, LedgerEntry ledgerEntry) {
        LedgerEntry existingEntry = ledgerEntryRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ledger entry not found with id: " + id));

        if (ledgerEntry.getZoneId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "zoneId cannot be null");
        }
        if (ledgerEntry.getContactId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contactId cannot be null");
        }

        if (!beachZoneRepo.existsById(ledgerEntry.getZoneId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Beach zone not found with id: " + ledgerEntry.getZoneId());
        }
        if (!contactRepo.existsById(ledgerEntry.getContactId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact not found with id: " + ledgerEntry.getContactId());
        }

        existingEntry.setZoneId(ledgerEntry.getZoneId());
        existingEntry.setContactId(ledgerEntry.getContactId());
        existingEntry.setAmount(ledgerEntry.getAmount());
        existingEntry.setType(ledgerEntry.getType());
        existingEntry.setDescription(ledgerEntry.getDescription());

        return ledgerEntryRepo.save(existingEntry);
    }

    @Override
    public void delete(Long id) {
        LedgerEntry existingEntry = ledgerEntryRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ledger entry not found with id: " + id));
        ledgerEntryRepo.delete(existingEntry);
    }

    @Override
    public FinancialSummary getFinancialSummary() {
        BigDecimal totalIncome = ledgerEntryRepo.sumAmountByType(LedgerEntryType.INCOME_PERMIT);
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        BigDecimal totalExpense = ledgerEntryRepo.sumAmountByType(LedgerEntryType.EXPENSE_LAB);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        return new FinancialSummary(totalIncome, totalExpense, netBalance);
    }

    @Override
    public FinancialSummary getFinancialSummaryByZone(Long zoneId) {
        BigDecimal totalIncome = ledgerEntryRepo.sumAmountByZoneIdAndType(zoneId, LedgerEntryType.INCOME_PERMIT);
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        BigDecimal totalExpense = ledgerEntryRepo.sumAmountByZoneIdAndType(zoneId, LedgerEntryType.EXPENSE_LAB);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        return new FinancialSummary(totalIncome, totalExpense, netBalance);
    }
}
