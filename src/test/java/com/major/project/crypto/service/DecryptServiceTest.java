package com.major.project.crypto.service;

import nu.pattern.OpenCV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        Mat testFrame = decryptService.decrypt(frame, dummyBytes);
        assertNotNull(testFrame);
    }
}
