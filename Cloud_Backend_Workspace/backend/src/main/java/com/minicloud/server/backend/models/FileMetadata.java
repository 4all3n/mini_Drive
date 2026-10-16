package com.minicloud.server.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a file stored in the Intelligent Cloud Drive.
 * 
 * Maps to the 'files' table in MariaDB/MySQL. Each file belongs to a single
 * owner (User) and can optionally be placed inside a virtual folder (FolderMetadata).
 * 
 * Physical storage uses UUID-prefixed filenames (storedName) to prevent
 * collisions, while originalName preserves the user-friendly display name.
 */
@Entity
@Table(name = "files")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The original, user-friendly filename as uploaded (e.g., "resume.pdf").
     * Displayed in the UI. Renamed via the rename endpoint.
     */
    @Column(nullable = false)
    private String fileName;

    /**
     * The UUID-prefixed filename stored on the physical disk
     * (e.g., "a1b2c3d4_resume.pdf"). Decouples display name from storage name,
     * guaranteeing zero filename collisions even across users.
     */
    private String storedName;

    private String fileType;
    private long fileSize;

    /**
     * Full path to the physical file on the server's disk.
     * Format: {storage.root.path}/{username}/{storedName}
     */
    private String serverFilePath;

    /**
     * Legacy string-based owner reference. Kept for backward compatibility
     * during migration. New code should use the 'owner' ManyToOne relationship.
     */
    private String uploadedBy;

    /**
     * Proper relational owner reference via Foreign Key to users table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    /**
     * The virtual folder this file belongs to. NULL means root level
     * (the user's "My Drive" top level).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderMetadata folder;

    private LocalDateTime uploadDate;
    private LocalDateTime updatedAt;

    /**
     * AI-predicted category (e.g., "Documents", "Code", "Media").
     * Set automatically by the FastAPI AI microservice during file upload.
     */
    private String category;

    // ======================== GETTERS & SETTERS ========================

    public Long getId() { return id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getServerFilePath() { return serverFilePath; }
    public void setServerFilePath(String serverFilePath) { this.serverFilePath = serverFilePath; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public FolderMetadata getFolder() { return folder; }
    public void setFolder(FolderMetadata folder) { this.folder = folder; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}