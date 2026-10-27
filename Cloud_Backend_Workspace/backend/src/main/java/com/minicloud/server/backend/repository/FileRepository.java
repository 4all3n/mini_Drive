package com.minicloud.server.backend.repository;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.models.FolderMetadata;
import com.minicloud.server.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for FileMetadata entities.
 * 
 * Spring Data JPA auto-generates SQL implementations for all declared methods
 * based on the method naming convention (Derived Query Methods).
 */
@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * Legacy query: find files by the string-based uploadedBy field.
     * Retained for backward compatibility during migration.
     */
    List<FileMetadata> findByUploadedBy(String username);

    /**
     * Find all files owned by a specific user inside a specific folder.
     */
    List<FileMetadata> findByOwnerAndFolder(User owner, FolderMetadata folder);

    /**
     * Find all files owned by a specific user at root level (no parent folder).
     */
    List<FileMetadata> findByOwnerAndFolderIsNull(User owner);

    /**
     * Find a specific file by ID ensuring the authenticated user owns it (IDOR prevention).
     */
    Optional<FileMetadata> findByIdAndOwner(Long id, User owner);

    /**
     * Find all files owned by a user (regardless of folder) for stats/search.
     */
    List<FileMetadata> findByOwner(User owner);

    /**
     * Find all files inside a specific folder (for recursive deletion).
     */
    List<FileMetadata> findByFolder(FolderMetadata folder);

    /**
     * Calculate total storage used by a specific user.
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.owner = :owner")
    long sumFileSizeByOwner(@Param("owner") User owner);

    /**
     * Search files owned by a user matching a case-insensitive query substring.
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.owner = :owner AND LOWER(f.fileName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<FileMetadata> searchByOwnerAndFileName(@Param("owner") User owner, @Param("query") String query);

    /**
     * Find all files that are uncategorized (e.g., uploaded while AI service was offline).
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.category = 'Uncategorized' OR f.category IS NULL OR TRIM(f.category) = ''")
    List<FileMetadata> findUncategorizedFiles();

    /**
     * Count uncategorized files to perform cheap checks before processing.
     */
    @Query("SELECT COUNT(f) FROM FileMetadata f WHERE f.category = 'Uncategorized' OR f.category IS NULL OR TRIM(f.category) = ''")
    long countUncategorizedFiles();

    /**
     * Find uncategorized files owned by a specific user.
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.owner = :owner AND (f.category = 'Uncategorized' OR f.category IS NULL OR TRIM(f.category) = '')")
    List<FileMetadata> findUncategorizedFilesByOwner(@Param("owner") User owner);
}