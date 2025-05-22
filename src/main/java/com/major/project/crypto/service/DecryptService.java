package com.major.project.crypto.service;

import java.util.HashMap;

import org.opencv.core.Mat;

import org.springframework.stereotype.Service;

@Service
public class DecryptService {

    HashMap<Integer, String> metadataMap = new HashMap<>();

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

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double[] encryptedPixel = frame.get(i, j); // [B, G, R]
                double[] originalPixel = new double[channels];

                for (int k = 0; k < channels; k++) {
                    int idx = (i + j + k) % keyLen;
                    int rotatedKey = rotateBitsRight(keyHexBytes[idx] & 0xFF, 3);

                    // Undo XOR
                    int shifted = (int) encryptedPixel[k] ^ rotatedKey;

                    // Undo shiftBits by applying reverse shift (circular right shift)
                    originalPixel[k] = shiftBitsRight(shifted, 5);
                }

                decryptedFrame.put(i, j, originalPixel);
            }
        }

        return decryptedFrame;
    }
}
