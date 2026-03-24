package com.minicloud.client.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthNetworkService {

    private static final String BASE_URL = "http://localhost:8080/api/auth";
    
    private final HttpClient httpClient;

    public AuthNetworkService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean authenticateUser(String username, String password) {
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        
        System.out.println("DEBUG: Sending Login Payload: " + jsonPayload);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Server Response: " + response.statusCode() + " | " + response.body());

            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("Network Error Check Spring Boot Server.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(String username, String password) {
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        
        System.out.println("Sending Register Payload: " + jsonPayload);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Server Response: " + response.statusCode() + " | " + response.body());

            return response.statusCode() == 200 || response.statusCode() == 201;

        } catch (Exception e) {
            System.err.println("Network Error Check Spring Boot Server.");
            e.printStackTrace();
            return false;
        }
    }
}