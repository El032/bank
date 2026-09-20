package com.example.bank.actuator;

import com.example.bank.model.AccountStatus;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BankHealthIndicatorTest {

    private AccountRepository accountRepository;
    private UserRepository userRepository;

    private BankHealthIndicator bankHealthIndicator;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        userRepository = mock(UserRepository.class);

        bankHealthIndicator = new BankHealthIndicator(
                accountRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("health возвращает UP с данными банка")
    void health_shouldReturnUp() {

        when(accountRepository.count()).thenReturn(17L);
        when(userRepository.count()).thenReturn(3L);

        when(accountRepository.findByStatus(AccountStatus.ACTIVE))
                .thenReturn(List.of());

        Health health = bankHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());

        assertEquals(17L, health.getDetails().get("accounts"));
        assertEquals(3L, health.getDetails().get("users"));
        assertEquals(0L, health.getDetails().get("activeAccounts"));
        assertEquals("Банк работает", health.getDetails().get("status"));
    }

    @Test
    @DisplayName("health возвращает DOWN при ошибке")
    void health_shouldReturnDown_whenException() {

        when(accountRepository.count())
                .thenThrow(new RuntimeException("Database unavailable"));

        Health health = bankHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());

        assertEquals(
                "Database unavailable",
                health.getDetails().get("error")
        );
    }
}