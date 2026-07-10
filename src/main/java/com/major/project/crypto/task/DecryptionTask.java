package com.major.project.crypto.task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.major.project.crypto.module.VideoUtils;
import com.major.project.crypto.service.DecryptService;

@Slf4j
@Component
public class DecryptionTask {

    private final VideoUtils videoUtils;
    private final String decryptedVideo;
    private final DecryptService decrypt;

    @Autowired
    public DecryptionTask(DecryptService decrypt,
                          @Value("${video.decrypted-video}") String decryptedVideo,
                          VideoUtils videoUtils) {
        this.decrypt = decrypt;
        this.decryptedVideo = decryptedVideo;
        this.videoUtils = videoUtils;
    }

    @SneakyThrows
    public void decrypt() {
        LOGGER.info("Decryption Started");
        byte[] encryptedBytes = videoUtils.getEncryptedBytes();
        byte[] keyHexBytes = videoUtils.getMetadata();
        byte[] transformedKey = Arrays.copyOf(keyHexBytes, keyHexBytes.length);
        byte[] decryptedBytes = decrypt.decrypt(encryptedBytes, transformedKey);

        Path decryptedPath = Paths.get(decryptedVideo);
        Files.write(decryptedPath, decryptedBytes);

        byte[] original = Files.readAllBytes(Paths.get(videoUtils.getInputFilePath()));
        LOGGER.info("Decryption {}", Arrays.equals(original, decryptedBytes) ? "SUCCESSFUL" : "FAILED");
    }
}
