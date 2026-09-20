package com.example.bank.repository;

import com.example.bank.model.Transaction;
import com.example.bank.model.TransactionSource;
import com.example.bank.model.TransactionType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdAndType(Long account_id,
                                             TransactionType type);

    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    List<Transaction> findByAccountId(Long accountId);

    long countByAccountId(Long accountId);
    Page<Transaction> findByAccountIdAndType(
            Long accountId, TransactionType type, Pageable pageable);

    Long id(Long id);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.account.id = :accountId
          AND t.type = :type
          AND t.source = :source
          AND t.createdAt >= :from
        """)
    BigDecimal sumAmount(
            @Param("accountId") Long accountId,
            @Param("type") TransactionType type,
            @Param("source") TransactionSource source,
            @Param("from") LocalDateTime from
    );
}
