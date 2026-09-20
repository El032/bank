package com.example.bank.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransferCompletedEventTest {

    @Test
    @DisplayName("Конструктор с параметрами корректно заполняет все поля")
    void constructor_shouldSetAllFields() {
        Long transferId = 100L;
        Long fromAccountId = 10L;
        Long toAccountId = 20L;
        String fromOwner = "eldar";
        String toOwner = "ali";
        BigDecimal amount = new BigDecimal("250.50");

        TransferCompletedEvent event = new TransferCompletedEvent(
                transferId,
                fromAccountId,
                toAccountId,
                fromOwner,
                toOwner,
                amount
        );

        assertEquals(transferId, event.getTransferId());
        assertEquals(fromAccountId, event.getFromAccountId());
        assertEquals(toAccountId, event.getToAccountId());
        assertEquals(fromOwner, event.getFromOwner());
        assertEquals(toOwner, event.getToOwner());
        assertEquals(amount, event.getAmount());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    @DisplayName("Пустой конструктор создаёт объект с null полями")
    void defaultConstructor_shouldCreateEmptyObject() {
        TransferCompletedEvent event = new TransferCompletedEvent();

        assertNotNull(event);
        assertNull(event.getTransferId());
        assertNull(event.getFromAccountId());
        assertNull(event.getToAccountId());
        assertNull(event.getFromOwner());
        assertNull(event.getToOwner());
        assertNull(event.getAmount());
        assertNull(event.getOccurredAt());
    }

    @Test
    @DisplayName("Setters корректно изменяют поля")
    void setters_shouldUpdateFields() {
        TransferCompletedEvent event = new TransferCompletedEvent();

        event.setTransferId(1L);
        event.setFromAccountId(10L);
        event.setToAccountId(20L);
        event.setFromOwner("eldar");
        event.setToOwner("ali");

        BigDecimal amount = new BigDecimal("500.00");
        event.setAmount(amount);

        LocalDateTime occurredAt =
                LocalDateTime.of(2026, 9, 19, 12, 30);
        event.setOccurredAt(occurredAt);

        assertEquals(1L, event.getTransferId());
        assertEquals(10L, event.getFromAccountId());
        assertEquals(20L, event.getToAccountId());
        assertEquals("eldar", event.getFromOwner());
        assertEquals("ali", event.getToOwner());
        assertEquals(amount, event.getAmount());
        assertEquals(occurredAt, event.getOccurredAt());
    }

    @Test
    @DisplayName("toString возвращает информацию о переводе")
    void toString_shouldReturnExpectedString() {
        TransferCompletedEvent event = new TransferCompletedEvent(
                100L,
                10L,
                20L,
                "eldar",
                "ali",
                new BigDecimal("250.50")
        );

        String result = event.toString();

        assertEquals(
                "TransferCompletedEvent{transferId=100, amount=250.50, from=eldar, to=ali}",
                result
        );
    }
}
