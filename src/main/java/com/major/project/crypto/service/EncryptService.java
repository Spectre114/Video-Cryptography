package com.major.project.crypto.service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class EncryptService {

    private int rotateBits(int value, int shift) {
        return ((value << shift) | (value >>> (8 - shift))) & 0xFF;
    }

    private int shiftBits(int value, int shift) {
        return ((value << shift) | (value >>> (8 - shift))) & 0xFF;
    }

    public byte[] frameToByte(Mat frame) {
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return data;
    }

    public String generateHexKey(byte[] frame, boolean sha256) {
        return sha256 ? DigestUtils.sha256Hex(frame) : DigestUtils.sha1Hex(frame);
    }


    public Mat encrypt(Mat frame, byte[] keyHexBytes) {
        int rows = frame.rows();
        int cols = frame.cols();
        int channels = frame.channels();
        int keyLen = keyHexBytes.length;

        Mat encryptedFrame = new Mat(rows, cols, frame.type());

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double[] pixel = frame.get(i, j); // pixel has [B, G, R]
                double[] encryptedPixel = new double[channels];

                for (int k = 0; k < channels; k++) {
                    // index computation like (i + j + k) % len(key)
                    int idx = (i + j + k) % keyLen;
                    int rotatedKey = rotateBits(keyHexBytes[idx] & 0xFF, 3);
                    int shiftedPixel = shiftBits((int) pixel[k], 5);
                    encryptedPixel[k] = shiftedPixel ^ rotatedKey;
                }

                encryptedFrame.put(i, j, encryptedPixel);
            }
        }

        return encryptedFrame;
    }
}
