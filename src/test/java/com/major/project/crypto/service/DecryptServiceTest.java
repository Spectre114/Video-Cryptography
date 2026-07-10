package com.major.project.crypto.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import nu.pattern.OpenCV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
public class DecryptServiceTest {

    @MockitoBean
    Mat frame;

    @InjectMocks
    DecryptService decryptService;

    @BeforeEach
    public void setUp() {
        OpenCV.loadLocally();
        frame = new Mat(10, 10, CvType.CV_8UC3);
    }

    @Test
    public void decryptFrameTest() {

        byte[] dummyBytes = new byte[1024];
        byte[] dummyKey = new byte[1024];
        byte[] testFrame = decryptService.decrypt(dummyBytes, dummyKey);
        assertNotNull(testFrame);
    }
}
