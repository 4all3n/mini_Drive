package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.services.AiCategorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AiControllerTest {

    private AiCategorizationService aiService;
    private AiController controller;

    @BeforeEach
    void setUp() {
        aiService = mock(AiCategorizationService.class);
        controller = new AiController(aiService);
    }

    @Test
    void testOnAiOnlineWhenServiceIsOnline() {
        when(aiService.isOnline()).thenReturn(true);
        when(aiService.recategorizeUncategorizedFiles()).thenReturn(4);

        ResponseEntity<?> response = controller.onAiOnline();
        assertEquals(200, response.getStatusCode().value());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals(true, body.get("aiOnline"));
        assertEquals(4, body.get("recategorizedCount"));
    }

    @Test
    void testOnAiOnlineWhenServiceIsOffline() {
        when(aiService.isOnline()).thenReturn(false);

        ResponseEntity<?> response = controller.onAiOnline();
        assertEquals(200, response.getStatusCode().value());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("aiOnline"));
        assertEquals(0, body.get("recategorizedCount"));
        verify(aiService, never()).recategorizeUncategorizedFiles();
    }

    @Test
    void testGetStatus() {
        when(aiService.isOnline()).thenReturn(true);
        when(aiService.getAiServiceUrl()).thenReturn("http://127.0.0.1:8000/predict");
        when(aiService.getHealthUrl()).thenReturn("http://127.0.0.1:8000/health");

        ResponseEntity<?> response = controller.getStatus();
        assertEquals(200, response.getStatusCode().value());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("online"));
        assertEquals("http://127.0.0.1:8000/predict", body.get("serviceUrl"));
        assertEquals("http://127.0.0.1:8000/health", body.get("healthUrl"));
    }
}
