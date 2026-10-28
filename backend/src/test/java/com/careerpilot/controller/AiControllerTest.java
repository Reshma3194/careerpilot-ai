package com.careerpilot.controller;

import com.careerpilot.ai.GeminiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exercises resume upload + the AI endpoints with Gemini mocked out (no API key needed). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    GeminiClient geminiClient;

    private void uploadResume(String auth) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cv.txt", "text/plain",
                "Jane Doe. Java, Spring Boot, PostgreSQL, REST APIs. Built a job tracker.".getBytes());
        mockMvc.perform(multipart("/api/resumes").file(file).header("Authorization", auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.fileName").value("cv.txt"))
                .andExpect(jsonPath("$.data.extractedText").doesNotExist());
    }

    @Test
    void analyzesResumeAgainstPastedJobDescription() throws Exception {
        String auth = TestSupport.bearer(TestSupport.registerAndGetToken(mockMvc, mapper));
        uploadResume(auth);

        when(geminiClient.generateJson(anyString())).thenReturn(
                "{\"matchScore\":140,\"summary\":\"Good fit\",\"strengths\":[\"Java\"],"
                        + "\"gaps\":[\"Docker\"],\"missingKeywords\":[\"Kubernetes\"],\"suggestions\":[\"Add metrics\"]}");

        mockMvc.perform(post("/api/ai/resume-analysis")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Backend Dev\",\"jobDescription\":\"Java, Spring, Docker\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matchScore").value(100)) // clamped from 140
                .andExpect(jsonPath("$.data.strengths[0]").value("Java"));
    }

    @Test
    void analysisWithoutResumeIsNotFound() throws Exception {
        String auth = TestSupport.bearer(TestSupport.registerAndGetToken(mockMvc, mapper));
        mockMvc.perform(post("/api/ai/resume-analysis")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobDescription\":\"Java\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void malformedAiReplyBecomesBadGateway() throws Exception {
        String auth = TestSupport.bearer(TestSupport.registerAndGetToken(mockMvc, mapper));
        uploadResume(auth);
        when(geminiClient.generateJson(anyString())).thenReturn("this is not json");

        mockMvc.perform(post("/api/ai/resume-analysis")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobDescription\":\"Java\"}"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void generatesInterviewQuestionsAndEvaluatesAnswers() throws Exception {
        String auth = TestSupport.bearer(TestSupport.registerAndGetToken(mockMvc, mapper));

        String jobResponse = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Backend Dev\",\"company\":\"Acme\",\"description\":\"Java, Spring\"}"))
                .andReturn().getResponse().getContentAsString();
        long jobId = mapper.readTree(jobResponse).path("data").path("id").asLong();

        when(geminiClient.generateJson(anyString())).thenReturn(
                "{\"questions\":[{\"question\":\"Explain dependency injection\",\"category\":\"technical\"}]}");
        mockMvc.perform(post("/api/ai/interview-questions")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobId\":" + jobId + ",\"count\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions[0].category").value("technical"));

        when(geminiClient.generateJson(anyString())).thenReturn(
                "{\"score\":7,\"feedback\":\"Solid\",\"strengths\":[\"Clear\"],"
                        + "\"improvements\":[\"Add example\"],\"sampleAnswer\":\"DI is...\"}");
        mockMvc.perform(post("/api/ai/evaluate-answer")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Explain DI\",\"answer\":\"Objects get dependencies injected\","
                                + "\"jobId\":" + jobId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.score").value(7));
    }

    @Test
    void rejectsUnsupportedResumeFileType() throws Exception {
        String auth = TestSupport.bearer(TestSupport.registerAndGetToken(mockMvc, mapper));
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "x".getBytes());
        mockMvc.perform(multipart("/api/resumes").file(file).header("Authorization", auth))
                .andExpect(status().isBadRequest());
    }
}
