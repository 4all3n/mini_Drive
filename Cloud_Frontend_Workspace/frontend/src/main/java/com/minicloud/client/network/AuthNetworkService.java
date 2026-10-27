package com.minicloud.client.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthNetworkService {

    private static final String BASE_URL = ServerConfig.getBaseUrl() + "/api/auth";
    
    private final HttpClient httpClient;

    // Static JWT token storage — shared across the application session
    private static String jwtToken = null;

    public AuthNetworkService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Get the stored JWT token for use in authenticated requests.
     */
    public static String getToken() {
        return jwtToken;
    }

    /**
     * Clear the stored token (used on logout).
     */
    public static void clearToken() {
        jwtToken = null;
    }

    public boolean authenticateUser(String username, String password) {
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        String targetUrl = BASE_URL + "/login";
        
        System.out.println("DEBUG: Sending Login request to: " + targetUrl);
        System.out.println("DEBUG: Sending Login Payload: " + jsonPayload);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Server Response: " + response.statusCode() + " | " + response.body());

            if (response.statusCode() == 200) {
                // Parse JWT token from JSON response: {"token":"...","username":"...","message":"..."}
                String body = response.body();
                int tokenKeyIndex = body.indexOf("\"token\"");
                if (tokenKeyIndex != -1) {
                    int colonIndex = body.indexOf(":", tokenKeyIndex);
                    int valueStart = body.indexOf("\"", colonIndex + 1) + 1;
                    int valueEnd = body.indexOf("\"", valueStart);
                    if (valueStart > 0 && valueEnd > valueStart) {
                        jwtToken = body.substring(valueStart, valueEnd);
                        System.out.println("DEBUG: JWT Token stored successfully. Length: " + jwtToken.length());
                    }
                }
                return true;
            }
            return false;

        } catch (Exception e) {
            System.err.println("Network Error Check Spring Boot Server at: " + targetUrl);
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(String username, String password) {
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        String targetUrl = BASE_URL + "/register";
        
        System.out.println("DEBUG: Sending Register request to: " + targetUrl);
        System.out.println("DEBUG: Sending Register Payload: " + jsonPayload);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Server Response: " + response.statusCode() + " | " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.err.println("Network Error Check Spring Boot Server at: " + targetUrl);
            e.printStackTrace();
            return false;
        }
    }
}