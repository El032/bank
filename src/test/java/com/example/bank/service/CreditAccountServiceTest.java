package com.example.bank.service;

import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.CreditAccountNotFoundException;
import com.example.bank.model.BankAccount;
import com.example.bank.model.CreditAccount;
import com.example.bank.model.Transaction;
import com.example.bank.model.TransactionType;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.CreditAccountRepository;
import com.example.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditAccountService — юнит-тесты")
class CreditAccountServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CreditAccountRepository creditAccountRepository;

    @InjectMocks
    private CreditAccountService service;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private CreditAccount creditAccount;

    @BeforeEach
    void setUp() {

        creditAccount = new CreditAccount(
                "Eldar",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        ReflectionTestUtils.setField(
                creditAccount,
                "id",
                1L
        );
    }

    // ==================== FIND BY ID ====================

    @Nested
    @DisplayName("Поиск кредитного счёта")
    class FindByIdTests {

        @Test
        @DisplayName("Кредитный счёт найден и возвращён")
        void findById_existingCreditAccount_shouldReturnAccount() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(creditAccount));

            CreditAccount result =
                    service.findById(1L);

            assertSame(
                    creditAccount,
                    result
            );

            verify(accountRepository)
                    .findById(1L);
        }

        @Test
        @DisplayName("Несуществующий счёт выбрасывает AccountNotFoundException")
        void findById_notFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.findById(99L)
            );
        }

        @Test
        @DisplayName("Обычный BankAccount нельзя получить как CreditAccount")
        void findById_nonCreditAccount_shouldThrow() {

            BankAccount bankAccount =
                    new BankAccount("Eldar");

            ReflectionTestUtils.setField(
                    bankAccount,
                    "id",
                    2L
            );

            when(accountRepository.findById(2L))
                    .thenReturn(Optional.of(bankAccount));

            assertThrows(
                    CreditAccountNotFoundException.class,
                    () -> service.findById(2L)
            );
        }
    }

    // ==================== ACCRUE INTEREST ====================

    @Nested
    @DisplayName("Начисление процентов")
    class AccrueInterestTests {

        @Test
        @DisplayName("Начисленные проценты сохраняются как INTEREST транзакция")
        void accrueInterest_shouldSaveInterestTransaction() {

            creditAccount.withdraw(
                    new BigDecimal("5000")
            );

            ReflectionTestUtils.setField(
                    creditAccount,
                    "nextPaymentDate",
                    LocalDate.now().minusDays(1)
            );

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(creditAccount));

            service.accrueInterest(1L);

            verify(transactionRepository)
                    .save(transactionCaptor.capture());

            Transaction transaction =
                    transactionCaptor.getValue();

            assertEquals(
                    TransactionType.INTEREST,
                    transaction.getType()
            );

            assertEquals(
                    new BigDecimal("50.00"),
                    transaction.getAmount()
            );

            assertEquals(
                    new BigDecimal("5050.00"),
                    creditAccount.getBalance()
            );
        }

        @Test
        @DisplayName("До наступления даты платежа транзакция не сохраняется")
        void accrueInterest_beforePaymentDate_shouldNotSaveTransaction() {

            creditAccount.withdraw(
                    new BigDecimal("5000")
            );

            ReflectionTestUtils.setField(
                    creditAccount,
                    "nextPaymentDate",
                    LocalDate.now().plusDays(1)
            );

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(creditAccount));

            service.accrueInterest(1L);

            verify(
                    transactionRepository,
                    never()
            ).save(any());

            assertEquals(
                    new BigDecimal("5000.00"),
                    creditAccount.getBalance()
            );
        }

        @Test
        @DisplayName("При нулевой задолженности транзакция не создаётся")
        void accrueInterest_zeroDebt_shouldNotSaveTransaction() {

            ReflectionTestUtils.setField(
                    creditAccount,
                    "nextPaymentDate",
                    LocalDate.now().minusDays(1)
            );

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(creditAccount));

            service.accrueInterest(1L);

            verify(
                    transactionRepository,
                    never()
            ).save(any());

            assertEquals(
                    new BigDecimal("0.00"),
                    creditAccount.getBalance()
            );
        }
    }

    // ==================== ACCRUE ALL INTEREST ====================

    @Nested
    @DisplayName("Начисление процентов всем кредитным счетам")
    class AccrueAllInterestTests {

        @Test
        @DisplayName("Проценты начисляются всем счетам, где они должны начислиться")
        void accrueAllInterest_shouldSaveInterestTransactions() {

            CreditAccount account1 = new CreditAccount(
                    "Eldar",
                    new BigDecimal("10000"),
                    new BigDecimal("12")
            );

            CreditAccount account2 = new CreditAccount(
                    "Ali",
                    new BigDecimal("20000"),
                    new BigDecimal("10")
            );

            account1.withdraw(new BigDecimal("5000"));
            account2.withdraw(new BigDecimal("10000"));

            ReflectionTestUtils.setField(
                    account1,
                    "nextPaymentDate",
                    LocalDate.now().minusDays(1)
            );

            ReflectionTestUtils.setField(
                    account2,
                    "nextPaymentDate",
                    LocalDate.now().minusDays(1)
            );

            when(creditAccountRepository.findAll())
                    .thenReturn(List.of(account1, account2));

            service.accrueAllInterest();

            verify(transactionRepository, times(2))
                    .save(transactionCaptor.capture());

            List<Transaction> transactions =
                    transactionCaptor.getAllValues();

            assertEquals(2, transactions.size());

            assertEquals(
                    TransactionType.INTEREST,
                    transactions.get(0).getType()
            );

            assertEquals(
                    TransactionType.INTEREST,
                    transactions.get(1).getType()
            );

            assertEquals(
                    new BigDecimal("50.00"),
                    transactions.get(0).getAmount()
            );

            assertEquals(
                    new BigDecimal("83.33"),
                    transactions.get(1).getAmount()
            );
        }

        @Test
        @DisplayName("Если проценты начислять не нужно, транзакции не сохраняются")
        void accrueAllInterest_beforePaymentDate_shouldNotSaveTransactions() {

            CreditAccount account1 = new CreditAccount(
                    "Eldar",
                    new BigDecimal("10000"),
                    new BigDecimal("12")
            );

            account1.withdraw(new BigDecimal("5000"));

            ReflectionTestUtils.setField(
                    account1,
                    "nextPaymentDate",
                    LocalDate.now().plusDays(1)
            );

            when(creditAccountRepository.findAll())
                    .thenReturn(List.of(account1));

            service.accrueAllInterest();

            verify(
                    transactionRepository,
                    never()
            ).save(any());
        }

        @Test
        @DisplayName("Пустой список кредитных счетов обрабатывается без ошибок")
        void accrueAllInterest_emptyList_shouldNotSaveTransactions() {

            when(creditAccountRepository.findAll())
                    .thenReturn(List.of());

            assertDoesNotThrow(
                    () -> service.accrueAllInterest()
            );

            verify(
                    transactionRepository,
                    never()
            ).save(any());
        }
    }
}