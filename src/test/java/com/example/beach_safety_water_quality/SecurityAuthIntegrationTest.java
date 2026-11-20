package com.example.beach_safety_water_quality;

import com.example.beach_safety_water_quality.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SecurityAuthIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepo userRepo;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testPublicAuthRegister() throws Exception {
        String uniqueUser = "sec_user_" + System.currentTimeMillis();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + uniqueUser + "\",\"password\":\"pass123\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testUnauthenticatedApiAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/beach/zones/getAll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPublicStaticResourceRootAccess() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
