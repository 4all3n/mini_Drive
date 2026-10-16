package com.minicloud.server.backend.models.dto;

import com.minicloud.server.backend.models.FolderMetadata;

/**
 * Data Transfer Object (DTO) for folder metadata.
 *
 * Decouples internal database entity representations from API responses,
 * exposing flat properties such as parentFolderId rather than full entity trees.
 */
public class FolderMetadataDTO {

    private Long id;
    private String name;
    private Long parentFolderId;
    private String createdAt;
    private String updatedAt;
    private String location;

    /**
     * Default no-argument constructor for serialization / deserialization.
     */
    public FolderMetadataDTO() {
    }

    /**
     * Parameterized constructor initializing all fields.
     *
     * @param id folder identifier
     * @param name folder display name
     * @param parentFolderId identifier of the parent folder, or null if at root
     * @param createdAt ISO-formatted creation timestamp string
     * @param updatedAt ISO-formatted last update timestamp string
     */
    public FolderMetadataDTO(Long id, String name, Long parentFolderId, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.parentFolderId = parentFolderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Static factory method to convert a {@link FolderMetadata} entity to a {@link FolderMetadataDTO}.
     *
     * @param folder the FolderMetadata entity
     * @return populated FolderMetadataDTO, or null if folder is null
     */
    public static FolderMetadataDTO fromEntity(FolderMetadata folder) {
        if (folder == null) {
            return null;
        }

        Long parentId = folder.getParentFolder() != null ? folder.getParentFolder().getId() : null;
        String createdAtStr = folder.getCreatedAt() != null ? folder.getCreatedAt().toString() : null;
        String updatedAtStr = folder.getUpdatedAt() != null ? folder.getUpdatedAt().toString() : null;

        return new FolderMetadataDTO(
                folder.getId(),
                folder.getName(),
                parentId,
                createdAtStr,
                updatedAtStr
        );
    }

    // ======================== GETTERS & SETTERS ========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
