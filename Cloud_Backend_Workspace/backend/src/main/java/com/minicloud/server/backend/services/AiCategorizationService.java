package com.minicloud.server.backend.services;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.models.User;
import com.minicloud.server.backend.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service that acts as a bridge between the Spring Boot backend and the FastAPI AI microservice
 * to automatically analyze and categorize uploaded files.
 *
 * Includes self-healing offline recovery:
 * When the AI microservice is offline, files are safely marked as "Uncategorized".
 * Once the AI model comes online, it automatically discovers and categorizes all pending files
 * that were uploaded while it was offline.
 */
@Service
public class AiCategorizationService {

    private final HttpClient httpClient;
    private final String aiServiceUrl;
    private final String healthUrl;
    private final FileRepository fileRepository;

    /**
     * Constructs the AiCategorizationService with constructor-injected AI service endpoint URL
     * and FileRepository for offline file reconciliation.
     *
     * @param aiServiceUrl   the endpoint URL of the FastAPI AI categorization microservice
     * @param fileRepository repository for reading and updating file metadata
     */
    public AiCategorizationService(@Value("${ai.service.url:http://127.0.0.1:8000/predict}") String aiServiceUrl,
                                   FileRepository fileRepository) {
        this.aiServiceUrl = aiServiceUrl;
        this.healthUrl = aiServiceUrl.contains("/predict")
                ? aiServiceUrl.replace("/predict", "/health")
                : (aiServiceUrl.endsWith("/") ? aiServiceUrl + "health" : aiServiceUrl + "/health");
        this.fileRepository = fileRepository;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Checks if the AI microservice is currently reachable and responding with HTTP 200.
     *
     * @return true if AI service is online, false otherwise
     */
    public boolean isOnline() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
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
            String jsonBody = "{\"filename\": \"" + escapeJson(fileName) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiServiceUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(4))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
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
            System.out.println("[AI Service] AI model is offline during upload of '" + fileName
                    + "'. Assigned 'Uncategorized'. Will be categorized automatically once AI comes online.");
        }

        return "Uncategorized";
    }

    /**
     * Finds all files in the database that were uploaded while the AI model was offline
     * (category is "Uncategorized", null, or blank) and queries the online AI model
     * to determine and update their categories.
     *
     * @return the number of files successfully categorized and updated
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recategorizeUncategorizedFiles() {
        if (!isOnline()) {
            return 0;
        }

        List<FileMetadata> pendingFiles = fileRepository.findUncategorizedFiles();
        if (pendingFiles == null || pendingFiles.isEmpty()) {
            return 0;
        }

        int updatedCount = 0;
        for (FileMetadata file : pendingFiles) {
            try {
                String predictedCategory = analyzeAndCategorize(file.getFileName());
                if (predictedCategory != null
                        && !predictedCategory.equalsIgnoreCase("Uncategorized")
                        && !predictedCategory.trim().isEmpty()) {
                    file.setCategory(predictedCategory);
                    file.setUpdatedAt(LocalDateTime.now());
                    fileRepository.save(file);
                    updatedCount++;
                    System.out.println("[AI Recategorization] File '" + file.getFileName()
                            + "' (ID: " + file.getId() + ") updated: Uncategorized -> " + predictedCategory);
                }
            } catch (Exception e) {
                System.err.println("[AI Recategorization] Failed to recategorize file '"
                        + file.getFileName() + "': " + e.getMessage());
            }
        }

        if (updatedCount > 0) {
            System.out.println("[AI Recategorization] Completed batch: " + updatedCount + " file(s) updated.");
        }
        return updatedCount;
    }

    /**
     * Finds and categorizes any uncategorized files owned by a specific user.
     * Called when a user accesses their drive so their files are updated without delay.
     *
     * @param owner the user whose uncategorized files to categorize
     * @return the number of files updated
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recategorizeUserFiles(User owner) {
        if (owner == null || !isOnline()) {
            return 0;
        }

        List<FileMetadata> pendingFiles = fileRepository.findUncategorizedFilesByOwner(owner);
        if (pendingFiles == null || pendingFiles.isEmpty()) {
            return 0;
        }

        int updatedCount = 0;
        for (FileMetadata file : pendingFiles) {
            try {
                String predictedCategory = analyzeAndCategorize(file.getFileName());
                if (predictedCategory != null
                        && !predictedCategory.equalsIgnoreCase("Uncategorized")
                        && !predictedCategory.trim().isEmpty()) {
                    file.setCategory(predictedCategory);
                    file.setUpdatedAt(LocalDateTime.now());
                    fileRepository.save(file);
                    updatedCount++;
                    System.out.println("[AI Recategorization] User '" + owner.getUsername() + "' file '"
                            + file.getFileName() + "' updated: Uncategorized -> " + predictedCategory);
                }
            } catch (Exception e) {
                System.err.println("[AI Recategorization] Failed to recategorize file '"
                        + file.getFileName() + "': " + e.getMessage());
            }
        }
        return updatedCount;
    }

    /**
     * Autonomous background monitor:
     * Periodically checks if the AI service is online and recategorizes any pending files
     * that were uploaded when the AI was offline.
     * Runs every 10 seconds (fixedDelay = 10000ms), starting 5 seconds after startup.
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void autoRecategorizeWhenOnline() {
        try {
            long uncategorizedCount = fileRepository.countUncategorizedFiles();
            if (uncategorizedCount > 0) {
                if (isOnline()) {
                    System.out.println("[AI Monitor] AI model detected ONLINE with "
                            + uncategorizedCount + " uncategorized file(s). Running recategorization...");
                    int updated = recategorizeUncategorizedFiles();
                    if (updated > 0) {
                        System.out.println("[AI Monitor] Successfully recategorized "
                                + updated + " file(s) that were uploaded while AI was offline.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AI Monitor] Auto-recategorization check failed: " + e.getMessage());
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String getAiServiceUrl() {
        return aiServiceUrl;
    }

    public String getHealthUrl() {
        return healthUrl;
    }
}
