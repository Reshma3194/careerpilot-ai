package com.careerpilot.dto;

import java.util.List;

public record AnswerFeedback(
        int score,
        String feedback,
        List<String> strengths,
        List<String> improvements,
        String sampleAnswer) {
}
