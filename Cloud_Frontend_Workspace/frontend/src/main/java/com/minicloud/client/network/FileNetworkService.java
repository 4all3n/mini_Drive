package com.minicloud.client.network;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FileNetworkService {

    private static final String BASE_URL = "http://localhost:8080/api/files";
    private final HttpClient httpClient;

    public FileNetworkService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(15))
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
                return response.body(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to server.";
        }
    }

    public List<com.minicloud.client.ui.DashboardScreen.FileRecord> fetchUserFiles(String username) {
        List<com.minicloud.client.ui.DashboardScreen.FileRecord> fileList = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/list?username=" + username))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                String[] files = response.body().split(";;;");
                for (String f : files) {
                    if (f.isEmpty()) continue;
                    String[] parts = f.split("\\|");
                    
                    Long id = parts.length > 0 ? Long.parseLong(parts[0]) : 0L;
                    String name = parts.length > 1 ? parts[1] : "Unknown";
                    String type = parts.length > 2 ? parts[2] : "Unknown";
                    String size = parts.length > 3 ? formatFileSize(Long.parseLong(parts[3])) : "0 B";
                    
                    // --- GRAB THE AI CATEGORY ---
                    String category = parts.length > 4 ? parts[4] : "Uncategorized";
                    
                    fileList.add(new com.minicloud.client.ui.DashboardScreen.FileRecord(id, name, type, size, category));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public boolean downloadFile(Long fileId, String fileName, File destinationFolder) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/download/" + fileId))
                    .GET()
                    .build();

            Path destinationFile = new File(destinationFolder, fileName).toPath();
            HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(destinationFile));
            
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFile(Long fileId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/delete/" + fileId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private byte[] buildMultipartBody(String filename, byte[] fileBytes, String username, String boundary) throws IOException {
        String crlf = "\r\n";
        String twoHyphens = "--";

        StringBuilder builder = new StringBuilder();
        builder.append(twoHyphens).append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"username\"").append(crlf).append(crlf);
        builder.append(username).append(crlf);

        builder.append(twoHyphens).append(boundary).append(crlf);
        builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"").append(crlf);
        builder.append("Content-Type: application/octet-stream").append(crlf).append(crlf);

        byte[] textPart1 = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] closingBoundary = (crlf + twoHyphens + boundary + twoHyphens + crlf).getBytes(StandardCharsets.UTF_8);

        byte[] finalBody = new byte[textPart1.length + fileBytes.length + closingBoundary.length];
        System.arraycopy(textPart1, 0, finalBody, 0, textPart1.length);
        System.arraycopy(fileBytes, 0, finalBody, textPart1.length, fileBytes.length);
        System.arraycopy(closingBoundary, 0, finalBody, textPart1.length + fileBytes.length, closingBoundary.length);

        return finalBody;
    }
}