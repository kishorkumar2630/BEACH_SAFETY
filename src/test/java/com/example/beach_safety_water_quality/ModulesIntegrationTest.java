package com.example.beach_safety_water_quality;

import com.example.beach_safety_water_quality.model.*;
import com.example.beach_safety_water_quality.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ModulesIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private WaterSampleRepo waterSampleRepo;

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private LedgerEntryRepo ledgerEntryRepo;

    @Autowired
    private BeachZoneRepo beachZoneRepo;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        waterSampleRepo.deleteAll();
        contactRepo.deleteAll();
        ledgerEntryRepo.deleteAll();
        beachZoneRepo.deleteAll();
    }

    // ==========================================
    // MODULE 2: Water Quality Monitoring Tests
    // ==========================================

    @Test
    void testCreateWaterSample_Safe() throws Exception {
        BeachZone zone = beachZoneRepo.save(new BeachZone("North Beach", FlagStatus.RED, "BZ-NB-001"));

        String json = String.format("""
                {
                    "zoneId": %d,
                    "enterococciCount": 35,
                    "ecoliCount": 120,
                    "sampledAt": "2026-09-23T10:00:00",
                    "isSafe": false
                }
                """, zone.getId());

        mockMvc.perform(post("/api/beach/samples/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.zoneId", is(zone.getId().intValue())))
                .andExpect(jsonPath("$.enterococciCount", is(35)))
                .andExpect(jsonPath("$.ecoliCount", is(120)))
                .andExpect(jsonPath("$.isSafe", is(true)));

        BeachZone updatedZone = beachZoneRepo.findById(zone.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(FlagStatus.GREEN, updatedZone.getCurrentFlagStatus());
    }

    @Test
    void testCreateWaterSample_Unsafe_UpdatesFlagToRed() throws Exception {
        BeachZone zone = beachZoneRepo.save(new BeachZone("Sunset Beach", FlagStatus.GREEN, "BZ-SS-001"));

        String json = String.format("""
                {
                    "zoneId": %d,
                    "enterococciCount": 150,
                    "ecoliCount": 300,
                    "sampledAt": "2026-09-23T10:00:00",
                    "isSafe": true
                }
                """, zone.getId());

        mockMvc.perform(post("/api/beach/samples/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.isSafe", is(false)));

        BeachZone updatedZone = beachZoneRepo.findById(zone.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(FlagStatus.RED, updatedZone.getCurrentFlagStatus());
    }

    @Test
    void testGetAllWaterSamples() throws Exception {
        waterSampleRepo.save(new WaterSample(1L, 10, 50, LocalDateTime.now(), true));

        mockMvc.perform(get("/api/beach/samples/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetWaterSampleById() throws Exception {
        WaterSample sample = waterSampleRepo.save(new WaterSample(1L, 10, 50, LocalDateTime.now(), true));

        mockMvc.perform(get("/api/beach/samples/get/" + sample.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sample.getId().intValue())));

        mockMvc.perform(get("/api/beach/samples/get/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateWaterSample() throws Exception {
        WaterSample sample = waterSampleRepo.save(new WaterSample(1L, 10, 50, LocalDateTime.now(), true));

        String updateJson = """
                {
                    "zoneId": 1,
                    "enterococciCount": 200,
                    "ecoliCount": 500,
                    "sampledAt": "2026-09-23T11:00:00",
                    "isSafe": false
                }
                """;

        mockMvc.perform(put("/api/beach/samples/update/" + sample.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSafe", is(false)))
                .andExpect(jsonPath("$.enterococciCount", is(200)));

        mockMvc.perform(put("/api/beach/samples/update/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteWaterSample() throws Exception {
        WaterSample sample = waterSampleRepo.save(new WaterSample(1L, 10, 50, LocalDateTime.now(), true));

        mockMvc.perform(delete("/api/beach/samples/delete/" + sample.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/beach/samples/delete/99999"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // MODULE 3: Contact Management Tests
    // ==========================================

    @Test
    void testCreateContact() throws Exception {
        String json = """
                {
                    "name": "Oceanic Testing Labs",
                    "type": "VENDOR"
                }
                """;

        mockMvc.perform(post("/api/contacts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Oceanic Testing Labs")))
                .andExpect(jsonPath("$.type", is("VENDOR")));
    }

    @Test
    void testGetAllContacts() throws Exception {
        contactRepo.save(new Contact("Municipal Safety Board", ContactType.INTERNAL));

        mockMvc.perform(get("/api/contacts/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetContactById() throws Exception {
        Contact contact = contactRepo.save(new Contact("Coastal Event Organizer", ContactType.CUSTOMER));

        mockMvc.perform(get("/api/contacts/get/" + contact.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Coastal Event Organizer")));

        mockMvc.perform(get("/api/contacts/get/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateContact() throws Exception {
        Contact contact = contactRepo.save(new Contact("Old Name", ContactType.VENDOR));

        String updateJson = """
                {
                    "name": "New Name",
                    "type": "INTERNAL"
                }
                """;

        mockMvc.perform(put("/api/contacts/update/" + contact.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.type", is("INTERNAL")));

        mockMvc.perform(put("/api/contacts/update/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteContact() throws Exception {
        Contact contact = contactRepo.save(new Contact("To Delete", ContactType.CUSTOMER));

        mockMvc.perform(delete("/api/contacts/delete/" + contact.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/contacts/delete/99999"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // MODULE 4: Financial Ledger Management Tests
    // ==========================================

    @Test
    void testCreateLedgerEntry() throws Exception {
        BeachZone zone = beachZoneRepo.save(new BeachZone("Central Beach", FlagStatus.GREEN, "BZ-CB-001"));
        Contact contact = contactRepo.save(new Contact("Lab Corp", ContactType.VENDOR));

        String json = String.format("""
                {
                    "zoneId": %d,
                    "contactId": %d,
                    "amount": 750.50,
                    "type": "EXPENSE_LAB",
                    "description": "Weekly bacterial culture analysis"
                }
                """, zone.getId(), contact.getId());

        mockMvc.perform(post("/api/ledger/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.zoneId", is(zone.getId().intValue())))
                .andExpect(jsonPath("$.contactId", is(contact.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(750.50)))
                .andExpect(jsonPath("$.type", is("EXPENSE_LAB")))
                .andExpect(jsonPath("$.description", is("Weekly bacterial culture analysis")));
    }

    @Test
    void testCreateLedgerEntry_InvalidZone() throws Exception {
        Contact contact = contactRepo.save(new Contact("Lab Corp", ContactType.VENDOR));

        String json = String.format("""
                {
                    "zoneId": 9999,
                    "contactId": %d,
                    "amount": 5000,
                    "type": "INCOME_PERMIT",
                    "description": "Invalid zone test"
                }
                """, contact.getId());

        mockMvc.perform(post("/api/ledger/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        org.junit.jupiter.api.Assertions.assertEquals(0, ledgerEntryRepo.count());
    }

    @Test
    void testCreateLedgerEntry_InvalidContact() throws Exception {
        BeachZone zone = beachZoneRepo.save(new BeachZone("Central Beach", FlagStatus.GREEN, "BZ-CB-001"));

        String json = String.format("""
                {
                    "zoneId": %d,
                    "contactId": 9999,
                    "amount": 5000,
                    "type": "INCOME_PERMIT",
                    "description": "Invalid contact test"
                }
                """, zone.getId());

        mockMvc.perform(post("/api/ledger/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        org.junit.jupiter.api.Assertions.assertEquals(0, ledgerEntryRepo.count());
    }

    @Test
    void testGetAllLedgerEntries() throws Exception {
        ledgerEntryRepo.save(new LedgerEntry(1L, 1L, new BigDecimal("1500.00"), LedgerEntryType.INCOME_PERMIT, "Commercial beach permit"));

        mockMvc.perform(get("/api/ledger/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetLedgerEntryById() throws Exception {
        LedgerEntry entry = ledgerEntryRepo.save(new LedgerEntry(1L, 1L, new BigDecimal("250.00"), LedgerEntryType.EXPENSE_LAB, "Test sample cost"));

        mockMvc.perform(get("/api/ledger/get/" + entry.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(entry.getId().intValue())));

        mockMvc.perform(get("/api/ledger/get/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateLedgerEntry() throws Exception {
        BeachZone zone1 = beachZoneRepo.save(new BeachZone("Beach 1", FlagStatus.GREEN, "BZ-1"));
        BeachZone zone2 = beachZoneRepo.save(new BeachZone("Beach 2", FlagStatus.GREEN, "BZ-2"));
        Contact contact1 = contactRepo.save(new Contact("Vendor 1", ContactType.VENDOR));
        Contact contact2 = contactRepo.save(new Contact("Customer 2", ContactType.CUSTOMER));

        LedgerEntry entry = ledgerEntryRepo.save(new LedgerEntry(zone1.getId(), contact1.getId(), new BigDecimal("100.00"), LedgerEntryType.EXPENSE_LAB, "Original"));

        String updateJson = String.format("""
                {
                    "zoneId": %d,
                    "contactId": %d,
                    "amount": 350.00,
                    "type": "INCOME_PERMIT",
                    "description": "Updated Permit Fee"
                }
                """, zone2.getId(), contact2.getId());

        mockMvc.perform(put("/api/ledger/update/" + entry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(350.00)))
                .andExpect(jsonPath("$.type", is("INCOME_PERMIT")))
                .andExpect(jsonPath("$.description", is("Updated Permit Fee")));

        mockMvc.perform(put("/api/ledger/update/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateLedgerEntry_InvalidZone() throws Exception {
        BeachZone zone = beachZoneRepo.save(new BeachZone("Beach 1", FlagStatus.GREEN, "BZ-1"));
        Contact contact = contactRepo.save(new Contact("Vendor 1", ContactType.VENDOR));
        LedgerEntry entry = ledgerEntryRepo.save(new LedgerEntry(zone.getId(), contact.getId(), new BigDecimal("100.00"), LedgerEntryType.EXPENSE_LAB, "Original"));

        String updateJson = String.format("""
                {
                    "zoneId": 9999,
                    "contactId": %d,
                    "amount": 350.00,
                    "type": "INCOME_PERMIT",
                    "description": "Updated"
                }
                """, contact.getId());

        mockMvc.perform(put("/api/ledger/update/" + entry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest());

        LedgerEntry unchanged = ledgerEntryRepo.findById(entry.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("100.00"), unchanged.getAmount());
        org.junit.jupiter.api.Assertions.assertEquals(zone.getId(), unchanged.getZoneId());
    }

    @Test
    void testUpdateLedgerEntry_InvalidContact() throws Exception {
        BeachZone zone = beachZoneRepo.save(new BeachZone("Beach 1", FlagStatus.GREEN, "BZ-1"));
        Contact contact = contactRepo.save(new Contact("Vendor 1", ContactType.VENDOR));
        LedgerEntry entry = ledgerEntryRepo.save(new LedgerEntry(zone.getId(), contact.getId(), new BigDecimal("100.00"), LedgerEntryType.EXPENSE_LAB, "Original"));

        String updateJson = String.format("""
                {
                    "zoneId": %d,
                    "contactId": 9999,
                    "amount": 350.00,
                    "type": "INCOME_PERMIT",
                    "description": "Updated"
                }
                """, zone.getId());

        mockMvc.perform(put("/api/ledger/update/" + entry.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest());

        LedgerEntry unchanged = ledgerEntryRepo.findById(entry.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("100.00"), unchanged.getAmount());
        org.junit.jupiter.api.Assertions.assertEquals(contact.getId(), unchanged.getContactId());
    }

    @Test
    void testDeleteLedgerEntry() throws Exception {
        LedgerEntry entry = ledgerEntryRepo.save(new LedgerEntry(1L, 1L, new BigDecimal("100.00"), LedgerEntryType.EXPENSE_LAB, "To Delete"));

        mockMvc.perform(delete("/api/ledger/delete/" + entry.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/ledger/delete/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetFinancialSummary_Empty() throws Exception {
        mockMvc.perform(get("/api/ledger/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome", is(0)))
                .andExpect(jsonPath("$.totalExpense", is(0)))
                .andExpect(jsonPath("$.netBalance", is(0)));
    }

    @Test
    void testGetFinancialSummary_WithData() throws Exception {
        ledgerEntryRepo.save(new LedgerEntry(1L, 1L, new BigDecimal("50000.00"), LedgerEntryType.INCOME_PERMIT, "Permit 1"));
        ledgerEntryRepo.save(new LedgerEntry(1L, 2L, new BigDecimal("10000.00"), LedgerEntryType.INCOME_PERMIT, "Permit 2"));
        ledgerEntryRepo.save(new LedgerEntry(1L, 3L, new BigDecimal("15000.00"), LedgerEntryType.EXPENSE_LAB, "Lab Test 1"));
        ledgerEntryRepo.save(new LedgerEntry(1L, 4L, new BigDecimal("5000.00"), LedgerEntryType.EXPENSE_LAB, "Lab Test 2"));

        mockMvc.perform(get("/api/ledger/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome", is(60000.00)))
                .andExpect(jsonPath("$.totalExpense", is(20000.00)))
                .andExpect(jsonPath("$.netBalance", is(40000.00)));
    }

    @Test
    void testGetFinancialSummaryByZone_Empty() throws Exception {
        mockMvc.perform(get("/api/ledger/summary/zone/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome", is(0)))
                .andExpect(jsonPath("$.totalExpense", is(0)))
                .andExpect(jsonPath("$.netBalance", is(0)));
    }

    @Test
    void testGetFinancialSummaryByZone_WithData() throws Exception {
        // Zone 1 entries
        ledgerEntryRepo.save(new LedgerEntry(1L, 1L, new BigDecimal("30000.00"), LedgerEntryType.INCOME_PERMIT, "Zone 1 Permit"));
        ledgerEntryRepo.save(new LedgerEntry(1L, 2L, new BigDecimal("10000.00"), LedgerEntryType.EXPENSE_LAB, "Zone 1 Lab"));

        // Zone 2 entries
        ledgerEntryRepo.save(new LedgerEntry(2L, 1L, new BigDecimal("50000.00"), LedgerEntryType.INCOME_PERMIT, "Zone 2 Permit"));
        ledgerEntryRepo.save(new LedgerEntry(2L, 2L, new BigDecimal("25000.00"), LedgerEntryType.EXPENSE_LAB, "Zone 2 Lab"));

        // Zone 1 summary: income = 30000, expense = 10000, balance = 20000
        mockMvc.perform(get("/api/ledger/summary/zone/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome", is(30000.00)))
                .andExpect(jsonPath("$.totalExpense", is(10000.00)))
                .andExpect(jsonPath("$.netBalance", is(20000.00)));

        // Zone 2 summary: income = 50000, expense = 25000, balance = 25000
        mockMvc.perform(get("/api/ledger/summary/zone/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome", is(50000.00)))
                .andExpect(jsonPath("$.totalExpense", is(25000.00)))
                .andExpect(jsonPath("$.netBalance", is(25000.00)));
    }
}

