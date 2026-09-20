package com.example.bank.service;

import com.example.bank.model.OutboxEvent;
import com.example.bank.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRecoveryServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private OutboxRecoveryService outboxRecoveryService;

    @BeforeEach
    void setUp() {
        outboxRecoveryService =
                new OutboxRecoveryService(outboxEventRepository);
    }

    @Test
    void recoverStuckEvents_shouldResetProcessingForStuckEvents() {

        OutboxEvent event = new OutboxEvent(
                "TRANSFER_COMPLETED",
                "Transfer",
                1L,
                "{}"
        );

        event.setProcessing(true);
        event.setProcessingAt(LocalDateTime.now().minusMinutes(10));

        when(outboxEventRepository
                .findByPublishedFalseAndProcessingTrueAndProcessingAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(event));

        when(outboxEventRepository.saveAll(anyList()))
                .thenReturn(List.of(event));

        outboxRecoveryService.recoverStuckEvents();

        assertFalse(event.isProcessing());
        assertNull(event.getProcessingAt());

        verify(outboxEventRepository).saveAll(List.of(event));
    }

    @Test
    void recoverStuckEvents_shouldHandleEmptyList() {

        when(outboxEventRepository
                .findByPublishedFalseAndProcessingTrueAndProcessingAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        when(outboxEventRepository.saveAll(anyList()))
                .thenReturn(List.of());

        outboxRecoveryService.recoverStuckEvents();

        verify(outboxEventRepository).saveAll(List.of());
    }

    @Test
    void recoverStuckEvents_shouldResetProcessingForAllEvents() {

        OutboxEvent event1 = new OutboxEvent(
                "TRANSFER_COMPLETED",
                "Transfer",
                1L,
                "{}"
        );

        OutboxEvent event2 = new OutboxEvent(
                "TRANSFER_COMPLETED",
                "Transfer",
                2L,
                "{}"
        );

        event1.setProcessing(true);
        event1.setProcessingAt(LocalDateTime.now().minusMinutes(10));

        event2.setProcessing(true);
        event2.setProcessingAt(LocalDateTime.now().minusMinutes(20));

        List<OutboxEvent> events = List.of(event1, event2);

        when(outboxEventRepository
                .findByPublishedFalseAndProcessingTrueAndProcessingAtBefore(any(LocalDateTime.class)))
                .thenReturn(events);

        when(outboxEventRepository.saveAll(anyList()))
                .thenReturn(events);

        outboxRecoveryService.recoverStuckEvents();

        assertFalse(event1.isProcessing());
        assertNull(event1.getProcessingAt());

        assertFalse(event2.isProcessing());
        assertNull(event2.getProcessingAt());

        verify(outboxEventRepository).saveAll(events);
    }
}