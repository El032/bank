package com.example.bank.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private AccountMetrics accountMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        accountMetrics = new AccountMetrics(meterRegistry);
    }

    @Test
    @DisplayName("Создание счёта увеличивает счётчик")
    void accountCreated_shouldIncrementCounter() {

        accountMetrics.accountCreated();
        accountMetrics.accountCreated();

        Counter counter = meterRegistry
                .get("bank.accounts.created")
                .counter();

        assertEquals(2.0, counter.count());
    }

    @Test
    @DisplayName("Создание карты увеличивает счётчик")
    void accountCardsCreated_shouldIncrementCounter() {

        accountMetrics.accountCardsCreated();
        accountMetrics.accountCardsCreated();
        accountMetrics.accountCardsCreated();

        Counter counter = meterRegistry
                .get("bank.accounts.cards.created")
                .counter();

        assertEquals(3.0, counter.count());
    }
}