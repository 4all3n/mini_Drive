package com.minicloud.server.backend.models.dto;

import com.minicloud.server.backend.models.FileMetadata;

/**
 * Data Transfer Object (DTO) for file metadata.
 *
 * Decouples internal database entities from external API responses,
 * preventing accidental exposure of internal server paths or lazy loading issues.
 */
public class FileMetadataDTO {

    private Long id;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String category;
    private String uploadDate;
    private Long folderId;
    private String folderName;

    /**
     * Default no-argument constructor for serialization / deserialization.
     */
    public FileMetadataDTO() {
    }

    /**
     * Parameterized constructor initializing all fields.
     *
     * @param id unique identifier of the file
     * @param fileName display name of the file
     * @param fileType MIME type / content type
     * @param fileSize size in bytes
     * @param category AI-assigned category (e.g. "Documents", "Media")
     * @param uploadDate ISO-formatted upload timestamp string
     * @param folderId parent folder ID, or null if in root
     * @param folderName parent folder display name, or null if in root
     */
    public FileMetadataDTO(Long id, String fileName, String fileType, long fileSize,
                           String category, String uploadDate, Long folderId, String folderName) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.category = category;
        this.uploadDate = uploadDate;
        this.folderId = folderId;
        this.folderName = folderName;
    }

    /**
     * Static factory method to convert a {@link FileMetadata} JPA entity to a {@link FileMetadataDTO}.
     *
     * @param file the FileMetadata entity
     * @return populated FileMetadataDTO, or null if file is null
     */
    public static FileMetadataDTO fromEntity(FileMetadata file) {
        if (file == null) {
            return null;
        }

        Long folderId = null;
        String folderName = null;

        // TODO: Update once FileMetadata is fully evolved across all environments.
        // Safely checks if folder reference is present on entity and extracts ID and name.
        if (file.getFolder() != null) {
            folderId = file.getFolder().getId();
            folderName = file.getFolder().getName();
        }

        String uploadDateStr = file.getUploadDate() != null ? file.getUploadDate().toString() : null;

        return new FileMetadataDTO(
                file.getId(),
                file.getFileName(),
                file.getFileType(),
                file.getFileSize(),
                file.getCategory(),
                uploadDateStr,
                folderId,
                folderName
        );
    }

    // ======================== GETTERS & SETTERS ========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
