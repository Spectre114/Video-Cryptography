package com.major.project.crypto.service;

import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

@Service
public class DecryptService {

    public int rotateBitsRight(int value, int shift) {
        return ((value >>> shift) | (value << (8 - shift))) & 0xFF;
    }

    public int shiftBitsRight(int value, int shift) {
        return ((value >>> shift) | (value << (8 - shift))) & 0xFF;
    }

    public Mat decryptFrame(Mat frame, byte[] keyHexBytes) {
        int rows = frame.rows();
        int cols = frame.cols();
        int channels = frame.channels();
        int keyLen = keyHexBytes.length;

        Mat decryptedFrame = new Mat(rows, cols, frame.type());

        double[] encryptedPixel;
        double[] originalPixel = new double[channels];

        int totalPixels = rows * cols;

        for (int index = 0; index < totalPixels; index++) {
            int i = index / cols;
            int j = index % cols;

            encryptedPixel = frame.get(i, j);

            for (int k = 0; k < channels; k++) {
                int idx = (i + j + k) % keyLen;
                originalPixel[k] = shiftBitsRight((int) encryptedPixel[k], 5);
            }

            decryptedFrame.put(i, j, originalPixel);
        }

        return decryptedFrame;
    }
}
