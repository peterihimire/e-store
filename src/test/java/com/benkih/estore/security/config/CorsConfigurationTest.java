package com.benkih.estore.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {"cors.allowed-origins=https://shop.example.com"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsConfigurationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void allowsPreflightFromDevOriginOnProtectedPath() throws Exception {
        mockMvc.perform(
                        options("/api/v1/orders/order/user")
                                .header("Origin", "http://localhost:5173")
                                .header("Access-Control-Request-Method", "GET")
                                .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }

    @Test
    void allowsOriginFromEnvironmentProperty() throws Exception {
        mockMvc.perform(
                        options("/api/v1/orders/order/user")
                                .header("Origin", "https://shop.example.com")
                                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(
                        header().string("Access-Control-Allow-Origin", "https://shop.example.com"));
    }

    @Test
    void rejectsUnknownOrigin() throws Exception {
        mockMvc.perform(
                        options("/api/v1/orders/order/user")
                                .header("Origin", "https://evil.example.com")
                                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void addsCorsHeadersToActualResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
