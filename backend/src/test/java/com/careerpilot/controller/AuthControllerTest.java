package com.careerpilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void registerReturnsTokenAndRejectsDuplicateEmail() throws Exception {
        String email = TestSupport.uniqueEmail();
        String body = "{\"fullName\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"Password123\"}";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value(email));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void registerValidatesInput() throws Exception {
        String body = "{\"fullName\":\"\",\"email\":\"not-an-email\",\"password\":\"short\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginWorksWithCorrectPasswordAndFailsWithWrongOne() throws Exception {
        String email = TestSupport.uniqueEmail();
        String register = "{\"fullName\":\"Login User\",\"email\":\"" + email + "\",\"password\":\"Password123\"}";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(register))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"WrongPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireValidToken() throws Exception {
        mockMvc.perform(get("/api/jobs")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/jobs").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsProfileWithoutPassword() throws Exception {
        String token = TestSupport.registerAndGetToken(mockMvc, mapper);
        mockMvc.perform(get("/api/auth/me").header("Authorization", TestSupport.bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }
}
