package com.example.bank.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountNumberGeneratorTest {

    private final AccountNumberGenerator generator =
            new AccountNumberGenerator();


    @Test
    void shouldGenerateAccountNumberWithCorrectFormat() {

        String accountNumber = generator.generate();

        assertTrue(
                accountNumber.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}-\\d{4}")
        );
    }


    @Test
    void shouldGenerateAccountNumberWithCorrectPrefix() {

        String accountNumber = generator.generate();

        assertTrue(
                accountNumber.startsWith("4081-7910-")
        );
    }


    @Test
    void shouldGenerateAccountNumberWithCorrectLength() {

        String accountNumber = generator.generate();

        assertEquals(24, accountNumber.length());
    }


    @Test
    void shouldGenerateAccountNumberWithFiveBlocks() {

        String accountNumber = generator.generate();

        String[] blocks = accountNumber.split("-");

        assertEquals(5, blocks.length);

        assertEquals("4081", blocks[0]);
        assertEquals("7910", blocks[1]);

        assertEquals(4, blocks[2].length());
        assertEquals(4, blocks[3].length());
        assertEquals(4, blocks[4].length());
    }


    @Test
    void shouldGenerateOnlyDigitsInRandomBlocks() {

        String accountNumber = generator.generate();

        String[] blocks = accountNumber.split("-");

        for (int i = 2; i < blocks.length; i++) {
            assertTrue(
                    blocks[i].matches("\\d{4}")
            );
        }
    }


    @Test
    void shouldGenerateAccountNumberWithoutUnexpectedCharacters() {

        String accountNumber = generator.generate();

        assertTrue(
                accountNumber.matches("[0-9-]+")
        );
    }


    @Test
    void shouldGenerateMultipleAccountNumbers() {

        for (int i = 0; i < 100; i++) {

            String accountNumber = generator.generate();

            assertNotNull(accountNumber);

            assertEquals(24, accountNumber.length());

            assertTrue(
                    accountNumber.matches(
                            "\\d{4}-\\d{4}-\\d{4}-\\d{4}-\\d{4}"
                    )
            );

            assertTrue(
                    accountNumber.startsWith("4081-7910-")
            );
        }
    }
}