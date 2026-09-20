package com.example.bank.service;

import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.SavingsAccountNotFoundException;
import com.example.bank.model.BankAccount;
import com.example.bank.model.SavingsAccount;
import com.example.bank.model.Transaction;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.SavingsAccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavingsAccountService {
    private final TransactionRepository  transactionRepository;
    private final AccountRepository accountRepository;
    private  final SavingsAccountRepository savingsAccountRepository;

    public SavingsAccountService(TransactionRepository transactionRepository,
                                 AccountRepository accountRepository,
                                 SavingsAccountRepository savingsAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.savingsAccountRepository = savingsAccountRepository;

    }

    public SavingsAccount findById(Long id)  {
        BankAccount account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if(!(account instanceof SavingsAccount)) {
          throw new SavingsAccountNotFoundException(id);
        }
       return (SavingsAccount) account;
    }

    public void accrueInterest(Long id){
        SavingsAccount savingsAccount = findById(id);
        Transaction transaction = savingsAccount.accrueInterest();
         if(transaction !=null){
             transactionRepository.save(transaction);
         }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void accrueAllInterest(){

        List<SavingsAccount> savingsAccounts = savingsAccountRepository.findAll();

        for (SavingsAccount savingsAccount : savingsAccounts) {
            Transaction transaction = savingsAccount.accrueInterest();

            if(transaction !=null){
                transactionRepository.save(transaction);
            }
        }
    }
}
