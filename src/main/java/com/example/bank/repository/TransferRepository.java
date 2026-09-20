package com.example.bank.repository;

import com.example.bank.model.Transfer;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByFromAccountId(Long accountId);

    List<Transfer> findByToAccountId(Long accountId);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transfer t
        WHERE t.fromAccount.id = :accountId
          AND t.status = 'COMPLETED'
          AND t.createdAt >= :from
          AND t.createdAt < :to
        """)
    BigDecimal sumOutgoingTransfers(
            @Param("accountId") Long accountId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

