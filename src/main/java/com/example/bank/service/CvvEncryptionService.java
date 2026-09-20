package com.example.bank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CvvEncryptionService {

    @Value("${cvv.encryption-key}")
    private String encryptionKey;

    public String encrypt(String cvv) {

        try {
            byte[] iv = new byte[12];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            SecretKeySpec key = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(128, iv);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] encrypted = cipher.doFinal(
                    cvv.getBytes(StandardCharsets.UTF_8)
            );

            byte[] result = new byte[iv.length + encrypted.length];

            System.arraycopy(
                    iv, 0,
                    result, 0,
                    iv.length
            );

            System.arraycopy(
                    encrypted, 0,
                    result, iv.length, encrypted.length
            );

            return Base64.getEncoder()
                    .encodeToString(result);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Не удалось зашифровать CVV",
                    e
            );
        }
    }

    public String decrypt(String encryptedCvv) {

        try {
            byte[] data = Base64.getDecoder()
                    .decode(encryptedCvv);

            byte[] iv = new byte[12];
            byte[] encrypted = new byte[data.length - 12];

            System.arraycopy(
                    data,
                    0,
                    iv,
                    0,
                    iv.length
            );

            System.arraycopy(
                    data,
                    iv.length,
                    encrypted,
                    0,
                    encrypted.length
            );

            SecretKeySpec key = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );

            Cipher cipher = Cipher.getInstance(
                    "AES/GCM/NoPadding"
            );

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(128, iv);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    gcmSpec
            );

            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Не удалось расшифровать CVV",
                    e
            );
        }
    }
}