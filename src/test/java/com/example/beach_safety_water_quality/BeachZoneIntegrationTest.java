package com.example.beach_safety_water_quality;

import com.example.beach_safety_water_quality.model.BeachZone;
import com.example.beach_safety_water_quality.model.FlagStatus;
import com.example.beach_safety_water_quality.repo.BeachZoneRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class BeachZoneIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BeachZoneRepo beachZoneRepo;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        beachZoneRepo.deleteAll();
    }

    @Test
    void testCreateBeachZone_WithDefaultFlagStatus() throws Exception {
        String requestJson = """
                {
                    "name": "North Shore Beach",
                    "analyticAccountCode": "BZ-NS-001"
                }
                """;

        mockMvc.perform(post("/api/beach/zones/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("North Shore Beach")))
                .andExpect(jsonPath("$.currentFlagStatus", is("GREEN")))
                .andExpect(jsonPath("$.analyticAccountCode", is("BZ-NS-001")));
    }

    @Test
    void testCreateBeachZone_WithExplicitFlagStatus() throws Exception {
        String requestJson = """
                {
                    "name": "South Bay Beach",
                    "currentFlagStatus": "RED",
                    "analyticAccountCode": "BZ-SB-002"
                }
                """;

        mockMvc.perform(post("/api/beach/zones/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("South Bay Beach")))
                .andExpect(jsonPath("$.currentFlagStatus", is("RED")))
                .andExpect(jsonPath("$.analyticAccountCode", is("BZ-SB-002")));
    }

    @Test
    void testGetAllBeachZones() throws Exception {
        BeachZone zone1 = new BeachZone("Zone 1", FlagStatus.GREEN, "Z-001");
        BeachZone zone2 = new BeachZone("Zone 2", FlagStatus.RED, "Z-002");
        beachZoneRepo.save(zone1);
        beachZoneRepo.save(zone2);

        mockMvc.perform(get("/api/beach/zones/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Zone 1")))
                .andExpect(jsonPath("$[1].name", is("Zone 2")));
    }

    @Test
    void testGetBeachZoneById_Success() throws Exception {
        BeachZone saved = beachZoneRepo.save(new BeachZone("Central Beach", FlagStatus.GREEN, "BZ-CB-003"));

        mockMvc.perform(get("/api/beach/zones/get/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Central Beach")))
                .andExpect(jsonPath("$.currentFlagStatus", is("GREEN")))
                .andExpect(jsonPath("$.analyticAccountCode", is("BZ-CB-003")));
    }

    @Test
    void testGetBeachZoneById_NotFound() throws Exception {
        mockMvc.perform(get("/api/beach/zones/get/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateBeachZone_Success() throws Exception {
        BeachZone saved = beachZoneRepo.save(new BeachZone("Original Name", FlagStatus.GREEN, "BZ-UP-004"));

        String updateJson = """
                {
                    "name": "Updated Name",
                    "currentFlagStatus": "RED",
                    "analyticAccountCode": "BZ-UP-004-MOD"
                }
                """;

        mockMvc.perform(put("/api/beach/zones/update/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.currentFlagStatus", is("RED")))
                .andExpect(jsonPath("$.analyticAccountCode", is("BZ-UP-004-MOD")));
    }

    @Test
    void testUpdateBeachZone_NotFound() throws Exception {
        String updateJson = """
                {
                    "name": "Non Existent",
                    "currentFlagStatus": "GREEN",
                    "analyticAccountCode": "NON-EXISTENT"
                }
                """;

        mockMvc.perform(put("/api/beach/zones/update/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBeachZone_Success() throws Exception {
        BeachZone saved = beachZoneRepo.save(new BeachZone("To Be Deleted", FlagStatus.GREEN, "BZ-DEL-005"));

        mockMvc.perform(delete("/api/beach/zones/delete/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/beach/zones/get/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBeachZone_NotFound() throws Exception {
        mockMvc.perform(delete("/api/beach/zones/delete/99999"))
                .andExpect(status().isNotFound());
    }
}
