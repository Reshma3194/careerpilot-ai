package com.careerpilot.controller;

import com.careerpilot.dto.AnswerEvaluationRequest;
import com.careerpilot.dto.AnswerFeedback;
import com.careerpilot.dto.ApiResponse;
import com.careerpilot.dto.InterviewQuestionsResponse;
import com.careerpilot.dto.InterviewRequest;
import com.careerpilot.dto.ResumeAnalysisRequest;
import com.careerpilot.dto.ResumeAnalysisResponse;
import com.careerpilot.service.AiInterviewService;
import com.careerpilot.service.ResumeAnalysisService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ResumeAnalysisService resumeAnalysisService;
    private final AiInterviewService aiInterviewService;

    public AiController(ResumeAnalysisService resumeAnalysisService, AiInterviewService aiInterviewService) {
        this.resumeAnalysisService = resumeAnalysisService;
        this.aiInterviewService = aiInterviewService;
    }

    @PostMapping("/resume-analysis")
    public ApiResponse<ResumeAnalysisResponse> analyzeResume(Authentication auth,
                                                             @RequestBody ResumeAnalysisRequest request) {
        return ApiResponse.ok(resumeAnalysisService.analyze(auth.getName(), request));
    }

    @PostMapping("/interview-questions")
    public ApiResponse<InterviewQuestionsResponse> interviewQuestions(Authentication auth,
                                                                      @Valid @RequestBody InterviewRequest request) {
        return ApiResponse.ok(aiInterviewService.generateQuestions(auth.getName(), request));
    }

    @PostMapping("/evaluate-answer")
    public ApiResponse<AnswerFeedback> evaluateAnswer(Authentication auth,
                                                      @Valid @RequestBody AnswerEvaluationRequest request) {
        return ApiResponse.ok(aiInterviewService.evaluateAnswer(auth.getName(), request));
    }
}
