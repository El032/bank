package com.example.bank.repository;

import com.example.bank.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    //List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OutboxEvent> findFirstByPublishedFalseAndProcessingFalseOrderByCreatedAtAsc();
    List<OutboxEvent> findByPublishedFalseAndProcessingTrueAndProcessingAtBefore(
            LocalDateTime time
    );
}