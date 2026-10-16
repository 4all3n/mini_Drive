package com.minicloud.server.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service that acts as a bridge between the Spring Boot backend and the FastAPI AI microservice
 * to automatically analyze and categorize uploaded files.
 */
@Service
public class AiCategorizationService {

    private final HttpClient httpClient;
    private final String aiServiceUrl;

    /**
     * Constructs the AiCategorizationService with constructor-injected AI service endpoint URL.
     *
     * @param aiServiceUrl the endpoint URL of the FastAPI AI categorization microservice
     */
    public AiCategorizationService(@Value("${ai.service.url}") String aiServiceUrl) {
        this.aiServiceUrl = aiServiceUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Sends the file name to the AI microservice for classification and returns the category.
     * Falls back to "Uncategorized" if the service is unreachable or an error occurs.
     *
     * @param fileName the name of the file to categorize
     * @return predicted category or "Uncategorized" on failure
     */
    public String analyzeAndCategorize(String fileName) {
        try {
            String jsonBody = "{\"filename\": \"" + fileName + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiServiceUrl))
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