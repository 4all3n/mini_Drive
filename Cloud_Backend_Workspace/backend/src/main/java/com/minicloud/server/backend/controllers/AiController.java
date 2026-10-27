package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.services.AiCategorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller providing status, health, and notification hooks for the AI Microservice.
 * Allows the AI service to notify the backend as soon as it comes online,
 * immediately triggering the recategorization of files uploaded while offline.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiCategorizationService aiService;

    public AiController(AiCategorizationService aiService) {
        this.aiService = aiService;
    }

    /**
     * Webhook called by the AI microservice on startup to announce it is online.
     * Instantly triggers recategorization of any pending files.
     */
    @PostMapping("/online-notify")
    public ResponseEntity<?> onAiOnline() {
        boolean online = aiService.isOnline();
        int recategorized = 0;
        if (online) {
            recategorized = aiService.recategorizeUncategorizedFiles();
        }
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "aiOnline", online,
                "recategorizedCount", recategorized,
                "message", online
                        ? "AI microservice is online. Processed " + recategorized + " pending file(s)."
                        : "AI service could not be reached via health endpoint."
        ));
    }

    /**
     * Checks current connection status to the AI microservice.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        boolean online = aiService.isOnline();
        return ResponseEntity.ok(Map.of(
                "online", online,
                "serviceUrl", aiService.getAiServiceUrl(),
                "healthUrl", aiService.getHealthUrl()
        ));
    }

    /**
     * Explicit trigger to force recategorization of any uncategorized files.
     */
    @PostMapping("/recategorize")
    public ResponseEntity<?> triggerRecategorize() {
        boolean online = aiService.isOnline();
        if (!online) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "error",
                    "message", "AI microservice is currently offline."
            ));
        }
        int count = aiService.recategorizeUncategorizedFiles();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "recategorizedCount", count
        ));
    }
}
