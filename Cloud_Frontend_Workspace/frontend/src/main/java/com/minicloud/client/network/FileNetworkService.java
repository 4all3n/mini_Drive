package com.minicloud.client.network;

import com.minicloud.client.models.BreadcrumbRecord;
import com.minicloud.client.models.DriveItem;
import com.minicloud.client.models.FolderRecord;
import com.minicloud.client.ui.DashboardScreen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Network service managing REST API communication for files, virtual folders,
 * and unified drive contents.
 */
public class FileNetworkService {

    private static final String BASE_URL = ServerConfig.getBaseUrl() + "/api/files";
    private static final String FOLDER_URL = ServerConfig.getBaseUrl() + "/api/folders";
    private static final String DRIVE_URL = ServerConfig.getBaseUrl() + "/api/drive";

    private final HttpClient httpClient;

    public FileNetworkService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Attaches JWT Bearer token to the request builder if available.
     */
    private HttpRequest.Builder withAuth(HttpRequest.Builder builder) {
        String token = AuthNetworkService.getToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    // ======================== FILE OPERATIONS ========================

    public String uploadFile(File file, String username) {
        return uploadFile(file, username, null);
    }

    public String uploadFile(File file, String username, Long folderId) {
        return uploadFile(file, file.getName(), username, folderId);
    }

    public String uploadFile(File file, String targetFileName, String username, Long folderId) {
        try {
            String boundary = "---MiniCloudBoundary" + System.currentTimeMillis();
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String finalName = (targetFileName != null && !targetFileName.trim().isEmpty()) ? targetFileName : file.getName();
            byte[] body = buildMultipartBody(finalName, fileBytes, username, boundary);

            String url = BASE_URL + "/upload";
            if (folderId != null) {
                url += "?folderId=" + folderId;
            }

            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return "SUCCESS";
            } else {
                return response.body(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to connect to server: " + e.getMessage();
        }
    }

    public List<DashboardScreen.FileRecord> fetchUserFiles(String username) {
        return fetchUserFiles(username, null);
    }

    public List<DashboardScreen.FileRecord> fetchUserFiles(String username, Long folderId) {
        List<DashboardScreen.FileRecord> fileList = new ArrayList<>();
        try {
            String url = BASE_URL + "/list";
            if (folderId != null) {
                url += "?folderId=" + folderId;
            } else if (username != null) {
                url += "?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
            }

            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                String body = response.body().trim();
                if (body.startsWith("[")) {
                    // JSON format returned by evolved backend
                    fileList.addAll(parseFileMetadataJson(body));
                } else {
                    // Legacy delimited string fallback: id|fileName|fileType|fileSize|category;;;
                    String[] files = body.split(";;;");
                    for (String f : files) {
                        if (f.isEmpty()) continue;
                        String[] parts = f.split("\\|");
                        
                        Long id = parts.length > 0 ? Long.parseLong(parts[0]) : 0L;
                        String name = parts.length > 1 ? parts[1] : "Unknown";
                        String type = parts.length > 2 ? parts[2] : "Unknown";
                        String size = parts.length > 3 ? formatFileSize(Long.parseLong(parts[3])) : "0 B";
                        String category = parts.length > 4 ? parts[4] : "Uncategorized";
                        
                        fileList.add(new DashboardScreen.FileRecord(id, name, type, size, category));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public boolean downloadFile(Long fileId, String fileName, File destinationFolder) {
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
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

    public byte[] fetchFileBytes(Long fileId) {
        if (fileId == null) return null;

        // Try /download/{id} first (canonical file streaming endpoint supported across all server builds),
        // then fall back to /preview/{id}.
        String[] candidateUrls = {
                BASE_URL + "/download/" + fileId,
                BASE_URL + "/preview/" + fileId
        };

        for (String url : candidateUrls) {
            try {
                HttpRequest request = withAuth(HttpRequest.newBuilder())
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200 && response.body() != null) {
                    System.out.println("[FileNetworkService] Successfully fetched " + response.body().length + " bytes from " + url);
                    return response.body();
                } else {
                    System.err.println("[FileNetworkService] Request to " + url + " returned status: " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("[FileNetworkService] Failed to fetch bytes from " + url + ": " + e.getMessage());
            }
        }
        return null;
    }

    public boolean deleteFile(Long fileId) {
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
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

    public boolean renameFile(Long fileId, String newName) {
        try {
            String url = BASE_URL + "/rename/" + fileId + "?newName=" + URLEncoder.encode(newName, StandardCharsets.UTF_8);
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean moveFile(Long fileId, Long targetFolderId) {
        try {
            String url = BASE_URL + "/move/" + fileId;
            if (targetFolderId != null) {
                url += "?folderId=" + targetFolderId;
            }
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean copyFile(Long fileId) {
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(BASE_URL + "/copy/" + fileId))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String fetchFileProperties(Long fileId) {
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(BASE_URL + "/properties/" + fileId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String fetchStorageStats() {
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(BASE_URL + "/stats"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ======================== FOLDER OPERATIONS ========================

    public FolderRecord createFolder(String name, Long parentId) {
        try {
            String url = FOLDER_URL + "/create?name=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
            if (parentId != null) {
                url += "&parentId=" + parentId;
            }

            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String body = response.body();
                Long id = extractLongField(body, "id");
                String folderName = extractStringField(body, "name");
                Long pId = extractLongField(body, "parentFolderId");
                String createdAt = extractStringField(body, "createdAt");
                String updatedAt = extractStringField(body, "updatedAt");
                return new FolderRecord(id, folderName != null ? folderName : name, pId, createdAt, updatedAt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetches all virtual folders across all levels for tree navigation and move operations.
     */
    public List<FolderRecord> fetchAllFolders() {
        List<FolderRecord> folders = new ArrayList<>();
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(FOLDER_URL + "/all"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                folders.addAll(parseFolderMetadataJson(response.body().trim()));
                return folders;
            }
        } catch (Exception e) {
            System.err.println("fetchAllFolders error: " + e.getMessage());
        }
        // Fallback to fetchFolders(null) if /all is not available
        return fetchFolders(null);
    }

    public List<FolderRecord> fetchFolders(Long parentId) {
        List<FolderRecord> folders = new ArrayList<>();
        try {
            String url = FOLDER_URL + "/list";
            if (parentId != null) {
                url += "?parentId=" + parentId;
            }

            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                folders.addAll(parseFolderMetadataJson(response.body().trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folders;
    }

    public boolean renameFolder(Long folderId, String newName) {
        try {
            String url = FOLDER_URL + "/rename/" + folderId + "?newName=" + URLEncoder.encode(newName, StandardCharsets.UTF_8);
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean moveFolder(Long folderId, Long targetParentId) {
        try {
            String url = FOLDER_URL + "/move/" + folderId;
            if (targetParentId != null) {
                url += "?parentId=" + targetParentId;
            }
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFolder(Long folderId) {
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(FOLDER_URL + "/delete/" + folderId))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BreadcrumbRecord> fetchBreadcrumb(Long folderId) {
        List<BreadcrumbRecord> breadcrumbs = new ArrayList<>();
        if (folderId == null) {
            breadcrumbs.add(new BreadcrumbRecord(null, "My Drive"));
            return breadcrumbs;
        }
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(FOLDER_URL + "/breadcrumb/" + folderId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                breadcrumbs.addAll(parseBreadcrumbJson(response.body().trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (breadcrumbs.isEmpty()) {
            breadcrumbs.add(new BreadcrumbRecord(null, "My Drive"));
        }
        return breadcrumbs;
    }

    public String fetchFolderProperties(Long folderId) {
        try {
            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(FOLDER_URL + "/properties/" + folderId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ======================== COMBINED DRIVE VIEW ========================

    /**
     * Fetches folders, files, and breadcrumbs for a given location in a single roundtrip.
     */
    public List<DriveItem> fetchDriveContents(Long folderId, List<BreadcrumbRecord> outBreadcrumbs) {
        List<DriveItem> items = new ArrayList<>();
        try {
            String url = DRIVE_URL + "/contents";
            if (folderId != null) {
                url += "?folderId=" + folderId;
            }

            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("FileNetworkService.fetchDriveContents failed: HTTP " + response.statusCode() + " -> " + response.body());
                return items;
            }

            String body = response.body().trim();
            if (body.isEmpty()) {
                return items;
            }

            return parseDriveContentsResponse(body, folderId, outBreadcrumbs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Searches files and folders recursively inside subfolders (or across the entire drive).
     */
    public List<DriveItem> searchDrive(String query, Long folderId, List<BreadcrumbRecord> outBreadcrumbs) {
        List<DriveItem> items = new ArrayList<>();
        try {
            String url = DRIVE_URL + "/search?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
            if (folderId != null) {
                url += "&folderId=" + folderId;
            }

            HttpRequest request = withAuth(HttpRequest.newBuilder())
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("FileNetworkService.searchDrive failed: HTTP " + response.statusCode() + " -> " + response.body());
                return items;
            }

            String body = response.body().trim();
            if (body.isEmpty()) {
                return items;
            }

            return parseDriveContentsResponse(body, folderId, outBreadcrumbs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private List<DriveItem> parseDriveContentsResponse(String body, Long folderId, List<BreadcrumbRecord> outBreadcrumbs) {
        List<DriveItem> items = new ArrayList<>();

        // Extract breadcrumb section
        int bcIdx = body.indexOf("\"breadcrumb\"");
        if (bcIdx != -1 && outBreadcrumbs != null) {
            int arrStart = body.indexOf("[", bcIdx);
            int arrEnd = findMatchingBracket(body, arrStart);
            if (arrStart != -1 && arrEnd != -1) {
                String bcJson = body.substring(arrStart, arrEnd + 1);
                outBreadcrumbs.clear();
                outBreadcrumbs.addAll(parseBreadcrumbJson(bcJson));
            }
        }

        // Extract folders
        int fldIdx = body.indexOf("\"folders\"");
        if (fldIdx != -1) {
            int arrStart = body.indexOf("[", fldIdx);
            int arrEnd = findMatchingBracket(body, arrStart);
            if (arrStart != -1 && arrEnd != -1) {
                String fldJson = body.substring(arrStart, arrEnd + 1);
                List<FolderRecord> folders = parseFolderMetadataJson(fldJson);
                for (FolderRecord fr : folders) {
                    items.add(DriveItem.fromFolder(fr));
                }
            }
        }

        // Extract files
        int filesIdx = body.indexOf("\"files\"");
        if (filesIdx != -1) {
            int arrStart = body.indexOf("[", filesIdx);
            int arrEnd = findMatchingBracket(body, arrStart);
            if (arrStart != -1 && arrEnd != -1) {
                String filesJson = body.substring(arrStart, arrEnd + 1);
                List<DashboardScreen.FileRecord> files = parseFileMetadataJson(filesJson);
                for (DashboardScreen.FileRecord f : files) {
                    items.add(new DriveItem(
                            f.getId(),
                            f.getFileName(),
                            f.getFileType(),
                            f.getFileSize(),
                            f.getCategory(),
                            false,
                            null,
                            folderId,
                            f.getLocation()
                    ));
                }
            }
        }

        return items;
    }

    // ======================== JSON PARSING HELPERS ========================

    private int findMatchingBracket(String text, int start) {
        if (start < 0 || start >= text.length()) return -1;
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private List<String> extractJsonObjects(String jsonArray) {
        List<String> list = new ArrayList<>();
        if (jsonArray == null) return list;
        String text = jsonArray.trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        if (text.isEmpty()) return list;

        int depth = 0;
        int objStart = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == '{') {
                    if (depth == 0) objStart = i;
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0 && objStart != -1) {
                        list.add(text.substring(objStart + 1, i));
                        objStart = -1;
                    }
                }
            }
        }
        return list;
    }

    private List<DashboardScreen.FileRecord> parseFileMetadataJson(String json) {
        List<DashboardScreen.FileRecord> list = new ArrayList<>();
        List<String> objects = extractJsonObjects(json);
        for (String obj : objects) {
            Long id = extractLongField(obj, "id");
            String fileName = extractStringField(obj, "fileName");
            String fileType = extractStringField(obj, "fileType");
            Long fileSize = extractLongField(obj, "fileSize");
            String category = extractStringField(obj, "category");
            String folderName = extractStringField(obj, "folderName");

            list.add(new DashboardScreen.FileRecord(
                    id != null ? id : 0L,
                    fileName != null ? fileName : "Unknown",
                    fileType != null ? fileType : "Unknown",
                    fileSize != null ? formatFileSize(fileSize) : "0 B",
                    category != null ? category : "Uncategorized",
                    folderName != null ? folderName : "My Drive"
            ));
        }
        return list;
    }

    private List<FolderRecord> parseFolderMetadataJson(String json) {
        List<FolderRecord> list = new ArrayList<>();
        List<String> objects = extractJsonObjects(json);
        for (String obj : objects) {
            Long id = extractLongField(obj, "id");
            String name = extractStringField(obj, "name");
            Long parentFolderId = extractLongField(obj, "parentFolderId");
            String createdAt = extractStringField(obj, "createdAt");
            String updatedAt = extractStringField(obj, "updatedAt");
            String location = extractStringField(obj, "location");

            list.add(new FolderRecord(
                    id,
                    name != null ? name : "Untitled Folder",
                    parentFolderId,
                    createdAt,
                    updatedAt,
                    location != null ? location : "My Drive"
            ));
        }
        return list;
    }

    private List<BreadcrumbRecord> parseBreadcrumbJson(String json) {
        List<BreadcrumbRecord> list = new ArrayList<>();
        List<String> objects = extractJsonObjects(json);
        for (String obj : objects) {
            Long id = extractLongField(obj, "id");
            String name = extractStringField(obj, "name");
            list.add(new BreadcrumbRecord(id, name != null ? name : "Drive"));
        }
        return list;
    }

    private String extractStringField(String obj, String fieldName) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(obj);
        if (m.find()) {
            return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return null;
    }

    private Long extractLongField(String obj, String fieldName) {
        String pattern = "\"" + fieldName + "\"\\s*:\\s*([0-9]+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(obj);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
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