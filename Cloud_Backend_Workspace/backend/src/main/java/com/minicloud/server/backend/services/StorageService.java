package com.minicloud.server.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service responsible for all physical file system operations.
 *
 * Encapsulates storage logic (save, delete, copy) behind a clean interface,
 * isolating controllers from direct java.nio.file calls. Each user gets their
 * own subdirectory under the configurable root path to prevent cross-user collisions.
 *
 * Key design decision: Files are stored with UUID-prefixed names on disk
 * (e.g., "a1b2c3d4_resume.pdf") to guarantee uniqueness, while the original
 * display name is kept in the database only.
 */
@Service
public class StorageService {

    private final Path rootPath;

    /**
     * Constructor with root path injection from application.properties.
     * Falls back to "./cloud_storage" if property is absent.
     */
    public StorageService(@Value("${storage.root.path:./cloud_storage}") String rootPath) {
        this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
    }

    /**
     * Creates the per-user storage directory if it does not exist.
     * Called during user registration to ensure the directory is ready
     * before any file uploads.
     *
     * @param username the username whose directory to create
     * @throws IOException if directory creation fails
     */
    public void createUserDirectory(String username) throws IOException {
        Path userDir = rootPath.resolve(username);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }
    }

    /**
     * Saves a file to the user's storage directory with a UUID-prefixed filename.
     *
     * @param username        the owner's username
     * @param originalFileName the user-supplied filename (sanitized internally)
     * @param fileBytes        the raw file content
     * @return the stored filename (UUID + underscore + sanitized original name)
     * @throws IOException              if writing fails
     * @throws SecurityException        if path traversal is detected
     */
    public String saveFile(String username, String originalFileName, byte[] fileBytes) throws IOException {
        String cleanName = StringUtils.cleanPath(originalFileName);
        if (cleanName.contains("..")) {
            throw new SecurityException("Invalid filename: directory traversal not allowed.");
        }

        String storedName = UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "_" + cleanName;
        Path userDir = rootPath.resolve(username);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        Path targetPath = userDir.resolve(storedName).normalize();
        // Final path traversal check
        if (!targetPath.startsWith(userDir)) {
            throw new SecurityException("Path traversal attempt blocked.");
        }

        Files.write(targetPath, fileBytes);
        return storedName;
    }

    /**
     * Returns the absolute Path to a stored file.
     *
     * @param username      the file owner's username
     * @param storedFileName the UUID-prefixed filename on disk
     * @return resolved absolute Path
     */
    public Path getFilePath(String username, String storedFileName) {
        return rootPath.resolve(username).resolve(storedFileName).normalize();
    }

    /**
     * Deletes a physical file from the user's storage directory.
     *
     * @param username      the file owner's username
     * @param storedFileName the UUID-prefixed filename to delete
     * @throws IOException if deletion fails
     */
    public void deleteFile(String username, String storedFileName) throws IOException {
        Path filePath = getFilePath(username, storedFileName);
        Files.deleteIfExists(filePath);
    }

    /**
     * Creates a copy of an existing file with a new UUID prefix.
     * Used by the "Copy" file operation. The new copy is in the same
     * user directory since folder placement is virtual (DB-only).
     *
     * @param username          the file owner's username
     * @param existingStoredName the current stored filename on disk
     * @param originalFileName   the display name for the new copy
     * @return the new stored filename for the copy
     * @throws IOException if copying fails
     */
    public String copyFile(String username, String existingStoredName, String originalFileName) throws IOException {
        Path source = getFilePath(username, existingStoredName);
        String cleanName = StringUtils.cleanPath(originalFileName);
        String newStoredName = UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "_" + cleanName;
        Path target = rootPath.resolve(username).resolve(newStoredName);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return newStoredName;
    }
}
