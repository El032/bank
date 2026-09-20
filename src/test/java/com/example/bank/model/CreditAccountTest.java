package com.example.bank.model;

import com.example.bank.exception.CreditLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CreditAccountTest {

    @Test
    void shouldCreateCreditAccount() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        assertEquals(new BigDecimal("10000"), account.getCreditLimit());
        assertEquals(new BigDecimal("12"), account.getInterestRate());
        assertEquals(new BigDecimal("0.00"), account.getBalance());
    }

    @Test
    void shouldRejectInvalidCreditLimit() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CreditAccount(
                        "Test",
                        BigDecimal.ZERO,
                        new BigDecimal("12")
                )
        );
    }

    @Test
    void shouldRejectInvalidInterestRate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CreditAccount(
                        "Test",
                        new BigDecimal("10000"),
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void shouldSetPaymentDatesOnFirstWithdrawal() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("3000"));

        assertEquals(
                LocalDate.now(),
                account.getFirstTransactionDate()
        );

        assertEquals(
                LocalDate.now().plusMonths(1),
                account.getNextPaymentDate()
        );

        assertEquals(
                new BigDecimal("3000.00"),
                account.getBalance()
        );
    }

    @Test
    void shouldNotChangePaymentDatesOnSecondWithdrawal() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("3000"));

        LocalDate firstTransactionDate = account.getFirstTransactionDate();
        LocalDate nextPaymentDate = account.getNextPaymentDate();

        account.withdraw(new BigDecimal("1000"));

        assertEquals(firstTransactionDate, account.getFirstTransactionDate());
        assertEquals(nextPaymentDate, account.getNextPaymentDate());
        assertEquals(new BigDecimal("4000.00"), account.getBalance());
    }

    @Test
    void shouldNotExceedCreditLimit() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("8000"));

        assertThrows(
                CreditLimitExceededException.class,
                () -> account.withdraw(new BigDecimal("2001"))
        );

        assertEquals(
                new BigDecimal("8000.00"),
                account.getBalance()
        );
    }

    @Test
    void shouldAllowWithdrawalUpToCreditLimit() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("8000"));
        account.withdraw(new BigDecimal("2000"));

        assertEquals(
                new BigDecimal("10000.00"),
                account.getBalance()
        );
    }

    @Test
    void shouldKeepBalanceWhenWithdrawalExceedsCreditLimit() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("9000"));

        assertThrows(
                CreditLimitExceededException.class,
                () -> account.withdraw(new BigDecimal("1001"))
        );

        assertEquals(
                new BigDecimal("9000.00"),
                account.getBalance()
        );
    }

    @Test
    void shouldDecreaseDebtAfterDeposit() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("5000"));
        account.deposit(new BigDecimal("2000"));

        assertEquals(
                new BigDecimal("3000.00"),
                account.getBalance()
        );
    }

    @Test
    void shouldAccrueInterestAfterPaymentDate() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("5000"));

        ReflectionTestUtils.setField(
                account,
                "nextPaymentDate",
                LocalDate.now().minusDays(1)
        );

        Transaction transaction = account.accrueInterest();

        assertNotNull(transaction);
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
                account.getBalance()
        );
    }

    @Test
    void shouldMoveNextPaymentDateAfterAccrual() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("5000"));

        LocalDate oldPaymentDate = LocalDate.now().minusDays(1);

        ReflectionTestUtils.setField(
                account,
                "nextPaymentDate",
                oldPaymentDate
        );

        account.accrueInterest();

        assertEquals(
                oldPaymentDate.plusMonths(1),
                account.getNextPaymentDate()
        );
    }

    @Test
    void shouldNotAccrueInterestBeforePaymentDate() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("5000"));

        ReflectionTestUtils.setField(
                account,
                "nextPaymentDate",
                LocalDate.now().plusDays(1)
        );

        Transaction transaction = account.accrueInterest();

        assertNull(transaction);
        assertEquals(
                new BigDecimal("5000.00"),
                account.getBalance()
        );
    }

    @Test
    void shouldNotAccrueInterestWhenDebtIsZero() {

        CreditAccount account = new CreditAccount(
                "Test",
                new BigDecimal("10000"),
                new BigDecimal("12")
        );

        account.withdraw(new BigDecimal("5000"));
        account.deposit(new BigDecimal("5000"));

        ReflectionTestUtils.setField(
                account,
                "nextPaymentDate",
                LocalDate.now().minusDays(1)
        );

        Transaction transaction = account.accrueInterest();

        assertNull(transaction);
        assertEquals(
                new BigDecimal("0.00"),
                account.getBalance()
        );
    }
}