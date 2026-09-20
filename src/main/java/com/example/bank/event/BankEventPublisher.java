package com.example.bank.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class BankEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(BankEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${bank.kafka.topics.transfer-completed}")
    private String transferCompletedTopic;

    @Value("${bank.kafka.topics.account-created}")
    private String accountCreatedTopic;

    public BankEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, Object>> publishTransferCompleted(
            TransferCompletedEvent event) {

        String key = "transfer-" + event.getTransferId();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(transferCompletedTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                        "Событие перевода опубликовано: transferId={}, partition={}, offset={}",
                        event.getTransferId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error(
                        "Ошибка публикации события перевода: transferId={}",
                        event.getTransferId(),
                        ex
                );
            }
        });

        return future;
    }

    public void publishAccountCreated(AccountCreatedEvent event) {
        String key = "account-" + event.getAccountId();
        kafkaTemplate.send(accountCreatedTopic, key, event);
        log.info("Событие создания счёта опубликовано: accountId={}",
                event.getAccountId());
    }
}