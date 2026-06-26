package com.major.project.crypto.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class ShutdownCleanup {

    private final String decryptedVideo;

    private final String encryptedVideo;

    public ShutdownCleanup(@Value("${video.decrypted-video}") String decryptedVideo,
                           @Value("${video.output-encrypted}") String encryptedVideo) {
        this.decryptedVideo = decryptedVideo;
        this.encryptedVideo = encryptedVideo;
    }
    @PreDestroy
    public void cleanup() {
        LOGGER.info("Deleting decrypted and encrypted video");
        deleteFile(decryptedVideo);
        deleteFile(encryptedVideo);
    }

    private void deleteFile(String filePath) {
        try {
            Path path = Path.of(filePath);

            if (Files.exists(path)) {
                Files.delete(path);
                LOGGER.info("Deleted file: {}", filePath);
            } else {
                LOGGER.info("File not found: {}", filePath);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to delete file: {}", filePath, e);
        }
    }
}