package com.minicloud.server.backend.controllers;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.repository.FileRepository;
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

    // Define a folder on Laptop 2 to store the physical files
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/cloud_storage/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file, 
            @RequestParam("username") String username) {
        
        try {
            // 1. Create the storage directory if it doesn't exist
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. Save the physical file to the server's hard drive
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);

            // 3. Create the Metadata object
            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(file.getOriginalFilename());
            metadata.setFileType(file.getContentType());
            metadata.setFileSize(file.getSize());
            metadata.setServerFilePath(path.toString());
            metadata.setUploadedBy(username);
            metadata.setUploadDate(LocalDateTime.now());

            // 4. Save metadata to MySQL
            fileRepository.save(metadata);

            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file.");
        }
    }

    // --- ADD THIS NEW METHOD ---
    @GetMapping("/list")
    public ResponseEntity<String> getUserFiles(@RequestParam("username") String username) {
        // 1. Ask the database for all files uploaded by this specific user
        List<FileMetadata> files = fileRepository.findByUploadedBy(username);
        
        // 2. Build a simple custom string to send back (e.g., file1.pdf|PDF|1024;;;file2.png|PNG|2048)
        StringBuilder sb = new StringBuilder();
        for (FileMetadata f : files) {
            sb.append(f.getFileName()).append("|")
              .append(f.getFileType()).append("|")
              .append(f.getFileSize()).append(";;;"); // Using ;;; to separate different files
        }
        
        return ResponseEntity.ok(sb.toString());
    }
}