package com.example.bank.service;

import com.example.bank.event.BankEventPublisher;
import com.example.bank.event.TransferCompletedEvent;
import com.example.bank.model.OutboxEvent;
import com.example.bank.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final BankEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final OutboxEventClaimService claimService;
    private final OutboxRecoveryService recoveryService;

    public OutboxEventProcessor(
            OutboxEventRepository outboxEventRepository,
            BankEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            OutboxEventClaimService claimService,
            OutboxRecoveryService recoveryService) {

        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.claimService = claimService;
        this.recoveryService = recoveryService;
    }

    @Scheduled(fixedDelay = 60000)
    public void recoverStuckEvents() {
        recoveryService.recoverStuckEvents();
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {

        OutboxEvent outboxEvent = claimService.claimNextEvent();

        if (outboxEvent == null) {
            return;
        }

        try {
            TransferCompletedEvent event =
                    objectMapper.readValue(
                            outboxEvent.getPayload(),
                            TransferCompletedEvent.class
                    );

            eventPublisher
                    .publishTransferCompleted(event)
                    .get();

            outboxEvent.setPublished(true);
            outboxEvent.setProcessing(false);
            outboxEvent.setProcessingAt(null);

            outboxEventRepository.save(outboxEvent);

        } catch (Exception e) {

            outboxEvent.setProcessing(false);
            outboxEvent.setProcessingAt(null);
            outboxEventRepository.save(outboxEvent);

            System.err.println(
                    "Ошибка обработки OutboxEvent id="
                            + outboxEvent.getId()
            );

            e.printStackTrace();
        }
    }
}