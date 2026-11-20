package com.example.beach_safety_water_quality.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "contact_id", nullable = false)
    private Long contactId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LedgerEntryType type;

    @Column(name = "description")
    private String description;

    public LedgerEntry() {
    }

    public LedgerEntry(Long id, Long zoneId, Long contactId, BigDecimal amount, LedgerEntryType type, String description) {
        this.id = id;
        this.zoneId = zoneId;
        this.contactId = contactId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public LedgerEntry(Long zoneId, Long contactId, BigDecimal amount, LedgerEntryType type, String description) {
        this.zoneId = zoneId;
        this.contactId = contactId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LedgerEntryType getType() {
        return type;
    }

    public void setType(LedgerEntryType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

