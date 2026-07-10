package com.major.project.crypto.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShutdownCleanup {

    @PreDestroy
    public void cleanup() {
        LOGGER.info("Deleting encrypted file");
        cleanupUploadedVideos();
    }

    private void cleanupUploadedVideos() {
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploaded-videos");

        if (!Files.exists(uploadDir)) {
            LOGGER.info("Upload directory does not exist: {}", uploadDir);
            return;
        }

        try (var paths = Files.list(uploadDir)) {
            paths.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                    LOGGER.info("Deleted: {}", path.getFileName());
                } catch (IOException e) {
                    LOGGER.error("Failed to delete {}", path, e);
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to clean upload directory", e);
        }
    }
}