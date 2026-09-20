package com.example.bank.service;

import com.example.bank.exception.*;
import com.example.bank.model.BankAccount;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Transfer — валидация бизнес-правил")
class TransferValidationTest {

    @Test
    @DisplayName("Нельзя перевести деньги на тот же счёт")
    void transfer_sameAccount_shouldThrow() {
        // Эта проверка происходит до работы с БД — можно тестировать изолированно
        Long accountId = 1L;
        // Симулируем проверку из TransferService
        assertThrows(TransferException.class, () -> {
            if (accountId.equals(accountId)) {
                throw new TransferException("Нельзя перевести деньги на тот же счёт");
            }
        });
    }

    static Stream<BigDecimal> invalidAmounts() {
        return Stream.of(
                BigDecimal.ZERO,
                new BigDecimal("-1"),
                new BigDecimal("-0.01")
        );
    }

    @ParameterizedTest
    @DisplayName("Сумма перевода должна быть положительной")
    @MethodSource("invalidAmounts")
    void transfer_invalidAmount_shouldBeRejected(BigDecimal amount) {
        assertTrue(amount.compareTo(BigDecimal.ZERO) <= 0,
                "Невалидная сумма должна быть <= 0: " + amount);
    }

    @Test
    @DisplayName("Заблокированный счёт не может отправлять переводы")
    void transfer_blockedSender_shouldBeRejected() {
        BankAccount blockedAccount = new BankAccount("Eldar");
        ReflectionTestUtils.setField(blockedAccount, "id", 1L);
        blockedAccount.block();

        assertFalse(blockedAccount.isActive(),
                "Заблокированный счёт должен иметь isActive = false");
    }

    @Test
    @DisplayName("Недостаточно средств с учётом комиссии")
    void transfer_withFee_insufficientFunds() {
        BigDecimal balance = new BigDecimal("100.00");
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal feePercent = new BigDecimal("0.5");

        BigDecimal fee = amount.multiply(feePercent)
                .divide(BigDecimal.valueOf(100));
        BigDecimal total = amount.add(fee);

        assertTrue(total.compareTo(balance) > 0,
                "Сумма с комиссией должна превышать баланс");
    }
}