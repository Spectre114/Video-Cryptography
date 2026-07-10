package com.major.project.crypto.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opencv.core.Mat;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class EncryptServiceTest {

    @MockitoBean
    Mat frame1;

    @MockitoBean
    Mat frame2;

    @InjectMocks
    EncryptService encryptService;

    Path dummyVideoPath;

    @BeforeEach
    @SneakyThrows
    void setup() {
        dummyVideoPath = Files.createTempFile("original", ".mp4");
        Files.write(dummyVideoPath, "dummy".getBytes());
    }

    @Test
    public void sha256_generateHexTest() {
        String hexKey = encryptService.generateHexKey(dummyVideoPath, true);
        assertNotNull(hexKey);
    }

    @Test
    public void sha1_generateHexTest() {
        String hexKey = encryptService.generateHexKey(dummyVideoPath, false);
        assertNotNull(hexKey);
    }

    @Test
    public void encryptTest() {
        byte[] keyByte = new byte[1024];
        byte[] testFrame = encryptService.encrypt(dummyVideoPath, keyByte);
        assertNotNull(testFrame);
    }
}
