package com.example.bank.exception;

import com.example.bank.model.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {


    @Test
    @DisplayName("CreditPaymentExceededException корректно формирует сообщение")
    void creditPaymentExceededException_shouldContainCorrectMessage() {
        BigDecimal paymentAmount = new BigDecimal("1500.00");
        BigDecimal currentDebt = new BigDecimal("1000.00");

        CreditPaymentExceededException exception =
                new CreditPaymentExceededException(paymentAmount, currentDebt);

        assertEquals(
                "Сумма платежа 1500.00 превышает текущую задолженность 1000.00",
                exception.getMessage()
        );

        assertInstanceOf(RuntimeException.class, exception);
    }



    @Test
    @DisplayName("InvalidCardStatusTransitionException корректно создаётся")
    void invalidCardStatusTransitionException_shouldContainMessage() {
        String message = "Недопустимый переход статуса карты";

        InvalidCardStatusTransitionException exception =
                new InvalidCardStatusTransitionException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("CannotCloseAccountException корректно создаётся")
    void cannotCloseAccountException_shouldContainMessage() {
        String message = "Невозможно закрыть счёт";

        CannotCloseAccountException exception =
                new CannotCloseAccountException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("InvalidAccountStatusTransitionException корректно создаётся")
    void invalidAccountStatusTransitionException_shouldContainMessage() {
        String message = "Недопустимый переход статуса счёта";

        InvalidAccountStatusTransitionException exception =
                new InvalidAccountStatusTransitionException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }


    @Test
    @DisplayName("InactiveAccountException без параметров формирует сообщение")
    void inactiveAccountException_withoutArguments_shouldContainCorrectMessage() {
        InactiveAccountException exception =
                new InactiveAccountException();

        assertEquals("Счёт заблокирован!", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("InactiveAccountException с id формирует сообщение")
    void inactiveAccountException_withId_shouldContainCorrectMessage() {
        InactiveAccountException exception =
                new InactiveAccountException(10L);

        assertEquals("Счёт с id = 10 заблокирован!", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("InactiveAccountException со статусом CLOSED формирует сообщение о закрытом счёте")
    void inactiveAccountException_withClosedStatus_shouldContainClosedMessage() {
        InactiveAccountException exception =
                new InactiveAccountException(10L, AccountStatus.CLOSED);

        assertEquals("Счёт с id = 10 закрыт!", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("InactiveAccountException с активным статусом формирует сообщение о блокировке")
    void inactiveAccountException_withNonClosedStatus_shouldContainBlockedMessage() {
        InactiveAccountException exception =
                new InactiveAccountException(10L, AccountStatus.ACTIVE);

        assertEquals("Счёт с id = 10 заблокирован!", exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }


    @Test
    @DisplayName("AccountNotFoundException корректно формирует сообщение и сохраняет id")
    void accountNotFoundException_shouldContainCorrectMessageAndId() {
        Long id = 42L;

        AccountNotFoundException exception =
                new AccountNotFoundException(id);

        assertEquals("Счёт не найден: 42", exception.getMessage());
        assertEquals(id, exception.getId());
        assertInstanceOf(RuntimeException.class, exception);
    }


    @Test
    @DisplayName("InvalidCardStatusTransitionException сохраняет сообщение")
    void invalidCardStatusTransitionException_shouldStoreMessage() {
        String message = "Нельзя перевести карту из CLOSED в ACTIVE";

        InvalidCardStatusTransitionException exception =
                new InvalidCardStatusTransitionException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("CannotCloseAccountException сохраняет сообщение")
    void cannotCloseAccountException_shouldStoreMessage() {
        String message = "Нельзя закрыть счёт с активными переводами";

        CannotCloseAccountException exception =
                new CannotCloseAccountException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("InvalidAccountStatusTransitionException сохраняет сообщение")
    void invalidAccountStatusTransitionException_shouldStoreMessage() {
        String message = "Недопустимый переход статуса счёта";

        InvalidAccountStatusTransitionException exception =
                new InvalidAccountStatusTransitionException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }



}
