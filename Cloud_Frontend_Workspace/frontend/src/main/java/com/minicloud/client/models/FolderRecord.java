package com.minicloud.client.models;

/**
 * Client-side model representing a virtual folder.
 */
public class FolderRecord {
    private Long id;
    private String name;
    private Long parentFolderId;
    private String createdAt;
    private String updatedAt;
    private String location;

    public FolderRecord() {}

    public FolderRecord(Long id, String name, Long parentFolderId, String createdAt, String updatedAt) {
        this(id, name, parentFolderId, createdAt, updatedAt, "My Drive");
    }

    public FolderRecord(Long id, String name, Long parentFolderId, String createdAt, String updatedAt, String location) {
        this.id = id;
        this.name = name;
        this.parentFolderId = parentFolderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(Long parentFolderId) { this.parentFolderId = parentFolderId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLocation() { return location != null ? location : "My Drive"; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return name;
    }
}
