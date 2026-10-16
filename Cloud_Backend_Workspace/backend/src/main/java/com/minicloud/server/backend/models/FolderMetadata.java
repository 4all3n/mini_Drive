package com.minicloud.server.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a virtual folder in the Intelligent Cloud Drive.
 *
 * Folders provide a hierarchical file organization system similar to Google Drive.
 * Folders support arbitrary nesting via a self-referencing ManyToOne relationship:
 * a parentFolder of {@code null} signifies a root-level folder in the user's drive.
 *
 * Multi-tenancy is enforced by associating every folder directly with an owner (User).
 */
@Entity
@Table(name = "folders")
public class FolderMetadata {

    /**
     * Unique identifier for the folder (Primary Key, auto-incremented).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Display name of the folder (e.g., "Documents", "Projects").
     */
    @Column(nullable = false)
    private String name;

    /**
     * Owner of the folder. Every folder belongs to exactly one user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Self-referencing relationship to the parent folder.
     * If {@code null}, this folder resides at the root level ("My Drive").
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FolderMetadata parentFolder;

    /**
     * Timestamp when the folder was created.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the folder was last modified or renamed.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Default no-argument constructor required by JPA.
     */
    public FolderMetadata() {
    }

    /**
     * Parameterized constructor for creating a new folder.
     *
     * @param name display name of the folder
     * @param owner user who owns this folder
     * @param parentFolder parent folder, or null if at root level
     */
    public FolderMetadata(String name, User owner, FolderMetadata parentFolder) {
        this.name = name;
        this.owner = owner;
        this.parentFolder = parentFolder;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public FolderMetadata getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(FolderMetadata parentFolder) {
        this.parentFolder = parentFolder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
