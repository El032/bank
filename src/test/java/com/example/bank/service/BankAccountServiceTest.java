package com.example.bank.service;

import com.example.bank.actuator.AccountMetrics;
import com.example.bank.dto.StatementResponse;
import com.example.bank.event.BankEventPublisher;
import com.example.bank.exception.*;
import com.example.bank.model.*;
import com.example.bank.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccountService — юнит-тесты")
class BankAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankEventPublisher eventPublisher;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private AccountMetrics accountMetrics;

    @InjectMocks
    private BankAccountService service;

    @Captor
    private ArgumentCaptor<BankAccount> accountCaptor;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private BankAccount testAccount;
    private User activeUser;

    @BeforeEach
    void setUp() {

        testAccount = new BankAccount("Eldar");

        ReflectionTestUtils.setField(
                testAccount,
                "id",
                1L
        );

        testAccount.activate();

        activeUser = new User(
                "Eldar",
                "eldar@test.com",
                "Eldar",
                "password"
        );

        activeUser.activate();
    }


    // =========================================================
    // FIND BY ID
    // =========================================================

    @Nested
    @DisplayName("Поиск счёта по ID")
    class FindByIdTests {

        @Test
        @DisplayName("Существующий счёт возвращается")
        void findById_existing_shouldReturnAccount() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            BankAccount result = service.findById(1L);

            assertEquals(testAccount, result);

            verify(accountRepository)
                    .findById(1L);
        }


        @Test
        @DisplayName("Несуществующий счёт выбрасывает AccountNotFoundException")
        void findById_notFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            AccountNotFoundException exception =
                    assertThrows(
                            AccountNotFoundException.class,
                            () -> service.findById(99L)
                    );

            assertTrue(
                    exception.getMessage().contains("99")
            );

            verify(accountRepository)
                    .findById(99L);
        }
    }


    // =========================================================
    // GET ALL ACCOUNTS
    // =========================================================

    @Nested
    @DisplayName("Получение всех счетов")
    class GetAllAccountsTests {

        @Test
        @DisplayName("Возвращает все счета")
        void getAllAccounts_shouldReturnAll() {

            BankAccount account2 =
                    new BankAccount("Kola");

            ReflectionTestUtils.setField(
                    account2,
                    "id",
                    2L
            );

            when(accountRepository.findAll())
                    .thenReturn(
                            List.of(
                                    testAccount,
                                    account2
                            )
                    );

            List<BankAccount> result =
                    service.getAllAccounts();

            assertEquals(2, result.size());
            assertEquals(testAccount, result.get(0));
            assertEquals(account2, result.get(1));

            verify(accountRepository)
                    .findAll();
        }


        @Test
        @DisplayName("Пустой репозиторий возвращает пустой список")
        void getAllAccounts_empty_shouldReturnEmptyList() {

            when(accountRepository.findAll())
                    .thenReturn(List.of());

            List<BankAccount> result =
                    service.getAllAccounts();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(accountRepository)
                    .findAll();
        }
    }


    // =========================================================
    // PAGINATION
    // =========================================================

    @Nested
    @DisplayName("Пагинация")
    class PaginationTests {

        private Pageable pageable() {
            return PageRequest.of(0, 20);
        }


        @Test
        @DisplayName("getAllAccountsPaged передаёт Pageable в repository")
        void getAllAccountsPaged_shouldDelegateToRepository() {

            Pageable pageable = pageable();

            Page<BankAccount> expected =
                    new PageImpl<>(
                            List.of(testAccount),
                            pageable,
                            1
                    );

            when(accountRepository.findAll(pageable))
                    .thenReturn(expected);

            Page<BankAccount> result =
                    service.getAllAccountsPaged(pageable);

            assertEquals(expected, result);

            verify(accountRepository)
                    .findAll(pageable);
        }


        @Test
        @DisplayName("getAccountsPaged передаёт status и Pageable")
        void getAccountsPaged_shouldDelegateToRepository() {

            Pageable pageable = pageable();

            Page<BankAccount> expected =
                    new PageImpl<>(
                            List.of(testAccount),
                            pageable,
                            1
                    );

            when(accountRepository.findByStatus(
                    AccountStatus.ACTIVE,
                    pageable
            )).thenReturn(expected);

            Page<BankAccount> result =
                    service.getAccountsPaged(
                            AccountStatus.ACTIVE,
                            pageable
                    );

            assertEquals(expected, result);

            verify(accountRepository)
                    .findByStatus(
                            AccountStatus.ACTIVE,
                            pageable
                    );
        }


        @Test
        @DisplayName("searchByName передаёт имя и Pageable")
        void searchByName_shouldDelegateToRepository() {

            Pageable pageable = pageable();

            Page<BankAccount> expected =
                    new PageImpl<>(
                            List.of(testAccount),
                            pageable,
                            1
                    );

            when(accountRepository
                    .findByOwnerContainingIgnoreCase(
                            "eld",
                            pageable
                    ))
                    .thenReturn(expected);

            Page<BankAccount> result =
                    service.searchByName(
                            "eld",
                            pageable
                    );

            assertEquals(expected, result);

            verify(accountRepository)
                    .findByOwnerContainingIgnoreCase(
                            "eld",
                            pageable
                    );
        }


        @Test
        @DisplayName("getAccountsPagedByUsername передаёт username и Pageable")
        void getAccountsPagedByUsername_shouldDelegateToRepository() {

            Pageable pageable = pageable();

            Page<BankAccount> expected =
                    new PageImpl<>(
                            List.of(testAccount),
                            pageable,
                            1
                    );

            when(accountRepository
                    .findByUser_UserName(
                            "Eldar",
                            pageable
                    ))
                    .thenReturn(expected);

            Page<BankAccount> result =
                    service.getAccountsPagedByUsername(
                            "Eldar",
                            pageable
                    );

            assertEquals(expected, result);

            verify(accountRepository)
                    .findByUser_UserName(
                            "Eldar",
                            pageable
                    );
        }


        @Test
        @DisplayName("getAccountsPagedByUsername с status передаёт все параметры")
        void getAccountsPagedByUsernameWithStatus_shouldDelegateToRepository() {

            Pageable pageable = pageable();

            Page<BankAccount> expected =
                    new PageImpl<>(
                            List.of(testAccount),
                            pageable,
                            1
                    );

            when(accountRepository
                    .findByUser_UserNameAndStatus(
                            "Eldar",
                            AccountStatus.ACTIVE,
                            pageable
                    ))
                    .thenReturn(expected);

            Page<BankAccount> result =
                    service.getAccountsPagedByUsername(
                            AccountStatus.ACTIVE,
                            "Eldar",
                            pageable
                    );

            assertEquals(expected, result);

            verify(accountRepository)
                    .findByUser_UserNameAndStatus(
                            "Eldar",
                            AccountStatus.ACTIVE,
                            pageable
                    );
        }


        @Test
        @DisplayName("searchByNameForUser передаёт все параметры")
        void searchByNameForUser_shouldDelegateToRepository() {

            Pageable pageable = pageable();

            Page<BankAccount> expected =
                    new PageImpl<>(
                            List.of(testAccount),
                            pageable,
                            1
                    );

            when(accountRepository
                    .findByOwnerContainingIgnoreCaseAndUser_UserName(
                            "eld",
                            "Eldar",
                            pageable
                    ))
                    .thenReturn(expected);

            Page<BankAccount> result =
                    service.searchByNameForUser(
                            "eld",
                            "Eldar",
                            pageable
                    );

            assertEquals(expected, result);

            verify(accountRepository)
                    .findByOwnerContainingIgnoreCaseAndUser_UserName(
                            "eld",
                            "Eldar",
                            pageable
                    );
        }
    }


    // =========================================================
    // UPDATE ACCOUNT
    // =========================================================

    @Nested
    @DisplayName("Обновление счёта")
    class UpdateAccountTests {

        @Test
        @DisplayName("Обновляет owner")
        void updateAccount_shouldUpdateOwner() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(accountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BankAccount result =
                    service.updateAccount(
                            1L,
                            "Alex",
                            null
                    );

            assertEquals("Alex", result.getOwner());

            verify(accountRepository)
                    .save(accountCaptor.capture());

            assertEquals(
                    "Alex",
                    accountCaptor.getValue().getOwner()
            );
        }


        @Test
        @DisplayName("Null owner не изменяет owner")
        void updateAccount_nullOwner_shouldKeepOwner() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(accountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BankAccount result =
                    service.updateAccount(
                            1L,
                            null,
                            null
                    );

            assertEquals(
                    "Eldar",
                    result.getOwner()
            );

            verify(accountRepository)
                    .save(testAccount);
        }


        @Test
        @DisplayName("Переданный status пока не изменяет статус")
        void updateAccount_statusProvided_shouldNotChangeStatus() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(accountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BankAccount result =
                    service.updateAccount(
                            1L,
                            null,
                            AccountStatus.BLOCKED
                    );

            assertTrue(result.isActive());

            verify(accountRepository)
                    .save(testAccount);
        }


        @Test
        @DisplayName("Несуществующий счёт нельзя обновить")
        void updateAccount_notFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.updateAccount(
                            99L,
                            "Alex",
                            null
                    )
            );

            verify(accountRepository, never())
                    .save(any());
        }
    }


    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Nested
    @DisplayName("Изменение статуса")
    class UpdateStatusTests {

        @ParameterizedTest
        @EnumSource(
                value = AccountStatus.class,
                names = {"ACTIVE", "BLOCKED", "CLOSED"}
        )
        @DisplayName("Корректно обрабатывает каждый статус")
        void updateStatus_shouldHandleAllStatuses(
                AccountStatus status
        ) {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(accountRepository.save(any(BankAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BankAccount result =
                    service.updateStatus(
                            1L,
                            status
                    );

            assertEquals(
                    testAccount,
                    result
            );

            verify(accountRepository)
                    .save(testAccount);

            switch (status) {
                case ACTIVE ->
                        assertTrue(testAccount.isActive());

                case BLOCKED ->
                        assertFalse(testAccount.isActive());

                case CLOSED ->
                        assertFalse(testAccount.isActive());
            }
        }


        @Test
        @DisplayName("Null status ничего не изменяет")
        void updateStatus_null_shouldReturnAccountWithoutSave() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            BankAccount result =
                    service.updateStatus(
                            1L,
                            null
                    );

            assertSame(
                    testAccount,
                    result
            );

            assertTrue(testAccount.isActive());

            verify(accountRepository, never())
                    .save(any());
        }


        @Test
        @DisplayName("Несуществующий счёт выбрасывает исключение")
        void updateStatus_notFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.updateStatus(
                            99L,
                            AccountStatus.BLOCKED
                    )
            );

            verify(accountRepository, never())
                    .save(any());
        }
    }


    // =========================================================
    // DEPOSIT
    // =========================================================

    @Nested
    @DisplayName("Пополнение счёта")
    class DepositTests {

        @Test
        @DisplayName("Успешное пополнение")
        void deposit_success_shouldIncreaseBalanceAndSaveTransaction() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            service.deposit(
                    1L,
                    new BigDecimal("500.00")
            );

            assertEquals(
                    new BigDecimal("500.00"),
                    testAccount.getBalance()
            );

            verify(transactionRepository)
                    .save(transactionCaptor.capture());

            Transaction transaction =
                    transactionCaptor.getValue();

            assertEquals(
                    TransactionType.DEPOSIT,
                    transaction.getType()
            );

            assertEquals(
                    new BigDecimal("500.00"),
                    transaction.getAmount()
            );

            assertEquals(
                    TransactionSource.API,
                    transaction.getSource()
            );
        }


        @Test
        @DisplayName("Пополнение с указанным source сохраняет этот source")
        void deposit_customSource_shouldSaveSource() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            service.deposit(
                    1L,
                    new BigDecimal("300.00"),
                    TransactionSource.API
            );

            verify(transactionRepository)
                    .save(transactionCaptor.capture());

            assertEquals(
                    TransactionSource.API,
                    transactionCaptor.getValue().getSource()
            );
        }


        @Test
        @DisplayName("Несуществующий счёт")
        void deposit_accountNotFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.deposit(
                            99L,
                            new BigDecimal("100.00")
                    )
            );

            verifyNoInteractions(userRepository);

            verify(transactionRepository, never())
                    .save(any());
        }


        @Test
        @DisplayName("Пользователь не найден")
        void deposit_userNotFound_shouldThrow() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    UserInactiveException.class,
                    () -> service.deposit(
                            1L,
                            new BigDecimal("100.00")
                    )
            );

            assertEquals(
                    0, testAccount.getBalance().compareTo(BigDecimal.ZERO)
            );

            verify(transactionRepository, never())
                    .save(any());
        }


        @Test
        @DisplayName("Неактивный пользователь не может пополнять счёт")
        void deposit_inactiveUser_shouldThrow() {

            activeUser.deactivate();

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            assertThrows(
                    InactiveAccountException.class,
                    () -> service.deposit(
                            1L,
                            new BigDecimal("100.00")
                    )
            );

            assertEquals(
                    0, testAccount.getBalance().compareTo(BigDecimal.ZERO)
            );

            verify(transactionRepository, never())
                    .save(any());
        }
    }


    // =========================================================
    // WITHDRAW
    // =========================================================

    @Nested
    @DisplayName("Снятие средств")
    class WithdrawTests {

        @Test
        @DisplayName("Успешное снятие")
        void withdraw_success_shouldDecreaseBalance() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            service.deposit(
                    1L,
                    new BigDecimal("1000.00")
            );

            service.withdraw(
                    1L,
                    new BigDecimal("300.00")
            );

            assertEquals(
                    new BigDecimal("700.00"),
                    testAccount.getBalance()
            );
        }


        @Test
        @DisplayName("После снятия создаётся WITHDRAW транзакция")
        void withdraw_success_shouldSaveWithdrawTransaction() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            service.deposit(
                    1L,
                    new BigDecimal("1000.00")
            );

            clearInvocations(transactionRepository);

            service.withdraw(
                    1L,
                    new BigDecimal("200.00")
            );

            verify(transactionRepository)
                    .save(transactionCaptor.capture());

            Transaction transaction =
                    transactionCaptor.getValue();

            assertEquals(
                    TransactionType.WITHDRAW,
                    transaction.getType()
            );

            assertEquals(
                    new BigDecimal("200.00"),
                    transaction.getAmount()
            );

            assertEquals(
                    TransactionSource.API,
                    transaction.getSource()
            );
        }


        @Test
        @DisplayName("Недостаточно средств")
        void withdraw_insufficientFunds_shouldThrow() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            service.deposit(
                    1L,
                    new BigDecimal("1000.00")
            );

            clearInvocations(transactionRepository);

            assertThrows(
                    InsufficientFundsException.class,
                    () -> service.withdraw(
                            1L,
                            new BigDecimal("1001.00")
                    )
            );

            assertEquals(
                    new BigDecimal("1000.00"),
                    testAccount.getBalance()
            );

            verify(transactionRepository, never())
                    .save(any());
        }


        @Test
        @DisplayName("Снятие с несуществующего счёта")
        void withdraw_accountNotFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.withdraw(
                            99L,
                            new BigDecimal("100.00")
                    )
            );

            verify(transactionRepository, never())
                    .save(any());
        }


        @Test
        @DisplayName("Пользователь не найден")
        void withdraw_userNotFound_shouldThrow() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.empty());

            assertThrows(
                    UserInactiveException.class,
                    () -> service.withdraw(
                            1L,
                            new BigDecimal("100.00")
                    )
            );

            verify(transactionRepository, never())
                    .save(any());
        }


        @Test
        @DisplayName("Неактивный пользователь не может снимать")
        void withdraw_inactiveUser_shouldThrow() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            activeUser.deactivate();

            assertThrows(
                    InactiveAccountException.class,
                    () -> service.withdraw(
                            1L,
                            new BigDecimal("100.00")
                    )
            );

            assertEquals(
                    0, testAccount.getBalance().compareTo(BigDecimal.ZERO)
            );

            verify(transactionRepository, never())
                    .save(any());
        }


        @Test
        @DisplayName("Снятие с указанным source сохраняет source")
        void withdraw_customSource_shouldSaveSource() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(userRepository.findByUserName("Eldar"))
                    .thenReturn(Optional.of(activeUser));

            service.deposit(
                    1L,
                    new BigDecimal("1000.00")
            );

            clearInvocations(transactionRepository);

            service.withdraw(
                    1L,
                    new BigDecimal("100.00"),
                    TransactionSource.API
            );

            verify(transactionRepository)
                    .save(transactionCaptor.capture());

            assertEquals(
                    TransactionSource.API,
                    transactionCaptor.getValue().getSource()
            );
        }
    }


    // =========================================================
    // TRANSACTION HISTORY
    // =========================================================

    @Nested
    @DisplayName("История транзакций")
    class TransactionHistoryTests {

        private Pageable pageable() {
            return PageRequest.of(0, 10);
        }


        @Test
        @DisplayName("Возвращает историю с пагинацией")
        void getTransactionHistoryPaged_shouldReturnPage() {

            Pageable pageable = pageable();

            Transaction transaction =
                    new Transaction(
                            testAccount,
                            new BigDecimal("100"),
                            TransactionType.DEPOSIT,
                            TransactionSource.API
                    );

            Page<Transaction> expected =
                    new PageImpl<>(
                            List.of(transaction),
                            pageable,
                            1
                    );

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(transactionRepository
                    .findByAccountId(1L, pageable))
                    .thenReturn(expected);

            Page<Transaction> result =
                    service.getTransactionHistoryPaged(
                            1L,
                            pageable
                    );

            assertEquals(expected, result);

            verify(transactionRepository)
                    .findByAccountId(1L, pageable);
        }


        @Test
        @DisplayName("История с пагинацией для несуществующего счёта")
        void getTransactionHistoryPaged_notFound_shouldThrow() {

            Pageable pageable = pageable();

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.getTransactionHistoryPaged(
                            99L,
                            pageable
                    )
            );

            verify(transactionRepository, never())
                    .findByAccountId(
                            anyLong(),
                            any(Pageable.class)
                    );
        }


        @Test
        @DisplayName("Возвращает историю без пагинации")
        void getTransactionHistory_shouldReturnPage() {

            Pageable pageable = pageable();

            Page<Transaction> expected =
                    new PageImpl<>(
                            List.of(),
                            pageable,
                            0
                    );

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(transactionRepository
                    .findByAccountId(1L, pageable))
                    .thenReturn(expected);

            Page<Transaction> result =
                    service.getTransactionHistory(
                            1L,
                            pageable
                    );

            assertEquals(expected, result);

            verify(transactionRepository)
                    .findByAccountId(1L, pageable);
        }


        @Test
        @DisplayName("История без пагинации для несуществующего счёта")
        void getTransactionHistory_notFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.getTransactionHistory(
                            99L,
                            pageable()
                    )
            );

            verify(transactionRepository, never())
                    .findByAccountId(
                            anyLong(),
                            any(Pageable.class)
                    );
        }
    }


    // =========================================================
    // STATEMENT
    // =========================================================

    @Nested
    @DisplayName("Выписка")
    class StatementTests {

        @Test
        @DisplayName("Формирует StatementResponse")
        void getStatement_shouldBuildStatement() {

            Transaction transaction =
                    new Transaction(
                            testAccount,
                            new BigDecimal("500"),
                            TransactionType.DEPOSIT,
                            TransactionSource.API
                    );

            Transfer outgoingTransfer = mock(Transfer.class);
            Transfer incomingTransfer = mock(Transfer.class);

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(transactionRepository.findByAccountId(1L))
                    .thenReturn(List.of(transaction));

            when(transactionRepository.countByAccountId(1L))
                    .thenReturn(1L);

            when(transferRepository.findByFromAccountId(1L))
                    .thenReturn(
                            List.of(outgoingTransfer)
                    );

            when(transferRepository.findByToAccountId(1L))
                    .thenReturn(
                            List.of(
                                    incomingTransfer,
                                    mock(Transfer.class)
                            )
                    );

            StatementResponse result =
                    service.getStatement(1L);

            assertNotNull(result);

            verify(transactionRepository)
                    .findByAccountId(1L);

            verify(transactionRepository)
                    .countByAccountId(1L);

            verify(transferRepository)
                    .findByFromAccountId(1L);

            verify(transferRepository)
                    .findByToAccountId(1L);
        }


        @Test
        @DisplayName("Для счёта без операций формируется пустая статистика")
        void getStatement_withoutOperations_shouldReturnStatement() {

            when(accountRepository.findById(1L))
                    .thenReturn(Optional.of(testAccount));

            when(transactionRepository.findByAccountId(1L))
                    .thenReturn(List.of());

            when(transactionRepository.countByAccountId(1L))
                    .thenReturn(0L);

            when(transferRepository.findByFromAccountId(1L))
                    .thenReturn(List.of());

            when(transferRepository.findByToAccountId(1L))
                    .thenReturn(List.of());

            StatementResponse result =
                    service.getStatement(1L);

            assertNotNull(result);

            verify(transactionRepository)
                    .countByAccountId(1L);

            verify(transferRepository)
                    .findByFromAccountId(1L);

            verify(transferRepository)
                    .findByToAccountId(1L);
        }


        @Test
        @DisplayName("Выписка для несуществующего счёта")
        void getStatement_notFound_shouldThrow() {

            when(accountRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    AccountNotFoundException.class,
                    () -> service.getStatement(99L)
            );

            verifyNoInteractions(transactionRepository);
            verifyNoInteractions(transferRepository);
        }
    }
}


