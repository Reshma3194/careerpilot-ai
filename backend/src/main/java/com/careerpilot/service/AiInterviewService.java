package com.careerpilot.service;

import com.careerpilot.ai.AiPromptBuilder;
import com.careerpilot.ai.GeminiClient;
import com.careerpilot.dto.AnswerEvaluationRequest;
import com.careerpilot.dto.AnswerFeedback;
import com.careerpilot.dto.InterviewQuestion;
import com.careerpilot.dto.InterviewQuestionsResponse;
import com.careerpilot.dto.InterviewRequest;
import com.careerpilot.entity.Job;
import com.careerpilot.exception.AiServiceException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.JobRepository;
import com.careerpilot.repository.ResumeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiInterviewService {

    private static final int DEFAULT_COUNT = 8;
    private static final int MAX_COUNT = 15;

    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final GeminiClient geminiClient;
    private final AiPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public AiInterviewService(JobRepository jobRepository, ResumeRepository resumeRepository,
                              GeminiClient geminiClient, AiPromptBuilder promptBuilder,
                              ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.geminiClient = geminiClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    public InterviewQuestionsResponse generateQuestions(String email, InterviewRequest request) {
        Job job = jobRepository.findByIdAndUserEmail(request.jobId(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        String resumeText = null;
        if (request.resumeId() != null) {
            resumeText = resumeRepository.findByIdAndUserEmail(request.resumeId(), email)
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found"))
                    .getExtractedText();
        } else {
            resumeText = resumeRepository.findFirstByUserEmailOrderByUploadedAtDesc(email)
                    .map(r -> r.getExtractedText()).orElse(null);
        }

        int count = request.count() == null ? DEFAULT_COUNT : Math.max(3, Math.min(MAX_COUNT, request.count()));
        String json = geminiClient.generateJson(
                promptBuilder.interviewQuestions(job.getTitle(), job.getDescription(), resumeText, count));

        try {
            InterviewQuestionsResponse parsed = objectMapper.readValue(json, InterviewQuestionsResponse.class);
            List<InterviewQuestion> questions = parsed.questions() == null ? List.of() : parsed.questions();
            if (questions.isEmpty()) {
                throw new AiServiceException("The AI returned no questions. Please try again.");
            }
            return new InterviewQuestionsResponse(questions);
        } catch (JsonProcessingException ex) {
            throw new AiServiceException("The AI returned unreadable questions. Please try again.");
        }
    }

    public AnswerFeedback evaluateAnswer(String email, AnswerEvaluationRequest request) {
        String title = null;
        String description = null;
        if (request.jobId() != null) {
            Job job = jobRepository.findByIdAndUserEmail(request.jobId(), email)
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
            title = job.getTitle();
            description = job.getDescription();
        }

        String json = geminiClient.generateJson(
                promptBuilder.evaluateAnswer(title, description, request.question(), request.answer()));

        try {
            AnswerFeedback f = objectMapper.readValue(json, AnswerFeedback.class);
            return new AnswerFeedback(
                    Math.max(0, Math.min(10, f.score())),
                    f.feedback() == null ? "" : f.feedback(),
                    f.strengths() == null ? List.of() : f.strengths(),
                    f.improvements() == null ? List.of() : f.improvements(),
                    f.sampleAnswer() == null ? "" : f.sampleAnswer());
        } catch (JsonProcessingException ex) {
            throw new AiServiceException("The AI returned unreadable feedback. Please try again.");
        }
    }
}
