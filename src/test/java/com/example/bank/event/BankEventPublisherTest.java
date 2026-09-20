package com.example.bank.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private BankEventPublisher bankEventPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                bankEventPublisher,
                "transferCompletedTopic",
                "transfer-completed"
        );

        ReflectionTestUtils.setField(
                bankEventPublisher,
                "accountCreatedTopic",
                "account-created"
        );
    }

    @Test
    @DisplayName("publishTransferCompleted успешно отправляет событие")
    void publishTransferCompleted_shouldPublishEventSuccessfully() {

        TransferCompletedEvent event = new TransferCompletedEvent(
                100L,
                10L,
                20L,
                "eldar",
                "ali",
                new BigDecimal("250.50")
        );

        CompletableFuture<SendResult<String, Object>> future =
                new CompletableFuture<>();

        when(kafkaTemplate.send(
                "transfer-completed",
                "transfer-100",
                event
        )).thenReturn(future);

        CompletableFuture<SendResult<String, Object>> result =
                bankEventPublisher.publishTransferCompleted(event);

        assertSame(future, result);

        SendResult<String, Object> sendResult = mock(SendResult.class);

        org.apache.kafka.clients.producer.RecordMetadata metadata =
                mock(org.apache.kafka.clients.producer.RecordMetadata.class);

        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.partition()).thenReturn(2);
        when(metadata.offset()).thenReturn(15L);

        future.complete(sendResult);

        verify(kafkaTemplate).send(
                "transfer-completed",
                "transfer-100",
                event
        );
    }

    @Test
    @DisplayName("publishTransferCompleted обрабатывает ошибку отправки")
    void publishTransferCompleted_shouldHandleError() {

        TransferCompletedEvent event = new TransferCompletedEvent(
                200L,
                10L,
                20L,
                "eldar",
                "ali",
                new BigDecimal("100.00")
        );

        CompletableFuture<SendResult<String, Object>> future =
                new CompletableFuture<>();

        when(kafkaTemplate.send(
                "transfer-completed",
                "transfer-200",
                event
        )).thenReturn(future);

        CompletableFuture<SendResult<String, Object>> result =
                bankEventPublisher.publishTransferCompleted(event);

        RuntimeException exception =
                new RuntimeException("Kafka unavailable");

        future.completeExceptionally(exception);

        assertSame(future, result);

        verify(kafkaTemplate).send(
                "transfer-completed",
                "transfer-200",
                event
        );
    }

    @Test
    @DisplayName("publishAccountCreated отправляет событие создания счёта")
    void publishAccountCreated_shouldPublishEvent() {

        AccountCreatedEvent event = new AccountCreatedEvent(
                50L,
                "eldar",
                new BigDecimal("1000.00")
        );

        bankEventPublisher.publishAccountCreated(event);

        verify(kafkaTemplate).send(
                "account-created",
                "account-50",
                event
        );
    }

    @Test
    @DisplayName("publishTransferCompleted формирует правильный ключ Kafka")
    void publishTransferCompleted_shouldUseCorrectKey() {

        TransferCompletedEvent event = new TransferCompletedEvent(
                777L,
                1L,
                2L,
                "eldar",
                "ali",
                new BigDecimal("500.00")
        );

        CompletableFuture<SendResult<String, Object>> future =
                new CompletableFuture<>();

        when(kafkaTemplate.send(
                anyString(),
                anyString(),
                any()
        )).thenReturn(future);

        bankEventPublisher.publishTransferCompleted(event);

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(
                eq("transfer-completed"),
                keyCaptor.capture(),
                eq(event)
        );

        assertEquals("transfer-777", keyCaptor.getValue());
    }

    @Test
    @DisplayName("publishAccountCreated формирует правильный ключ Kafka")
    void publishAccountCreated_shouldUseCorrectKey() {

        AccountCreatedEvent event = new AccountCreatedEvent(
                999L,
                "eldar",
                new BigDecimal("2000.00")
        );

        bankEventPublisher.publishAccountCreated(event);

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(
                eq("account-created"),
                keyCaptor.capture(),
                eq(event)
        );

        assertEquals("account-999", keyCaptor.getValue());
    }
}
