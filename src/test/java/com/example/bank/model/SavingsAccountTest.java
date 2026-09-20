//package com.example.bank.model;
//
//import org.junit.jupiter.api.Test;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class SavingsAccountTest {
//
//    @Test
//    void shouldAccrueInterest() {
//
//        SavingsAccount account = new SavingsAccount(
//                "Test",
//                new BigDecimal("5000"),
//                new BigDecimal("12")
//        );
//        account.setLastInterest(LocalDate.now().minusMonths(1));
//        Transaction transaction = account.accrueInterest();
//
//        assertEquals(new BigDecimal("5050.00"), account.getBalance());
//        assertNotNull(transaction);
//        assertEquals(new BigDecimal("50.00"), transaction.getAmount());
//        assertEquals(TransactionType.INTEREST, transaction.getType());
//    }
//
//    @Test
//    void shouldNotAccrueInterestBeforeOneMonth() {
//        SavingsAccount account = new SavingsAccount(
//                "Test",
//                new BigDecimal("5000"),
//                new BigDecimal("12")
//        );
//        account.setLastInterest(LocalDate.now().minusDays(10));
//        Transaction transaction = account.accrueInterest();
//
//        assertNull(transaction);
//        assertEquals(new BigDecimal("5000.00"), account.getBalance());
//    }
//
//    @Test
//    void shouldUpdateLastInterestAfterAccrual() {
//
//        SavingsAccount account = new SavingsAccount(
//                "Test",
//                new BigDecimal("5000"),
//                new BigDecimal("12")
//        );
//
//        LocalDate oldLastInterest = account.getLastInterest();
//
//        account.setLastInterest(oldLastInterest.minusMonths(1));
//
//        account.accrueInterest();
//
//        assertEquals(oldLastInterest, account.getLastInterest());
//    }
//
//    @Test
//    void shouldNotAllowDeposit() {
//        SavingsAccount account = new SavingsAccount(
//                "Test",
//                new BigDecimal("5000"),
//                new BigDecimal("12")
//        );
//
//        assertThrows(
//                UnsupportedOperationException.class,
//                () -> account.deposit(new BigDecimal("100"))
//        );
//
//        assertEquals(new BigDecimal("5000.00"), account.getBalance());
//    }
//}
