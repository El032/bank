package com.example.bank.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountCreatedEventTest {

    @Test
    @DisplayName("Конструктор с параметрами корректно заполняет поля")
    void constructor_shouldSetFields() {
        Long accountId = 10L;
        String owner = "eldar";
        BigDecimal initialBalance = new BigDecimal("1000.50");

        AccountCreatedEvent event =
                new AccountCreatedEvent(accountId, owner, initialBalance);

        assertEquals(accountId, event.getAccountId());
        assertEquals(owner, event.getOwner());
        assertEquals(initialBalance, event.getInitialBalance());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    @DisplayName("Пустой конструктор создаёт объект")
    void defaultConstructor_shouldCreateObject() {
        AccountCreatedEvent event = new AccountCreatedEvent();

        assertNotNull(event);
        assertNull(event.getAccountId());
        assertNull(event.getOwner());
        assertNull(event.getInitialBalance());
        assertNull(event.getOccurredAt());
    }

    @Test
    @DisplayName("setAccountId изменяет accountId")
    void setAccountId_shouldUpdateAccountId() {
        AccountCreatedEvent event = new AccountCreatedEvent();

        event.setAccountId(25L);

        assertEquals(25L, event.getAccountId());
    }

    @Test
    @DisplayName("setOwner изменяет owner")
    void setOwner_shouldUpdateOwner() {
        AccountCreatedEvent event = new AccountCreatedEvent();

        event.setOwner("admin");

        assertEquals("admin", event.getOwner());
    }

    @Test
    @DisplayName("setOccurredAt изменяет occurredAt")
    void setOccurredAt_shouldUpdateOccurredAt() {
        AccountCreatedEvent event = new AccountCreatedEvent();
        LocalDateTime time = LocalDateTime.of(2026, 9, 19, 12, 0);

        event.setOccurredAt(time);

        assertEquals(time, event.getOccurredAt());
    }
}

