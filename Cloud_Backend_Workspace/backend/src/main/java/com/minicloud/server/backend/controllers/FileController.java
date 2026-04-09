package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.repository.FileRepository;
import com.minicloud.server.backend.services.AiCategorizationService; // IMPORT THE AI SERVICE
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AiCategorizationService aiService; // INJECT THE AI SERVICE

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/cloud_storage/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file, 
            @RequestParam("username") String username) {
        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);

            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(file.getOriginalFilename());
            metadata.setFileType(file.getContentType());
            metadata.setFileSize(file.getSize());
            metadata.setServerFilePath(path.toString());
            metadata.setUploadedBy(username);
            metadata.setUploadDate(LocalDateTime.now());

            // --- ASK THE AI FOR THE CATEGORY ---
            String category = aiService.analyzeAndCategorize(file.getOriginalFilename());
            metadata.setCategory(category);

            fileRepository.save(metadata);

            return ResponseEntity.status(HttpStatus.OK).body("SUCCESS");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file.");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<String> getUserFiles(@RequestParam("username") String username) {
        List<FileMetadata> files = fileRepository.findByUploadedBy(username);
        StringBuilder sb = new StringBuilder();
        for (FileMetadata f : files) {
            sb.append(f.getId()).append("|")
              .append(f.getFileName()).append("|")
              .append(f.getFileType()).append("|")
              .append(f.getFileSize()).append("|")
              .append(f.getCategory()).append(";;;"); // ADDED CATEGORY TO THE STRING
        }
        return ResponseEntity.ok(sb.toString());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long id) {
        try {
            FileMetadata metadata = fileRepository.findById(id).orElseThrow();
            Path filePath = Paths.get(metadata.getServerFilePath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + metadata.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            FileMetadata metadata = fileRepository.findById(id).orElseThrow();
            Path filePath = Paths.get(metadata.getServerFilePath());
            Files.deleteIfExists(filePath);
            fileRepository.deleteById(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed");
        }
    }
}