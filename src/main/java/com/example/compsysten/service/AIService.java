package com.example.compsysten.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Calls Google Gemini 1.5 Flash to categorize a student complaint
 * and assign a priority level.
 *
 * On any failure the service returns a fallback response so the
 * complaint-filing flow is never blocked.
 */
@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final String PROMPT_TEMPLATE =
            """
            Categorize this student complaint and assign priority.

            Categories:
            - Academic
            - Hostel
            - Facilities
            - Administration
            - Other

            Priority:
            - Low
            - Medium
            - High
            - Urgent

            Return ONLY valid JSON in this exact format with no extra text, no markdown:
            {
              "category": "...",
              "priority": "..."
            }

            Complaint:
            """;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Analyzes complaint text and returns category + priority.
     *
     * @param complaintText the raw complaint text
     * @return map with "category", "priority", and "source" keys
     */
    public Map<String, String> analyze(String complaintText) {
        try {
            String prompt = PROMPT_TEMPLATE + complaintText;

            // Build Gemini request body
            String requestBody = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {
                              "text": %s
                            }
                          ]
                        }
                      ]
                    }
                    """.formatted(toJsonString(prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    GEMINI_URL + apiKey,
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String text = extractTextFromGeminiResponse((Map<?, ?>) response.getBody());
                logger.info("Gemini raw response text: {}", text);
                return parseAnalysis(text);
            }

        } catch (Exception e) {
            logger.warn("Gemini API call failed, returning fallback. Reason: {}", e.getMessage());
        }

        return fallback();
    }

    /**
     * Navigates the nested Gemini response structure:
     * candidates[0].content.parts[0].text
     */
    private String extractTextFromGeminiResponse(Map<?, ?> body) {
        try {
            java.util.List<?> candidates = (java.util.List<?>) body.get("candidates");
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
            java.util.List<?> parts = (java.util.List<?>) content.get("parts");
            Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
            return (String) firstPart.get("text");
        } catch (Exception e) {
            logger.warn("Failed to extract text from Gemini response structure: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses the JSON text returned by Gemini into a result map.
     * Uses simple string extraction to avoid pulling in a JSON library.
     */
    private Map<String, String> parseAnalysis(String text) {
        if (text == null || text.isBlank()) {
            return fallback();
        }

        try {
            // Strip markdown code fences if Gemini wraps in ```json ... ```
            String cleaned = text.replaceAll("```json", "").replaceAll("```", "").trim();

            String category = extractJsonValue(cleaned, "category");
            String priority  = extractJsonValue(cleaned, "priority");

            if (isValidCategory(category) && isValidPriority(priority)) {
                return Map.of("category", category, "priority", priority, "source", "ai");
            }
        } catch (Exception e) {
            logger.warn("Failed to parse Gemini JSON response: {}", e.getMessage());
        }

        return fallback();
    }

    /** Pulls a string value for a given key from a simple flat JSON string. */
    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return null;

        int colonIdx = json.indexOf(':', keyIdx);
        int startQuote = json.indexOf('"', colonIdx);
        int endQuote = json.indexOf('"', startQuote + 1);

        if (startQuote == -1 || endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote).trim();
    }

    private boolean isValidCategory(String v) {
        return v != null && java.util.Set.of(
                "Academic", "Hostel", "Facilities", "Administration", "Other"
        ).contains(v);
    }

    private boolean isValidPriority(String v) {
        return v != null && java.util.Set.of(
                "Low", "Medium", "High", "Urgent"
        ).contains(v);
    }

    private Map<String, String> fallback() {
        return Map.of("category", "Other", "priority", "Medium", "source", "fallback");
    }

    /** Escapes and wraps a string for safe embedding in a JSON value. */
    private String toJsonString(String raw) {
        String escaped = raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }
}
