package com.major.project.crypto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class VideoUtils {

    String inputFilePath;
    byte[] metadata;
    byte[] encryptedBytes;

    private static int rotateBits(int value, int shift) {
        return ((value << shift) | (value >>> (8 - shift))) & 0xFF;
    }

    public static byte[] transformKey(byte[] keyHexBytes) {
        int keyLen = keyHexBytes.length;

        for(int i = 0; i < keyLen; i++) {
            int rotatedKey = rotateBits(keyHexBytes[i] & 0xFF, 3);
            int shiftedKey = rotateBits(keyHexBytes[i] & 0xFF, 5);

            keyHexBytes[i] = (byte)((shiftedKey ^ rotatedKey) & 0xFF);
        }

        return keyHexBytes;
    }
}
