package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.models.FolderMetadata;
import com.minicloud.server.backend.models.User;
import com.minicloud.server.backend.models.dto.FolderMetadataDTO;
import com.minicloud.server.backend.repository.FileRepository;
import com.minicloud.server.backend.repository.FolderRepository;
import com.minicloud.server.backend.repository.UserRepository;
import com.minicloud.server.backend.services.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for virtual folder operations.
 *
 * Folders exist only in the database — no physical directories are created on disk
 * (mirrors Google Drive's architecture). This makes move and rename operations O(1).
 *
 * Endpoints:
 *   POST   /api/folders/create          — create a new folder
 *   GET    /api/folders/list             — list subfolders (optional parentId)
 *   PUT    /api/folders/rename/{id}      — rename a folder
 *   PUT    /api/folders/move/{id}        — move folder to a new parent
 *   DELETE /api/folders/delete/{id}      — recursively delete folder and contents
 *   GET    /api/folders/breadcrumb/{id}  — get breadcrumb trail for a folder
 *   GET    /api/folders/properties/{id}  — get folder properties
 */
@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public FolderController(FolderRepository folderRepository,
                            FileRepository fileRepository,
                            UserRepository userRepository,
                            StorageService storageService) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    // ======================== HELPERS ========================

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : null;
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    // ======================== CREATE ========================

    /**
     * Create a new folder. If parentId is null, folder is created at root level.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createFolder(
            @RequestParam("name") String name,
            @RequestParam(value = "parentId", required = false) Long parentId) {
        try {
            User owner = getAuthenticatedUser();

            FolderMetadata parent = null;
            if (parentId != null) {
                parent = folderRepository.findByIdAndOwner(parentId, owner)
                        .orElseThrow(() -> new RuntimeException("Parent folder not found"));
            }

            FolderMetadata folder = new FolderMetadata(name, owner, parent);
            folderRepository.save(folder);

            return ResponseEntity.status(HttpStatus.CREATED).body(FolderMetadataDTO.fromEntity(folder));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== LIST ========================

    /**
     * List all folders belonging to the authenticated user (entire hierarchy).
     * Used by sidebar tree view and Move dialog.
     */
    @Transactional(readOnly = true)
    @GetMapping("/all")
    public ResponseEntity<?> listAllFolders() {
        try {
            User owner = getAuthenticatedUser();
            List<FolderMetadata> folders = folderRepository.findByOwner(owner);

            List<FolderMetadataDTO> dtos = folders.stream()
                    .map(FolderMetadataDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /**
     * List subfolders inside a parent folder. If parentId is null, lists root folders.
     */
    @Transactional(readOnly = true)
    @GetMapping("/list")
    public ResponseEntity<?> listFolders(
            @RequestParam(value = "parentId", required = false) Long parentId) {
        try {
            User owner = getAuthenticatedUser();
            List<FolderMetadata> folders;

            if (parentId == null) {
                folders = folderRepository.findByOwnerAndParentFolderIsNull(owner);
            } else {
                FolderMetadata parent = folderRepository.findByIdAndOwner(parentId, owner)
                        .orElseThrow(() -> new RuntimeException("Folder not found"));
                folders = folderRepository.findByOwnerAndParentFolder(owner, parent);
            }

            List<FolderMetadataDTO> dtos = folders.stream()
                    .map(FolderMetadataDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== RENAME ========================

    /**
     * Rename a folder. Database-only operation.
     */
    @PutMapping("/rename/{id}")
    public ResponseEntity<?> renameFolder(
            @PathVariable Long id,
            @RequestParam("newName") String newName) {
        try {
            User owner = getAuthenticatedUser();
            FolderMetadata folder = folderRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));

            folder.setName(newName);
            folder.setUpdatedAt(LocalDateTime.now());
            folderRepository.save(folder);

            return ResponseEntity.ok(FolderMetadataDTO.fromEntity(folder));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== MOVE ========================

    /**
     * Move a folder to a new parent. Includes circular reference detection.
     * Pass parentId=null to move to root.
     */
    @PutMapping("/move/{id}")
    public ResponseEntity<?> moveFolder(
            @PathVariable Long id,
            @RequestParam(value = "parentId", required = false) Long targetParentId) {
        try {
            User owner = getAuthenticatedUser();
            FolderMetadata folder = folderRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));

            // Cannot move a folder into itself
            if (targetParentId != null && targetParentId.equals(id)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cannot move a folder into itself"));
            }

            FolderMetadata targetParent = null;
            if (targetParentId != null) {
                targetParent = folderRepository.findByIdAndOwner(targetParentId, owner)
                        .orElseThrow(() -> new RuntimeException("Target parent folder not found"));

                // Circular reference check: walk up from target to root
                FolderMetadata ancestor = targetParent;
                while (ancestor != null) {
                    if (ancestor.getId().equals(id)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "Cannot move folder into its own descendant (circular reference)"));
                    }
                    ancestor = ancestor.getParentFolder();
                }
            }

            folder.setParentFolder(targetParent);
            folder.setUpdatedAt(LocalDateTime.now());
            folderRepository.save(folder);

            return ResponseEntity.ok(FolderMetadataDTO.fromEntity(folder));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== DELETE (RECURSIVE) ========================

    /**
     * Recursively delete a folder, all its subfolders, and all contained files.
     * Physical files are deleted from disk. Database records are removed.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFolder(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FolderMetadata folder = folderRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));

            deleteFolderRecursive(folder, owner.getUsername());

            return ResponseEntity.ok(Map.of("message", "Folder and all contents deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /**
     * Recursive helper: deletes all files in the folder, then all child folders,
     * then the folder itself.
     */
    private void deleteFolderRecursive(FolderMetadata folder, String username) {
        // 1. Delete all files in this folder
        List<FileMetadata> files = fileRepository.findByFolder(folder);
        for (FileMetadata file : files) {
            try {
                storageService.deleteFile(username, file.getStoredName());
            } catch (Exception e) {
                System.err.println("Warning: could not delete physical file: " + file.getStoredName());
            }
            fileRepository.delete(file);
        }

        // 2. Recursively delete child folders
        List<FolderMetadata> children = folderRepository.findByParentFolder(folder);
        for (FolderMetadata child : children) {
            deleteFolderRecursive(child, username);
        }

        // 3. Delete this folder
        folderRepository.delete(folder);
    }

    // ======================== BREADCRUMB ========================

    /**
     * Build breadcrumb trail from root to the given folder.
     * Returns an ordered list: [{id:null, name:"My Drive"}, {id:1, name:"Docs"}, ...]
     */
    @GetMapping("/breadcrumb/{id}")
    public ResponseEntity<?> getBreadcrumb(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FolderMetadata folder = folderRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));

            List<com.minicloud.server.backend.models.dto.DriveContentsDTO.BreadcrumbItem> trail = new ArrayList<>();
            FolderMetadata current = folder;
            while (current != null) {
                trail.add(0, new com.minicloud.server.backend.models.dto.DriveContentsDTO.BreadcrumbItem(current.getId(), current.getName()));
                current = current.getParentFolder();
            }
            // Add "My Drive" root
            trail.add(0, new com.minicloud.server.backend.models.dto.DriveContentsDTO.BreadcrumbItem(null, "My Drive"));

            return ResponseEntity.ok(trail);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ======================== PROPERTIES ========================

    /**
     * Get folder properties including item count and total size.
     */
    @GetMapping("/properties/{id}")
    public ResponseEntity<?> getFolderProperties(@PathVariable Long id) {
        try {
            User owner = getAuthenticatedUser();
            FolderMetadata folder = folderRepository.findByIdAndOwner(id, owner)
                    .orElseThrow(() -> new RuntimeException("Folder not found"));

            List<FileMetadata> files = fileRepository.findByFolder(folder);
            List<FolderMetadata> subfolders = folderRepository.findByOwnerAndParentFolder(owner, folder);

            long totalSize = files.stream().mapToLong(FileMetadata::getFileSize).sum();

            return ResponseEntity.ok(Map.of(
                    "id", folder.getId(),
                    "name", folder.getName(),
                    "fileCount", files.size(),
                    "subfolderCount", subfolders.size(),
                    "totalSizeBytes", totalSize,
                    "createdAt", folder.getCreatedAt() != null ? folder.getCreatedAt().toString() : "unknown",
                    "updatedAt", folder.getUpdatedAt() != null ? folder.getUpdatedAt().toString() : "unknown",
                    "owner", owner.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }
}
