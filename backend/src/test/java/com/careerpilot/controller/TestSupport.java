package com.careerpilot.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

final class TestSupport {

    private TestSupport() {
    }

    static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    /** Registers a fresh user and returns its JWT. */
    static String registerAndGetToken(MockMvc mockMvc, ObjectMapper mapper) throws Exception {
        String body = "{\"fullName\":\"Test User\",\"email\":\"" + uniqueEmail() + "\",\"password\":\"Password123\"}";
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(response);
        return json.path("data").path("token").asText();
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }
}
