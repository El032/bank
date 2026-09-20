package com.example.bank.service;

import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.CreditAccountNotFoundException;
import com.example.bank.model.BankAccount;
import com.example.bank.model.CreditAccount;
import com.example.bank.model.Transaction;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CreditAccountRepository;
import com.example.bank.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditAccountService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CreditAccountRepository creditAccountRepository;

    public CreditAccountService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CreditAccountRepository creditAccountRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.creditAccountRepository = creditAccountRepository;
    }

    public CreditAccount findById(Long id) {

        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (!(account instanceof CreditAccount creditAccount)) {
            throw new CreditAccountNotFoundException(id);
        }

        return creditAccount;
    }

    public void accrueInterest(Long id) {

        CreditAccount creditAccount = findById(id);

        Transaction transaction = creditAccount.accrueInterest();

        if (transaction != null) {
            transactionRepository.save(transaction);
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
      public void accrueAllInterest() {

        List<CreditAccount> creditAccounts =
                creditAccountRepository.findAll();

        for (CreditAccount creditAccount : creditAccounts) {

            Transaction transaction =
                    creditAccount.accrueInterest();

            if (transaction != null) {
                transactionRepository.save(transaction);
            }
        }
    }
}