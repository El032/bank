package com.example.bank.model;

import com.example.bank.exception.InactiveAccountException;
import com.example.bank.exception.InsufficientFundsException;
import com.example.bank.exception.InvalidAmountException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BankAccount — тесты модели")
class BankAccountTest {

    private BankAccount account;

    @BeforeEach
    void setUp() {
        account = new BankAccount("Eldar");
        ReflectionTestUtils.setField(account, "id", 1L);


    }

    @Nested
    @DisplayName("Пополнение счёта (deposit)")
    class DepositTests {

        @Test
        @DisplayName("Успешное пополнение увеличивает баланс")
        void deposit_shouldIncreaseBalance() {
            // Arrange
            BigDecimal amount = new BigDecimal("1000000000000.00");

            // Act
            account.deposit(amount);

            // Assert
            assertEquals(new BigDecimal("1000000000000.00"), account.getBalance());
        }


        @ParameterizedTest
        @DisplayName("Пополнение на нулевую или отрицательную сумму запрещено")
        @ValueSource(strings = {"0","-1", "-100", "-0.01"})
        void deposit_invalidAmount_shouldThrow(String amount) {
            assertThrows(InvalidAmountException.class,
                    () -> account.deposit(new BigDecimal(amount)));
        }

        @Test
        @DisplayName("Пополнение с заблокированного счёта запрещено")
        void deposit_blockedAccount_shouldThrow(){
            account.block();
            assertThrows(InactiveAccountException.class,
                    () -> account.deposit(new BigDecimal("500.00")));
        }

        @Test
        @DisplayName("Успешно после двух пополнений баланса")
        void deposit_shouldIncreaseTwoBalance(){
            BigDecimal depositAmount1 = new BigDecimal("500.00");
            BigDecimal depositAmount2 = new BigDecimal("300.00");

            account.deposit(depositAmount1);
            account.deposit(depositAmount2);

            assertEquals(new BigDecimal("800.00"), account.getBalance());
        }

        @Test
        @DisplayName("Проверь несколько операций подряд")
        void depositAndWithdraw_shouldIncreaseBalance(){
            BigDecimal depositAmount1 = new BigDecimal("1000.00");
            BigDecimal withdrawAmount1 = new BigDecimal("200.00");
            BigDecimal depositAmount2 = new BigDecimal("50.00");
            BigDecimal withdrawAmount2 = new BigDecimal("100.00");
            account.deposit(depositAmount1);
            account.withdraw(withdrawAmount1);
            account.deposit(depositAmount2);
            account.withdraw(withdrawAmount2);
            assertEquals(new BigDecimal("750.00"), account.getBalance());

        }

        @Test
        @DisplayName("После неудачного снятия денег баланс не должен измениться")
        void withdraw_failed_shouldNotChangeBalance(){

            BigDecimal depositAmount1 = new BigDecimal("500.00");

            BigDecimal withdrawAmount1 = new BigDecimal("1000.00");


            account.deposit(depositAmount1);
            assertThrows(InsufficientFundsException.class,
                    () -> account.withdraw(withdrawAmount1));

            assertEquals(new BigDecimal("500.00"), account.getBalance());

        }

        @Test
        @DisplayName("после неудачного пополнения баланс тоже не меняется")
        void deposit_failed_shouldNotChangeBalance(){
            BigDecimal depositAmount1 = new BigDecimal("1000.00");
            BigDecimal depositAmount2 = new BigDecimal("-200.00");
            account.deposit(depositAmount1);
            assertThrows(InvalidAmountException.class,
                    () -> account.deposit(depositAmount2));

            assertEquals(new BigDecimal("1000.00"), account.getBalance());
        }
        @Test
        @DisplayName("последовательность операций")
        void deposit_withdraw_shouldDecreaseBalance(){
            BigDecimal depositAmount1 = new BigDecimal("1000.00");
            BigDecimal withdrawAmount1 = new BigDecimal("200.00");
            BigDecimal depositAmount2 = new BigDecimal("50.00");
            BigDecimal withdrawAmount2 = new BigDecimal("300.00");
            BigDecimal depositAmount3 = new BigDecimal("150.00");
            BigDecimal withdrawAmount3 = new BigDecimal("100.00");
            account.deposit(depositAmount1);
            account.withdraw(withdrawAmount1);
            account.withdraw(withdrawAmount2);
            account.deposit(depositAmount2);
            account.deposit(depositAmount3);
            account.withdraw(withdrawAmount3);
            assertEquals(new BigDecimal("600.00"), account.getBalance());
        }

    }

    @Nested
    @DisplayName("Снятие средств (withdraw)")
    class WithdrawTests {

        @Test
        @DisplayName("Успешное снятие уменьшает баланс")
        void withdraw_shouldDecreaseBalance() {
            // Arrange
            BigDecimal initialBalance = new BigDecimal("1000.00");
            BigDecimal withdrawAmount = new BigDecimal("300.00");

            // Act
            account.deposit(initialBalance);
            account.withdraw(withdrawAmount);

            // Assert
            assertEquals(new BigDecimal("700.00"), account.getBalance());
        }

        @Test
        @DisplayName("Снятие всего баланса оставляет ноль")
        void withdraw_fullBalance_leavesZero() {
            BigDecimal amount = new BigDecimal("1000.00");
            account.deposit(amount);
            account.withdraw(new BigDecimal("1000.00"));
            assertEquals(BigDecimal.ZERO.setScale(2), account.getBalance());
        }

        @Test
        @DisplayName("Снятие больше баланса выбрасывает исключение")
        void withdraw_moreThanBalance_shouldThrow() {
            InsufficientFundsException ex = assertThrows(
                    InsufficientFundsException.class,
                    () -> account.withdraw(new BigDecimal("1500.00"))
            );
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("Недостаточно"));
        }

        @ParameterizedTest
        @DisplayName("Снятие на нулевую или отрицательную сумму запрещено")
        @ValueSource(strings = {"0","-1", "-100", "-0.01"})
        void withdraw_invalidAmount_shouldThrow(String amount) {
            assertThrows(InvalidAmountException.class,
                    () -> account.withdraw(new BigDecimal(amount)));
        }

        @Test
        @DisplayName("Снятие с заблокированного счёта запрещено")
        void withdraw_blockedAccount_shouldThrow() {
            account.block();
            assertThrows(InactiveAccountException.class,
                    () -> account.withdraw(new BigDecimal("100.00")));
        }
    }

    @Test
    @DisplayName("Новый счёт по умолчанию активен")
    void newAccount_shouldBeActiveByDefault() {
        BankAccount newAccount = new BankAccount("Kola");
        assertTrue(newAccount.isActive());
    }

    @Test
    @DisplayName("Все поля заполнены корректно после создания")
    void newAccount_shouldHaveCorrectFields() {
        assertAll(
                () -> assertEquals("Eldar", account.getOwner()),
                () -> assertEquals(new BigDecimal("0.00"), account.getBalance()),
                () -> assertTrue(account.isActive()),
                () -> assertEquals(1L, account.getId())
        );
    }
}