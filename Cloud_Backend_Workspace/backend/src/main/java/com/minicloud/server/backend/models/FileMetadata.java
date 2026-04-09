package com.minicloud.server.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    private String fileType;
    private long fileSize; 
    private String serverFilePath; 
    private String uploadedBy; 
    private LocalDateTime uploadDate;

    // --- NEW: The AI Category ---
    private String category;

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getServerFilePath() { return serverFilePath; }
    public void setServerFilePath(String serverFilePath) { this.serverFilePath = serverFilePath; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    
    // Getter and Setter for Category
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}