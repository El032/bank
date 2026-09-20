package com.example.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CvvGeneratorTest {

    private CvvGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CvvGenerator();
    }

    @Test
    void shouldGenerateCvv() {
        String cvv = generator.generate();

        assertNotNull(cvv);
    }

    @Test
    void shouldGenerateExactlyThreeCharacters() {
        String cvv = generator.generate();

        assertEquals(3, cvv.length());
    }

    @Test
    void shouldGenerateOnlyDigits() {
        String cvv = generator.generate();

        assertTrue(cvv.matches("\\d{3}"));
    }

    @Test
    void shouldGenerateCvvWithLeadingZeros() {
        boolean foundLeadingZero = false;

        for (int i = 0; i < 1000; i++) {
            String cvv = generator.generate();

            if (cvv.startsWith("0")) {
                foundLeadingZero = true;
                break;
            }
        }

        assertTrue(foundLeadingZero);
    }

    @Test
    void shouldGenerateDifferentCvvs() {
        Set<String> cvvs = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            cvvs.add(generator.generate());
        }

        assertTrue(cvvs.size() > 1);
    }

    @Test
    void shouldGenerateExactlyThreeDigitCvvs() {
        for (int i = 0; i < 100; i++) {
            String cvv = generator.generate();

            assertEquals(3, cvv.length());
            assertTrue(cvv.matches("\\d{3}"));
        }
    }
}