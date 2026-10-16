package com.minicloud.client.models;

/**
 * Unified model for displaying both Folders and Files in the central Drive table.
 * Supports icon differentiation (📁 vs 📄), type display, size, and category.
 */
public class DriveItem {

    private Long id;
    private String name;
    private String displayName;
    private String type;
    private String size;
    private String category;
    private boolean folder;
    private String uploadDate;
    private Long parentFolderId;
    private String location;

    public DriveItem() {}

    public DriveItem(Long id, String name, String type, String size, String category, boolean folder, String uploadDate, Long parentFolderId) {
        this(id, name, type, size, category, folder, uploadDate, parentFolderId, "My Drive");
    }

    public DriveItem(Long id, String name, String type, String size, String category, boolean folder, String uploadDate, Long parentFolderId, String location) {
        this.id = id;
        this.name = name;
        this.displayName = (folder ? "📁  " : "📄  ") + name;
        this.type = type;
        this.size = size;
        this.category = category;
        this.folder = folder;
        this.uploadDate = uploadDate;
        this.parentFolderId = parentFolderId;
        this.location = (location != null && !location.isEmpty()) ? location : "My Drive";
    }

    public static DriveItem fromFolder(FolderRecord folder) {
        return new DriveItem(
                folder.getId(),
                folder.getName(),
                "Folder",
                "--",
                "Folder",
                true,
                folder.getCreatedAt(),
                folder.getParentFolderId(),
                folder.getLocation()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        this.displayName = (folder ? "📁  " : "📄  ") + name;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isFolder() { return folder; }
    public void setFolder(boolean folder) {
        this.folder = folder;
        this.displayName = (folder ? "📁  " : "📄  ") + name;
    }

    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }

    public Long getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(Long parentFolderId) { this.parentFolderId = parentFolderId; }

    public String getLocation() {
        return location != null && !location.isEmpty() ? location : "My Drive";
    }
    public void setLocation(String location) {
        this.location = location;
    }
}
