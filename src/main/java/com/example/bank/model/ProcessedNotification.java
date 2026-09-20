package com.example.bank.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "processed_notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_notifications_transfer_id",
                        columnNames = "transfer_id"
                )
        }
)
public class ProcessedNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", nullable = false)
    private Long transferId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedNotification() {
    }

    public ProcessedNotification(Long transferId) {
        this.transferId = transferId;
        this.processedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTransferId() {
        return transferId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}