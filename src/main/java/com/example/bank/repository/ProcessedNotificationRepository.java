package com.example.bank.repository;

import com.example.bank.model.ProcessedNotification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProcessedNotificationRepository
        extends JpaRepository<ProcessedNotification, Long> {

    boolean existsByTransferId(Long transferId);


}