package com.cpa.yusin.quiz.common.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthCheckControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HealthCheckController controller = new HealthCheckController();
        ReflectionTestUtils.setField(controller, "env", "test");
        ReflectionTestUtils.setField(controller, "serverPort", "8080");
        ReflectionTestUtils.setField(controller, "serverAddress", "127.0.0.1");
        ReflectionTestUtils.setField(controller, "serverName", "yusin-quiz");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void healthCheckShouldReturnServerMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/hc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.env").value("test"))
                .andExpect(jsonPath("$.serverPort").value("8080"))
                .andExpect(jsonPath("$.serverAddress").value("127.0.0.1"))
                .andExpect(jsonPath("$.serverName").value("yusin-quiz"));
    }

    @Test
    void envShouldReturnCurrentEnvironment() throws Exception {
        mockMvc.perform(get("/api/v1/env"))
                .andExpect(status().isOk())
                .andExpect(content().string("test"));
    }
}
