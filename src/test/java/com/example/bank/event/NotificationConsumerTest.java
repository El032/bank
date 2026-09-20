package com.example.bank.event;

import com.example.bank.model.ProcessedNotification;
import com.example.bank.repository.ProcessedNotificationRepository;
import com.example.bank.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationConsumerTest {

    private EmailService emailService;
    private ProcessedNotificationRepository processedNotificationRepository;
    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        processedNotificationRepository =
                mock(ProcessedNotificationRepository.class);

        notificationConsumer = new NotificationConsumer(
                emailService,
                processedNotificationRepository
        );
    }

    @Test
    @DisplayName("Успешная обработка перевода отправляет email и сохраняет уведомление")
    void handleTransferCompleted_shouldProcessSuccessfully() {

        TransferCompletedEvent event = new TransferCompletedEvent(
                100L,
                10L,
                20L,
                "eldar",
                "ali",
                new BigDecimal("250.50")
        );

        when(processedNotificationRepository.existsByTransferId(100L))
                .thenReturn(false);

        notificationConsumer.handleTransferCompleted(
                event,
                1,
                50L
        );

        verify(processedNotificationRepository)
                .existsByTransferId(100L);

        verify(emailService)
                .sendTransferEmail(event);

        verify(processedNotificationRepository)
                .save(any(ProcessedNotification.class));
    }

    @Test
    @DisplayName("Повторное событие не обрабатывается повторно")
    void handleTransferCompleted_shouldSkipAlreadyProcessedEvent() {

        TransferCompletedEvent event = new TransferCompletedEvent(
                200L,
                10L,
                20L,
                "eldar",
                "ali",
                new BigDecimal("100.00")
        );

        when(processedNotificationRepository.existsByTransferId(200L))
                .thenReturn(true);

        notificationConsumer.handleTransferCompleted(
                event,
                0,
                100L
        );

        verify(processedNotificationRepository)
                .existsByTransferId(200L);

        verify(emailService, never())
                .sendTransferEmail(any(TransferCompletedEvent.class));

        verify(processedNotificationRepository, never())
                .save(any(ProcessedNotification.class));
    }

    @Test
    @DisplayName("Ошибка обработки перевода пробрасывается дальше")
    void handleTransferCompleted_shouldRethrowException() {

        TransferCompletedEvent event = new TransferCompletedEvent(
                300L,
                10L,
                20L,
                "eldar",
                "ali",
                new BigDecimal("500.00")
        );

        when(processedNotificationRepository.existsByTransferId(300L))
                .thenReturn(false);

        RuntimeException exception =
                new RuntimeException("Email service unavailable");

        doThrow(exception)
                .when(emailService)
                .sendTransferEmail(event);

        assertThrows(
                RuntimeException.class,
                () -> notificationConsumer.handleTransferCompleted(
                        event,
                        2,
                        150L
                )
        );

        verify(emailService)
                .sendTransferEmail(event);

        verify(processedNotificationRepository, never())
                .save(any(ProcessedNotification.class));
    }

    @Test
    @DisplayName("Обработка созданного счёта выполняется без ошибок")
    void handleAccountCreated_shouldProcessSuccessfully() {

        AccountCreatedEvent event = new AccountCreatedEvent(
                500L,
                "eldar",
                new BigDecimal("1000.00")
        );

        notificationConsumer.handleAccountCreated(event);

        verifyNoInteractions(emailService);
        verifyNoInteractions(processedNotificationRepository);
    }
}
