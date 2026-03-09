package com.minicloud.client.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthNetworkService {

    // The base URL of your partner's Spring Boot server. 
    // For Review 1, assuming both run on localhost for testing, or use their IP address.
    private static final String BASE_URL = "http://localhost:8080/api/auth";
    
    // Create a single, reusable HttpClient
    private final HttpClient httpClient;

    public AuthNetworkService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Method to handle user login
    public boolean authenticateUser(String username, String password) {
        // 1. Manually format the JSON payload (keeps Review 1 simple without extra libraries)
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        
        System.out.println("DEBUG: Sending Login Payload -> " + jsonPayload);

        try {
            // 2. Build the HTTP POST request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // 3. Send the request and get the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DEBUG: Server Response -> " + response.statusCode() + " | " + response.body());

            // 4. Return true if the server says OK (200), false otherwise
            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("DEBUG: Network Error - Is the Spring Boot server running?");
            e.printStackTrace();
            return false;
        }
    }

    // Method to handle user registration
    public boolean registerUser(String username, String password) {
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        
        System.out.println("DEBUG: Sending Register Payload -> " + jsonPayload);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("DEBUG: Server Response -> " + response.statusCode() + " | " + response.body());

            // 201 Created or 200 OK means success
            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.err.println("DEBUG: Network Error - Is the Spring Boot server running?");
            e.printStackTrace();
            return false;
        }
    }
}