package com.minicloud.server.backend.services;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class AiCategorizationService {

    private final HttpClient httpClient;
    private final String PYTHON_AI_URL = "http://127.0.0.1:8000/predict";

    public AiCategorizationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public String analyzeAndCategorize(String fileName) {
        try {
            String jsonBody = "{\"filename\": \"" + fileName + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PYTHON_AI_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Proper JSON key extraction — finds "category":"<value>" in the response
                // Handles cases where the category value might contain special characters
                String body = response.body();
                int keyIndex = body.indexOf("\"category\"");
                if (keyIndex != -1) {
                    int colonIndex = body.indexOf(":", keyIndex);
                    int valueStart = body.indexOf("\"", colonIndex + 1) + 1;
                    int valueEnd = body.indexOf("\"", valueStart);
                    if (valueStart > 0 && valueEnd > valueStart) {
                        return body.substring(valueStart, valueEnd).trim();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("WARNING: Python AI Server is offline. Falling back to default.");
        }
        
        return "Uncategorized"; 
    }
}