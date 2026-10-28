package com.careerpilot.dto;

/**
 * Either jobId (a tracked job) or jobDescription (pasted text) must be given.
 * resumeId is optional; the most recently uploaded resume is used when omitted.
 */
public record ResumeAnalysisRequest(
        Long resumeId,
        Long jobId,
        String jobTitle,
        String jobDescription) {
}
