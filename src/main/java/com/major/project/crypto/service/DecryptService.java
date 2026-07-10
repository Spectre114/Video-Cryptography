package com.major.project.crypto.service;

import org.springframework.stereotype.Service;

import com.major.project.crypto.module.VideoUtils;

@Service
public class DecryptService {

    /**
     * Decrypt the encrypted frame.
     *
     * @param encryptedBytes encrypted frame
     * @param keyHexBytes bytes of generated hex string
     * @return decrypted frame
     */
    public byte[] decrypt(byte[] encryptedBytes, byte[] keyHexBytes) {
        int keyLen = keyHexBytes.length;

        byte[] transformedKey = VideoUtils.transformKey(keyHexBytes);
        for (int i = 0; i < encryptedBytes.length; i++) {
            encryptedBytes[i] = (byte) (encryptedBytes[i] ^ transformedKey[i % keyLen]);
        }

        return encryptedBytes;
    }

}
