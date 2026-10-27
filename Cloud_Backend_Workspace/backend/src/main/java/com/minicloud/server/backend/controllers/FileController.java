package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.models.FolderMetadata;
import com.minicloud.server.backend.models.User;
import com.minicloud.server.backend.models.dto.FileMetadataDTO;
import com.minicloud.server.backend.repository.FileRepository;
import com.minicloud.server.backend.repository.FolderRepository;
import com.minicloud.server.backend.repository.UserRepository;
import com.minicloud.server.backend.services.AiCategorizationService;
import com.minicloud.server.backend.services.StorageService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for file operations in the Intelligent Cloud Drive.
 *
 * All endpoints extract the authenticated username from the JWT token in the
 * SecurityContext — no request parameter spoofing is possible (IDOR prevention).
 *
 * Endpoints:
 *   POST   /api/files/upload          — upload a file (optional folderId)
 *   GET    /api/files/list             — list files (optional folderId filter)
 *   GET    /api/files/download/{id}    — download a file
 *   DELETE /api/files/delete/{id}      — delete a file
 *   PUT    /api/files/rename/{id}      — rename a file
 *   PUT    /api/files/move/{id}        — move file to another folder
 *   POST   /api/files/copy/{id}        — copy a file
 *   GET    /api/files/properties/{id}  — get detailed file properties
 *   GET    /api/files/stats            — get user storage statistics
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final AiCategorizationService aiService;
    private final StorageService storageService;

    public FileController(FileRepository fileRepository,
                          UserRepository userRepository,
                          FolderRepository folderRepository,
                          AiCategorizationService aiService,
                          StorageService storageService) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.aiService = aiService;
        this.storageService = storageService;
    }

    // ======================== HELPER METHODS ========================

    /**
     * Extracts the authenticated username from the JWT-backed SecurityContext.
     * This is the single source of truth for "who is the current user" — never
     * from request params.
     */
    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }

    /**
     * Loads the User entity for the authenticated principal.
     * Throws if the user doesn't exist in the database.
     */
    private User getAuthenticatedUser() {
        String username = getAuthenticatedUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }

    // ======================== UPLOAD ========================

    /**
     * Upload a file to the user's drive.
     *
     * @param file     the multipart file payload
     * @param folderId optional target folder ID (null = root level)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Long folderId) {
        try {
            User owner = getAuthenticatedUser();
            String username = owner.getUsername();

            String cleanFileName = StringUtils.cleanPath(file.getOriginalFilename());

            // Save to per-user directory with UUID-prefixed name
            String storedName = storageService.saveFile(username, cleanFileName, file.getBytes());

            // Resolve target folder if provided
            FolderMetadata folder = null;
            if (folderId != null) {
                folder = folderRepository.findByIdAndOwner(folderId, owner)
                        .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));
            }

            // Build metadata entity
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(cleanFileName);
            metadata.setStoredName(storedName);
            metadata.setFileType(file.getContentType());
            metadata.setFileSize(file.getSize());
            metadata.setServerFilePath(storageService.getFilePath(username, storedName).toString());
            metadata.setUploadedBy(username); // Legacy field for backward compat
            metadata.setOwner(owner);
            metadata.setFolder(folder);
            metadata.setUploadDate(LocalDateTime.now());
            metadata.setUpdatedAt(LocalDateTime.now());

            // AI categorization
            String category = aiService.analyzeAndCategorize(cleanFileName);
            metadata.setCategory(category);

            fileRepository.save(metadata);

            return ResponseEntity.ok(FileMetadataDTO.fromEntity(metadata));

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    // ======================== LIST FILES ========================

    /**
     * List files for the authenticated user, optionally filtered by folder.
     *
     * @param folderId if null, returns root-level files; otherwise files in that folder
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserFiles(
            @RequestParam(value = "folderId", required = false) Long folderId) {
        try {
            User owner = getAuthenticatedUser();

            // Self-healing: if AI came online, recategorize any files uploaded while offline
            try {
                if (aiService != null && aiService.isOnline()) {
                    aiService.recategorizeUserFiles(owner);
                }
            } catch (Exception ignored) {}

            List<FileMetadata> files;

            if (folderId == null) {
                files = fileRepository.findByOwnerAndFolderIsNull(owner);
            } else {
                FolderMetadata folder = folderRepository.findByIdAndOwner(folderId, owner)
                        .orElseThrow(() -> new RuntimeException("Folder not found"));
                files = fileRepository.findByOwnerAndFolder(owner, folder);
            }

            List<FileMetadataDTO> dtos = files.stream()
                    .map(FileMetadataDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== DOWNLOAD ========================

    /**
     * Download a file by ID. Verifies ownership via JWT.
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FileMetadata metadata = fileRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            Path filePath = storageService.getFilePath(owner.getUsername(), metadata.getStoredName());
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + metadata.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "File not found on disk"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== PREVIEW ========================

    /**
     * Preview a file by ID (inline streaming). Verifies ownership via JWT.
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<?> previewFile(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FileMetadata metadata = fileRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            Path filePath = storageService.getFilePath(owner.getUsername(), metadata.getStoredName());
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = metadata.getFileType();
                if (contentType == null || contentType.isBlank() || contentType.equals("application/octet-stream")) {
                    try {
                        contentType = java.nio.file.Files.probeContentType(filePath);
                    } catch (Exception ignored) {}
                }
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + metadata.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "File not found on disk"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== DELETE ========================

    /**
     * Delete a file by ID. Removes from both disk and database.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FileMetadata metadata = fileRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            // Delete physical file
            storageService.deleteFile(owner.getUsername(), metadata.getStoredName());
            // Delete database record
            fileRepository.delete(metadata);

            return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Delete failed: " + e.getMessage()));
        }
    }

    // ======================== RENAME ========================

    /**
     * Rename a file. Only updates the display name in the database —
     * the physical UUID-prefixed filename on disk is unchanged (O(1) operation).
     */
    @PutMapping("/rename/{id}")
    public ResponseEntity<?> renameFile(
            @PathVariable Long id,
            @RequestParam("newName") String newName) {
        try {
            User owner = getAuthenticatedUser();
            FileMetadata metadata = fileRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            String cleanName = StringUtils.cleanPath(newName);
            if (cleanName.contains("..")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid filename"));
            }

            metadata.setFileName(cleanName);
            metadata.setUpdatedAt(LocalDateTime.now());
            fileRepository.save(metadata);

            return ResponseEntity.ok(FileMetadataDTO.fromEntity(metadata));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== MOVE ========================

    /**
     * Move a file to a different folder. Database UPDATE only — no disk I/O.
     * Pass folderId=null (or omit) to move to root.
     */
    @PutMapping("/move/{id}")
    public ResponseEntity<?> moveFile(
            @PathVariable Long id,
            @RequestParam(value = "folderId", required = false) Long targetFolderId) {
        try {
            User owner = getAuthenticatedUser();
            FileMetadata metadata = fileRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            FolderMetadata targetFolder = null;
            if (targetFolderId != null) {
                targetFolder = folderRepository.findByIdAndOwner(targetFolderId, owner)
                        .orElseThrow(() -> new RuntimeException("Target folder not found"));
            }

            metadata.setFolder(targetFolder);
            metadata.setUpdatedAt(LocalDateTime.now());
            fileRepository.save(metadata);

            return ResponseEntity.ok(FileMetadataDTO.fromEntity(metadata));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== COPY ========================

    /**
     * Creates a copy of a file. Duplicates both the physical file (new UUID)
     * and the database record. Copy lands in the same folder as the original.
     */
    @PostMapping("/copy/{id}")
    public ResponseEntity<?> copyFile(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FileMetadata original = fileRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            // Physical copy with new UUID
            String newStoredName = storageService.copyFile(
                    owner.getUsername(), original.getStoredName(), original.getFileName());

            // Database copy
            FileMetadata copy = new FileMetadata();
            copy.setFileName("Copy of " + original.getFileName());
            copy.setStoredName(newStoredName);
            copy.setFileType(original.getFileType());
            copy.setFileSize(original.getFileSize());
            copy.setServerFilePath(storageService.getFilePath(owner.getUsername(), newStoredName).toString());
            copy.setUploadedBy(owner.getUsername());
            copy.setOwner(owner);
            copy.setFolder(original.getFolder());
            copy.setUploadDate(LocalDateTime.now());
            copy.setUpdatedAt(LocalDateTime.now());
            copy.setCategory(original.getCategory());

            fileRepository.save(copy);

            return ResponseEntity.ok(FileMetadataDTO.fromEntity(copy));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== PROPERTIES ========================

    /**
     * Get detailed file properties (metadata, location, timestamps).
     */
    @GetMapping("/properties/{id}")
    public ResponseEntity<?> getFileProperties(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FileMetadata metadata = fileRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            String folderPath = "My Drive";
            if (metadata.getFolder() != null) {
                folderPath = "My Drive / " + metadata.getFolder().getName();
            }

            return ResponseEntity.ok(Map.of(
                    "id", metadata.getId(),
                    "fileName", metadata.getFileName(),
                    "fileType", metadata.getFileType() != null ? metadata.getFileType() : "unknown",
                    "fileSize", metadata.getFileSize(),
                    "category", metadata.getCategory() != null ? metadata.getCategory() : "Uncategorized",
                    "location", folderPath,
                    "uploadDate", metadata.getUploadDate() != null ? metadata.getUploadDate().toString() : "unknown",
                    "lastModified", metadata.getUpdatedAt() != null ? metadata.getUpdatedAt().toString() : "unknown",
                    "owner", owner.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== STATS ========================

    /**
     * Returns storage statistics for the authenticated user including per-category breakdown and quota.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            User owner = getAuthenticatedUser();
            List<FileMetadata> files = fileRepository.findByOwner(owner);
            long totalSize = fileRepository.sumFileSizeByOwner(owner);
            long fileCount = files.size();

            long quotaBytes = 500L * 1024L * 1024L; // 500 MB default quota
            double quotaUsedPercent = (totalSize * 100.0) / quotaBytes;
            if (quotaUsedPercent > 100.0) quotaUsedPercent = 100.0;

            Map<String, Long> categorySizes = new HashMap<>();
            Map<String, Long> categoryCounts = new HashMap<>();

            for (FileMetadata f : files) {
                String cat = f.getCategory() != null && !f.getCategory().isBlank()
                        ? f.getCategory() : "Uncategorized";
                categorySizes.put(cat, categorySizes.getOrDefault(cat, 0L) + f.getFileSize());
                categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0L) + 1L);
            }

            Map<String, Double> categorySizeMB = new HashMap<>();
            for (Map.Entry<String, Long> entry : categorySizes.entrySet()) {
                categorySizeMB.put(entry.getKey(), Math.round((entry.getValue() / (1024.0 * 1024.0)) * 100.0) / 100.0);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("username", owner.getUsername());
            response.put("totalFiles", fileCount);
            response.put("totalSizeBytes", totalSize);
            response.put("totalSizeMB", String.format(Locale.US, "%.2f", totalSize / (1024.0 * 1024.0)));
            response.put("quotaSizeBytes", quotaBytes);
            response.put("quotaSizeMB", 500.0);
            response.put("quotaUsedPercent", Math.round(quotaUsedPercent * 10.0) / 10.0);
            response.put("categorySizes", categorySizes);
            response.put("categoryCounts", categoryCounts);
            response.put("categorySizeMB", categorySizeMB);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }
}