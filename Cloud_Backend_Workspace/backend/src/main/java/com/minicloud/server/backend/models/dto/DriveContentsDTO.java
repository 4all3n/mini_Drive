package com.minicloud.server.backend.models.dto;

import java.util.List;

/**
 * Combined response DTO for a single "drive view" request.
 * Returns the folders and files inside the currently viewed folder,
 * plus a breadcrumb trail for UI navigation.
 */
public class DriveContentsDTO {

    private List<FolderMetadataDTO> folders;
    private List<FileMetadataDTO> files;
    private List<BreadcrumbItem> breadcrumb;

    public DriveContentsDTO() {}

    public DriveContentsDTO(List<FolderMetadataDTO> folders, List<FileMetadataDTO> files, List<BreadcrumbItem> breadcrumb) {
        this.folders = folders;
        this.files = files;
        this.breadcrumb = breadcrumb;
    }

    // Getters & Setters
    public List<FolderMetadataDTO> getFolders() { return folders; }
    public void setFolders(List<FolderMetadataDTO> folders) { this.folders = folders; }
    public List<FileMetadataDTO> getFiles() { return files; }
    public void setFiles(List<FileMetadataDTO> files) { this.files = files; }
    public List<BreadcrumbItem> getBreadcrumb() { return breadcrumb; }
    public void setBreadcrumb(List<BreadcrumbItem> breadcrumb) { this.breadcrumb = breadcrumb; }

    /**
     * Represents one segment in the breadcrumb navigation trail.
     * E.g., My Drive > Documents > Reports would be:
     * [{id:null, name:"My Drive"}, {id:1, name:"Documents"}, {id:5, name:"Reports"}]
     */
    public static class BreadcrumbItem {
        private Long id;
        private String name;

        public BreadcrumbItem() {}
        public BreadcrumbItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
