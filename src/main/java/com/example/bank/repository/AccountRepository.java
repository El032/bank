package com.example.bank.repository;

import com.example.bank.model.AccountStatus;
import com.example.bank.model.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<BankAccount, Long> {
    Page<BankAccount> findByStatus(AccountStatus status, Pageable pageable);


    Page<BankAccount> findByBalanceGreaterThan(BigDecimal balance, Pageable pageable);

    Page<BankAccount> findByOwnerContainingIgnoreCase(String name, Pageable pageable);

    List<BankAccount> findByOwnerContainingIgnoreCase(String name);

    // Кастомный запрос с пагинацией
    @Query("SELECT a FROM BankAccount a WHERE a.status = 'ACTIVE' AND a.balance >= :minBalance")
    Page<BankAccount> findActiveWithMinBalance(
            @Param("minBalance") BigDecimal minBalance,
            Pageable pageable);

    List<BankAccount> findByStatus(AccountStatus status);
    Optional<BankAccount> findByOwner(String owner);

    Optional<BankAccount> findByAccountNumber(String accountNumber);
    //Найти счета с балансом выше указанного
    List<BankAccount> findByBalanceGreaterThan(BigDecimal amount);
    //Найти активные счета отсортированные по балансу
    List<BankAccount> findByStatusOrderByBalanceDesc(AccountStatus status);
   //Кастомный JPQL
   @Query("SELECT a FROM BankAccount a WHERE a.balance BETWEEN :min AND :max")
   List<BankAccount> findByBalanceRange(
          @Param("min") BigDecimal min,
          @Param("max") BigDecimal max);
    // Нативный SQL-запрос (иногда нужен для сложных случаев)
    @Query(value = "SELECT * FROM accounts WHERE owner ILIKE %:name%",
            nativeQuery = true)
    List<BankAccount> searchByOwnerName(@Param("name") String name);
    // Решение N+1 — загружаем транзакции вместе со счетами одним JOIN
    @Query("SELECT DISTINCT a FROM BankAccount a LEFT JOIN FETCH a.transactions")
    List<BankAccount> findAllWithTransactions();

    @EntityGraph(attributePaths = {"transactions"})
    Optional<BankAccount> findWithTransactionsById(Long id);

    Page<BankAccount> findByUser_UserName(String username, Pageable pageable);

    Page<BankAccount> findByUser_UserNameAndStatus(
            String username,
            AccountStatus status,
            Pageable pageable);

    Page<BankAccount> findByOwnerContainingIgnoreCaseAndUser_UserName(
            String owner,
            String username,
            Pageable pageable);

    @Query("""
       select a 
       from BankAccount a
       join fetch a.user
       where a.id = :id
       """)
    Optional<BankAccount> findByIdWithUser(@Param("id") Long id);
}