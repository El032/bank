package com.example.bank.service;

import com.example.bank.model.OutboxEvent;
import com.example.bank.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxRecoveryService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxRecoveryService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void recoverStuckEvents() {

        LocalDateTime timeout =
                LocalDateTime.now().minusMinutes(5);

        List<OutboxEvent> events =
                outboxEventRepository
                        .findByPublishedFalseAndProcessingTrueAndProcessingAtBefore(
                                timeout
                        );

        for (OutboxEvent event : events) {
            event.setProcessing(false);
            event.setProcessingAt(null);
        }

        outboxEventRepository.saveAll(events);
    }
}