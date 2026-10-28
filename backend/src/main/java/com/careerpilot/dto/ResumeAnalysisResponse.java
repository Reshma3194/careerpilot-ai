package com.careerpilot.dto;

import java.util.List;

public record ResumeAnalysisResponse(
        int matchScore,
        String summary,
        List<String> strengths,
        List<String> gaps,
        List<String> missingKeywords,
        List<String> suggestions) {
}
