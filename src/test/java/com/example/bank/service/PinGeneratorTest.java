package com.example.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PinGeneratorTest {

    private PinGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PinGenerator();
    }

    @Test
    void shouldGeneratePin() {
        String pin = generator.generate();

        assertNotNull(pin);
    }

    @Test
    void shouldGenerateExactlyFourCharacters() {
        String pin = generator.generate();

        assertEquals(4, pin.length());
    }

    @Test
    void shouldGenerateOnlyDigits() {
        String pin = generator.generate();

        assertTrue(pin.matches("\\d{4}"));
    }

    @Test
    void shouldGeneratePinWithLeadingZeros() {
        boolean foundLeadingZero = false;

        for (int i = 0; i < 1000; i++) {
            String pin = generator.generate();

            if (pin.startsWith("0")) {
                foundLeadingZero = true;
                break;
            }
        }

        assertTrue(foundLeadingZero);
    }

    @Test
    void shouldGenerateDifferentPins() {
        Set<String> pins = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            pins.add(generator.generate());
        }

        assertTrue(pins.size() > 1);
    }

    @Test
    void shouldGenerateExactlyFourDigitPins() {
        for (int i = 0; i < 100; i++) {
            String pin = generator.generate();

            assertEquals(4, pin.length());
            assertTrue(pin.matches("\\d{4}"));
        }
    }
}