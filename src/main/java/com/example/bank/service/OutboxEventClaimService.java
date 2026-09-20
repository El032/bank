package com.example.bank.service;

import com.example.bank.model.OutboxEvent;
import com.example.bank.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OutboxEventClaimService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventClaimService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public OutboxEvent claimNextEvent() {

        Optional<OutboxEvent> optionalEvent =
                outboxEventRepository
                        .findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc();

        if (optionalEvent.isEmpty()) {
            return null;
        }

        OutboxEvent event = optionalEvent.get();

        event.setProcessing(true);
        event.setProcessingAt(LocalDateTime.now());

        return outboxEventRepository.save(event);


    }
}