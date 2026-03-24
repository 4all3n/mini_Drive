package com.minicloud.client.network;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;

public class FileNetworkService {

    // Remember to change localhost to Laptop 2's IP address when testing across devices!
    private static final String BASE_URL = "http://localhost:8080/api/files";
    private final HttpClient httpClient;

    public FileNetworkService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(15)) // Files take longer to send, so we increase the timeout
                .build();
    }

    public String uploadFile(File file, String username) {
        try {
            String boundary = "---MiniCloudBoundary" + System.currentTimeMillis();
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] body = buildMultipartBody(file.getName(), fileBytes, username, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/upload"))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return "SUCCESS";
            } else {
                // If the server sends an error (like our 413 Payload Too Large), return that exact message
                return response.body(); 
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to upload file. Server might be offline.";
        }
    }

    // A helper method to construct the raw HTTP network packet
    private byte[] buildMultipartBody(String filename, byte[] fileBytes, String username, String boundary) throws IOException {
        String crlf = "\r\n";
        String twoHyphens = "--";

        // Part 1: The Username
        StringBuilder builder = new StringBuilder();
        builder.append(twoHyphens).append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"username\"").append(crlf).append(crlf);
        builder.append(username).append(crlf);

        // Part 2: The File Header
        builder.append(twoHyphens).append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"").append(crlf);
        builder.append("Content-Type: application/octet-stream").append(crlf).append(crlf);

        // Combine string parts and binary file parts into one giant byte array
        byte[] textPart1 = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] closingBoundary = (crlf + twoHyphens + boundary + twoHyphens + crlf).getBytes(StandardCharsets.UTF_8);

        byte[] finalBody = new byte[textPart1.length + fileBytes.length + closingBoundary.length];
        System.arraycopy(textPart1, 0, finalBody, 0, textPart1.length);
        System.arraycopy(fileBytes, 0, finalBody, textPart1.length, fileBytes.length);
        System.arraycopy(closingBoundary, 0, finalBody, textPart1.length + fileBytes.length, closingBoundary.length);

        return finalBody;
    }

    public java.util.List<com.minicloud.client.ui.DashboardScreen.FileRecord> fetchUserFiles(String username) {
        java.util.List<com.minicloud.client.ui.DashboardScreen.FileRecord> fileList = new java.util.ArrayList<>();
        try {
            // 1. Ask the server for the file list
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/list?username=" + username))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 2. If the server responds with data, chop up the string to rebuild our Java UI objects
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                String[] files = response.body().split(";;;");
                for (String f : files) {
                    if (f.isEmpty()) continue;
                    String[] parts = f.split("\\|");
                    
                    String name = parts.length > 0 ? parts[0] : "Unknown";
                    String type = parts.length > 1 ? parts[1] : "Unknown";
                    String size = parts.length > 2 ? formatFileSize(Long.parseLong(parts[2])) : "0 B";
                    
                    fileList.add(new com.minicloud.client.ui.DashboardScreen.FileRecord(name, type, size));
                }
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Failed to fetch file list.");
            e.printStackTrace();
        }
        return fileList;
    }

    // A helper method to make the byte numbers look pretty (e.g., 1048576 becomes "1.0 MB")
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}