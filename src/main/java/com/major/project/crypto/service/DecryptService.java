package com.major.project.crypto.service;

import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

@Service
public class DecryptService {

    private int rotateBits(int value, int shift) {
        return ((value << shift) | (value >>> (8 - shift))) & 0xFF;
    }

    public int shiftBitsRight(int value, int shift) {
        return ((value >>> shift) | (value << (8 - shift))) & 0xFF;
    }

    /**
     * Decrypt the encrypted frame.
     *
     * @param frame encrypted frame
     * @param keyHexBytes bytes of generated hex string
     * @return decrypted frame
     */
    public Mat decrypt(Mat frame, byte[] keyHexBytes) {
        int rows = frame.rows();
        int cols = frame.cols();
        int channels = frame.channels();
        int keyLen = keyHexBytes.length;

        Mat decryptedFrame = new Mat(rows, cols, frame.type());

        int totalPixels = rows * cols;
        for (int idxPixel = 0; idxPixel < totalPixels; idxPixel++) {
            int i = idxPixel / cols;  // row index
            int j = idxPixel % cols;  // column index

            double[] encryptedPixel = frame.get(i, j); // [B, G, R]
            double[] originalPixel = new double[channels];

            for (int k = 0; k < channels; k++) {
                int idx = (i + j + k) % keyLen;
                // Use same rotation as encryption - XOR will undo the encryption
                int rotatedKey = rotateBits(keyHexBytes[idx] & 0xFF, 3);

                // Undo XOR first
                int shifted = (int) encryptedPixel[k] ^ rotatedKey;

                // Undo shiftBits by applying reverse shift (circular right shift)
                originalPixel[k] = shiftBitsRight(shifted, 5);
            }

            decryptedFrame.put(i, j, originalPixel.clone());
        }

        return decryptedFrame;
    }

}
