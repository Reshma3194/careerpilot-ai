package com.careerpilot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class JobTrackerFlowTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    private long createJob(String token) throws Exception {
        String response = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", TestSupport.bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Java Developer\",\"company\":\"Acme\",\"location\":\"Hyderabad\","
                                + "\"description\":\"Spring Boot, REST, SQL\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Java Developer"))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).path("data").path("id").asLong();
    }

    @Test
    void jobAndApplicationLifecycle() throws Exception {
        String token = TestSupport.registerAndGetToken(mockMvc, mapper);
        String auth = TestSupport.bearer(token);
        long jobId = createJob(token);

        mockMvc.perform(get("/api/jobs").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // track the job
        String appResponse = mockMvc.perform(post("/api/applications")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":" + jobId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SAVED"))
                .andExpect(jsonPath("$.data.job.company").value("Acme"))
                .andReturn().getResponse().getContentAsString();
        long appId = mapper.readTree(appResponse).path("data").path("id").asLong();

        // adding the same job twice is a conflict
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":" + jobId + "}"))
                .andExpect(status().isConflict());

        // move to APPLIED
        mockMvc.perform(patch("/api/applications/" + appId)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPLIED"))
                .andExpect(jsonPath("$.data.appliedDate").isNotEmpty());

        mockMvc.perform(get("/api/applications/stats").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.APPLIED").value(1))
                .andExpect(jsonPath("$.data.SAVED").value(0))
                .andExpect(jsonPath("$.data.TOTAL").value(1));

        mockMvc.perform(get("/api/applications?status=APPLIED").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/applications?status=NOPE").header("Authorization", auth))
                .andExpect(status().isBadRequest());

        // deleting the job removes its application too
        mockMvc.perform(delete("/api/jobs/" + jobId).header("Authorization", auth))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/applications").header("Authorization", auth))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void usersCannotSeeEachOthersJobs() throws Exception {
        String owner = TestSupport.registerAndGetToken(mockMvc, mapper);
        String other = TestSupport.registerAndGetToken(mockMvc, mapper);
        long jobId = createJob(owner);

        mockMvc.perform(get("/api/jobs/" + jobId).header("Authorization", TestSupport.bearer(other)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/jobs").header("Authorization", TestSupport.bearer(other)))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void jobValidationRejectsMissingTitle() throws Exception {
        String token = TestSupport.registerAndGetToken(mockMvc, mapper);
        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", TestSupport.bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"company\":\"Acme\"}"))
                .andExpect(status().isBadRequest());
    }
}
