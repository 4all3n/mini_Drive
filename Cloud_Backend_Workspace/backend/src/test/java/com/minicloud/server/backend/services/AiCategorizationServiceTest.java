package com.minicloud.server.backend.services;

import com.minicloud.server.backend.models.FileMetadata;
import com.minicloud.server.backend.models.User;
import com.minicloud.server.backend.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AiCategorizationServiceTest {

    private FileRepository fileRepository;

    @BeforeEach
    void setUp() {
        fileRepository = mock(FileRepository.class);
    }

    @Test
    void testIsOnlineReturnsFalseWhenServiceDown() {
        // Points to an unreachable port
        AiCategorizationService service = new AiCategorizationService("http://127.0.0.1:59999/predict", fileRepository);
        assertFalse(service.isOnline());
    }

    @Test
    void testAnalyzeAndCategorizeFallbackWhenOffline() {
        AiCategorizationService service = new AiCategorizationService("http://127.0.0.1:59999/predict", fileRepository);
        String category = service.analyzeAndCategorize("document.pdf");
        assertEquals("Uncategorized", category);
    }

    @Test
    void testRecategorizeUncategorizedFilesWhenOffline() {
        AiCategorizationService service = new AiCategorizationService("http://127.0.0.1:59999/predict", fileRepository);

        FileMetadata file1 = new FileMetadata();
        file1.setId(1L);
        file1.setFileName("test.pdf");
        file1.setCategory("Uncategorized");

        when(fileRepository.findUncategorizedFiles()).thenReturn(List.of(file1));

        int updated = service.recategorizeUncategorizedFiles();
        // Since AI is offline, nothing should be updated
        assertEquals(0, updated);
        verify(fileRepository, never()).save(any());
    }

    @Test
    void testRecategorizeWhenOnlineWithMockServer() {
        // Subclass to simulate online state and mock responses
        AiCategorizationService service = new AiCategorizationService("http://127.0.0.1:8000/predict", fileRepository) {
            @Override
            public boolean isOnline() {
                return true;
            }

            @Override
            public String analyzeAndCategorize(String fileName) {
                if (fileName.endsWith(".pdf")) return "Documents";
                if (fileName.endsWith(".py")) return "Code";
                if (fileName.endsWith(".mp3")) return "Media";
                return "Uncategorized";
            }
        };

        FileMetadata f1 = new FileMetadata();
        f1.setId(1L);
        f1.setFileName("annual_report.pdf");
        f1.setCategory("Uncategorized");

        FileMetadata f2 = new FileMetadata();
        f2.setId(2L);
        f2.setFileName("script.py");
        f2.setCategory(null);

        FileMetadata f3 = new FileMetadata();
        f3.setId(3L);
        f3.setFileName("song.mp3");
        f3.setCategory("");

        when(fileRepository.findUncategorizedFiles()).thenReturn(Arrays.asList(f1, f2, f3));

        int updated = service.recategorizeUncategorizedFiles();

        assertEquals(3, updated);
        assertEquals("Documents", f1.getCategory());
        assertEquals("Code", f2.getCategory());
        assertEquals("Media", f3.getCategory());
        verify(fileRepository, times(3)).save(any(FileMetadata.class));
    }

    @Test
    void testRecategorizeUserFilesScopedToOwner() {
        User user = new User();
        user.setId(10L);
        user.setUsername("testuser");

        AiCategorizationService service = new AiCategorizationService("http://127.0.0.1:8000/predict", fileRepository) {
            @Override
            public boolean isOnline() {
                return true;
            }

            @Override
            public String analyzeAndCategorize(String fileName) {
                return "Documents";
            }
        };

        FileMetadata f1 = new FileMetadata();
        f1.setId(100L);
        f1.setFileName("notes.txt");
        f1.setCategory("Uncategorized");
        f1.setOwner(user);

        when(fileRepository.findUncategorizedFilesByOwner(user)).thenReturn(Collections.singletonList(f1));

        int updated = service.recategorizeUserFiles(user);

        assertEquals(1, updated);
        assertEquals("Documents", f1.getCategory());
        verify(fileRepository, times(1)).save(f1);
    }
}
