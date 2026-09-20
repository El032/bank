package com.example.bank.service;

import com.example.bank.event.BankEventPublisher;
import com.example.bank.event.TransferCompletedEvent;
import com.example.bank.model.OutboxEvent;
import com.example.bank.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private BankEventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OutboxEventClaimService claimService;

    @Mock
    private OutboxRecoveryService recoveryService;

    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OutboxEventProcessor(
                outboxEventRepository,
                eventPublisher,
                objectMapper,
                claimService,
                recoveryService
        );
    }

    @Test
    void shouldDoNothingWhenNoEventAvailable() {

        when(claimService.claimNextEvent())
                .thenReturn(null);

        processor.processOutbox();

        verify(claimService).claimNextEvent();

        verifyNoInteractions(
                objectMapper,
                eventPublisher,
                outboxEventRepository
        );
    }

    @Test
    void shouldProcessOutboxEventSuccessfully() throws Exception {

        OutboxEvent outboxEvent = new OutboxEvent(
                "TRANSFER_COMPLETED",
                "Transfer",
                1L,
                "test-payload"
        );

        outboxEvent.setProcessing(true);

        TransferCompletedEvent event =
                mock(TransferCompletedEvent.class);

        when(claimService.claimNextEvent())
                .thenReturn(outboxEvent);

        when(objectMapper.readValue(
                "test-payload",
                TransferCompletedEvent.class
        )).thenReturn(event);

        when(eventPublisher.publishTransferCompleted(event))
                .thenReturn(CompletableFuture.completedFuture(null));

        when(outboxEventRepository.save(outboxEvent))
                .thenReturn(outboxEvent);

        processor.processOutbox();

        assertTrue(outboxEvent.isPublished());
        assertFalse(outboxEvent.isProcessing());
        assertNull(outboxEvent.getProcessingAt());

        verify(objectMapper).readValue(
                "test-payload",
                TransferCompletedEvent.class
        );

        verify(eventPublisher)
                .publishTransferCompleted(event);

        verify(outboxEventRepository)
                .save(outboxEvent);
    }

    @Test
    void shouldResetProcessingWhenDeserializationFails()
            throws Exception {

        OutboxEvent outboxEvent = new OutboxEvent(
                "TRANSFER_COMPLETED",
                "Transfer",
                1L,
                "invalid-payload"
        );

        outboxEvent.setProcessing(true);

        when(claimService.claimNextEvent())
                .thenReturn(outboxEvent);

        when(objectMapper.readValue(
                "invalid-payload",
                TransferCompletedEvent.class
        )).thenThrow(new RuntimeException("Invalid JSON"));

        when(outboxEventRepository.save(outboxEvent))
                .thenReturn(outboxEvent);

        processor.processOutbox();

        assertFalse(outboxEvent.isProcessing());
        assertNull(outboxEvent.getProcessingAt());

        verify(objectMapper).readValue(
                "invalid-payload",
                TransferCompletedEvent.class
        );

        verify(eventPublisher, never())
                .publishTransferCompleted(any());

        verify(outboxEventRepository)
                .save(outboxEvent);
    }

    @Test
    void shouldResetProcessingWhenPublishingFails()
            throws Exception {

        OutboxEvent outboxEvent = new OutboxEvent(
                "TRANSFER_COMPLETED",
                "Transfer",
                1L,
                "test-payload"
        );

        outboxEvent.setProcessing(true);

        TransferCompletedEvent event =
                mock(TransferCompletedEvent.class);

        when(claimService.claimNextEvent())
                .thenReturn(outboxEvent);

        when(objectMapper.readValue(
                "test-payload",
                TransferCompletedEvent.class
        )).thenReturn(event);

        CompletableFuture<SendResult<String, Object>> failedFuture =
                new CompletableFuture<>();

        failedFuture.completeExceptionally(
                new RuntimeException("Kafka error")
        );

        when(eventPublisher.publishTransferCompleted(event))
                .thenReturn(failedFuture);

        when(outboxEventRepository.save(outboxEvent))
                .thenReturn(outboxEvent);

        processor.processOutbox();

        assertFalse(outboxEvent.isProcessing());
        assertNull(outboxEvent.getProcessingAt());

        verify(eventPublisher)
                .publishTransferCompleted(event);

        verify(outboxEventRepository)
                .save(outboxEvent);
    }

    @Test
    void shouldRecoverStuckEvents() {

        processor.recoverStuckEvents();

        verify(recoveryService)
                .recoverStuckEvents();

        verifyNoInteractions(
                claimService,
                eventPublisher,
                objectMapper,
                outboxEventRepository
        );
    }
}