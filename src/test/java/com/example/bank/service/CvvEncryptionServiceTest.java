package com.example.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class CvvEncryptionServiceTest {

    private CvvEncryptionService service;

    private static final String ENCRYPTION_KEY =
            "12345678901234567890123456789012";


    @BeforeEach
    void setUp() {

        service = new CvvEncryptionService();

        ReflectionTestUtils.setField(
                service,
                "encryptionKey",
                ENCRYPTION_KEY
        );
    }


    // =========================================================
    // encrypt()
    // =========================================================

    @Test
    void shouldEncryptCvv() {

        String cvv = "123";

        String encrypted = service.encrypt(cvv);

        assertNotNull(encrypted);
        assertFalse(encrypted.isBlank());

        assertNotEquals(cvv, encrypted);
    }


    @Test
    void shouldEncryptCvvIntoValidBase64() {

        String cvv = "123";

        String encrypted = service.encrypt(cvv);

        assertDoesNotThrow(() ->
                Base64.getDecoder().decode(encrypted)
        );
    }


    @Test
    void shouldGenerateDifferentCiphertextForSameCvv() {

        String cvv = "123";

        String encrypted1 = service.encrypt(cvv);
        String encrypted2 = service.encrypt(cvv);

        assertNotEquals(
                encrypted1,
                encrypted2
        );
    }


    @Test
    void shouldPreserveCvvAfterEncryptionAndDecryption() {

        String cvv = "123";

        String encrypted = service.encrypt(cvv);
        String decrypted = service.decrypt(encrypted);

        assertEquals(
                cvv,
                decrypted
        );
    }


    // =========================================================
    // decrypt()
    // =========================================================

    @Test
    void shouldDecryptEncryptedCvv() {

        String originalCvv = "456";

        String encrypted = service.encrypt(originalCvv);

        String decrypted = service.decrypt(encrypted);

        assertEquals(
                originalCvv,
                decrypted
        );
    }


    @Test
    void shouldWorkWithDifferentCvvValues() {

        String[] cvvs = {
                "000",
                "123",
                "456",
                "789",
                "999"
        };

        for (String cvv : cvvs) {

            String encrypted = service.encrypt(cvv);
            String decrypted = service.decrypt(encrypted);

            assertEquals(
                    cvv,
                    decrypted
            );
        }
    }


    @Test
    void shouldThrowExceptionWhenEncryptedValueIsInvalid() {

        String invalidEncryptedCvv =
                "not-valid-encrypted-data";

        assertThrows(
                IllegalStateException.class,
                () -> service.decrypt(invalidEncryptedCvv)
        );
    }


    @Test
    void shouldThrowExceptionWhenEncryptedDataIsTooShort() {

        String shortData = Base64.getEncoder()
                .encodeToString(new byte[5]);

        assertThrows(
                IllegalStateException.class,
                () -> service.decrypt(shortData)
        );
    }


    @Test
    void shouldNotDecryptWithDifferentEncryptionKey() {

        String encrypted = service.encrypt("123");

        CvvEncryptionService anotherService =
                new CvvEncryptionService();

        ReflectionTestUtils.setField(
                anotherService,
                "encryptionKey",
                "98765432109876543210987654321098"
        );

        assertThrows(
                IllegalStateException.class,
                () -> anotherService.decrypt(encrypted)
        );
    }

    @Test
    void shouldThrowExceptionWhenEncryptionKeyIsInvalid() {

        ReflectionTestUtils.setField(
                service,
                "encryptionKey",
                "invalid-key"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.encrypt("123")
        );

        assertEquals(
                "Не удалось зашифровать CVV",
                exception.getMessage()
        );

        assertNotNull(exception.getCause());
    }
}