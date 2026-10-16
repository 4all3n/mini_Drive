package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.models.FolderMetadata;
import com.minicloud.server.backend.models.User;
import com.minicloud.server.backend.models.dto.DriveContentsDTO;
import com.minicloud.server.backend.models.dto.FileMetadataDTO;
import com.minicloud.server.backend.models.dto.FolderMetadataDTO;
import com.minicloud.server.backend.repository.FileRepository;
import com.minicloud.server.backend.repository.FolderRepository;
import com.minicloud.server.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Combined "Drive View" controller that returns both folders and files
 * for a given location in a single API call — reducing frontend round-trips.
 *
 * This mirrors how Google Drive's API works: one request returns the full
 * contents of the current view (subfolders + files + navigation breadcrumb).
 *
 * Endpoint:
 *   GET /api/drive/contents?folderId={id}  — returns DriveContentsDTO
 */
@RestController
@RequestMapping("/api/drive")
public class DriveController {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public DriveController(FolderRepository folderRepository,
                           FileRepository fileRepository,
                           UserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : null;
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    /**
     * Returns the complete contents of a drive location (folder or root).
     *
     * @param folderId null for root ("My Drive"), or a folder ID to view its contents
     * @return DriveContentsDTO containing subfolders, files, and breadcrumb
     */
    @Transactional(readOnly = true)
    @GetMapping("/contents")
    public ResponseEntity<?> getDriveContents(
            @RequestParam(value = "folderId", required = false) Long folderId) {
        try {
            User owner = getAuthenticatedUser();

            List<FolderMetadata> folders;
            List<FileMetadata> files;
            List<DriveContentsDTO.BreadcrumbItem> breadcrumb = new ArrayList<>();

            // Always start breadcrumb with "My Drive"
            breadcrumb.add(new DriveContentsDTO.BreadcrumbItem(null, "My Drive"));

            if (folderId == null) {
                // Root level
                folders = folderRepository.findByOwnerAndParentFolderIsNull(owner);
                files = fileRepository.findByOwnerAndFolderIsNull(owner);
            } else {
                // Inside a specific folder
                FolderMetadata currentFolder = folderRepository.findByIdAndOwner(folderId, owner)
                        .orElseThrow(() -> new RuntimeException("Folder not found or access denied"));

                folders = folderRepository.findByOwnerAndParentFolder(owner, currentFolder);
                files = fileRepository.findByOwnerAndFolder(owner, currentFolder);

                // Build breadcrumb trail from root to current folder
                List<DriveContentsDTO.BreadcrumbItem> trail = new ArrayList<>();
                FolderMetadata ancestor = currentFolder;
                while (ancestor != null) {
                    trail.add(0, new DriveContentsDTO.BreadcrumbItem(ancestor.getId(), ancestor.getName()));
                    ancestor = ancestor.getParentFolder();
                }
                breadcrumb.addAll(trail);
            }

            // Convert entities to DTOs
            List<FolderMetadataDTO> folderDTOs = folders.stream()
                    .map(FolderMetadataDTO::fromEntity)
                    .collect(Collectors.toList());

            List<FileMetadataDTO> fileDTOs = files.stream()
                    .map(FileMetadataDTO::fromEntity)
                    .collect(Collectors.toList());

            DriveContentsDTO response = new DriveContentsDTO(folderDTOs, fileDTOs, breadcrumb);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    /**
     * Recursive drive search: searches files and folders across all subfolders.
     *
     * @param query    the search keyword (case-insensitive substring match)
     * @param folderId optional scope: null searches entire drive;
     *                 non-null searches inside that folder and all its descendant subfolders
     * @return DriveContentsDTO containing matching subfolders, files, and breadcrumb
     */
    @Transactional(readOnly = true)
    @GetMapping("/search")
    public ResponseEntity<?> searchDrive(
            @RequestParam("query") String query,
            @RequestParam(value = "folderId", required = false) Long folderId) {
        try {
            User owner = getAuthenticatedUser();
            String cleanQuery = (query != null) ? query.trim() : "";
            if (cleanQuery.isEmpty()) {
                return getDriveContents(folderId);
            }

            // 1. Fetch all folders for this user to build subtree and path hierarchy
            List<FolderMetadata> allFolders = folderRepository.findByOwner(owner);

            // Build folder ID -> FolderMetadata lookup
            Map<Long, FolderMetadata> folderMap = allFolders.stream()
                    .collect(Collectors.toMap(FolderMetadata::getId, f -> f));

            // Build folder ID -> display path string (e.g. "My Drive / Projects / 2026")
            Map<Long, String> folderPathMap = new HashMap<>();
            for (FolderMetadata f : allFolders) {
                folderPathMap.put(f.getId(), buildPathString(f, folderMap));
            }

            // 2. Determine target subtree
            Set<Long> subtreeFolderIds = null;
            String scopeName = "My Drive";
            if (folderId != null) {
                FolderMetadata scopedFolder = folderRepository.findByIdAndOwner(folderId, owner)
                        .orElseThrow(() -> new RuntimeException("Folder not found"));
                scopeName = scopedFolder.getName();

                subtreeFolderIds = new HashSet<>();
                subtreeFolderIds.add(folderId);
                boolean added;
                do {
                    added = false;
                    for (FolderMetadata f : allFolders) {
                        if (f.getParentFolder() != null && subtreeFolderIds.contains(f.getParentFolder().getId())) {
                            if (subtreeFolderIds.add(f.getId())) {
                                added = true;
                            }
                        }
                    }
                } while (added);
            }

            // 3. Search Folders
            String lowerQuery = cleanQuery.toLowerCase();
            final Set<Long> activeSubtree = subtreeFolderIds;
            List<FolderMetadata> matchingFolders = allFolders.stream()
                    .filter(f -> {
                        if (activeSubtree != null) {
                            if (f.getId().equals(folderId) || !activeSubtree.contains(f.getId())) {
                                return false;
                            }
                        }
                        return f.getName().toLowerCase().contains(lowerQuery);
                    })
                    .collect(Collectors.toList());

            // 4. Search Files
            List<FileMetadata> matchingFiles = fileRepository.searchByOwnerAndFileName(owner, cleanQuery);
            if (activeSubtree != null) {
                matchingFiles = matchingFiles.stream()
                        .filter(f -> f.getFolder() != null && activeSubtree.contains(f.getFolder().getId()))
                        .collect(Collectors.toList());
            }

            // 5. Convert to DTOs with path annotations
            List<FolderMetadataDTO> folderDTOs = matchingFolders.stream()
                    .map(f -> {
                        FolderMetadataDTO dto = FolderMetadataDTO.fromEntity(f);
                        String parentPath = f.getParentFolder() != null ? folderPathMap.get(f.getParentFolder().getId()) : "My Drive";
                        dto.setLocation(parentPath != null ? parentPath : "My Drive");
                        return dto;
                    })
                    .collect(Collectors.toList());

            List<FileMetadataDTO> fileDTOs = matchingFiles.stream()
                    .map(f -> {
                        FileMetadataDTO dto = FileMetadataDTO.fromEntity(f);
                        String loc = f.getFolder() != null ? folderPathMap.get(f.getFolder().getId()) : "My Drive";
                        dto.setFolderName(loc != null ? loc : "My Drive");
                        return dto;
                    })
                    .collect(Collectors.toList());

            // 6. Build Breadcrumb
            List<DriveContentsDTO.BreadcrumbItem> breadcrumb = new ArrayList<>();
            breadcrumb.add(new DriveContentsDTO.BreadcrumbItem(null, "My Drive"));
            if (folderId != null) {
                breadcrumb.add(new DriveContentsDTO.BreadcrumbItem(folderId, scopeName));
            }
            int totalResults = folderDTOs.size() + fileDTOs.size();
            breadcrumb.add(new DriveContentsDTO.BreadcrumbItem(null, "Search: \"" + cleanQuery + "\" (" + totalResults + ")"));

            return ResponseEntity.ok(new DriveContentsDTO(folderDTOs, fileDTOs, breadcrumb));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    private String buildPathString(FolderMetadata folder, Map<Long, FolderMetadata> folderMap) {
        List<String> segments = new ArrayList<>();
        FolderMetadata curr = folder;
        while (curr != null) {
            segments.add(0, curr.getName());
            curr = curr.getParentFolder() != null ? folderMap.get(curr.getParentFolder().getId()) : null;
        }
        segments.add(0, "My Drive");
        return String.join(" / ", segments);
    }
}
