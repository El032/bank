package com.example.bank.service;

import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.SavingsAccountNotFoundException;
import com.example.bank.model.BankAccount;
import com.example.bank.model.SavingsAccount;
import com.example.bank.model.Transaction;
import com.example.bank.model.TransactionType;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.SavingsAccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsAccountServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SavingsAccountRepository savingsAccountRepository;

    @InjectMocks
    private SavingsAccountService service;


    // =========================================================
    // findById()
    // =========================================================

    @Test
    void shouldFindSavingsAccountById() {

        Long accountId = 1L;

        SavingsAccount account = mock(SavingsAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        SavingsAccount result = service.findById(accountId);

        assertSame(account, result);

        verify(accountRepository).findById(accountId);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(savingsAccountRepository);
    }


    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {

        Long accountId = 1L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> service.findById(accountId)
        );

        verify(accountRepository).findById(accountId);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(savingsAccountRepository);
    }


    @Test
    void shouldThrowSavingsAccountNotFoundExceptionWhenAccountIsNotSavingsAccount() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(
                SavingsAccountNotFoundException.class,
                () -> service.findById(accountId)
        );

        verify(accountRepository).findById(accountId);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(savingsAccountRepository);
    }


    // =========================================================
    // accrueInterest()
    // =========================================================

    @Test
    void shouldAccrueInterestAndSaveTransaction() {

        Long accountId = 1L;

        SavingsAccount account = mock(SavingsAccount.class);

        Transaction transaction = new Transaction(
                account,
                new BigDecimal("50.00"),
                TransactionType.INTEREST
        );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(account.accrueInterest())
                .thenReturn(transaction);

        service.accrueInterest(accountId);

        verify(accountRepository).findById(accountId);
        verify(account).accrueInterest();
        verify(transactionRepository).save(transaction);

        verifyNoInteractions(savingsAccountRepository);
    }


    @Test
    void shouldNotSaveTransactionWhenInterestIsNotDue() {

        Long accountId = 1L;

        SavingsAccount account = mock(SavingsAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(account.accrueInterest())
                .thenReturn(null);

        service.accrueInterest(accountId);

        verify(accountRepository).findById(accountId);
        verify(account).accrueInterest();

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verifyNoInteractions(savingsAccountRepository);
    }


    @Test
    void shouldNotAccrueInterestWhenAccountDoesNotExist() {

        Long accountId = 1L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> service.accrueInterest(accountId)
        );

        verify(accountRepository).findById(accountId);

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(savingsAccountRepository);
    }


    @Test
    void shouldNotAccrueInterestWhenAccountIsNotSavingsAccount() {

        Long accountId = 1L;

        BankAccount account = mock(BankAccount.class);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(
                SavingsAccountNotFoundException.class,
                () -> service.accrueInterest(accountId)
        );

        verify(accountRepository).findById(accountId);

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(savingsAccountRepository);
    }


    // =========================================================
    // accrueAllInterest()
    // =========================================================

    @Test
    void shouldAccrueInterestForAllSavingsAccounts() {

        SavingsAccount account1 = mock(SavingsAccount.class);
        SavingsAccount account2 = mock(SavingsAccount.class);

        Transaction transaction1 = new Transaction(
                account1,
                new BigDecimal("50.00"),
                TransactionType.INTEREST
        );

        Transaction transaction2 = new Transaction(
                account2,
                new BigDecimal("100.00"),
                TransactionType.INTEREST
        );

        when(savingsAccountRepository.findAll())
                .thenReturn(List.of(account1, account2));

        when(account1.accrueInterest())
                .thenReturn(transaction1);

        when(account2.accrueInterest())
                .thenReturn(transaction2);

        service.accrueAllInterest();

        verify(savingsAccountRepository).findAll();

        verify(account1).accrueInterest();
        verify(account2).accrueInterest();

        verify(transactionRepository).save(transaction1);
        verify(transactionRepository).save(transaction2);

        verifyNoInteractions(accountRepository);
    }


    @Test
    void shouldAccrueInterestOnlyForAccountsWhereInterestIsDue() {

        SavingsAccount account1 = mock(SavingsAccount.class);
        SavingsAccount account2 = mock(SavingsAccount.class);

        Transaction transaction = new Transaction(
                account1,
                new BigDecimal("50.00"),
                TransactionType.INTEREST
        );

        when(savingsAccountRepository.findAll())
                .thenReturn(List.of(account1, account2));

        when(account1.accrueInterest())
                .thenReturn(transaction);

        when(account2.accrueInterest())
                .thenReturn(null);

        service.accrueAllInterest();

        verify(savingsAccountRepository).findAll();

        verify(account1).accrueInterest();
        verify(account2).accrueInterest();

        verify(transactionRepository).save(transaction);

        verify(transactionRepository, times(1))
                .save(any(Transaction.class));
    }


    @Test
    void shouldNotSaveTransactionsWhenInterestIsNotDueForAnyAccount() {

        SavingsAccount account1 = mock(SavingsAccount.class);
        SavingsAccount account2 = mock(SavingsAccount.class);

        when(savingsAccountRepository.findAll())
                .thenReturn(List.of(account1, account2));

        when(account1.accrueInterest())
                .thenReturn(null);

        when(account2.accrueInterest())
                .thenReturn(null);

        service.accrueAllInterest();

        verify(savingsAccountRepository).findAll();

        verify(account1).accrueInterest();
        verify(account2).accrueInterest();

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    @Test
    void shouldDoNothingWhenThereAreNoSavingsAccounts() {

        when(savingsAccountRepository.findAll())
                .thenReturn(List.of());

        service.accrueAllInterest();

        verify(savingsAccountRepository).findAll();

        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(accountRepository);
    }


    @Test
    void shouldProcessAllSavingsAccountsInReturnedList() {

        SavingsAccount account1 = mock(SavingsAccount.class);
        SavingsAccount account2 = mock(SavingsAccount.class);
        SavingsAccount account3 = mock(SavingsAccount.class);

        when(savingsAccountRepository.findAll())
                .thenReturn(List.of(account1, account2, account3));

        when(account1.accrueInterest()).thenReturn(null);
        when(account2.accrueInterest()).thenReturn(null);
        when(account3.accrueInterest()).thenReturn(null);

        service.accrueAllInterest();

        verify(account1).accrueInterest();
        verify(account2).accrueInterest();
        verify(account3).accrueInterest();
    }
}