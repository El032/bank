package com.example.bank.service;

import com.example.bank.model.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CardNumberGeneratorTest {

    private CardNumberGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CardNumberGenerator();
    }

    @Test
    void shouldGenerateDebitCardNumber() {
        String cardNumber = generator.generate(AccountType.DEBIT);

        assertNotNull(cardNumber);
        assertEquals(19, cardNumber.length());
        assertTrue(cardNumber.startsWith("4169-7833-"));
    }

    @Test
    void shouldGenerateCreditCardNumber() {
        String cardNumber = generator.generate(AccountType.CREDIT);

        assertNotNull(cardNumber);
        assertEquals(19, cardNumber.length());
        assertTrue(cardNumber.startsWith("2200-2002-"));
    }

    @Test
    void shouldGenerateCardNumberInCorrectFormatForDebit() {
        String cardNumber = generator.generate(AccountType.DEBIT);

        assertTrue(
                Pattern.matches(
                        "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
                        cardNumber
                )
        );
    }

    @Test
    void shouldGenerateCardNumberInCorrectFormatForCredit() {
        String cardNumber = generator.generate(AccountType.CREDIT);

        assertTrue(
                Pattern.matches(
                        "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
                        cardNumber
                )
        );
    }

    @Test
    void shouldNotGenerateCardForSavingsAccount() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(AccountType.SAVINGS)
        );

        assertEquals(
                "Для сберегательного счёта карта не создаётся",
                exception.getMessage()
        );
    }

    @Test
    void shouldGenerateEightRandomDigitsAfterPrefix() {
        String cardNumber = generator.generate(AccountType.DEBIT);

        String[] blocks = cardNumber.split("-");

        assertEquals(4, blocks.length);
        assertEquals(4, blocks[0].length());
        assertEquals(4, blocks[1].length());
        assertEquals(4, blocks[2].length());
        assertEquals(4, blocks[3].length());

        assertEquals("4169", blocks[0]);
        assertEquals("7833", blocks[1]);
    }

    @Test
    void shouldGenerateDifferentCardNumbers() {
        Set<String> cardNumbers = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            cardNumbers.add(
                    generator.generate(AccountType.DEBIT)
            );
        }

        assertTrue(cardNumbers.size() > 1);
    }

    @Test
    void shouldGenerateOnlyDigitsAndHyphens() {
        String cardNumber = generator.generate(AccountType.CREDIT);

        assertTrue(cardNumber.matches("[0-9-]+"));
    }

    @Test
    void shouldGenerateExactlyFourBlocks() {
        String cardNumber = generator.generate(AccountType.DEBIT);

        String[] blocks = cardNumber.split("-");

        assertEquals(4, blocks.length);
    }
}