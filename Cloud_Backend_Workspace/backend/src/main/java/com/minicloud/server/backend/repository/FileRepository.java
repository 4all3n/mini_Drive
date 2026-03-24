package com.minicloud.server.backend.repository;

import com.minicloud.server.backend.models.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {
    // Custom query to find all files belonging to a specific user
    List<FileMetadata> findByUploadedBy(String username);
}