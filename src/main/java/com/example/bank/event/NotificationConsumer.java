package com.example.bank.event;

import com.example.bank.repository.ProcessedNotificationRepository;
import com.example.bank.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.example.bank.model.ProcessedNotification;

@Component
public class NotificationConsumer {

    private final EmailService emailService;
    private final ProcessedNotificationRepository processedNotificationRepository;

    private static final Logger log =
            LoggerFactory.getLogger(NotificationConsumer.class);

    public NotificationConsumer(EmailService emailService,
                                ProcessedNotificationRepository processedNotificationRepository) {
        this.emailService = emailService;
        this.processedNotificationRepository = processedNotificationRepository;
    }

    // Слушаем топик bank.transfer.completed
    @KafkaListener(
            topics = "${bank.kafka.topics.transfer-completed}",
            groupId = "bank-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransferCompleted(
            @Payload TransferCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Получено событие перевода: partition={}, offset={}, event={}",
                partition, offset, event);

        try {
            if (processedNotificationRepository.existsByTransferId(event.getTransferId())) {
                log.info("Уведомление уже обработано: transferId={}",
                        event.getTransferId());
                return;
            }
            // Отправляем настоящее email-уведомление
            sendTransferEmail(event);

            // Симулируем запись в аудит
            recordAudit(event);

            processedNotificationRepository.save(
                    new ProcessedNotification(event.getTransferId())
            );

            log.info("Уведомление о переводе обработано: transferId={}",
                    event.getTransferId());

        } catch (Exception e) {
            log.error("Ошибка обработки события перевода: transferId={}",
                    event.getTransferId(), e);
            // При исключении — Kafka повторит доставку (at least once)
            throw e;
        }
    }

    @KafkaListener(
            topics = "${bank.kafka.topics.account-created}",
            groupId = "bank-notification-group"
    )
    public void handleAccountCreated(@Payload AccountCreatedEvent event) {
        log.info("Новый счёт создан: accountId={}, owner={}",
                event.getAccountId(), event.getOwner());
        // Здесь — приветственное письмо новому клиенту
        sendWelcomeEmail(event);
    }

    private void sendTransferEmail(TransferCompletedEvent event) {

        emailService.sendTransferEmail(event);
    }

    private void recordAudit(TransferCompletedEvent event) {
        log.info("Аудит записан: перевод #{} на {} руб.",
                event.getTransferId(), event.getAmount());
    }

    private void sendWelcomeEmail(AccountCreatedEvent event) {
        log.info("Приветственный email отправлен: {}", event.getOwner());
    }
}