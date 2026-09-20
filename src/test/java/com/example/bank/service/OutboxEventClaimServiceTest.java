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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventClaimServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private OutboxEventClaimService service;

    @BeforeEach
    void setUp() {
        service = new OutboxEventClaimService(
                outboxEventRepository
        );
    }

    @Test
    void shouldClaimNextEvent() {
        OutboxEvent event = new OutboxEvent();

        when(outboxEventRepository
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.of(event));

        when(outboxEventRepository.save(event))
                .thenReturn(event);

        OutboxEvent result = service.claimNextEvent();

        assertSame(event, result);

        assertTrue(event.isProcessing());
        assertNotNull(event.getProcessingAt());

        verify(outboxEventRepository)
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc();

        verify(outboxEventRepository)
                .save(event);
    }

    @Test
    void shouldReturnNullWhenNoEventFound() {
        when(outboxEventRepository
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.empty());

        OutboxEvent result = service.claimNextEvent();

        assertNull(result);

        verify(outboxEventRepository)
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc();

        verify(outboxEventRepository, never())
                .save(any(OutboxEvent.class));
    }

    @Test
    void shouldSetProcessingToTrue() {
        OutboxEvent event = new OutboxEvent();

        when(outboxEventRepository
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.of(event));

        when(outboxEventRepository.save(event))
                .thenReturn(event);

        service.claimNextEvent();

        assertTrue(event.isProcessing());
    }

    @Test
    void shouldSetProcessingAt() {
        OutboxEvent event = new OutboxEvent();

        when(outboxEventRepository
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.of(event));

        when(outboxEventRepository.save(event))
                .thenReturn(event);

        LocalDateTime before = LocalDateTime.now();

        service.claimNextEvent();

        LocalDateTime after = LocalDateTime.now();

        assertNotNull(event.getProcessingAt());

        assertFalse(event.getProcessingAt().isBefore(before));
        assertFalse(event.getProcessingAt().isAfter(after));
    }

    @Test
    void shouldSaveClaimedEvent() {
        OutboxEvent event = new OutboxEvent();

        when(outboxEventRepository
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.of(event));

        when(outboxEventRepository.save(event))
                .thenReturn(event);

        service.claimNextEvent();

        ArgumentCaptor<OutboxEvent> captor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent savedEvent = captor.getValue();

        assertSame(event, savedEvent);
        assertTrue(savedEvent.isProcessing());
        assertNotNull(savedEvent.getProcessingAt());
    }

    @Test
    void shouldReturnSavedEvent() {
        OutboxEvent event = new OutboxEvent();
        OutboxEvent savedEvent = new OutboxEvent();

        when(outboxEventRepository
                .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc())
                .thenReturn(Optional.of(event));

        when(outboxEventRepository.save(event))
                .thenReturn(savedEvent);

        OutboxEvent result = service.claimNextEvent();

        assertSame(savedEvent, result);

        verify(outboxEventRepository)
                .save(event);
    }
}