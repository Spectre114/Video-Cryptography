package com.major.project.crypto.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opencv.core.CvType;
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

    @BeforeEach
    public void setUp() {
        OpenCV.loadLocally();
        frame1 = new Mat(10,10, CvType.CV_8UC1);
        frame2 = new Mat(10,10, CvType.CV_8UC3);
    }

    @Test
    public void grayFrameToByteTest() {

        byte[] dummyFrame = encryptService.frameToByte(frame1);
        assertNotNull(dummyFrame);
    }

    @Test
    public void colorFrameToByteTest() {

        byte[] dummyFrame = encryptService.frameToByte(frame2);
        assertNotNull(dummyFrame);
    }

    @Test
    public void sha256_generateHexTest() {
        byte[] dummyFrame = encryptService.frameToByte(frame2);
        String hexKey = encryptService.generateHexKey(dummyFrame, true);
        assertNotNull(hexKey);
    }

    @Test
    public void sha1_generateHexTest() {
        byte[] dummyFrame = new byte[1024];
        String hexKey = encryptService.generateHexKey(dummyFrame, false);
        assertNotNull(hexKey);
    }

    @Test
    public void encryptTest() {
        byte[] keyByte = new byte[1024];
        Mat testFrame = encryptService.encrypt(frame2, keyByte);
        assertNotNull(testFrame);
    }
}
