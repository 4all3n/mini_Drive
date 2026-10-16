package com.minicloud.server.backend.repository;

import com.minicloud.server.backend.models.FolderMetadata;
import com.minicloud.server.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for FolderMetadata entities.
 *
 * Spring Data JPA auto-generates SQL implementations for all declared methods
 * based on the method naming convention (Derived Query Methods).
 */
@Repository
public interface FolderRepository extends JpaRepository<FolderMetadata, Long> {

    /** Find all folders owned by a specific user across all levels. */
    List<FolderMetadata> findByOwner(User owner);

    /** Find all subfolders inside a specific parent folder for a given owner. */
    List<FolderMetadata> findByOwnerAndParentFolder(User owner, FolderMetadata parentFolder);

    /** Find all root-level folders for a given owner (parentFolder is null). */
    List<FolderMetadata> findByOwnerAndParentFolderIsNull(User owner);

    /** Find a specific folder by ID, ensuring the authenticated user owns it (IDOR prevention). */
    Optional<FolderMetadata> findByIdAndOwner(Long id, User owner);

    /** Find all child folders inside a parent (for recursive deletion). */
    List<FolderMetadata> findByParentFolder(FolderMetadata parentFolder);
}
