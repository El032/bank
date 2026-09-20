package com.example.bank.service;

import com.example.bank.actuator.TransferMetrics;
import com.example.bank.exception.*;
import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.OutboxEventRepository;
import com.example.bank.repository.TransactionRepository;
import com.example.bank.repository.TransferRepository;
import com.example.bank.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.example.bank.exception.TransferMonthlyLimitException;
import com.fasterxml.jackson.core.JsonProcessingException;


import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService — unit-тесты")
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private TransferMetrics transferMetrics;

    @InjectMocks
    private TransferService service;

    @Captor
    private ArgumentCaptor<Transfer> transferCaptor;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private BankAccount testAccount1;
    private BankAccount testAccount2;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {

        testAccount1 = new BankAccount("Eldar");
        ReflectionTestUtils.setField(testAccount1, "id", 1L);
        testAccount1.activate();
        testAccount1.deposit(new BigDecimal("1000.00"));

        testAccount2 = new BankAccount("Elnara");
        ReflectionTestUtils.setField(testAccount2, "id", 2L);
        testAccount2.activate();
        testAccount2.deposit(new BigDecimal("500.00"));

        user1 = new User(
                "Eldar",
                "eldar@test.com",
                "Eldar",
                "password"
        );
        user1.activate();

        user2 = new User(
                "Elnara",
                "elnara@test.com",
                "Elnara",
                "password"
        );
        user2.activate();
    }

    @Test
    @DisplayName("Успешный перевод — оба баланса изменились корректно")
    void transfer_shouldMoveMoneyBetweenAccounts() throws Exception {

        // Arrange
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        when(transferRepository.sumOutgoingTransfers(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(BigDecimal.ZERO);

        Transfer savedTransfer = new Transfer(
                testAccount1,
                testAccount2,
                new BigDecimal("300.00")
        );

        ReflectionTestUtils.setField(savedTransfer, "id", 100L);

        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        // Act
        Transfer result = service.transfer(
                1L,
                2L,
                new BigDecimal("300.00")
        );

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(testAccount1, result.getFromAccount());
        assertEquals(testAccount2, result.getToAccount());
        assertEquals(
                new BigDecimal("300.00"),
                result.getAmount()
        );

        assertEquals(
                new BigDecimal("700.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("800.00"),
                testAccount2.getBalance()
        );

        // Две транзакции:
        // WITHDRAW у отправителя
        // DEPOSIT у получателя
        verify(transactionRepository, times(2))
                .save(transactionCaptor.capture());

        assertEquals(
                TransactionType.WITHDRAW,
                transactionCaptor.getAllValues().get(0).getType()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                transactionCaptor.getAllValues().get(1).getType()
        );

        assertEquals(
                new BigDecimal("300.00"),
                transactionCaptor.getAllValues().get(0).getAmount()
        );

        assertEquals(
                new BigDecimal("300.00"),
                transactionCaptor.getAllValues().get(1).getAmount()
        );

        // Проверяем Transfer
        verify(transferRepository)
                .save(transferCaptor.capture());

        Transfer capturedTransfer =
                transferCaptor.getValue();

        assertEquals(
                testAccount1,
                capturedTransfer.getFromAccount()
        );

        assertEquals(
                testAccount2,
                capturedTransfer.getToAccount()
        );

        assertEquals(
                new BigDecimal("300.00"),
                capturedTransfer.getAmount()
        );
        // количество выполненых запросов в прометеус
        verify(transferMetrics).transferSucceeded();
        //  сумма переводов в прометеус
        verify(transferMetrics).recordAmount(anyDouble());

        verify(transferMetrics).startTimer();

        verify(transferMetrics).stopTimer(any());

        verify(transferMetrics).transferStarted();

        verify(transferMetrics).transferFinished();


        // Проверяем Outbox
        verify(objectMapper)
                .writeValueAsString(any());

        verify(outboxEventRepository)
                .save(any());
    }

    @Test
    @DisplayName("Перевод самому себе запрещён")
    void transfer_sameAccount_shouldThrowException() {

        // Act + Assert
        assertThrows(
                TransferException.class,
                () -> service.transfer(
                        1L,
                        1L,
                        new BigDecimal("300.00")
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transferRepository);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод с несуществующего счёта отправителя")
    void transfer_fromAccountNotFound_shouldThrowException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        verify(accountRepository)
                .findById(1L);

        verify(accountRepository, never())
                .findById(2L);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transferRepository);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод на несуществующий счёт получателя")
    void transfer_toAccountNotFound_shouldThrowException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        verify(accountRepository)
                .findById(1L);

        verify(accountRepository)
                .findById(2L);

        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transferRepository);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод с заблокированного счёта запрещён")
    void transfer_fromAccountInactive_shouldThrowException() {

        testAccount1.block();

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        assertThrows(
                InactiveAccountException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                new BigDecimal("1000.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                testAccount2.getBalance()
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verify(transferRepository, never())
                .save(any());

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод на заблокированный счёт запрещён")
    void transfer_toAccountInactive_shouldThrowException() {

        testAccount2.block();

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        assertThrows(
                InactiveAccountException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                new BigDecimal("1000.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                testAccount2.getBalance()
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verify(transferRepository, never())
                .save(any());

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Недостаточно средств — перевод не выполняется")
    void transfer_insufficientFunds_shouldThrowException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        when(transferRepository.sumOutgoingTransfers(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(BigDecimal.ZERO);

        assertThrows(
                InsufficientFundsException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("1222.00")
                )
        );

        verify(transferMetrics).transferFailed();

        verify(transferMetrics).startTimer();

        verify(transferMetrics).stopTimer(any());

        verify(transferMetrics).transferStarted();

        verify(transferMetrics).transferFinished();

        assertEquals(
                new BigDecimal("1000.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                testAccount2.getBalance()
        );

        verify(transactionRepository, never())
                .save(any());

        verify(transferRepository, never())
                .save(any());

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод меньше 10 рублей запрещён")
    void transfer_belowMinimum_shouldThrowException() {

        assertThrows(
                TransferMinimumAmountException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("9.99")
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transferRepository);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод с null суммой запрещён")
    void transfer_nullAmount_shouldThrowException() {

        assertThrows(
                InvalidAmountException.class,
                () -> service.transfer(1L, 2L, null)
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transferRepository);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод с нулевой суммой запрещён")
    void transfer_zeroAmount_shouldThrowException() {

        assertThrows(
                TransferMinimumAmountException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        BigDecimal.ZERO
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transferRepository);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод с отрицательной суммой запрещён")
    void transfer_negativeAmount_shouldThrowException() {

        assertThrows(
                TransferMinimumAmountException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("-100.00")
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(transferRepository);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }


    @Test
    @DisplayName("Перевод от неактивного пользователя запрещён")
    void transfer_inactiveFromUser_shouldThrowException() {

        // Arrange
        user1.deactivate();

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        // Act + Assert
        assertThrows(
                UserInactiveException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        // Деньги не должны измениться
        assertEquals(
                new BigDecimal("1000.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                testAccount2.getBalance()
        );

        // История и перевод не создаются
        verify(transactionRepository, never())
                .save(any());

        verify(transferRepository, never())
                .save(any());

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод неактивному пользователю запрещён")
    void transfer_inactiveToUser_shouldThrowException() {

        // Arrange
        user2.deactivate();

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        // Act + Assert
        assertThrows(
                UserInactiveException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        // Балансы не должны измениться
        assertEquals(
                new BigDecimal("1000.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                testAccount2.getBalance()
        );

        verify(transactionRepository, never())
                .save(any());

        verify(transferRepository, never())
                .save(any());

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод с накопительного счёта запрещён")
    void transfer_fromSavings_shouldThrowException() {

        SavingsAccount savingsAccount = new SavingsAccount(
                "Eldar",
                new BigDecimal("5000.00"),
                new BigDecimal("12.00")
        );

        ReflectionTestUtils.setField(savingsAccount, "id", 3L);
        savingsAccount.activate();

        when(accountRepository.findById(3L))
                .thenReturn(Optional.of(savingsAccount));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        assertThrows(
                TransferException.class,
                () -> service.transfer(
                        3L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                new BigDecimal("5000.00"),
                savingsAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                testAccount2.getBalance()
        );

        verifyNoInteractions(userRepository);
        verify(transactionRepository, never())
                .save(any());
        verify(transferRepository, never())
                .save(any());
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Перевод на накопительный счёт запрещён")
    void transfer_toSavings_shouldThrowException() {

        SavingsAccount savingsAccount = new SavingsAccount(
                "Elnara",
                new BigDecimal("5000.00"),
                new BigDecimal("12.00")
        );

        ReflectionTestUtils.setField(savingsAccount, "id", 3L);
        savingsAccount.activate();

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(3L))
                .thenReturn(Optional.of(savingsAccount));

        assertThrows(
                TransferException.class,
                () -> service.transfer(
                        1L,
                        3L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                new BigDecimal("1000.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                savingsAccount.getBalance()
        );

        verifyNoInteractions(userRepository);
        verify(transactionRepository, never())
                .save(any());
        verify(transferRepository, never())
                .save(any());
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }



    @Test
    @DisplayName("Перевод ровно в пределах месячного лимита разрешён")
    void transfer_monthlyLimit_exact_shouldSucceed() throws Exception {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        when(transferRepository.sumOutgoingTransfers(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("499990.00"));

        Transfer savedTransfer = new Transfer(
                testAccount1,
                testAccount2,
                new BigDecimal("10.00")
        );

        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{}");

        Transfer result = service.transfer(
                1L,
                2L,
                new BigDecimal("10.00")
        );

        assertNotNull(result);

        assertEquals(
                new BigDecimal("990.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("510.00"),
                testAccount2.getBalance()
        );

        verify(transferRepository)
                .save(any(Transfer.class));

        verify(outboxEventRepository)
                .save(any());
    }

    @Test
    @DisplayName("Перевод сверх месячного лимита запрещён")
    void transfer_monthlyLimitExceeded_shouldThrowException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        when(transferRepository.sumOutgoingTransfers(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("500000.00"));

        assertThrows(
                TransferMonthlyLimitException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("10.00")
                )
        );

        assertEquals(
                new BigDecimal("1000.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("500.00"),
                testAccount2.getBalance()
        );

        verify(transactionRepository, never())
                .save(any());

        verify(transferRepository, never())
                .save(any(Transfer.class));

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }



    @Test
    @DisplayName("Ошибка при создании Outbox-события прерывает перевод")
    void transfer_outboxSerializationError_shouldFail() throws Exception {

        // Arrange
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        when(transferRepository.sumOutgoingTransfers(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(BigDecimal.ZERO);

        Transfer savedTransfer = new Transfer(
                testAccount1,
                testAccount2,
                new BigDecimal("300.00")
        );

        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("Ошибка сериализации"));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                "Ошибка сериализации",
                exception.getMessage()
        );

        // Outbox не должен сохраниться
        verify(outboxEventRepository, never())
                .save(any());

        // До ошибки перевод уже успел изменить объекты
        // В настоящей БД эти изменения должны откатиться
        assertEquals(
                new BigDecimal("700.00"),
                testAccount1.getBalance()
        );

        assertEquals(
                new BigDecimal("800.00"),
                testAccount2.getBalance()
        );
    }

    @Test
    @DisplayName("JsonProcessingException при сериализации превращается в RuntimeException")
    void transfer_jsonProcessingException_shouldBeWrapped() throws Exception {

        // Arrange
        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.of(user2));

        when(transferRepository.sumOutgoingTransfers(
                eq(1L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(BigDecimal.ZERO);

        Transfer savedTransfer = new Transfer(
                testAccount1,
                testAccount2,
                new BigDecimal("300.00")
        );

        when(transferRepository.save(any(Transfer.class)))
                .thenReturn(savedTransfer);

        doAnswer(invocation -> {
            throw JsonMappingException.fromUnexpectedIOE(
                    new IOException("Ошибка сериализации")
            );
        }).when(objectMapper).writeValueAsString(any());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        // Assert
        assertEquals(
                "Ошибка сериализации TransferCompletedEvent",
                exception.getMessage()
        );

        assertInstanceOf(
                JsonProcessingException.class,
                exception.getCause()
        );
    }

    @Test
    @DisplayName("История переводов возвращается и сортируется по дате")
    void getTransferHistory_shouldReturnSortedTransfers() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        Transfer olderTransfer = new Transfer(
                testAccount2,
                testAccount1,
                new BigDecimal("100.00")
        );

        Transfer newerTransfer = new Transfer(
                testAccount1,
                testAccount2,
                new BigDecimal("300.00")
        );

        ReflectionTestUtils.setField(
                olderTransfer,
                "createdAt",
                LocalDateTime.now().minusDays(2)
        );

        ReflectionTestUtils.setField(
                newerTransfer,
                "createdAt",
                LocalDateTime.now().minusDays(1)
        );

        when(transferRepository.findByFromAccountId(1L))
                .thenReturn(new java.util.ArrayList<>(
                        java.util.List.of(newerTransfer)
                ));

        when(transferRepository.findByToAccountId(1L))
                .thenReturn(new java.util.ArrayList<>(
                        java.util.List.of(olderTransfer)
                ));

        var result = service.getTransferHistory(1L);

        assertEquals(2, result.size());

        assertSame(newerTransfer, result.get(0));
        assertSame(olderTransfer, result.get(1));

        verify(accountRepository)
                .findById(1L);

        verify(transferRepository)
                .findByFromAccountId(1L);

        verify(transferRepository)
                .findByToAccountId(1L);
    }

    @Test
    @DisplayName("История переводов — счёт не найден")
    void getTransferHistory_accountNotFound_shouldThrowException() {

        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> service.getTransferHistory(99L)
        );

        verify(accountRepository)
                .findById(99L);

        verify(transferRepository, never())
                .findByFromAccountId(anyLong());

        verify(transferRepository, never())
                .findByToAccountId(anyLong());
    }

    @Test
    @DisplayName("Пользователь отправителя не найден")
    void transfer_fromUserNotFound_shouldThrowException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserInactiveException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        verify(userRepository)
                .findByUserName("Eldar");

        verify(userRepository, never())
                .findByUserName("Elnara");

        verify(transactionRepository, never())
                .save(any());

        verify(transferRepository, never())
                .save(any());

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    @DisplayName("Пользователь получателя не найден")
    void transfer_toUserNotFound_shouldThrowException() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(testAccount1));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(testAccount2));

        when(userRepository.findByUserName("Eldar"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByUserName("Elnara"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserInactiveException.class,
                () -> service.transfer(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        verify(userRepository)
                .findByUserName("Eldar");

        verify(userRepository)
                .findByUserName("Elnara");

        verify(transactionRepository, never())
                .save(any());

        verify(transferRepository, never())
                .save(any());

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(outboxEventRepository);
    }



}