//package com.example.bank.service;
//
//import com.example.bank.actuator.AccountMetrics;
//import com.example.bank.event.BankEventPublisher;
//import com.example.bank.exception.*;
//import com.example.bank.model.*;
//import com.example.bank.repository.*;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//
// import org.springframework.test.util.ReflectionTestUtils;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("BankAccountService — юнит-тесты с моками")
//class BankAccountServiceTest {
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @Mock
//    private TransactionRepository transactionRepository;
//
//    @Mock
//    private TransferRepository transferRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private BankEventPublisher eventPublisher;
//
//    @Mock
//    private AccountNumberGenerator accountNumberGenerator;
//
//    @Mock
//    private AccountMetrics accountMetrics;
//
//    @InjectMocks
//    private BankAccountService service;
//
//    @Captor
//    private ArgumentCaptor<BankAccount> accountCaptor;
//
//    @Captor
//    private ArgumentCaptor<Transaction> transactionCaptor;
//
//    private BankAccount testAccount;
//    private User activeUser;
//
//    @BeforeEach
//    void setUp() {
//
//        testAccount = new BankAccount("Eldar");
//
//        ReflectionTestUtils.setField(
//                testAccount,
//                "id",
//                1L
//        );
//
//        testAccount.activate();
//
//        activeUser = new User(
//                "Eldar",
//                "eldar@test.com",
//                "Eldar",
//                "password"
//        );
//
//        activeUser.activate();
//    }
//
//
//
//    // ==================== FIND BY ID ====================
//
//    @Nested
//    @DisplayName("Поиск счёта по ID")
//    class FindByIdTests {
//
//        @Test
//        @DisplayName("Существующий счёт возвращается корректно")
//        void findById_existing_shouldReturnAccount() {
//
//            when(accountRepository.findById(1L))
//                    .thenReturn(Optional.of(testAccount));
//
//            BankAccount result =
//                    service.findById(1L);
//
//            assertEquals(
//                    testAccount,
//                    result
//            );
//
//            verify(accountRepository)
//                    .findById(1L);
//        }
//
//        @Test
//        @DisplayName("Несуществующий ID выбрасывает AccountNotFoundException")
//        void findById_notFound_shouldThrow() {
//
//            when(accountRepository.findById(99L))
//                    .thenReturn(Optional.empty());
//
//            AccountNotFoundException ex =
//                    assertThrows(
//                            AccountNotFoundException.class,
//                            () -> service.findById(99L)
//                    );
//
//            assertTrue(
//                    ex.getMessage().contains("99")
//            );
//        }
//    }
//
//    // ==================== DEPOSIT ====================
//
//    @Nested
//    @DisplayName("Пополнение счёта")
//    class DepositTests {
//
//        @Test
//        @DisplayName("Успешное пополнение увеличивает баланс и записывает транзакцию")
//        void deposit_success_shouldIncreaseBalanceAndSaveTransaction() {
//
//            when(accountRepository.findById(1L))
//                    .thenReturn(Optional.of(testAccount));
//
//            when(userRepository.findByUserName("Eldar"))
//                    .thenReturn(Optional.of(activeUser));
//
//            service.deposit(
//                    1L,
//                    new BigDecimal("500.00")
//            );
//
//            assertEquals(
//                    new BigDecimal("500.00"),
//                    testAccount.getBalance()
//            );
//
//            verify(transactionRepository)
//                    .save(transactionCaptor.capture());
//
//            Transaction savedTx =
//                    transactionCaptor.getValue();
//
//            assertEquals(
//                    TransactionType.DEPOSIT,
//                    savedTx.getType()
//            );
//
//            assertEquals(
//                    new BigDecimal("500.00"),
//                    savedTx.getAmount()
//            );
//        }
//
//        @Test
//        @DisplayName("Пополнение заблокированного счёта запрещено")
//        void deposit_blockedAccount_shouldThrow() {
//
//            testAccount.block();
//
//            when(accountRepository.findById(1L))
//                    .thenReturn(Optional.of(testAccount));
//
//            when(userRepository.findByUserName("Eldar"))
//                    .thenReturn(Optional.of(activeUser));
//
//            assertThrows(
//                    InactiveAccountException.class,
//                    () -> service.deposit(
//                            1L,
//                            new BigDecimal("100.00")
//                    )
//            );
//
//            verify(
//                    transactionRepository,
//                    never()
//            ).save(any());
//        }
//
//        @Test
//        @DisplayName("Пополнение несуществующего счёта выбрасывает исключение")
//        void deposit_accountNotFound_shouldThrow() {
//
//            when(accountRepository.findById(anyLong()))
//                    .thenReturn(Optional.empty());
//
//            assertThrows(
//                    AccountNotFoundException.class,
//                    () -> service.deposit(
//                            99L,
//                            new BigDecimal("100.00")
//                    )
//            );
//
//            verify(
//                    transactionRepository,
//                    never()
//            ).save(any());
//        }
//    }
//
//    // ==================== WITHDRAW ====================
//
//    @Nested
//    @DisplayName("Снятие средств")
//    class WithdrawTests {
//
//        @Test
//        @DisplayName("Успешное снятие уменьшает баланс")
//        void withdraw_success_shouldDecreaseBalance() {
//
//            when(accountRepository.findById(1L))
//                    .thenReturn(Optional.of(testAccount));
//
//            when(userRepository.findByUserName("Eldar"))
//                    .thenReturn(Optional.of(activeUser));
//
//            service.deposit(
//                    1L,
//                    new BigDecimal("1000.00")
//            );
//
//            service.withdraw(
//                    1L,
//                    new BigDecimal("300.00")
//            );
//
//            assertEquals(
//                    new BigDecimal("700.00"),
//                    testAccount.getBalance()
//            );
//        }
//
//        @Test
//        @DisplayName("Недостаточно средств — операция отклоняется")
//        void withdraw_insufficientFunds_shouldThrow() {
//
//            when(accountRepository.findById(1L))
//                    .thenReturn(Optional.of(testAccount));
//
//            when(userRepository.findByUserName("Eldar"))
//                    .thenReturn(Optional.of(activeUser));
//
//            service.deposit(
//                    1L,
//                    new BigDecimal("1000.00")
//            );
//
//            clearInvocations(transactionRepository);
//
//            assertThrows(
//                    InsufficientFundsException.class,
//                    () -> service.withdraw(
//                            1L,
//                            new BigDecimal("1001.00")
//                    )
//            );
//
//            assertEquals(
//                    new BigDecimal("1000.00"),
//                    testAccount.getBalance()
//            );
//
//            verify(
//                    transactionRepository,
//                    never()
//            ).save(any());
//        }
//
//        @Test
//        @DisplayName("После снятия записывается транзакция WITHDRAW")
//        void withdraw_success_shouldSaveWithdrawTransaction() {
//
//            when(accountRepository.findById(1L))
//                    .thenReturn(Optional.of(testAccount));
//
//            when(userRepository.findByUserName("Eldar"))
//                    .thenReturn(Optional.of(activeUser));
//
//            service.deposit(
//                    1L,
//                    new BigDecimal("1000.00")
//            );
//
//            clearInvocations(transactionRepository);
//
//            service.withdraw(
//                    1L,
//                    new BigDecimal("200.00")
//            );
//
//            verify(transactionRepository)
//                    .save(transactionCaptor.capture());
//
//            assertEquals(
//                    TransactionType.WITHDRAW,
//                    transactionCaptor.getValue().getType()
//            );
//
//            assertEquals(
//                    new BigDecimal("200.00"),
//                    transactionCaptor.getValue().getAmount()
//            );
//        }
//    }
//
//
//
//    // ==================== GET ALL ACCOUNTS ====================
//
//    @Test
//    @DisplayName("getAllAccounts возвращает все счета из репозитория")
//    void getAllAccounts_shouldReturnAllFromRepository() {
//
//        BankAccount account2 =
//                new BankAccount("Kola");
//
//        ReflectionTestUtils.setField(
//                account2,
//                "id",
//                2L
//        );
//
//        when(accountRepository.findAll())
//                .thenReturn(
//                        List.of(
//                                testAccount,
//                                account2
//                        )
//                );
//
//        List<BankAccount> result =
//                service.getAllAccounts();
//
//        assertEquals(
//                2,
//                result.size()
//        );
//
//        verify(accountRepository)
//                .findAll();
//
//        verifyNoInteractions(
//                transactionRepository
//        );
//    }
//}