package com.careerpilot.service;

import com.careerpilot.ai.AiPromptBuilder;
import com.careerpilot.ai.GeminiClient;
import com.careerpilot.dto.ResumeAnalysisRequest;
import com.careerpilot.dto.ResumeAnalysisResponse;
import com.careerpilot.entity.Job;
import com.careerpilot.entity.Resume;
import com.careerpilot.exception.AiServiceException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.ApplicationRepository;
import com.careerpilot.repository.JobRepository;
import com.careerpilot.repository.ResumeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final GeminiClient geminiClient;
    private final AiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public ResumeAnalysisService(ResumeRepository resumeRepository, JobRepository jobRepository,
                                 ApplicationRepository applicationRepository, GeminiClient geminiClient,
                                 AiPromptBuilder promptBuilder, ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.geminiClient = geminiClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    // Deliberately not @Transactional: we don't want to hold a DB connection during the AI call.
    public ResumeAnalysisResponse analyze(String email, ResumeAnalysisRequest request) {
        Resume resume = request.resumeId() != null
                ? resumeRepository.findByIdAndUserEmail(request.resumeId(), email)
                        .orElseThrow(() -> new ResourceNotFoundException("Resume not found"))
                : resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Upload a resume first"));

        Job job = null;
        String title;
        String description;
        if (request.jobId() != null) {
            job = jobRepository.findByIdAndUserEmail(request.jobId(), email)
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
            title = job.getTitle();
            description = job.getDescription();
        } else {
            title = request.jobTitle();
            description = request.jobDescription();
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Provide a jobId with a description, or paste a jobDescription");
        }

        String json = geminiClient.generateJson(promptBuilder.resumeAnalysis(resume.getExtractedText(), title, description));
        ResumeAnalysisResponse result = normalize(parse(json));

        if (job != null) {
            int score = result.matchScore();
            applicationRepository.findByJobIdAndUserEmail(job.getId(), email).ifPresent(application -> {
                application.setMatchScore(score);
                applicationRepository.save(application);
            });
        }
        return result;
    }

    private ResumeAnalysisResponse parse(String json) {
        try {
            return objectMapper.readValue(json, ResumeAnalysisResponse.class);
        } catch (JsonProcessingException ex) {
            throw new AiServiceException("The AI returned an unreadable analysis. Please try again.");
        }
    }

    private ResumeAnalysisResponse normalize(ResumeAnalysisResponse r) {
        return new ResumeAnalysisResponse(
                Math.max(0, Math.min(100, r.matchScore())),
                r.summary() == null ? "" : r.summary(),
                orEmpty(r.strengths()),
                orEmpty(r.gaps()),
                orEmpty(r.missingKeywords()),
                orEmpty(r.suggestions()));
    }

    private static List<String> orEmpty(List<String> list) {
        return list == null ? List.of() : list;
    }
}
