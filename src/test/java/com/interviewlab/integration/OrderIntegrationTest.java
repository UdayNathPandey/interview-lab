package com.interviewlab.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void shouldCreateOrderEndToEnd() throws Exception {

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "customerName": "Integration User",
                            "customerEmail": "integration@gmail.com",
                            "amount": 5000,
                            "discount": 100
                        }
                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName")
                        .value("Integration User"))
                .andExpect(jsonPath("$.customerEmail")
                        .value("integration@gmail.com"))
                .andExpect(jsonPath("$.amount")
                        .value(4900));
    }

    @Test
    void shouldRejectInvalidOrder() throws Exception {

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "customerName": "",
                            "customerEmail": "integration@gmail.com",
                            "amount": 5000
                        }
                    """)
                )
                .andExpect(status().isBadRequest());
    }

}
