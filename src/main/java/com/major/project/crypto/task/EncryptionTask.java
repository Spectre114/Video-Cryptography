package com.major.project.crypto.task;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.major.project.crypto.module.VideoUtils;
import com.major.project.crypto.service.EncryptService;

@Component
@Getter
@Slf4j
public class EncryptionTask {

    private final VideoUtils videoUtils;
    private final String encryptedFile;
    private final boolean sha256;
    private final EncryptService encrypt;
    private final Boolean showEncryptedVideo;
    private final String encryptedVid;

    @Autowired
    public EncryptionTask(@Value("${video.output-encrypted}") String encryptedFile,
                          @Value("${encrypt.mode:true}") boolean sha256,
                          @Value("${video.output-encrypted}") String encryptedVid,
                          @Value("${video.show-encrypted:false}") Boolean showEncryptedVideo,
                          EncryptService encrypt,
                          VideoUtils videoUtils) {
        this.encryptedFile = encryptedFile;
        this.sha256 = sha256;
        this.showEncryptedVideo = showEncryptedVideo;
        this.encryptedVid = encryptedVid;
        this.encrypt = encrypt;
        this.videoUtils = videoUtils;
    }

    @SneakyThrows
    public void encrypt() {
        LOGGER.info("Encryption Started");

        Path videoPath = Paths.get(videoUtils.getInputFilePath());
        String keyHex = encrypt.generateHexKey(videoPath, sha256);
        byte[] keyHexBytes = keyHex.getBytes(StandardCharsets.UTF_8);
        videoUtils.setMetadata(keyHexBytes);
        byte[] transformedKey = Arrays.copyOf(keyHexBytes, keyHexBytes.length);
        byte[] encryptedBytes = encrypt.encrypt(videoPath, transformedKey);
        videoUtils.setEncryptedBytes(encryptedBytes);

        if (showEncryptedVideo) {
            Path encryptedPath = Paths.get(encryptedFile);
            Files.write(encryptedPath, encryptedBytes);
        }
        LOGGER.info("Encryption completed");
    }
}
