package com.major.project.crypto.service;

import java.nio.file.Files;
import java.nio.file.Path;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.major.project.crypto.module.VideoUtils;

@Service
@Getter
@Setter
public class EncryptService {


    private final String encryptionKey;

    public EncryptService(@Value("${encrypt.secret-key:}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    private int rotateBits(int value, int shift) {
        return ((value << shift) | (value >>> (8 - shift))) & 0xFF;
    }

    /**
     * Convert frames to bytes.
     *
     * @param video from the original video
     * @return bytes of frame
     */
    @SneakyThrows
    private byte[] videoToByte(Path video) {
        return Files.readAllBytes(video);
    }

    /**
     * Generate hex string for the frame.
     *
     * @param videopath from the original video
     * @param sha256 whether to use sha256 or sha1 for hex string
     * @return hex string
     */
    public String generateHexKey(Path videopath, boolean sha256) {
        return sha256 ? DigestUtils.sha256Hex(videoToByte(videopath)) : DigestUtils.sha1Hex(videoToByte(videopath));
    }

    /**
     * Encrypted the frame from the input video.
     *
     * @param videoPath from the original video
     * @param keyHexBytes bytes of generated hex string
     * @return encrypted frame
     */
    @SneakyThrows
    public byte[] encrypt(Path videoPath, byte[] keyHexBytes) {
        int keyLen = keyHexBytes.length;
        byte[] transformedKey = VideoUtils.transformKey(keyHexBytes);
        byte[] fileBytes = videoToByte(videoPath);
        for(int i = 0; i < fileBytes.length; i++) {
            fileBytes[i] = (byte) ((fileBytes[i] ^ transformedKey[i % keyLen]) & 0xFF);
        }
        return fileBytes;
    }

}
