package com.example.bank.service;

import com.example.bank.actuator.AccountMetrics;
import com.example.bank.dto.StatementResponse;
import com.example.bank.event.BankEventPublisher;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.InactiveAccountException;
import com.example.bank.exception.UserInactiveException;
import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.repository.TransferRepository;
import com.example.bank.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.util.List;

@Service
public class BankAccountService {

    private static final Logger log =
            LoggerFactory.getLogger(BankAccountService.class);


    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final BankEventPublisher eventPublisher;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountMetrics accountMetrics;


    public BankAccountService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              TransferRepository transferRepository,
                              UserRepository userRepository,
                              BankEventPublisher eventPublisher,
                              AccountNumberGenerator accountNumberGenerator,
                              AccountMetrics accountMetrics) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.accountNumberGenerator = accountNumberGenerator;
        this.accountMetrics = accountMetrics;

    }

//    public BankAccount createAccount(String owner) {
//        try {
//            BankAccount account = accountRepository.save(
//                    new BankAccount(owner));
//
//
//
//            eventPublisher.publishAccountCreated(
//                    new AccountCreatedEvent(
//                            account.getId(),
//                            owner,
//                            account.getBalance()
//                    )
//            );
//
//            log.info("Счёт успешно создан: accountId={}, owner={}",
//                    account.getId(), owner);
//
//            return account;
//
//        } catch (Exception e) {
//            log.error("Неожиданная ошибка при создании счёта: owner={}",
//                    owner, e);
//            throw e;
//        }
//    }

    // Все счета с пагинацией
    public Page<BankAccount> getAllAccountsPaged(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    // Счета по активности с пагинацией
    public Page<BankAccount> getAccountsPaged(AccountStatus status, Pageable pageable) {
        return accountRepository.findByStatus(status, pageable);
    }

    // Поиск по имени с пагинацией
    public Page<BankAccount> searchByName(String name, Pageable pageable) {
        return accountRepository.findByOwnerContainingIgnoreCase(name, pageable);
    }

    // История транзакций с пагинацией
    public Page<Transaction> getTransactionHistoryPaged(Long accountId, Pageable pageable) {
        findById(accountId); // проверяем существование
        return transactionRepository.findByAccountId(accountId, pageable);
    }



    public List<BankAccount> getAllAccounts() {

        return accountRepository.findAll();
    }


    public BankAccount findById(Long id) {
        try {
            return accountRepository.findById(id)
                    .orElseThrow(() -> new AccountNotFoundException(id));
        } catch (AccountNotFoundException e) {
            log.warn("Счёт не найден: accountId={}, message={}",
                    id, e.getMessage());
            throw e;
        }
    }

    public BankAccount updateAccount(Long id, String newOwner,
                                     AccountStatus newStatus) {
        BankAccount account = findById(id);

        if (newOwner != null) {
            account.setOwner(newOwner);
        }

        if (newStatus != null) {
            // пока здесь ничего не придумываем
        }

        return accountRepository.save(account);
    }

    public BankAccount updateStatus(Long id, AccountStatus newStatus) {
        BankAccount account = findById(id);

        if (newStatus == null) {
            return account;
        }

        switch (newStatus) {
            case ACTIVE -> account.activate();
            case BLOCKED -> account.block();
            case CLOSED -> account.close();
        }

        return accountRepository.save(account);
    }




    @Transactional
    public void deposit(Long id, BigDecimal amount) {
        deposit(id, amount, TransactionSource.API);
    }

    @Transactional
    public void deposit(
            Long id,
            BigDecimal amount,
            TransactionSource source
    ) {
        BankAccount account = findById(id);

        String username = account.getOwner();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() ->
                        new UserInactiveException(username));

        if (!user.isActive()) {
            log.warn("Попытка операции с заблокированным пользователем: accountId={}, username={}",
                    id, username);
            throw new InactiveAccountException();
        }

        account.deposit(amount);

        transactionRepository.save(
                new Transaction(
                        account,
                        amount,
                        TransactionType.DEPOSIT,
                        source
                )
        );

        log.info("Счёт пополнен: accountId={}, amount={}, source={}",
                id, amount, source);
    }



    @Transactional
    public void withdraw(Long id, BigDecimal amount) {
        withdraw(id, amount, TransactionSource.API);
    }

    @Transactional
    public void withdraw(
            Long id,
            BigDecimal amount,
            TransactionSource source
    ) {
        BankAccount account = findById(id);

        String username = account.getOwner();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() ->
                        new UserInactiveException(username));

        if (!user.isActive()) {
            log.warn("Попытка операции с заблокированным пользователем: accountId={}, username={}",
                    id, username);
            throw new InactiveAccountException();
        }

        account.withdraw(amount);

        transactionRepository.save(
                new Transaction(
                        account,
                        amount,
                        TransactionType.WITHDRAW,
                        source
                )
        );
        log.info("Снятие со счёта: accountId={}, amount={}, source={}",
                id, amount, source);
    }




    public Page<Transaction> getTransactionHistory(Long accountId, Pageable pageable) {
        findById(accountId);
        return transactionRepository.findByAccountId(accountId,pageable);
    }

    public StatementResponse getStatement(Long accountId) {
        BankAccount account = findById(accountId);

        List<Transaction> transactions =
                transactionRepository.findByAccountId(accountId);

        long transactionCount =
                transactionRepository.countByAccountId(accountId);

        List<Transfer> outgoing =
                transferRepository.findByFromAccountId(accountId);

        List<Transfer> incoming =
                transferRepository.findByToAccountId(accountId);

        int outgoingCount = outgoing.size();
        int incomingCount = incoming.size();
        return new StatementResponse(accountId,
                account.getOwner(),
                account.getBalance(),
                account.isActive(),
                transactionCount,
                outgoingCount,
                incomingCount);
    }

    public Page<BankAccount> getAccountsPagedByUsername(
            String username,
            Pageable pageable) {

        return accountRepository.findByUser_UserName(username, pageable);
    }

    public Page<BankAccount> getAccountsPagedByUsername(
            AccountStatus status,
            String username,
            Pageable pageable) {

        return accountRepository.findByUser_UserNameAndStatus(
                username,
                status,
                pageable
        );
    }

    public Page<BankAccount> searchByNameForUser(
            String name,
            String username,
            Pageable pageable) {

        return accountRepository.findByOwnerContainingIgnoreCaseAndUser_UserName(
                name,
                username,
                pageable
        );
    }



}