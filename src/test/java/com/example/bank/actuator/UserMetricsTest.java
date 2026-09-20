package com.example.bank.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private UserMetrics userMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        userMetrics = new UserMetrics(meterRegistry);
    }

    @Test
    @DisplayName("Создание пользователя увеличивает счётчик")
    void userCreated_shouldIncrementCounter() {

        userMetrics.userCreated();
        userMetrics.userCreated();
        userMetrics.userCreated();

        Counter counter = meterRegistry
                .get("bank.users.created")
                .counter();

        assertEquals(3.0, counter.count());
    }
}