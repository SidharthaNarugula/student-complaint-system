package com.example.compsysten.controller;

import com.example.compsysten.service.AIService;
import com.example.compsysten.service.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Exposes a single endpoint:  POST /api/ai/analyze
 *
 * Completely isolated from the existing complaint/auth/user logic.
 * The rate limiter is checked BEFORE calling the Gemini API.
 */
@RestController
@RequestMapping("/api/ai")
@Validated
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final AIService aiService;
    private final RateLimiter rateLimiter;

    public AIController(AIService aiService, RateLimiter rateLimiter) {
        this.aiService = aiService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * POST /api/ai/analyze
     * Body: { "text": "complaint text" }
     * Response: { "category": "...", "priority": "...", "source": "ai|fallback" }
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody Map<String, String> body) {
        String text = body.get("text");

        if (text == null || text.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Field 'text' must not be blank."));
        }

        // Rate limit check BEFORE hitting the API (per review feedback)
        if (!rateLimiter.tryAcquire()) {
            logger.warn("AI analyze rate limit exceeded");
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded. Please try again later."));
        }

        logger.info("AI analyze request received (text length: {})", text.length());
        Map<String, String> result = aiService.analyze(text);
        logger.info("AI analyze result: {}", result);

        return ResponseEntity.ok(result);
    }
}
