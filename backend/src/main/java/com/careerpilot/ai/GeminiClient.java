package com.careerpilot.ai;

import com.careerpilot.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** Thin wrapper around the Gemini generateContent REST endpoint. */
@Component
public class GeminiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(@Qualifier("geminiRestClient") RestClient restClient,
                        @Value("${app.gemini.api-key:}") String apiKey,
                        @Value("${app.gemini.model}") String model) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    /** Sends the prompt and returns the model's reply, requested (and cleaned) as raw JSON text. */
    public String generateJson(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException(
                    "Gemini API key is not configured. Set the GEMINI_API_KEY environment variable.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "responseMimeType", "application/json"));

        try {
            JsonNode response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode text = response == null ? null
                    : response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (text == null || text.isMissingNode() || text.asText().isBlank()) {
                throw new AiServiceException("The AI model returned an empty response");
            }
            return stripCodeFences(text.asText());
        } catch (RestClientResponseException ex) {
            throw new AiServiceException("The AI service rejected the request (HTTP "
                    + ex.getStatusCode().value() + ")");
        } catch (RestClientException ex) {
            throw new AiServiceException("Could not reach the AI service");
        }
    }

    static String stripCodeFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}
